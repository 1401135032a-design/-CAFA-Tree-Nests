package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HotTopic
import com.example.data.Post
import com.example.ui.components.AiCampusStatsCard
import com.example.ui.components.AuroraPageBackground
import com.example.ui.components.EditorialHairline
import com.example.ui.components.EditorialPostCard
import com.example.ui.components.FluidGrayHeroCanvas
import com.example.ui.components.formatCompactCount
import com.example.ui.theme.*
import com.example.viewmodel.YangmeiUiState

/**
 * Ultra-compact Weibo-Hot-Search-style ranking board ("央美热搜榜").
 * Single-line high-density rows: rank number + concise title + heat count + micro tag (沸/热/新/荐).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotTopicsScreen(
    uiState: YangmeiUiState,
    onBack: () -> Unit,
    onOpenTopic: (String) -> Unit,
    onRequestAiStats: (String?) -> Unit = {}
) {
    val rankedTopics = remember(uiState.hotTopics) {
        uiState.hotTopics.sortedByDescending { it.discussionCount }
    }
    val totalTagCount = remember(uiState.visiblePosts) {
        uiState.visiblePosts.flatMap { it.topicTags }.distinct().size
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraPageBackground(seed = 8, headerWashHeight = 175.dp)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
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
                                text = "央美热搜榜",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("topics_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        Text(
                            text = "实时更新",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("hot_topics_list"),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "hot_topics_ai_stats") {
                    AiCampusStatsCard(
                        query = "",
                        aiStatsState = uiState.aiStatsState,
                        matchedCount = uiState.visiblePosts.size,
                        totalTagCount = totalTagCount,
                        onRunAnalysis = { onRequestAiStats(null) },
                        onSelectTag = { picked -> onOpenTopic(if (picked.startsWith("#")) picked else "#$picked") }
                    )
                }

                item(key = "hot_rank_board_card") {
                    HotSearchBoardCard(
                        rankedTopics = rankedTopics,
                        onOpenTopic = onOpenTopic
                    )
                }
            }
        }
    }
}

@Composable
internal fun HotSearchBoardCard(
    rankedTopics: List<HotTopic>,
    onOpenTopic: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.red < 0.2f
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (isDarkTheme) 0.dp else 1.5.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 0.85.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDarkTheme) 0.9f else 0.72f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            rankedTopics.forEachIndexed { idx, topic ->
                HotSearchRankRow(
                    rank = idx + 1,
                    topic = topic,
                    onClick = { onOpenTopic(topic.tag) }
                )
                if (idx < rankedTopics.lastIndex) {
                    EditorialHairline(
                        thickness = 0.55.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun HotSearchRankRow(
    rank: Int,
    topic: HotTopic,
    onClick: () -> Unit
) {
    val rankColor = when (rank) {
        1 -> VermilionRed
        2 -> Color(0xFFEA580C)
        3 -> Color(0xFFD97706)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val badgeChar = topic.heatStatus.trim().take(1)
    val badgeColor = when (badgeChar) {
        "沸" -> VermilionRed
        "热" -> Color(0xFFEA580C)
        "新" -> AuroraFuchsia
        "荐" -> AuroraIndigo
        else -> null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp)
            .testTag("hot_rank_row_$rank"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Rank number (tabular compact width)
        Box(
            modifier = Modifier.width(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = if (rank <= 3) 16.sp else 14.sp,
                    fontWeight = if (rank <= 3) FontWeight.ExtraBold else FontWeight.SemiBold
                ),
                color = rankColor
            )
        }

        // Concise single-line topic title + inline heat count
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = topic.tag.removePrefix("#"),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = if (rank <= 3) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Text(
                text = formatCompactCount(topic.discussionCount),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                color = MaterialTheme.colorScheme.outlineVariant,
                maxLines = 1
            )
        }

        // Compact 1-character Weibo-style status badge
        if (badgeColor != null && badgeChar.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeChar,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topicTag: String,
    uiState: YangmeiUiState,
    onBack: () -> Unit,
    onComposeWithTopic: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenTopic: (String) -> Unit,
    onLikePost: (String) -> Unit,
    onBookmarkPost: (String) -> Unit,
    onSharePost: (Post) -> Unit,
    onVotePoll: (String, String) -> Unit
) {
    val topic: HotTopic? = remember(uiState.hotTopics, topicTag) {
        uiState.hotTopics.firstOrNull { it.tag.equals(topicTag, ignoreCase = true) }
    }
    val matchingPosts = remember(uiState.visiblePosts, topicTag) {
        uiState.visiblePosts.filter { post ->
            post.topicTags.any { it.equals(topicTag, ignoreCase = true) } ||
                post.title.contains(topicTag.removePrefix("#"), true) ||
                post.body.contains(topicTag.removePrefix("#"), true)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(topicTag, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onComposeWithTopic) {
                        Icon(Icons.Outlined.Edit, contentDescription = "参与此话题发布")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            item(key = "topic_poster_hero") {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(152.dp)
                    ) {
                        FluidGrayHeroCanvas(
                            modifier = Modifier.fillMaxSize(),
                            darkVariant = false,
                            seed = topicTag.hashCode()
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${matchingPosts.size} 篇内容",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = topicTag,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = topic?.description ?: "关于「$topicTag」的校园匿名讨论。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    EditorialHairline()
                }
            }

            if (matchingPosts.isEmpty()) {
                item(key = "topic_empty") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("该话题下暂时还没有更多内容", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = onComposeWithTopic, shape = RoundedCornerShape(10.dp)) {
                            Text("匿名参与 $topicTag")
                        }
                    }
                }
            } else {
                items(matchingPosts, key = { it.id }) { post ->
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
