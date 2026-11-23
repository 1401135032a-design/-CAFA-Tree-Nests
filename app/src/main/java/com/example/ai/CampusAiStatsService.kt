package com.example.ai

import android.util.Log
import com.example.data.CourseRecord
import com.example.data.ExperienceArticle
import com.example.data.Post
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "CampusAiStatsService"

data class AiKeywordStat(
    val keyword: String,
    val count: Int,
    val heatLabel: String,
    val insight: String
)

data class AiSentimentShare(
    val label: String,
    val percentage: Int
)

data class AiCampusStatsReport(
    val queryLabel: String,
    val headline: String,
    val summary: String,
    val topKeywords: List<AiKeywordStat>,
    val sentimentBreakdown: List<AiSentimentShare>,
    val recommendedTags: List<String>
)

sealed interface AiStatsUiState {
    data object Idle : AiStatsUiState
    data class Loading(val queryLabel: String) : AiStatsUiState
    data class Success(val report: AiCampusStatsReport) : AiStatsUiState
    data class Error(val queryLabel: String, val message: String) : AiStatsUiState
}

/**
 * Gemini-powered campus tag, keyword, and trend statistical analyzer via Firebase AI Logic.
 * Uses structured JSON output (Schema.obj + responseMimeType = "application/json") with lazy init.
 */
class CampusAiStatsService {

