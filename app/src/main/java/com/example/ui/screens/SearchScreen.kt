package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.components.AiCampusStatsCard
import com.example.ui.components.AuroraPageBackground
import com.example.ui.components.EditorialHairline
import com.example.ui.components.EditorialPostCard
import com.example.ui.components.MonoTagChip
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.VermilionRed
import com.example.ui.theme.YangmeiPrimaryGradient
import com.example.viewmodel.YangmeiUiState

private enum class SearchScope(val label: String) {
    ALL("全部"),
    POSTS("帖子"),
    COURSES("选课评价"),
    EXPERIENCES("学长学姐说")
}

/**
 * Search screen aligned identically with the Weibo-Hot-Search-style ranking board (HotTopicsScreen).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: YangmeiUiState,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenTopic: (String) -> Unit,
    onOpenCourseArchive: () -> Unit,
    onOpenExperience: (String) -> Unit,
    onLikePost: (String) -> Unit,
    onBookmarkPost: (String) -> Unit,
    onSharePost: (Post) -> Unit,
    onVotePoll: (String, String) -> Unit,
    onRequestAiStats: (String?) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var scope by remember { mutableStateOf(SearchScope.ALL) }
    val rankedTopics = remember(uiState.hotTopics) {
        uiState.hotTopics.sortedByDescending { it.discussionCount }
    }
    val liveTagStats = remember(uiState.visiblePosts) {
        uiState.visiblePosts
            .flatMap { it.topicTags }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(12)
    }

    val normalizedQuery = remember(query) { query.trim().removePrefix("#") }

    val matchedPosts = remember(uiState.visiblePosts, normalizedQuery) {
        if (normalizedQuery.isBlank()) emptyList()
        else uiState.visiblePosts.filter {
            it.title.contains(normalizedQuery, true) ||
                it.body.contains(normalizedQuery, true) ||
                it.category.titleCn.contains(normalizedQuery, true) ||
                it.author.nickname.contains(normalizedQuery, true) ||
                (it.location?.contains(normalizedQuery, true) == true) ||
                it.topicTags.any { t -> t.removePrefix("#").contains(normalizedQuery, true) }
        }
    }
    val matchedCourses = remember(uiState.courses, normalizedQuery) {
        if (normalizedQuery.isBlank()) emptyList()
        else uiState.courses.filter {
            it.name.contains(normalizedQuery, true) ||
                it.instructor.contains(normalizedQuery, true) ||
                it.college.contains(normalizedQuery, true) ||
                it.summary.contains(normalizedQuery, true)
        }
    }
    val matchedExperiences = remember(uiState.experiences, normalizedQuery) {
        if (normalizedQuery.isBlank()) emptyList()
        else uiState.experiences.filter {
            it.title.contains(normalizedQuery, true) ||
                it.summary.contains(normalizedQuery, true) ||
                it.body.contains(normalizedQuery, true) ||
                it.tags.any { t -> t.removePrefix("#").contains(normalizedQuery, true) } ||
                it.college.contains(normalizedQuery, true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraPageBackground(seed = 8, headerWashHeight = 175.dp)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    val keyboardController = LocalSoftwareKeyboardController.current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp, end = 12.dp, top = 10.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("search_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("AI 搜索帖子、选课、经验或提问...") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { query = "" }) {
                                        Icon(
                                            Icons.Outlined.Close,
                                            contentDescription = "清空搜索",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                    onRequestAiStats(normalizedQuery.ifBlank { null })
                                }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_input_field")
                        )
                        Row(
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(YangmeiPrimaryGradient)
                                .clickable {
                                    keyboardController?.hide()
                                    onRequestAiStats(normalizedQuery.ifBlank { null })
                                }
                                .padding(horizontal = 12.dp)
                                .testTag("search_ai_submit_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = "AI 搜索",
                                tint = PaperWhite,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "AI搜索",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = PaperWhite
                            )
                        }
                    }

                    if (query.isNotBlank()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(SearchScope.entries) { s ->
                                MonoTagChip(text = s.label, inverted = scope == s, onClick = { scope = s })
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("search_content_list"),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (normalizedQuery.isBlank()) {
                    // Live aggregated campus & custom tag chips (with post counts)
                    item(key = "trending_queries") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(liveTagStats, key = { it.key }) { (tag, count) ->
                                MonoTagChip(
                                    text = "$tag · $count",
                                    onClick = { query = tag.removePrefix("#") }
                                )
                            }
                        }
                    }

                    // Quick one-tap AI Search question prompts
                    item(key = "ai_quick_prompts") {
                        val quickPrompts = remember {
                            listOf("哪些选修课给分高", "水课推荐", "望京食堂吃什么", "留学作品集经验", "闲置画材")
                        }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(quickPrompts) { prompt ->
                                MonoTagChip(
                                    text = "✦ $prompt",
                                    onClick = {
                                        query = prompt
                                        onRequestAiStats(prompt)
                                    }
                                )
                            }
                        }
                    }

                    // Gemini AI Search & campus tag statistics card
                    item(key = "search_ai_stats_card") {
                        AiCampusStatsCard(
                            query = "",
                            aiStatsState = uiState.aiStatsState,
                            matchedCount = uiState.visiblePosts.size,
                            totalTagCount = liveTagStats.size,
                            onRunAnalysis = { onRequestAiStats(null) },
                            onSelectTag = { picked ->
                                query = picked
                                onRequestAiStats(picked)
                            }
                        )
                    }

                    // Identical header + Weibo-Hot-Search-style ranking board as HotTopicsScreen
                    item(key = "hot_search_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
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
                                    text = "央美热搜榜",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "实时更新",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item(key = "search_hot_rank_board_card") {
                        HotSearchBoardCard(
                            rankedTopics = rankedTopics,
                            onOpenTopic = onOpenTopic
                        )
                    }
                } else {
                    // Keyword-specific Gemini AI Search & Statistics card
                    item(key = "search_keyword_ai_stats") {
                        AiCampusStatsCard(
                            query = normalizedQuery,
                            aiStatsState = uiState.aiStatsState,
                            matchedCount = matchedPosts.size + matchedCourses.size + matchedExperiences.size,
                            totalTagCount = liveTagStats.size,
                            onRunAnalysis = { onRequestAiStats(normalizedQuery) },
                            onSelectTag = { picked ->
                                query = picked
                                onRequestAiStats(picked)
                            }
                        )
                    }

                    val noResults = matchedPosts.isEmpty() && matchedCourses.isEmpty() && matchedExperiences.isEmpty()
                    if (noResults) {
                        item(key = "no_results") {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 56.dp, horizontal = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("未找到与「$query」相关的内容", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "可以换一个学院名、课程关键词或话题标签试试。",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if ((scope == SearchScope.ALL || scope == SearchScope.COURSES) && matchedCourses.isNotEmpty()) {
                        item(key = "sec_courses") {
                            Text(
                                text = "选课评价 (${matchedCourses.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }
                        item(key = "sec_courses_card") {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    matchedCourses.forEachIndexed { idx, course ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(onClick = onOpenCourseArchive)
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                "${course.campus} · ${course.college} · ${course.courseType}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outlineVariant
                                            )
                                            Text(course.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                            Text(
                                                "${course.instructor} · 推荐 ${String.format("%.1f", course.computedRecommend)} (${course.reviews.size}人已评)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (idx < matchedCourses.lastIndex) {
                                            EditorialHairline(thickness = 0.6.dp, modifier = Modifier.padding(horizontal = 14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if ((scope == SearchScope.ALL || scope == SearchScope.EXPERIENCES) && matchedExperiences.isNotEmpty()) {
                        item(key = "sec_exp") {
                            Text(
                                text = "学长学姐说 (${matchedExperiences.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }
                        item(key = "sec_exp_card") {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    matchedExperiences.forEachIndexed { idx, article ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onOpenExperience(article.id) }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                "${article.college} · ${article.year}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outlineVariant
                                            )
                                            Text(article.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                            Text(article.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (idx < matchedExperiences.lastIndex) {
                                            EditorialHairline(thickness = 0.6.dp, modifier = Modifier.padding(horizontal = 14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if ((scope == SearchScope.ALL || scope == SearchScope.POSTS) && matchedPosts.isNotEmpty()) {
                        item(key = "sec_posts") {
                            Text(
                                text = "社区帖子 (${matchedPosts.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }
                        items(matchedPosts, key = { "sp_${it.id}" }) { post ->
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
