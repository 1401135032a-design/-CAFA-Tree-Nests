package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.YangmeiUiState
import kotlinx.coroutines.launch

private enum class HomeFeedTab(val label: String, val icon: ImageVector) {
    RECOMMENDED("推荐", Icons.Outlined.AutoAwesome),
    LATEST("最新", Icons.Outlined.Schedule),
    RELATIONSHIP("恋爱情感", Icons.Outlined.FavoriteBorder),
    MARKETPLACE("闲置交换", Icons.Outlined.Storefront),
    OPPORTUNITY("校园机会", Icons.Outlined.WorkOutline),
    LOST_FOUND("寻物", Icons.Outlined.FindInPage),
    MUTUAL_AID("校园互助", Icons.Outlined.VolunteerActivism)
}

/**
 * Compact, vibrant, post-first Home feed with Material 3 pull-to-refresh,
 * smooth finger-following vertical scroll, and horizontally scrollable multi-tab header bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: YangmeiUiState,
    onOpenSearch: () -> Unit,
    onOpenPublish: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenTopic: (String) -> Unit,
    onLikePost: (String) -> Unit,
    onBookmarkPost: (String) -> Unit,
    onSharePost: (Post) -> Unit,
    onVotePoll: (String, String) -> Unit,
    onRefreshHome: () -> Unit = {},
    onFabVisibilityChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(HomeFeedTab.RECOMMENDED) }
    val listState = rememberLazyListState()
    val tabListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val seedPostIds = remember { MockSeedData.initialPosts.map { it.id }.toSet() }
    val feedPosts = remember(uiState.visiblePosts, selectedTab) {
        when (selectedTab) {
            HomeFeedTab.RECOMMENDED -> {
                val (livePosts, seedPosts) = uiState.visiblePosts.partition { it.id !in seedPostIds }
                livePosts + seedPosts.sortedByDescending { it.likeCount + it.comments.size * 3 }
            }
            HomeFeedTab.LATEST -> uiState.visiblePosts
            HomeFeedTab.RELATIONSHIP -> uiState.visiblePosts.filter { it.category == PostCategory.RELATIONSHIP }
            HomeFeedTab.MARKETPLACE -> uiState.visiblePosts.filter { it.category == PostCategory.MARKETPLACE }
            HomeFeedTab.OPPORTUNITY -> uiState.visiblePosts.filter { it.category == PostCategory.OPPORTUNITY }
            HomeFeedTab.LOST_FOUND -> uiState.visiblePosts.filter { it.category == PostCategory.LOST_FOUND }
            HomeFeedTab.MUTUAL_AID -> uiState.visiblePosts.filter { it.category == PostCategory.MUTUAL_AID }
        }
    }

    // Windowed pagination keeps composition memory bounded as community scales to 5,000+ users
    var loadedLimit by remember(selectedTab) { mutableIntStateOf(20) }
    val pagedPosts = remember(feedPosts, loadedLimit) { feedPosts.take(loadedLimit) }
    val shouldLoadMore by remember(listState, feedPosts.size, loadedLimit) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            loadedLimit < feedPosts.size && lastVisible >= (loadedLimit - 4)
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            loadedLimit += 20
        }
    }

    ObserveScrollFabVisibility(
        listState = listState,
        onFabVisibilityChange = onFabVisibilityChange
    )

    Box(modifier = modifier.fillMaxSize()) {
        AuroraPageBackground(seed = 1, headerWashHeight = 220.dp)

        Column(modifier = Modifier.fillMaxSize()) {
            // Header sits cleanly above the progressive-blur aurora mask
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 10.dp, top = 6.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isDarkTheme = MaterialTheme.colorScheme.background.red < 0.2f
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        YangmeiTransparentLogo(
                            size = 44.dp,
                            onDarkSurface = isDarkTheme
                        )
                        GradientFlowTitle(
                            text = "杨梅树窝",
                            fontSize = 28.sp,
                            onDarkSurface = isDarkTheme
                        )
                    }

                    Row(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                            .border(1.3.dp, AuroraBorderGradient, RoundedCornerShape(999.dp))
                            .clickable(onClick = onOpenSearch)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("home_search_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "AI 搜索",
                            tint = if (isDarkTheme) AuroraLightIndigo else AuroraFocusIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "AI 搜索",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Smooth, zero-jump horizontal pill bar (fixed border & typography metrics)
                val isDarkTheme = MaterialTheme.colorScheme.background.red < 0.2f
                LazyRow(
                    state = tabListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("home_tabs_row"),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(
                        items = HomeFeedTab.entries,
                        key = { _, tab -> tab.name }
                    ) { index, tab ->
                        val isSelected = selectedTab == tab
                        val pillShape = RoundedCornerShape(10.dp)
                        Row(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(pillShape)
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .background(YangmeiPrimaryGradient)
                                            .border(1.2.dp, Color.Transparent, pillShape)
                                    } else {
                                        Modifier
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
                                            .border(1.2.dp, AuroraBorderGradient, pillShape)
                                    }
                                )
                                .clickable {
                                    selectedTab = tab
                                    scope.launch {
                                        val layoutInfo = tabListState.layoutInfo
                                        val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                                        val isClipped = itemInfo == null ||
                                            itemInfo.offset < layoutInfo.viewportStartOffset + 12 ||
                                            (itemInfo.offset + itemInfo.size) > layoutInfo.viewportEndOffset - 12
                                        if (isClipped) {
                                            val targetIdx = (index - 1).coerceAtLeast(0)
                                            tabListState.animateScrollToItem(targetIdx)
                                        }
                                    }
                                }
                                .padding(horizontal = 12.dp)
                                .testTag("home_tab_${tab.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (isSelected) PaperWhite else if (isDarkTheme) AuroraLightIndigo else AuroraFocusIndigo,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) PaperWhite else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Pull-to-refresh container with smooth finger-following LazyColumn
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefreshHome,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("home_pull_refresh_box")
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("home_feed_list"),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 112.dp)
                ) {
                    if (pagedPosts.isEmpty()) {
                        item(key = "home_empty_tab") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 72.dp, horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "当前分区暂时还没有内容",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "下拉可刷新最新云端帖子，或点击右下角 + 号发布第一条内容",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(
                            items = pagedPosts,
                            key = { it.id },
                            contentType = { it.layoutType }
                        ) { post ->
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
        }
    }
}

/**
 * Reusable futuristic Topic Poster card wrapped in unified `.gb-card` gradient border.
 */
@Composable
fun EditorialTopicPosterCard(
    topic: HotTopic,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fg = MaterialTheme.colorScheme.onSurface
    val sub = MaterialTheme.colorScheme.onSurfaceVariant

    GradientBorderCard(
        modifier = modifier.testTag("topic_poster_${topic.id}"),
        outerCornerRadius = 16.dp,
        borderWidth = 1.8.dp,
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = topic.tag,
                    style = MaterialTheme.typography.titleLarge,
                    color = fg,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = topic.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = sub,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${topic.postCount} 篇帖子 · ${topic.discussionCount} 讨论",
                    style = MaterialTheme.typography.bodySmall,
                    color = sub
                )
            }
        }
    }
}
