package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.components.AnonymousAvatar
import com.example.ui.components.EditorialHairline
import com.example.ui.components.MonoTagChip
import com.example.ui.components.categoryIconFor
import com.example.ui.theme.*
import com.example.viewmodel.YangmeiUiState

private data class PartitionComposeSpec(
    val headerTitle: String,
    val headerHint: String,
    val placeholder: String,
    val mediaHint: String,
    val locationPrompt: String,
    val curatedTags: List<String>,
    val ctaLabel: String
)

private fun composeSpecFor(category: PostCategory): PartitionComposeSpec = when (category) {
    PostCategory.TREE_HOLE -> PartitionComposeSpec(
        headerTitle = "匿名树洞 · 安静倾诉",
        headerHint = "在这里卸下防备，支持标记此刻情绪，可随时关闭评论仅留作自语",
        placeholder = "在杨梅树下放心倾诉此刻的心情、压力或深夜想法，这里没人知道你是谁...",
        mediaHint = "可附一张此刻看到的风景或随手涂鸦",
        locationPrompt = "添加此刻所在位置（可选）",
        curatedTags = listOf("#深夜树洞", "#情绪树洞", "#画室日常", "#花家地漫游", "#匿名倾诉"),
        ctaLabel = "投递树洞"
    )
    PostCategory.MARKETPLACE -> PartitionComposeSpec(
        headerTitle = "闲置交换 · 校内流转",
        headerHint = "填好分类、价格、成色与交接方式，方便央美同学快速定位并私信交换微信",
        placeholder = "描述画材/物品品牌、规格、使用痕迹、购买渠道与校内面交安排...",
        mediaHint = "建议附上 1~4 张实物实拍图，成交率更高",
        locationPrompt = "面交 / 自提地点（如：望京北门 / 燕郊宿舍）",
        curatedTags = listOf("#闲置画材", "#二手出物", "#教材流转", "#望京自提", "#燕郊自提"),
        ctaLabel = "发布闲置"
    )
    PostCategory.OPPORTUNITY -> PartitionComposeSpec(
        headerTitle = "校园机会 · 兼职与协作",
        headerHint = "清晰写明招募类型、报酬范围与技能要求，让合适专业的同学一眼看到",
        placeholder = "说明岗位职责、技能要求（如手绘/3D/排版/布展/拍摄）、时间周期与联系投递方式...",
        mediaHint = "可附项目海报、参考风格图或招募简章",
        locationPrompt = "工作地点（如：望京周边 / 798 / 线上远程）",
        curatedTags = listOf("#设计实习", "#艺术兼职", "#项目合作", "#展览助理", "#模特招募"),
        ctaLabel = "发布招募"
    )
    PostCategory.LOST_FOUND -> PartitionComposeSpec(
        headerTitle = "失物招领 · 校园互助找回",
        headerHint = "先选「失物寻找」或「拾物招领」，写明时间与具体地点，重要证件注意遮挡隐私",
        placeholder = "描述物品外观特征（颜色、型号、挂件、贴纸或内部标识），方便失主/拾主快速核对...",
        mediaHint = "附物品实物照或发现位置环境图更易找回",
        locationPrompt = "丢失 / 拾获地点（必填推荐，如：美术馆 / 7号楼）",
        curatedTags = listOf("#失物招领", "#校园寻物", "#一卡通", "#速写本", "#急寻"),
        ctaLabel = "发布寻物"
    )
    PostCategory.MUTUAL_AID -> PartitionComposeSpec(
        headerTitle = "校园互助 · 搭子与应急",
        headerHint = "拼车买材料、找看展/自习搭子、借工具或代取跑腿，写清时间与人数更易成行",
        placeholder = "说明出发时间、人数需求、工具型号或具体互助细节，方便顺路的同学直接响应...",
        mediaHint = "可附路线截图、材料清单或参考图",
        locationPrompt = "集合或互助地点（如：望京西门 / 红星美凯龙）",
        curatedTags = listOf("#校园互助", "#拼车买材料", "#寻找搭子", "#画材借用", "#代取跑腿"),
        ctaLabel = "发起互助"
    )
    PostCategory.EXHIBITION -> PartitionComposeSpec(
        headerTitle = "展览资讯 · 看展与导览",
        headerHint = "记录校内新展、毕业展、快闪空间或 798 周边展览，方便同学错峰观展",
        placeholder = "分享展览主题、参展艺术家、布展亮点、导览安排或你的看展笔记...",
        mediaHint = "建议附展览海报或现场空间照片",
        locationPrompt = "展览场馆（如：央美美术馆 / 7号楼展厅 / 798）",
        curatedTags = listOf("#校园展览", "#央美美术馆", "#看展日记", "#毕业季", "#798看展"),
        ctaLabel = "发布展讯"
    )
    PostCategory.ART_CREATION -> PartitionComposeSpec(
        headerTitle = "艺术创作 · 作品与过程手记",
        headerHint = "标记创作媒介与当前阶段，和不同院系同学交流材料技法、构图与策展思路",
        placeholder = "记录创作灵感、材料技法实验、色彩推敲，或聊聊卡壳的地方听听大家建议...",
        mediaHint = "上传最多 4 张草图、过程细节或成品局部",
        locationPrompt = "所在工作室 / 校区（可选）",
        curatedTags = listOf("#艺术创作", "#毕业作品", "#材料实验", "#工作室日常", "#作品互评"),
        ctaLabel = "发布作品"
    )
    PostCategory.RELATIONSHIP -> PartitionComposeSpec(
        headerTitle = "恋爱情感 · 匿名聊聊心动与困惑",
        headerHint = "纯匿名安全空间，聊聊校园里的心动瞬间、亲密关系相处或情感提问",
        placeholder = "写下关于心动、相处、遗憾或想听听大家看法的故事...",
        mediaHint = "可附一张氛围感配图（可选）",
        locationPrompt = "偶遇或纪念地点（可选）",
        curatedTags = listOf("#恋爱情感", "#央美心动", "#情感树洞", "#脱单随缘", "#关系讨论"),
        ctaLabel = "匿名发布"
    )
    else -> PartitionComposeSpec(
        headerTitle = "${category.titleCn} · 发布内容",
        headerHint = category.description,
        placeholder = "直接输入文字，分享你的央美日常、想法或互助需求...",
        mediaHint = "可添加最多 4 张图片",
        locationPrompt = "添加定位（可选）",
        curatedTags = MockSeedData.fixedPublishTags.take(6),
        ctaLabel = "发布"
    )
}

