package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log
import com.example.data.ContentModerationGateway
import com.example.data.ModerationResult
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.PromptBlockedException
import com.google.firebase.ai.type.ResponseStoppedException
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.File
import java.text.Normalizer
import kotlin.math.max

private const val MOD_TAG = "ContentModeration"

/**
 * Automatic Text & Image Content Safety Moderation Service for 杨梅树窝.
 *
 * Defense-in-depth architecture:
 * 1. Instant deterministic local text compliance engine (Unicode NFKC + anti-obfuscation
 *    separator stripping + illegal/harmful Chinese & English lexicons + doxxing detection).
 * 2. Local bitmap image safety pre-screening (format validation + graphic gore / explicit
 *    skin-dominance safety floor).
 * 3. Multimodal Gemini Vision + Text safety review via Firebase AI Logic (gemini-3.8-flash)
 *    using strict JSON schema output + built-in safety filter enforcement.
 */
class ContentModerationService(
    private val context: Context
) : ContentModerationGateway {

    private data class RuleCategory(
        val code: String,
        val label: String,
        val patterns: List<String>
    )

    private val illegalCategories: List<RuleCategory> = listOf(
        RuleCategory(
            code = "PORNOGRAPHY",
            label = "涉黄低俗或违法色情信息",
            patterns = listOf(
                "色情", "黄片", "毛片", "约炮", "一夜情", "援交", "嫖娼", "卖淫", "招嫖",
                "裸聊", "裸照", "露点", "无码", "成人视频", "情色网站", "性交易", "色诱",
                "萝莉岛", "幼女", "炼铜", "恋童", "强奸", "迷奸", "轮奸", "偷拍裙底",
                "porn", "pornhub", "xvideos", "nsfw", "hentai", "nude", "escort", "csam"
            )
        ),
        RuleCategory(
            code = "DRUGS_WEAPONS",
            label = "毒品、违禁品或危险武器信息",
            patterns = listOf(
                "冰毒", "海洛因", "可卡因", "摇头丸", "氯胺酮", "k粉", "吸毒", "贩毒",
                "买大麻", "卖大麻", "大麻种子", "飞叶子", "致幻剂", "迷药", "听话水",
                "春药", "笑气出售", "买卖枪支", "买枪", "造枪", "土制炸弹", "制造炸药",
                "自制炸弹", "管制刀具出售", "雷管", "毒药配方", "剧毒购买",
                "meth", "heroin", "cocaine", "buy gun", "make a bomb"
            )
        ),
        RuleCategory(
            code = "VIOLENCE_GORE",
            label = "暴力血腥、恐怖极端或危害公共安全内容",
            patterns = listOf(
                "杀人教程", "砍人", "屠杀", "血腥分尸", "虐杀动物", "虐猫视频", "恐怖袭击",
                "报复社会", "无差别袭击", "纵火教程", "投毒", "爆炸袭击", "买凶杀人",
                "terrorist attack", "mass shooting", "beheading", "gore video"
            )
        ),
        RuleCategory(
            code = "SELF_HARM",
            label = "自残、自杀诱导或危险伤害信息",
            patterns = listOf(
                "自杀教程", "教你怎么死", "一起自杀", "约死", "烧炭教程", "割腕教程",
                "跳楼教程", "安乐死药", "买安眠药自杀", "鼓励自残", "去死吧你",
                "kill yourself", "suicide method", "how to hang yourself"
            )
        ),
        RuleCategory(
            code = "FRAUD_GAMBLING",
            label = "电信诈骗、网络赌博、洗钱或违法黑产信息",
            patterns = listOf(
                "网络赌博", "网上赌场", "六合彩", "时时彩", "博彩代理", "棋牌上下分", "捕鱼下分",
                "洗钱", "跑分平台", "卖银行卡", "收银行卡", "办假证", "假毕业证", "假学生证",
                "假发票", "代考包过", "替考", "买答案", "考试作弊器", "裸贷", "套路贷",
                "校园贷放款", "刷单日赚", "兼职刷单", "窃听器", "针孔摄像头偷拍", "黑客接单",
                "盗号服务", "社工库", "查开房记录", "查个人信息", "传销拉人",
                "online casino", "money laundering", "fake id", "carding"
            )
        ),
        RuleCategory(
            code = "HATE_HARASSMENT",
            label = "人肉开盒、恶意侮辱诽谤或仇恨歧视内容",
            patterns = listOf(
                "人肉搜索", "开盒挂人", "全网开盒", "挂身份证", "曝光家庭住址", "全家死光",
                "死全家", "操你妈", "草泥马", "傻逼滚", "贱货去死", "婊子", "智障滚",
                "种族清洗", "纳粹万岁", "法西斯万岁"
            )
        ),
        RuleCategory(
            code = "EXTREMISM_CULT",
            label = "违法邪教或危害社会稳定的极端内容",
            patterns = listOf(
                "邪教组织", "全能神", "法轮功", "煽动颠覆", "分裂国家", "暴乱集会", "非法传教"
            )
        )
    )

    private val idCardPattern = Regex("""(?<!\d)[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx](?!\d)""")
    private val zeroWidthRegex = Regex("""[\u200B-\u200D\uFEFF\u00AD]""")
    private val separatorStripRegex = Regex("""[\s`~!@#$%^&*()\-_=+\[\]{};:'",.<>/?\\|·—。，、；：？！“”‘’（）【】《》]+""")

    private val moderationSchema = Schema.obj(
        properties = mapOf(
            "safe" to Schema.boolean(description = "内容与图片是否合法合规、适合在高校校园社区公开发布。违反法律或含不良内容时必须为 false。"),
            "category" to Schema.enumeration(
                values = listOf(
                    "SAFE",
                    "PORNOGRAPHY",
                    "VIOLENCE_GORE",
                    "DRUGS_WEAPONS",
                    "FRAUD_GAMBLING",
                    "SELF_HARM",
                    "HATE_HARASSMENT",
                    "PRIVACY_DOXXING",
                    "EXTREMISM_CULT",
                    "OTHER_HARMFUL"
                ),
                description = "审查分类结果"
            ),
            "reason" to Schema.string(description = "若不通过，用简洁明确的中文说明违规原因（不超过36字）；若通过则写「审查通过」。")
        )
    )

    private val aiModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = moderationSchema
            }
        )
    }

    private fun normalize(raw: String): String {
        return Normalizer.normalize(raw, Normalizer.Form.NFKC)
            .replace(zeroWidthRegex, "")
            .lowercase()
    }

    private fun compactForEvasion(normalized: String): String {
        return normalized.replace(separatorStripRegex, "")
    }

    override fun inspectText(content: String, blockedKeywords: List<String>): ModerationResult {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) {
            return ModerationResult(false, "内容不能为空", "EMPTY")
        }

        val normalized = normalize(trimmed)
        val compacted = compactForEvasion(normalized)

        // 1. Check illegal / harmful categories (both raw normalized & anti-evasion compacted)
        for (cat in illegalCategories) {
            for (word in cat.patterns) {
                val target = word.lowercase()
                val isAsciiWord = target.all { it.code < 128 }
                val matched = if (isAsciiWord) {
                    Regex("""(^|[^a-z0-9])${Regex.escape(target)}([^a-z0-9]|$)""").containsMatchIn(normalized)
                } else {
                    normalized.contains(target) || compacted.contains(target.replace(" ", ""))
                }
                if (matched) {
                    return ModerationResult(
                        passed = false,
                        reason = "自动审查未通过：内容涉嫌包含${cat.label}，请修改后再发布。",
                        category = cat.code
                    )
                }
            }
        }

        // 2. Check privacy leakage / doxxing (18-digit national ID number)
        if (idCardPattern.containsMatchIn(trimmed)) {
            return ModerationResult(
                passed = false,
                reason = "自动审查未通过：请勿发布身份证号等个人敏感隐私信息。",
                category = "PRIVACY_DOXXING"
            )
        }

        // 3. Check user personal blocked keywords
        val personalMatched = blockedKeywords.firstOrNull { kw ->
            val cleanKw = kw.trim()
            cleanKw.isNotEmpty() && normalized.contains(normalize(cleanKw))
        }
        if (personalMatched != null) {
            return ModerationResult(
                passed = false,
                reason = "内容包含你设置的屏蔽词：「$personalMatched」",
                category = "USER_BLOCKED"
            )
        }

        return ModerationResult(passed = true, reason = "审查通过", category = "SAFE")
    }

    override suspend fun inspectImages(mediaUris: List<String>): ModerationResult {
        if (mediaUris.isEmpty()) return ModerationResult(true, "审查通过", "SAFE")
        return inspectSubmission(textParts = emptyList(), mediaUris = mediaUris)
    }

    override suspend fun inspectSubmission(
        textParts: List<String>,
        mediaUris: List<String>,
        blockedKeywords: List<String>
    ): ModerationResult {
        val combinedText = textParts
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")

        // Step 1: Deterministic local text compliance check (instant)
        if (combinedText.isNotEmpty()) {
            val localTextResult = inspectText(combinedText, blockedKeywords)
            if (!localTextResult.passed) return localTextResult
        }

        // Step 2: Decode & locally pre-screen all attached images on IO dispatcher
        val loadedBitmaps = mutableListOf<Bitmap>()
        try {
            if (mediaUris.isNotEmpty()) {
                val mediaCheck = withContext(Dispatchers.IO) {
                    decodeAndPreScreenBitmaps(mediaUris.take(4), loadedBitmaps)
                }
                if (!mediaCheck.passed) {
                    return mediaCheck
                }
            }

            // Step 3: Multimodal AI safety review (text + images) via Firebase AI Logic
            if (combinedText.isNotEmpty() || loadedBitmaps.isNotEmpty()) {
                return runAiMultimodalReview(combinedText, loadedBitmaps)
            }

            return ModerationResult(true, "审查通过", "SAFE")
        } finally {
            loadedBitmaps.forEach { bmp ->
                if (!bmp.isRecycled) bmp.recycle()
            }
        }
    }

    private fun decodeAndPreScreenBitmaps(
        uris: List<String>,
        outBitmaps: MutableList<Bitmap>
    ): ModerationResult {
        for ((index, rawUri) in uris.withIndex()) {
            val bmp = decodeScaledBitmap(rawUri, maxDim = 768)
                ?: return ModerationResult(
                    passed = false,
                    reason = "第 ${index + 1} 张图片格式无效或无法读取，请重新选择合规图片。",
                    category = "INVALID_MEDIA"
                )
            val heuristic = runLocalImageSafetyFloor(bmp)
            if (!heuristic.passed) {
                bmp.recycle()
                return ModerationResult(
                    passed = false,
                    reason = "第 ${index + 1} 张图片${heuristic.reason}",
                    category = heuristic.category
                )
            }
            outBitmaps.add(bmp)
        }
        return ModerationResult(true)
    }

    private fun decodeScaledBitmap(uriString: String, maxDim: Int): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            fun openStream() = when (uri.scheme) {
                "content", "android.resource" -> context.contentResolver.openInputStream(uri)
                "file" -> uri.path?.let { File(it).inputStream() }
                null -> File(uriString).takeIf { it.exists() }?.inputStream()
                else -> null
            }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openStream()?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            var sampleSize = 1
            val longest = max(bounds.outWidth, bounds.outHeight)
            while (longest / sampleSize > maxDim * 2) {
                sampleSize *= 2
            }

            val opts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = openStream()?.use { BitmapFactory.decodeStream(it, null, opts) } ?: return null
            val scale = maxDim.toFloat() / max(decoded.width, decoded.height).toFloat()
            if (scale < 1f) {
                val targetW = (decoded.width * scale).toInt().coerceAtLeast(1)
                val targetH = (decoded.height * scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
                if (scaled !== decoded) decoded.recycle()
                scaled
            } else {
                decoded
            }
        } catch (e: Exception) {
            Log.w(MOD_TAG, "Failed to decode media uri: $uriString", e)
            null
        }
    }

    /**
     * Conservative offline pixel safety floor: catches blatant graphic blood-gore frames
     * or extreme unobstructed explicit flesh-dominant images even when offline, while
     * keeping false positives near zero for portraits, oil paintings, and campus scenes.
     */
    private fun runLocalImageSafetyFloor(bitmap: Bitmap): ModerationResult {
        val sample = Bitmap.createScaledBitmap(bitmap, 96, 96, true)
        try {
            val total = 96 * 96
            var centerPixels = 0
            var centerSkinPixels = 0
            var totalSkinPixels = 0
            var deepBloodPixels = 0

            val pixels = IntArray(total)
            sample.getPixels(pixels, 0, 96, 0, 0, 96, 96)

            for (y in 0 until 96) {
                val inCenter = y in 16..79
                for (x in 0 until 96) {
                    val p = pixels[y * 96 + x]
                    val r = Color.red(p)
                    val g = Color.green(p)
                    val b = Color.blue(p)

                    // YCbCr skin-tone cluster
                    val cb = 128.0 - 0.168736 * r - 0.331264 * g + 0.5 * b
                    val cr = 128.0 + 0.5 * r - 0.418688 * g - 0.081312 * b
                    val isSkin = r > 95 && g > 40 && b > 20 &&
                        (maxOf(r, g, b) - minOf(r, g, b)) > 15 &&
                        kotlin.math.abs(r - g) > 15 && r > g && r > b &&
                        cb in 77.0..127.0 && cr in 133.0..173.0

                    if (isSkin) totalSkinPixels++
                    if (inCenter && x in 16..79) {
                        centerPixels++
                        if (isSkin) centerSkinPixels++
                    }

                    // Deep saturated blood-crimson cluster
                    if (r in 110..215 && g < 32 && b < 32 && (r - g) > 95) {
                        deepBloodPixels++
                    }
                }
            }

            val totalSkinRatio = totalSkinPixels.toFloat() / total
            val centerSkinRatio = if (centerPixels > 0) centerSkinPixels.toFloat() / centerPixels else 0f
            val bloodRatio = deepBloodPixels.toFloat() / total

            if (totalSkinRatio > 0.72f && centerSkinRatio > 0.80f) {
                return ModerationResult(
                    passed = false,
                    reason = "疑似包含大面积裸露或低俗不适画面，已被自动审查拦截。",
                    category = "PORNOGRAPHY"
                )
            }
            if (bloodRatio > 0.48f) {
                return ModerationResult(
                    passed = false,
                    reason = "疑似包含重度血腥暴力画面，已被自动审查拦截。",
                    category = "VIOLENCE_GORE"
                )
            }
            return ModerationResult(true)
        } finally {
            if (sample !== bitmap && !sample.isRecycled) sample.recycle()
        }
    }

    private suspend fun runAiMultimodalReview(
        combinedText: String,
        bitmaps: List<Bitmap>
    ): ModerationResult {
        val systemPrompt = buildString {
            appendLine("你是中国高校校园社区「杨梅树窝」（中央美术学院）的自动内容与图片安全审查系统。")
            appendLine("请严格依据中国法律法规与校园社区安全规范，同时审查用户即将发布的文字内容和附带图片。")
            appendLine("必须拦截（safe = false）的情形包括：")
            appendLine("1. 涉黄、色情低俗、露骨裸露、性暗示招嫖、未成年人色情或偷拍；")
            appendLine("2. 暴力血腥、虐待动物、恐怖极端、制造武器/爆炸物或伤害他人；")
            appendLine("3. 涉毒、违禁药品、管制器具买卖；")
            appendLine("4. 电信诈骗、网络赌博、洗钱、办假证、代考作弊、刷单黑产、违法校园贷；")
            appendLine("5. 诱导自残或自杀；")
            appendLine("6. 人肉开盒、泄露他人身份证/手机号/家庭住址、严重人身攻击辱骂、仇恨歧视；")
            appendLine("7. 违法邪教、分裂国家或违法政治极端煽动；")
            appendLine("8. 图片中包含违法广告二维码、色情/血腥/暴恐画面或隐晦违规文字（OCR识别）。")
            appendLine("注意：正常的校园生活吐槽、情绪树洞倾诉、二手画材交易、失物招领、课程评价、正常的艺术作品/雕塑/展览照片（不含真实色情裸露与血腥恐怖）应当判为 safe = true。")
            if (combinedText.isNotEmpty()) {
                appendLine()
                appendLine("【待审文字】")
                appendLine(combinedText.take(2500))
            }
            if (bitmaps.isNotEmpty()) {
                appendLine()
                appendLine("【待审图片数量】：${bitmaps.size} 张，请逐一仔细检查画面内容及图中文字。")
            }
        }

        return try {
            val response = withTimeout(30_000L) {
                aiModel.generateContent(
                    content {
                        text(systemPrompt)
                        bitmaps.forEach { image(it) }
                    }
                )
            }
            val rawJson = response.text
            if (rawJson.isNullOrBlank()) {
                // Empty response from safety-filtered call -> treat as blocked if images present
                return if (bitmaps.isNotEmpty()) {
                    ModerationResult(
                        passed = false,
                        reason = "自动审查未通过：图片或文字触发安全拦截策略，请更换后重试。",
                        category = "SAFETY_BLOCKED"
                    )
                } else {
                    ModerationResult(true, "审查通过", "SAFE")
                }
            }
            val obj = JSONObject(rawJson)
            val safe = obj.optBoolean("safe", false)
            val category = obj.optString("category", if (safe) "SAFE" else "OTHER_HARMFUL")
            val reason = obj.optString("reason", "").trim()
            if (safe) {
                ModerationResult(true, "审查通过", "SAFE")
            } else {
                ModerationResult(
                    passed = false,
                    reason = if (reason.isNotBlank()) "自动审查未通过：$reason" else "自动审查未通过：内容或图片涉嫌违规不良信息，禁止发布。",
                    category = category
                )
            }
        } catch (e: PromptBlockedException) {
            Log.w(MOD_TAG, "Prompt/image blocked by Gemini safety filter", e)
            ModerationResult(
                passed = false,
                reason = "自动审查未通过：文字或图片触发安全红线，禁止发布违法或不良内容。",
                category = "SAFETY_BLOCKED"
            )
        } catch (e: ResponseStoppedException) {
            Log.w(MOD_TAG, "Response stopped by Gemini safety filter", e)
            ModerationResult(
                passed = false,
                reason = "自动审查未通过：内容或图片涉嫌违法违规信息，已被安全拦截。",
                category = "SAFETY_BLOCKED"
            )
        } catch (e: TimeoutCancellationException) {
            Log.w(MOD_TAG, "AI moderation timed out, local rules already applied", e)
            // Local deterministic text & pixel safety floor already passed
            ModerationResult(true, "本地安全审查通过", "LOCAL_FALLBACK")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(MOD_TAG, "AI multimodal review unavailable, relying on local safety engine", e)
            // Local deterministic text & pixel safety floor already passed
            ModerationResult(true, "本地安全审查通过", "LOCAL_FALLBACK")
        }
    }
}