    private val statsSchema = Schema.obj(
        properties = mapOf(
            "headline" to Schema.string(description = "一句简明有力的央美校园热词或关键词统计核心结论（不超过24字）"),
            "summary" to Schema.string(description = "2到3句客观的数据统计解读，概括讨论热度、标签分布与同学关注重点"),
            "topKeywords" to Schema.array(
                description = "排名前3到5的核心关键词或自定义标签统计项",
                items = Schema.obj(
                    properties = mapOf(
                        "keyword" to Schema.string(description = "关键词或以#开头的标签名称"),
                        "count" to Schema.integer(description = "关联帖子或提及次数的整数统计"),
                        "heatLabel" to Schema.enumeration(
                            values = listOf("飙升", "热门", "新锐", "常青"),
                            description = "热度趋势标签"
                        ),
                        "insight" to Schema.string(description = "一句话说明大家围绕这个词在聊什么（不超过22字）")
                    )
                )
            ),
            "sentimentBreakdown" to Schema.array(
                description = "3到4个社区情绪/主题占比（总和约100）",
                items = Schema.obj(
                    properties = mapOf(
                        "label" to Schema.string(description = "维度名称，如：创作与课程、情绪树洞、互助交换、生活吐槽"),
                        "percentage" to Schema.integer(description = "占比整数 0-100")
                    )
                )
            ),
            "recommendedTags" to Schema.array(
                description = "3到5个建议同学搜索或发帖使用的#自定义标签",
                items = Schema.string(description = "以#开头的简短标签")
            )
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = statsSchema
            }
        )
    }

    suspend fun analyzeCommunityStats(
        query: String?,
        posts: List<Post>,
        courses: List<CourseRecord>,
        experiences: List<ExperienceArticle>
    ): AiCampusStatsReport {
        val cleanQuery = query?.trim().orEmpty()
        val scopeLabel = if (cleanQuery.isBlank()) "全站校园标签与热词" else "关键词「$cleanQuery」"

        // Aggregate real local tag & partition statistics to ground Gemini analysis
        val tagCounts = posts
            .flatMap { it.topicTags }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(18)

        val categoryCounts = posts
            .groupingBy { it.category.titleCn }
            .eachCount()
            .entries
            .sortedByDescending { it.value }

        val relevantPosts = if (cleanQuery.isBlank()) {
            posts.take(18)
        } else {
            val bare = cleanQuery.removePrefix("#")
            posts.filter { p ->
                p.body.contains(bare, true) ||
                    p.title.contains(bare, true) ||
                    p.category.titleCn.contains(bare, true) ||
                    (p.location?.contains(bare, true) == true) ||
                    p.topicTags.any { it.contains(bare, true) }
            }.ifEmpty { posts.take(8) }.take(16)
        }

        val relevantCourses = if (cleanQuery.isBlank()) {
            courses.take(6)
        } else {
            val bare = cleanQuery.removePrefix("#")
            courses.filter { c ->
                c.name.contains(bare, true) ||
                    c.instructor.contains(bare, true) ||
                    c.college.contains(bare, true) ||
                    c.summary.contains(bare, true) ||
                    c.suitableFor.contains(bare, true)
            }.take(6)
        }

        val relevantExperiences = if (cleanQuery.isBlank()) {
            experiences.take(5)
        } else {
            val bare = cleanQuery.removePrefix("#")
            experiences.filter { e ->
                e.title.contains(bare, true) ||
                    e.summary.contains(bare, true) ||
                    e.body.contains(bare, true) ||
                    e.tags.any { it.contains(bare, true) }
            }.take(5)
        }

        val prompt = buildString {
            appendLine("你是中央美术学院（央美）匿名校园社区「杨梅树窝」的 AI 智能搜索与数据统计助手。")
            appendLine("请结合以下真实的社区帖子、自定义标签、选课评价和学长学姐经验，输出简体中文的结构化 JSON 回答与统计报告。")
            appendLine()
            appendLine("用户搜索 / 统计目标：$scopeLabel")
            appendLine("全站样本：帖子 ${posts.size} 条，选课评价 ${courses.size} 门，学长学姐说 ${experiences.size} 篇。")
            appendLine("当前标签频次：${tagCounts.joinToString("，") { "${it.key}(${it.value}条)" }.ifBlank { "暂无" }}")
            appendLine("分区分布：${categoryCounts.joinToString("，") { "${it.key}(${it.value}条)" }}")
            appendLine()
            appendLine("相关帖子摘要（${relevantPosts.size} 条）：")
            relevantPosts.forEachIndexed { idx, p ->
                val tags = p.topicTags.joinToString(" ")
                val loc = p.location?.let { " [定位:$it]" }.orEmpty()
                val snippet = p.body.replace("\n", " ").take(95)
                appendLine("${idx + 1}. [${p.category.titleCn}]$loc $tags | 赞${p.likeCount} 评${p.comments.size} | $snippet")
            }
            if (relevantCourses.isNotEmpty()) {
                appendLine()
                appendLine("相关选课评价（${relevantCourses.size} 门）：")
                relevantCourses.forEach { c ->
                    appendLine("- ${c.name}（${c.instructor} / ${c.college} / ${c.campus}）推荐${String.format("%.1f", c.computedRecommend)} 给分${String.format("%.1f", c.computedGradingFriendly)} 考勤:${c.topAttendanceAnswer} 考核:${c.topExamAnswer} | ${c.summary.take(70)}")
                }
            }
            if (relevantExperiences.isNotEmpty()) {
                appendLine()
                appendLine("相关学长学姐说（${relevantExperiences.size} 篇）：")
                relevantExperiences.forEach { e ->
                    appendLine("- [${e.category.labelCn} · ${e.year}] ${e.title}：${e.summary.take(75)}")
                }
            }
            appendLine()
            appendLine("要求：")
            appendLine("1. 若用户输入了具体关键词或问题，请在 headline 和 summary 中直接给出清晰、有帮助的 AI 搜索结论，并引用上述站内真实样本。")
            appendLine("2. 若未输入关键词，则总结当前央美社区最热讨论趋势与自定义标签分布。")
            appendLine("3. 语气简洁克制、有洞察力，所有 recommendedTags 均以 # 开头。")
        }

        try {
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val jsonString = response.text
                ?: throw IllegalStateException("AI 统计服务返回了空结果，请稍后重试。")

            val json = JSONObject(jsonString)
            val headline = json.optString("headline").ifBlank { "${scopeLabel}趋势统计" }
            val summary = json.optString("summary").ifBlank { "已完成当前样本的标签聚类与热度分布统计。" }

            val topKeywordsArr = json.optJSONArray("topKeywords")
            val topKeywords = buildList {
                if (topKeywordsArr != null) {
                    for (i in 0 until topKeywordsArr.length()) {
                        val obj = topKeywordsArr.optJSONObject(i) ?: continue
                        add(
                            AiKeywordStat(
                                keyword = obj.optString("keyword"),
                                count = obj.optInt("count", 1).coerceAtLeast(0),
                                heatLabel = obj.optString("heatLabel", "热门"),
                                insight = obj.optString("insight")
                            )
                        )
                    }
                }
            }.filter { it.keyword.isNotBlank() }

            val sentimentArr = json.optJSONArray("sentimentBreakdown")
            val sentimentBreakdown = buildList {
                if (sentimentArr != null) {
                    for (i in 0 until sentimentArr.length()) {
                        val obj = sentimentArr.optJSONObject(i) ?: continue
                        add(
                            AiSentimentShare(
                                label = obj.optString("label"),
                                percentage = obj.optInt("percentage", 0).coerceIn(0, 100)
                            )
                        )
                    }
                }
            }.filter { it.label.isNotBlank() }

            val recArr = json.optJSONArray("recommendedTags")
            val recommendedTags = buildList {
                if (recArr != null) {
                    for (i in 0 until recArr.length()) {
                        val raw = recArr.optString(i).trim()
                        if (raw.isNotEmpty()) {
                            add(if (raw.startsWith("#")) raw else "#$raw")
                        }
                    }
                }
            }.distinct().take(5)

            return AiCampusStatsReport(
                queryLabel = scopeLabel,
                headline = headline,
                summary = summary,
                topKeywords = topKeywords,
                sentimentBreakdown = sentimentBreakdown,
                recommendedTags = recommendedTags
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI statistics timed out", e)
            throw IllegalStateException("AI 统计分析超时（60秒），请检查网络后重试。", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI statistics generation failed", e)
            throw e
        }
    }
}
