package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.YangmeiUiState

// The 6 core Campus Life partitions in Discover
val campusLifeCategories = listOf(
    PostCategory.COURSE_ARCHIVE,
    PostCategory.SENIOR_EXPERIENCE,
    PostCategory.MARKETPLACE,
    PostCategory.OPPORTUNITY,
    PostCategory.LOST_FOUND,
    PostCategory.MUTUAL_AID
)

/**
 * Clean Discover hub organized into:
 * 1) 热门话题 (tapping opens Weibo-style hot search ranking list)
 * 2) 校园生活 (6 essential campus partitions)
 */
@Composable
fun DiscoverScreen(
    uiState: YangmeiUiState,
    onOpenSearch: () -> Unit,
    onOpenTopic: (String) -> Unit,
    onOpenAllTopics: () -> Unit,
    onOpenCourseArchive: () -> Unit,
    onOpenSeniorExperience: () -> Unit,
    onOpenCategory: (PostCategory) -> Unit,
    onFabVisibilityChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val campusLifePairs = remember { campusLifeCategories.chunked(2) }
    val listState = rememberLazyListState()

    ObserveScrollFabVisibility(
        listState = listState,
        onFabVisibilityChange = onFabVisibilityChange
    )

    fun navigateCategory(category: PostCategory) {
        when (category) {
            PostCategory.COURSE_ARCHIVE -> onOpenCourseArchive()
            PostCategory.SENIOR_EXPERIENCE -> onOpenSeniorExperience()
            else -> onOpenCategory(category)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AuroraPageBackground(seed = 2, headerWashHeight = 230.dp)

        Column(modifier = Modifier.fillMaxSize()) {
            // Header sits cleanly above the progressive-blur aurora mask
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProgressiveBlurTitle(text = "发现", fontSize = 26.sp)

                GradientBorderCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("discover_search_bar"),
                    outerCornerRadius = 14.dp,
                    borderWidth = 1.6.dp,
                    shadowStrength = 0.7f,
                    onClick = onOpenSearch
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 46.dp)
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "AI 搜索",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "AI 搜索选课、经验、闲置或提问...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(YangmeiPrimaryGradient)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = PaperWhite,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "AI搜索",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PaperWhite
                            )
                        }
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("discover_screen_list"),
                contentPadding = PaddingValues(top = 8.dp, bottom = 124.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            // Section 1: Hot Topics — Leftward Auto-Scrolling Numbered Ranking Strip
            item(key = "discover_hot_topics") {
                val rankedTopics = remember(uiState.hotTopics) {
                    uiState.hotTopics.sortedByDescending { it.discussionCount }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenAllTopics)
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .testTag("discover_topic_square_btn"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalFireDepartment,
                                contentDescription = null,
                                tint = VermilionRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "热门话题榜",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "完整热搜榜",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "查看热搜榜",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    HotTopicsAutoScrollStrip(
                        rankedTopics = rankedTopics,
                        onOpenTopic = onOpenTopic
                    )
                }
            }

            // Section 2: Campus Life (6 core partitions)
            item(key = "section_campus_life_title") {
                Column(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "校园生活",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "选课评价、学长学姐说、二手交易、实习与互助",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(campusLifePairs, key = { pair -> "life_${pair.joinToString("_") { it.name }}" }) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { category ->
                        DiscoverCategoryTile(
                            category = category,
                            onClick = { navigateCategory(category) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("discover_cat_${category.name.lowercase()}")
                        )
                    }
                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        }
    }
}

/**
 * Smooth leftward auto-scrolling horizontal ranked Hot Topics board.
 * Loops seamlessly across all ranked topics with clear numbered badges (01, 02, 03...),
 * pauses automatically while the user drags manually, and respects prefers-reduced-motion.
 */
@Composable
private fun HotTopicsAutoScrollStrip(
    rankedTopics: List<HotTopic>,
    onOpenTopic: (String) -> Unit
) {
    if (rankedTopics.isEmpty()) return
    val prefersReducedMotion = rememberPrefersReducedMotion()
    val startIndex = remember(rankedTopics.size) {
        val base = 1000
        base - (base % rankedTopics.size)
    }
    val rowState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val isDragged by rowState.interactionSource.collectIsDraggedAsState()

    // Continuous smooth leftward scroll (content moves right-to-left)
    LaunchedEffect(prefersReducedMotion, isDragged, rankedTopics.size) {
        if (prefersReducedMotion || isDragged) return@LaunchedEffect
        while (true) {
            withFrameNanos { }
            rowState.scrollBy(1.15f)
        }
    }

    LazyRow(
        state = rowState,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("discover_hot_topics_marquee")
    ) {
        items(count = Int.MAX_VALUE) { idx ->
            val rankIndex = idx % rankedTopics.size
            val topic = rankedTopics[rankIndex]
            RankedHotTopicScrollCard(
                rank = rankIndex + 1,
                topic = topic,
                onClick = { onOpenTopic(topic.tag) }
            )
        }
    }
}

@Composable
private fun RankedHotTopicScrollCard(
    rank: Int,
    topic: HotTopic,
    onClick: () -> Unit
) {
    val rankBg = when (rank) {
        1 -> VermilionRed
        2 -> Color(0xFFEA580C)
        3 -> Color(0xFFD97706)
        else -> AuroraIndigo
    }
    val badgeChar = topic.heatStatus.trim().take(1)
    val badgeColor = when (badgeChar) {
        "沸" -> VermilionRed
        "热" -> Color(0xFFEA580C)
        "新" -> AuroraFuchsia
        "荐" -> AuroraIndigo
        else -> null
    }

    GradientBorderCard(
        modifier = Modifier
            .width(224.dp)
            .height(126.dp)
            .testTag("discover_ranked_topic_$rank"),
        outerCornerRadius = 16.dp,
        borderWidth = 1.6.dp,
        shadowStrength = 0.65f,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            FluidGrayHeroCanvas(
                modifier = Modifier.fillMaxSize(),
                darkVariant = false,
                seed = topic.id.hashCode(),
                showGrid = false
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
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
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(rankBg)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NO.%02d".format(rank),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White
                            )
                        }
                        if (badgeColor != null && badgeChar.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(badgeColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = badgeChar,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Text(
                        text = "${formatCompactCount(topic.discussionCount)} 热度",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = topic.tag,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = topic.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DiscoverCategoryTile(
    category: PostCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badgeBg = when (category.ordinal % 4) {
        0 -> com.example.ui.theme.AcidTintBg
        1 -> com.example.ui.theme.MintTintBg
        2 -> com.example.ui.theme.SlateTintBg
        else -> com.example.ui.theme.SoftRoseTint
    }
    val badgeTint = when (category.ordinal % 4) {
        0 -> com.example.ui.theme.InkBlack
        1 -> com.example.ui.theme.FluorescentGreen
        2 -> com.example.ui.theme.SlateAccent
        else -> com.example.ui.theme.VermilionRed
    }

    val isDarkTheme = MaterialTheme.colorScheme.background.red < 0.2f

    Surface(
        modifier = modifier.height(98.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (isDarkTheme) 0.dp else 1.2.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 0.8.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDarkTheme) 0.85f else 0.72f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.titleCn,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIconFor(category),
                        contentDescription = null,
                        tint = badgeTint,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Dedicated filtered feed view for any Discover category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFeedScreen(
    category: PostCategory,
    uiState: YangmeiUiState,
    onBack: () -> Unit,
    onComposeInCategory: (PostCategory) -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenTopic: (String) -> Unit,
    onLikePost: (String) -> Unit,
    onBookmarkPost: (String) -> Unit,
    onSharePost: (Post) -> Unit,
    onVotePoll: (String, String) -> Unit
) {
    var selectedSubFilter by remember { mutableStateOf("全部") }
    val subFilters = remember(category) {
        when (category) {
            PostCategory.TREE_HOLE -> listOf("全部") + MockSeedData.treeHoleMoods
            PostCategory.MARKETPLACE -> listOf("全部", "画材", "教材", "电子产品", "家具", "生活用品", "服饰")
            PostCategory.OPPORTUNITY -> listOf("全部", "兼职招募", "实习招聘", "项目合作", "展览协作", "模特招募")
            PostCategory.LOST_FOUND -> listOf("全部", "失物寻找", "拾物招领")
            PostCategory.MUTUAL_AID -> listOf("全部", "拼车买材料", "寻找搭子", "代取跑腿", "学习互助")
            else -> emptyList()
        }
    }

    val filteredPosts = remember(uiState.visiblePosts, category, selectedSubFilter) {
        uiState.visiblePosts.filter { post ->
            val matchCat = post.category == category
            val matchSub = selectedSubFilter == "全部" ||
                post.moodTag == selectedSubFilter ||
                post.title.contains(selectedSubFilter) ||
                post.body.contains(selectedSubFilter) ||
                post.topicTags.any { it.contains(selectedSubFilter) }
            matchCat && matchSub
        }
    }

    val listState = rememberLazyListState()
    var showCategoryFab by remember { mutableStateOf(true) }
    ObserveScrollFabVisibility(
        listState = listState,
        onFabVisibilityChange = { showCategoryFab = it }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraPageBackground(seed = category.ordinal + 5, headerWashHeight = 190.dp)

        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = category.titleCn,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("category_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 104.dp)
            ) {
            item(key = "cat_banner") {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        FluidGrayHeroCanvas(
                            modifier = Modifier.fillMaxSize(),
                            darkVariant = false,
                            seed = category.ordinal + 20
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = category.description,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (category == PostCategory.TREE_HOLE) "无需标题，安静倾诉"
                                    else "共 ${filteredPosts.size} 条内容",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            CategoryAbstractEmblem(category = category, size = 34.dp)
                        }
                    }
                    EditorialHairline()
                }
            }

            if (subFilters.isNotEmpty()) {
                item(key = "cat_subfilters") {
                    Column {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(vertical = 12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(subFilters) { filter ->
                                val selected = filter == selectedSubFilter
                                MonoTagChip(
                                    text = filter,
                                    inverted = selected,
                                    accentDot = if (selected && category == PostCategory.OPPORTUNITY) FluorescentGreen else null,
                                    onClick = { selectedSubFilter = filter }
                                )
                            }
                        }
                        EditorialHairline()
                    }
                }
            }

            if (filteredPosts.isEmpty()) {
                item(key = "cat_empty") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(44.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CategoryAbstractEmblem(category = category, size = 40.dp)
                        Text(
                            text = "暂无内容",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Button(
                            onClick = { onComposeInCategory(category) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("发布第一条内容")
                        }
                    }
                }
            } else {
                items(filteredPosts, key = { it.id }) { post ->
                    EditorialPostCard(
                        post = post,
                        onPostClick = { onOpenPost(post.id) },
                        onTopicClick = onOpenTopic,
                        onLikeClick = { onLikePost(post.id) },
                        onBookmarkClick = { onBookmarkPost(post.id) },
                        onShareClick = { onSharePost(post) },
                        onVoteOption = { optId -> onVotePoll(post.id, optId) }
                    )
                }
            }
        }
        }

        // Dedicated in-partition floating '+' FAB: publishes directly into THIS category
        AuroraFloatingPlusButton(
            visible = showCategoryFab,
            contentDescription = "在${category.titleCn}发布",
            testTag = "category_compose_btn",
            onClick = { onComposeInCategory(category) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 18.dp, bottom = 20.dp)
        )
    }
}