/**
 * Partition-aware Publish Screen:
 * - Adapts header, structured fields, placeholders, media hints, and curated tags to each partition.
 * - When launched from a single partition (isPartitionLocked = true), locks onto that partition with
 *   a tailored partition banner instead of the global switcher.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PublishScreen(
    uiState: YangmeiUiState,
    initialCategory: PostCategory = PostCategory.TREE_HOLE,
    isPartitionLocked: Boolean = false,
    onClose: () -> Unit,
    onPublish: (
        title: String,
        body: String,
        category: PostCategory,
        topicTags: List<String>,
        mediaUris: List<String>,
        allowComments: Boolean,
        price: String?,
        tradeStatus: String?,
        location: String?,
        eventTime: String?,
        moodTag: String?,
        pollQuestion: String?,
        pollOptions: List<String>,
        asDraft: Boolean
    ) -> Unit
) {
    val draft = uiState.editingDraft
    val publishPartitions = remember {
        listOf(
            PostCategory.TREE_HOLE,
            PostCategory.MARKETPLACE,
            PostCategory.OPPORTUNITY,
            PostCategory.LOST_FOUND,
            PostCategory.MUTUAL_AID
        )
    }
    fun partitionDisplayName(cat: PostCategory): String =
        if (cat == PostCategory.TREE_HOLE) "情绪树洞" else cat.titleCn

    var selectedCategory by remember(draft, initialCategory, isPartitionLocked) {
        val candidate = draft?.category ?: initialCategory
        val resolved = if (isPartitionLocked) {
            candidate
        } else if (publishPartitions.contains(candidate)) {
            candidate
        } else {
            PostCategory.TREE_HOLE
        }
        mutableStateOf(resolved)
    }
    val spec = remember(selectedCategory) { composeSpecFor(selectedCategory) }
    val isDarkTheme = MaterialTheme.colorScheme.background.red < 0.2f

    // Common fields
    var body by remember(draft) { mutableStateOf(draft?.body ?: "") }
    var selectedTags by remember(draft) { mutableStateOf(draft?.topicTags ?: emptyList()) }
    var customTagInput by remember { mutableStateOf("") }
    var mediaUris by remember(draft) { mutableStateOf(draft?.mediaList?.mapNotNull { it.uriString } ?: emptyList()) }
    var allowComments by remember(draft) { mutableStateOf(draft?.allowComments ?: true) }
    var location by remember(draft) { mutableStateOf(draft?.location ?: "") }
    var eventTime by remember(draft) { mutableStateOf(draft?.eventTime ?: "") }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showPollBuilder by remember { mutableStateOf(false) }
    var pollQuestion by remember { mutableStateOf("") }
    var pollOptionA by remember { mutableStateOf("") }
    var pollOptionB by remember { mutableStateOf("") }
    var pollOptionC by remember { mutableStateOf("") }

    // Partition-specific structured fields
    var selectedMood by remember(draft) { mutableStateOf(draft?.moodTag ?: "迷茫") }
    // Marketplace
    var marketSubType by remember { mutableStateOf("画材") }
    var marketCondition by remember { mutableStateOf("九五新") }
    var price by remember(draft) { mutableStateOf(draft?.price ?: "") }
    var tradeStatus by remember(draft) { mutableStateOf(draft?.tradeStatus ?: "望京面交") }
    // Opportunity
    var oppType by remember { mutableStateOf("兼职招募") }
    var oppReward by remember { mutableStateOf("") }
    var oppMode by remember { mutableStateOf("线上远程") }
    // Lost & Found
    var lfType by remember { mutableStateOf("失物寻找") }
    var lfItemName by remember { mutableStateOf("") }
    // Mutual Aid
    var aidType by remember { mutableStateOf("拼车买材料") }
    var aidUrgency by remember { mutableStateOf("今天急需") }
    var aidReward by remember { mutableStateOf("") }
    // Exhibition
    var exhType by remember { mutableStateOf("校内个展") }
    var exhEntry by remember { mutableStateOf("校内免费") }
    // Art Creation
    var artMedium by remember { mutableStateOf("油画") }
    var artStage by remember { mutableStateOf("进行中求建议") }
    // Relationship
    var relVibe by remember { mutableStateOf("情感树洞") }

    val multiPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
    ) { uris ->
        if (uris.isNotEmpty()) {
            mediaUris = (mediaUris + uris.map { it.toString() }).distinct().take(4)
        }
    }

    fun submitPost(asDraft: Boolean) {
        val autoTags = mutableListOf<String>()
        val prefixParts = mutableListOf<String>()
        var finalTitle = ""
        var finalPrice: String? = null
        var finalTradeStatus: String? = null
        var finalMood: String? = null

        when (selectedCategory) {
            PostCategory.TREE_HOLE -> {
                finalMood = selectedMood
            }
            PostCategory.MARKETPLACE -> {
                autoTags += "#$marketSubType"
                finalPrice = price.trim().takeIf { it.isNotEmpty() }
                finalTradeStatus = "$marketCondition · $tradeStatus"
                prefixParts += "【$marketSubType · $marketCondition · $tradeStatus】"
            }
            PostCategory.OPPORTUNITY -> {
                autoTags += "#$oppType"
                val rewardPart = oppReward.trim().takeIf { it.isNotEmpty() }?.let { " · 报酬 $it" } ?: ""
                prefixParts += "【$oppType · $oppMode$rewardPart】"
                finalTitle = oppType
            }
            PostCategory.LOST_FOUND -> {
                autoTags += "#$lfType"
                val itemPart = lfItemName.trim().takeIf { it.isNotEmpty() }?.let { " $it" } ?: ""
                prefixParts += "【$lfType$itemPart】"
                finalTitle = "$lfType$itemPart".take(15)
            }
            PostCategory.MUTUAL_AID -> {
                autoTags += "#$aidType"
                val rewardPart = aidReward.trim().takeIf { it.isNotEmpty() }?.let { " · $it" } ?: ""
                prefixParts += "【$aidType · $aidUrgency$rewardPart】"
            }
            PostCategory.EXHIBITION -> {
                autoTags += "#$exhType"
                prefixParts += "【$exhType · $exhEntry】"
            }
            PostCategory.ART_CREATION -> {
                autoTags += "#$artMedium"
                prefixParts += "【$artMedium · $artStage】"
            }
            PostCategory.RELATIONSHIP -> {
                autoTags += "#$relVibe"
                finalMood = relVibe
            }
            else -> Unit
        }

        val rawBody = body.trim()
        val finalBody = if (rawBody.isNotEmpty() && prefixParts.isNotEmpty() && !rawBody.startsWith("【")) {
            "${prefixParts.joinToString(" ")}\n$rawBody"
        } else {
            rawBody
        }
        val mergedTags = (selectedTags + autoTags).distinct().take(6)

        onPublish(
            finalTitle,
            finalBody,
            selectedCategory,
            mergedTags,
            mediaUris,
            allowComments,
            finalPrice,
            finalTradeStatus,
            location,
            eventTime,
            finalMood,
            pollQuestion.takeIf { showPollBuilder },
            listOf(pollOptionA, pollOptionB, pollOptionC),
            asDraft
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnonymousAvatar(seed = uiState.currentPersona.avatarSeed, size = 28.dp)
                        Column {
                            Text(
                                text = if (isPartitionLocked) {
                                    "发布到 · ${partitionDisplayName(selectedCategory)}"
                                } else {
                                    partitionDisplayName(selectedCategory)
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = uiState.currentPersona.nickname,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose, modifier = Modifier.testTag("publish_close_btn")) {
                        Icon(Icons.Outlined.Close, contentDescription = "关闭发布页")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { submitPost(asDraft = true) },
                        enabled = !uiState.isModerating,
                        modifier = Modifier.testTag("publish_save_draft_btn")
                    ) {
                        Text("存草稿", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = { submitPost(asDraft = false) },
                        enabled = !uiState.isModerating,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .testTag("publish_submit_btn")
                    ) {
                        if (uiState.isModerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(15.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("审查中...", fontWeight = FontWeight.SemiBold)
                        } else {
                            Text(spec.ctaLabel, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.navigationBarsPadding().imePadding()) {
                    EditorialHairline()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    multiPhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("publish_add_image_btn")
                            ) {
                                Icon(Icons.Outlined.Image, contentDescription = "添加图片")
                            }
                            if (selectedCategory != PostCategory.LOST_FOUND && selectedCategory != PostCategory.MARKETPLACE) {
                                IconButton(
                                    onClick = { showPollBuilder = !showPollBuilder },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("publish_toggle_poll_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Poll,
                                        contentDescription = "添加投票",
                                        tint = if (showPollBuilder) AuroraIndigo else MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showLocationPicker = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("publish_toolbar_location_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = "选择定位",
                                    tint = if (location.isNotBlank()) AuroraIndigo else MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (allowComments) "允许评论" else "关闭评论",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Switch(
                                checked = allowComments,
                                onCheckedChange = { allowComments = it },
                                modifier = Modifier.testTag("publish_allow_comments_switch")
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Automatic Legal & Safety Moderation Protection Strip
            item(key = "publish_safety_guard_notice") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                        .testTag("publish_safety_notice"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = AuroraFocusIndigo,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (uiState.isModerating) {
                            "正在进行文字与图片自动安全审查，请稍候..."
                        } else {
                            "已开启全自动安全审查：严禁发布违法违规、涉黄暴恐、诈骗或不良文字与图片"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Top Partition Banner (locked single-partition) OR Partition Switcher (global entry)
            item(key = "partition_header_or_switcher") {
                if (isPartitionLocked) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .then(
                                if (isDarkTheme) Modifier.background(MaterialTheme.colorScheme.surface)
                                else Modifier.background(YangmeiFeaturedCardGradient)
                            )
                            .border(0.8.dp, AuroraIndigo.copy(alpha = 0.32f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(YangmeiPrimaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryIconFor(selectedCategory),
                                contentDescription = null,
                                tint = PaperWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = spec.headerTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = spec.headerHint,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Outlined.GridView, contentDescription = null, modifier = Modifier.size(16.dp), tint = SlateAccent)
                                    Text("选择发布分区", style = MaterialTheme.typography.titleSmall)
                                }
                                Text(
                                    text = "当前：${partitionDisplayName(selectedCategory)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                publishPartitions.forEach { cat ->
                                    val selected = cat == selectedCategory
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .then(
                                                if (selected) Modifier.background(YangmeiPrimaryGradient)
                                                else Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                            )
                                            .clickable { selectedCategory = cat }
                                            .padding(horizontal = 11.dp, vertical = 7.dp)
                                            .testTag("publish_cat_${cat.name.lowercase()}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = categoryIconFor(cat),
                                            contentDescription = null,
                                            tint = if (selected) PaperWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = partitionDisplayName(cat),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (selected) PaperWhite else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Text(
                                text = spec.headerHint,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Partition-Tailored Structured Form Section (placed prominently above or right alongside writing)
            item(key = "partition_structured_card_${selectedCategory.name}") {
                when (selectedCategory) {
                    PostCategory.TREE_HOLE -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("此刻情绪", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(MockSeedData.treeHoleMoods) { mood ->
                                        MonoTagChip(
                                            text = mood,
                                            inverted = selectedMood == mood,
                                            onClick = { selectedMood = mood }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    PostCategory.MARKETPLACE -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("闲置物品信息", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("画材", "教材", "电子产品", "家具", "生活用品", "服饰").forEach { itemType ->
                                        MonoTagChip(
                                            text = itemType,
                                            inverted = marketSubType == itemType,
                                            onClick = { marketSubType = itemType }
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = price,
                                        onValueChange = { price = it },
                                        label = { Text("出物价格 (¥)") },
                                        placeholder = { Text("如：120 / 可小刀") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(11.dp)
                                    )
                                    OutlinedTextField(
                                        value = eventTime,
                                        onValueChange = { eventTime = it },
                                        label = { Text("可面交时间") },
                                        placeholder = { Text("如：今晚/周末") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1.1f),
                                        shape = RoundedCornerShape(11.dp)
                                    )
                                }
                                Text("成色", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(listOf("全新未拆", "九五新", "九成新", "实用成色")) { cond ->
                                        MonoTagChip(
                                            text = cond,
                                            inverted = marketCondition == cond,
                                            onClick = { marketCondition = cond }
                                        )
                                    }
                                }
                                Text("交接方式", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(listOf("望京面交", "燕郊自提", "上海自提", "同城邮寄", "可换物")) { mode ->
                                        MonoTagChip(
                                            text = mode,
                                            inverted = tradeStatus == mode,
                                            onClick = { tradeStatus = mode }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    PostCategory.OPPORTUNITY -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("招募 / 机会要素", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("兼职招募", "实习招聘", "项目合作", "展览协作", "模特招募").forEach { t ->
                                        MonoTagChip(text = t, inverted = oppType == t, onClick = { oppType = t })
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = oppReward,
                                        onValueChange = { oppReward = it },
                                        label = { Text("薪资 / 报酬") },
                                        placeholder = { Text("如：日薪260 / 面议") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(11.dp)
                                    )
                                    OutlinedTextField(
                                        value = eventTime,
                                        onValueChange = { eventTime = it },
                                        label = { Text("周期 / 截止时间") },
                                        placeholder = { Text("如：本周五前") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(11.dp)
                                    )
                                }
                                Text("协作方式", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(listOf("线上远程", "望京周边", "校内协作", "短期驻场")) { m ->
                                        MonoTagChip(text = m, inverted = oppMode == m, onClick = { oppMode = m })
                                    }
                                }
                            }
                        }
                    }

                    PostCategory.LOST_FOUND -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("寻物 / 招领信息", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    listOf("失物寻找", "拾物招领").forEach { mode ->
                                        val selected = lfType == mode
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 44.dp)
                                                .clip(RoundedCornerShape(11.dp))
                                                .then(
                                                    if (selected) Modifier.background(YangmeiPrimaryGradient)
                                                    else Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                                )
                                                .clickable { lfType = mode },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = mode,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (selected) PaperWhite else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = lfItemName,
                                    onValueChange = { lfItemName = it },
                                    label = { Text("物品名称") },
                                    placeholder = { Text("如：校园一卡通 / 黑色速写本 / AirPods") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(11.dp)
                                )
                                OutlinedTextField(
                                    value = eventTime,
                                    onValueChange = { eventTime = it },
                                    label = { Text(if (lfType == "失物寻找") "遗失时间" else "拾获时间") },
                                    placeholder = { Text("如：今天上午 10:30 左右") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(11.dp)
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(listOf("刚刚", "今天上午", "今天下午", "昨天")) { t ->
                                        MonoTagChip(text = t, inverted = eventTime == t, onClick = { eventTime = t })
                                    }
                                }
                            }
                        }
                    }

                    PostCategory.MUTUAL_AID -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("互助需求卡片", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("拼车买材料", "寻找搭子", "代取跑腿", "学习互助", "借物求助").forEach { t ->
                                        MonoTagChip(text = t, inverted = aidType == t, onClick = { aidType = t })
                                    }
                                }
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(listOf("今天急需", "本周内", "随时可约")) { u ->
                                        MonoTagChip(text = u, inverted = aidUrgency == u, onClick = { aidUrgency = u })
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = eventTime,
                                        onValueChange = { eventTime = it },
                                        label = { Text("约定时间") },
                                        placeholder = { Text("如：今晚 18:30") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(11.dp)
                                    )
                                    OutlinedTextField(
                                        value = aidReward,
                                        onValueChange = { aidReward = it },
                                        label = { Text("分摊 / 酬谢（可选）") },
                                        placeholder = { Text("如：AA / 请喝咖啡") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(11.dp)
                                    )
                                }
                            }
                        }
                    }

                    PostCategory.EXHIBITION -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("展览档案信息", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("校内个展", "群展联展", "毕业展", "讲座导览", "校外看展").forEach { t ->
                                        MonoTagChip(text = t, inverted = exhType == t, onClick = { exhType = t })
                                    }
                                }
                                OutlinedTextField(
                                    value = eventTime,
                                    onValueChange = { eventTime = it },
                                    label = { Text("展期 / 开幕时间") },
                                    placeholder = { Text("如：5.12 - 5.28 · 每日 9:30-17:00") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(11.dp)
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(listOf("校内免费", "需预约", "开幕导览", "凭证免票")) { e ->
                                        MonoTagChip(text = e, inverted = exhEntry == e, onClick = { exhEntry = e })
                                    }
                                }
                            }
                        }
                    }

                    PostCategory.ART_CREATION -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("创作档案标签", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("油画", "国画", "版画", "雕塑", "实验艺术", "设计排版", "影像装置", "速写").forEach { m ->
                                        MonoTagChip(text = m, inverted = artMedium == m, onClick = { artMedium = m })
                                    }
                                }
                                Text("创作阶段", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(listOf("灵感草图", "进行中求建议", "成品展示", "毕设打磨")) { s ->
                                        MonoTagChip(text = s, inverted = artStage == s, onClick = { artStage = s })
                                    }
                                }
                            }
                        }
                    }

                    PostCategory.RELATIONSHIP -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("情感氛围", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(listOf("情感树洞", "心动记录", "恋爱求助", "脱单随缘")) { v ->
                                        MonoTagChip(text = v, inverted = relVibe == v, onClick = { relVibe = v })
                                    }
                                }
                            }
                        }
                    }

                    else -> Unit
                }
            }

            // Direct Text Writing Canvas Card with partition-specific placeholder & image prompt
            item(key = "writing_card") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BasicTextField(
                            value = body,
                            onValueChange = { body = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 148.dp)
                                .testTag("publish_body_input"),
                            decorationBox = { inner ->
                                Box {
                                    if (body.isEmpty()) {
                                        Text(
                                            text = spec.placeholder,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                    inner()
                                }
                            }
                        )

                        // Quick photo strip + partition-specific media hint
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    multiPhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddPhotoAlternate,
                                contentDescription = null,
                                tint = AuroraIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (mediaUris.isEmpty()) spec.mediaHint else "已添加 ${mediaUris.size}/4 张图片 · 点击继续添加",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (mediaUris.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(mediaUris) { uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(84.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(0.7.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "已选图片",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .background(InkBlack.copy(alpha = 0.78f), RoundedCornerShape(bottomStart = 8.dp))
                                                .clickable { mediaUris = mediaUris.filterNot { it == uri } }
                                                .padding(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Close,
                                                contentDescription = "移除图片",
                                                tint = PaperWhite,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Location Selector (contextual prompt per partition)
            item(key = "optional_location_card") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = BorderStroke(
                        width = if (location.isNotBlank()) 1.dp else 0.7.dp,
                        color = if (location.isNotBlank()) AuroraIndigo.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("publish_location_card"),
                    onClick = { showLocationPicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "定位",
                                tint = if (location.isNotBlank()) AuroraIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = if (location.isNotBlank()) location else spec.locationPrompt,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (location.isNotBlank()) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (location.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (location.isNotBlank()) "已选择校园定位 · 点击更换" else "点击选择央美校区地点或自定义位置",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                        if (location.isNotBlank()) {
                            IconButton(
                                onClick = { location = "" },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("publish_location_clear_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "清除定位",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Partition-Curated + Custom User-Written Tag Selector
            item(key = "fixed_tags_card") {
                val maxTags = 5
                fun commitCustomTag() {
                    val cleaned = customTagInput
                        .trim()
                        .removePrefix("#")
                        .replace(Regex("\\s+"), "")
                        .take(16)
                    if (cleaned.isNotEmpty() && selectedTags.size < maxTags) {
                        val formatted = "#$cleaned"
                        if (selectedTags.none { it.equals(formatted, ignoreCase = true) }) {
                            selectedTags = selectedTags + formatted
                        }
                        customTagInput = ""
                    }
                }
                val suggestedTags = remember(selectedCategory) {
                    (spec.curatedTags + MockSeedData.fixedPublishTags).distinct().take(10)
                }
                val customSelectedTags = selectedTags.filter { tag ->
                    suggestedTags.none { it.equals(tag, ignoreCase = true) }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Outlined.Tag, contentDescription = null, modifier = Modifier.size(16.dp), tint = SlateAccent)
                                Text("${selectedCategory.titleCn}推荐标签", style = MaterialTheme.typography.titleSmall)
                            }
                            Text(
                                text = "已选 ${selectedTags.size}/$maxTags",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customTagInput,
                                onValueChange = { if (it.length <= 18) customTagInput = it },
                                placeholder = { Text("自定义标签，如：国画写生 / 毕设互助") },
                                singleLine = true,
                                prefix = { Text("#", color = AuroraIndigo, fontWeight = FontWeight.Bold) },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { commitCustomTag() }),
                                shape = RoundedCornerShape(11.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("publish_custom_tag_input")
                            )
                            Button(
                                onClick = { commitCustomTag() },
                                enabled = customTagInput.trim().removePrefix("#").isNotEmpty() && selectedTags.size < maxTags,
                                shape = RoundedCornerShape(11.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                                modifier = Modifier
                                    .heightIn(min = 50.dp)
                                    .testTag("publish_custom_tag_add_btn")
                            ) {
                                Text("添加", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (customSelectedTags.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                customSelectedTags.forEach { tag ->
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(YangmeiPrimaryGradient)
                                            .clickable { selectedTags = selectedTags - tag }
                                            .padding(horizontal = 11.dp, vertical = 7.dp)
                                            .testTag("publish_custom_tag_chip_$tag"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PaperWhite
                                        )
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "移除自定义标签",
                                            tint = PaperWhite,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            suggestedTags.forEach { tag ->
                                val isSelected = selectedTags.contains(tag)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .then(
                                            if (isSelected) Modifier.background(YangmeiPrimaryGradient)
                                            else Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                        )
                                        .clickable {
                                            selectedTags = if (isSelected) {
                                                selectedTags - tag
                                            } else if (selectedTags.size < maxTags) {
                                                selectedTags + tag
                                            } else selectedTags
                                        }
                                        .padding(horizontal = 11.dp, vertical = 7.dp)
                                        .testTag("publish_tag_$tag"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = PaperWhite,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) PaperWhite else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Optional Poll Builder
            if (showPollBuilder) {
                item(key = "poll_builder") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("附带投票（至少填写两个选项）", style = MaterialTheme.typography.titleSmall)
                            OutlinedTextField(
                                value = pollQuestion,
                                onValueChange = { pollQuestion = it },
                                label = { Text("投票问题") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = pollOptionA,
                                onValueChange = { pollOptionA = it },
                                label = { Text("选项 1") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = pollOptionB,
                                onValueChange = { pollOptionB = it },
                                label = { Text("选项 2") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = pollOptionC,
                                onValueChange = { pollOptionC = it },
                                label = { Text("选项 3（可选）") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLocationPicker) {
        PublishLocationPickerSheet(
            currentLocation = location,
            onDismiss = { showLocationPicker = false },
            onSelectLocation = { picked ->
                location = picked
                showLocationPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PublishLocationPickerSheet(
    currentLocation: String,
    onDismiss: () -> Unit,
    onSelectLocation: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customInput by remember(currentLocation) { mutableStateOf(currentLocation) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = AuroraIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "选择所在位置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(
                    onClick = { onSelectLocation("") },
                    modifier = Modifier.testTag("location_picker_none_btn")
                ) {
                    Text("不显示定位", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customInput,
                    onValueChange = { customInput = it },
                    placeholder = { Text("自定义地点，如：7号楼四层 / 798艺术区") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("location_picker_custom_input")
                )
                Button(
                    onClick = { onSelectLocation(customInput.trim()) },
                    enabled = customInput.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .heightIn(min = 52.dp)
                        .testTag("location_picker_confirm_btn")
                ) {
                    Text("确定")
                }
            }

            Text(
                text = "央美常用校园定位",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 18.dp)
            ) {
                MockSeedData.cafaLocationPresets.forEach { preset ->
                    val selected = currentLocation == preset
                    MonoTagChip(
                        text = preset,
                        inverted = selected,
                        onClick = { onSelectLocation(preset) }
                    )
                }
            }
        }
    }
}
