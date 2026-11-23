package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.components.AnonymousAvatar
import com.example.ui.components.EditorialHairline
import com.example.ui.components.MonoTagChip
import com.example.ui.components.categoryIconFor
import com.example.ui.theme.*
import com.example.viewmodel.YangmeiUiState

/**
 * Clean Publish Screen with:
 * - Direct text entry (no title input required)
 * - All 7 campus partitions (匿名树洞 + 6 校园生活 partitions) visible at a glance
 * - Selectable fixed campus tags (including #情绪树洞 and #恋爱情感)
 * - Multi-photo picker, poll builder, and draft saving
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PublishScreen(
    uiState: YangmeiUiState,
    initialCategory: PostCategory = PostCategory.TREE_HOLE,
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
        (listOf(PostCategory.TREE_HOLE) + campusLifeCategories).distinct()
    }
    var selectedCategory by remember(draft, initialCategory) {
        val candidate = draft?.category ?: initialCategory
        mutableStateOf(if (publishPartitions.contains(candidate)) candidate else PostCategory.TREE_HOLE)
    }
    var body by remember(draft) { mutableStateOf(draft?.body ?: "") }
    var selectedTags by remember(draft) { mutableStateOf(draft?.topicTags ?: emptyList()) }
    var customTagInput by remember { mutableStateOf("") }
    var mediaUris by remember(draft) { mutableStateOf(draft?.mediaList?.mapNotNull { it.uriString } ?: emptyList()) }
    var allowComments by remember(draft) { mutableStateOf(draft?.allowComments ?: true) }
    var selectedMood by remember(draft) { mutableStateOf(draft?.moodTag ?: "迷茫") }
    var price by remember(draft) { mutableStateOf(draft?.price ?: "") }
    var tradeStatus by remember(draft) { mutableStateOf(draft?.tradeStatus ?: "校内面交") }
    var location by remember(draft) { mutableStateOf(draft?.location ?: "") }
    var eventTime by remember(draft) { mutableStateOf(draft?.eventTime ?: "") }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showPollBuilder by remember { mutableStateOf(false) }
    var pollQuestion by remember { mutableStateOf("") }
    var pollOptionA by remember { mutableStateOf("") }
    var pollOptionB by remember { mutableStateOf("") }
    var pollOptionC by remember { mutableStateOf("") }

    val multiPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
    ) { uris ->
        if (uris.isNotEmpty()) {
            mediaUris = (mediaUris + uris.map { it.toString() }).distinct().take(4)
        }
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
                                text = selectedCategory.titleCn,
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
                        onClick = {
                            onPublish(
                                "", body, selectedCategory, selectedTags, mediaUris, allowComments,
                                price.takeIf { selectedCategory == PostCategory.MARKETPLACE },
                                tradeStatus.takeIf { selectedCategory == PostCategory.MARKETPLACE },
                                location, eventTime,
                                selectedMood.takeIf { selectedCategory == PostCategory.TREE_HOLE },
                                pollQuestion.takeIf { showPollBuilder },
                                listOf(pollOptionA, pollOptionB, pollOptionC),
                                true
                            )
                        },
                        modifier = Modifier.testTag("publish_save_draft_btn")
                    ) {
                        Text("存草稿", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            onPublish(
                                "", body, selectedCategory, selectedTags, mediaUris, allowComments,
                                price.takeIf { selectedCategory == PostCategory.MARKETPLACE },
                                tradeStatus.takeIf { selectedCategory == PostCategory.MARKETPLACE },
                                location, eventTime,
                                selectedMood.takeIf { selectedCategory == PostCategory.TREE_HOLE },
                                pollQuestion.takeIf { showPollBuilder },
                                listOf(pollOptionA, pollOptionB, pollOptionC),
                                false
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .testTag("publish_submit_btn")
                    ) {
                        Text("发布", fontWeight = FontWeight.SemiBold)
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
            // Top Partition Selector Card — starts in 匿名树洞 (or active partition), switchable in 1 tap
            item(key = "category_card") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = androidx.compose.foundation.BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
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
                                Text("发布分区（共7个）", style = MaterialTheme.typography.titleSmall)
                            }
                            Text(
                                text = "当前：${selectedCategory.titleCn}",
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
                                        text = cat.titleCn,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selected) PaperWhite else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Direct Text Writing Canvas Card (no title required)
            item(key = "writing_card") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = androidx.compose.foundation.BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
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
                                .defaultMinSize(minHeight = 160.dp)
                                .testTag("publish_body_input"),
                            decorationBox = { inner ->
                                Box {
                                    if (body.isEmpty()) {
                                        Text(
                                            text = "直接输入文字，分享你的央美日常、情绪树洞、选课感受或互助需求...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                    inner()
                                }
                            }
                        )

                        // Attached Photos Strip
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

            // Preset + Custom User-Written Tag Selector (支持自己写标签，搜索关键词可直达)
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
                val customSelectedTags = selectedTags.filter { tag ->
                    MockSeedData.fixedPublishTags.none { it.equals(tag, ignoreCase = true) }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = androidx.compose.foundation.BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
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
                                Text("选择或自写标签", style = MaterialTheme.typography.titleSmall)
                            }
                            Text(
                                text = "已选 ${selectedTags.size}/$maxTags",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }

                        Text(
                            text = "自己写的标签会被关键词搜索与 AI 统计收录，方便同学搜到你的帖子",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Custom tag input row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customTagInput,
                                onValueChange = { if (it.length <= 18) customTagInput = it },
                                placeholder = { Text("输入自定义标签，如：国画写生 / 毕设互助") },
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

                        // Display user-written custom tags with quick remove button
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
                            MockSeedData.fixedPublishTags.forEach { tag ->
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

            // Optional Location Selector (available for all 7 publish partitions)
            item(key = "optional_location_card") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = androidx.compose.foundation.BorderStroke(
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
                                    text = if (location.isNotBlank()) location else "添加定位（可选）",
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

            // Contextual fields for specialized categories
            if (selectedCategory == PostCategory.TREE_HOLE) {
                item(key = "tree_hole_mood") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("此刻情绪", style = MaterialTheme.typography.titleSmall)
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
            }

            if (selectedCategory == PostCategory.MARKETPLACE) {
                item(key = "market_fields") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("价格 (¥)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = tradeStatus,
                            onValueChange = { tradeStatus = it },
                            label = { Text("交易方式") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            if (selectedCategory == PostCategory.MARKETPLACE ||
                selectedCategory == PostCategory.LOST_FOUND ||
                selectedCategory == PostCategory.OPPORTUNITY ||
                selectedCategory == PostCategory.MUTUAL_AID ||
                selectedCategory == PostCategory.EXHIBITION
            ) {
                item(key = "event_time_field") {
                    OutlinedTextField(
                        value = eventTime,
                        onValueChange = { eventTime = it },
                        label = { Text("时间（可选，如：今晚 19:00 / 周末可面交）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Optional Poll Builder
            if (showPollBuilder) {
                item(key = "poll_builder") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline)
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

            // Custom location entry
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
