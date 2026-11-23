package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ExperienceArticle
import com.example.data.ExperienceCategory
import com.example.data.MockSeedData
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.YangmeiUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SeniorExperienceScreen(
    uiState: YangmeiUiState,
    initialArticleId: String? = null,
    onBack: () -> Unit,
    onToggleLike: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onAddComment: (articleId: String, content: String, cohortLabel: String) -> Unit,
    onCreateArticle: (category: ExperienceCategory, cohort: String, content: String, onSuccess: () -> Unit) -> Unit,
    onRequestVerifiedToCompose: (onVerified: () -> Unit) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ExperienceCategory.ALL) }
    var activeArticleId by remember(initialArticleId) { mutableStateOf(initialArticleId) }
    var showCreateForm by remember { mutableStateOf(false) }

    val filteredArticles = remember(uiState.experiences, selectedCategory) {
        if (selectedCategory == ExperienceCategory.ALL) uiState.experiences
        else uiState.experiences.filter { it.category == selectedCategory }
    }

    val activeArticle = remember(uiState.experiences, activeArticleId) {
        uiState.experiences.firstOrNull { it.id == activeArticleId }
    }

    if (showCreateForm) {
        BackHandler { showCreateForm = false }
        CreateSeniorExperienceView(
            onClose = { showCreateForm = false },
            onSubmit = { category, cohort, content ->
                onCreateArticle(category, cohort, content) {
                    showCreateForm = false
                }
            }
        )
        return
    }

    if (activeArticle != null) {
        BackHandler {
            if (initialArticleId != null) onBack() else activeArticleId = null
        }
        ExperienceDetailView(
            article = activeArticle,
            isVerified = uiState.isVerified,
            onBack = {
                if (initialArticleId != null) onBack() else activeArticleId = null
            },
            onToggleLike = { onToggleLike(activeArticle.id) },
            onToggleBookmark = { onToggleBookmark(activeArticle.id) },
            onAddComment = { text, cohort -> onAddComment(activeArticle.id, text, cohort) }
        )
        return
    }

    val listState = rememberLazyListState()
    var fabVisible by remember { mutableStateOf(true) }
    ObserveScrollFabVisibility(listState = listState, onFabVisibilityChange = { fabVisible = it })

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text("学长学姐说", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("experience_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("senior_experience_list"),
                contentPadding = PaddingValues(bottom = 104.dp)
            ) {
                item(key = "exp_hero") {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(112.dp)
                        ) {
                            FluidGrayHeroCanvas(
                                modifier = Modifier.fillMaxSize(),
                                darkVariant = false,
                                seed = 33
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "把走过的路，留给后来的人。",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "考研、留学、毕设与工作室真实问答 · 回答需标注哪一届",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        EditorialHairline()
                    }
                }

                item(key = "exp_filters") {
                    Column {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(vertical = 12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(ExperienceCategory.entries) { cat ->
                                MonoTagChip(
                                    text = cat.labelCn,
                                    inverted = selectedCategory == cat,
                                    onClick = { selectedCategory = cat }
                                )
                            }
                        }
                        EditorialHairline()
                    }
                }

                items(filteredArticles, key = { it.id }) { article ->
                    ExperienceEditorialRow(
                        article = article,
                        onClick = { activeArticleId = article.id },
                        onToggleBookmark = { onToggleBookmark(article.id) }
                    )
                }
            }
        }

        // Dedicated '+' FAB to post directly inside 学长学姐说
        AuroraFloatingPlusButton(
            visible = fabVisible,
            contentDescription = "在学长学姐说发帖",
            testTag = "senior_experience_fab",
            onClick = {
                onRequestVerifiedToCompose {
                    showCreateForm = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 18.dp, bottom = 22.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CreateSeniorExperienceView(
    onClose: () -> Unit,
    onSubmit: (category: ExperienceCategory, cohort: String, content: String) -> Unit
) {
    val selectableCategories = remember {
        ExperienceCategory.entries.filter { it != ExperienceCategory.ALL }
    }
    var selectedCategory by remember { mutableStateOf(selectableCategories.first()) }
    var selectedCohort by remember { mutableStateOf(MockSeedData.seniorCohortOptions[2]) } // default 2024届
    var content by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("在学长学姐说发帖", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onClose, modifier = Modifier.testTag("exp_compose_close")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "exp_compose_card") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("你是哪一届的（必填）", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MockSeedData.seniorCohortOptions.forEach { cohort ->
                                    MonoTagChip(
                                        text = cohort,
                                        inverted = selectedCohort == cohort,
                                        onClick = { selectedCohort = cohort }
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("主题分类", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectableCategories.forEach { cat ->
                                    MonoTagChip(
                                        text = cat.labelCn,
                                        inverted = selectedCategory == cat,
                                        onClick = { selectedCategory = cat }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("直接写下你的经验分享或向学长学姐提问...") },
                            minLines = 6,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("exp_compose_body")
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(YangmeiPrimaryGradient)
                                .clickable { onSubmit(selectedCategory, selectedCohort, content) }
                                .testTag("exp_compose_submit"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "以「$selectedCohort」身份发布",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PaperWhite
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExperienceEditorialRow(
    article: ExperienceArticle,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .testTag("exp_card_${article.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExperienceCategoryBadge(category = article.category)
                    CohortNeutralPill(cohort = article.year)
                }
                IconButton(onClick = onToggleBookmark, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (article.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "收藏经验",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = article.body.ifBlank { article.summary },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    article.tags.take(3).forEach { tag ->
                        Text("#$tag", style = MaterialTheme.typography.labelSmall, color = SlateAccent)
                    }
                }
                Text(
                    text = "${article.comments.size} 回答 · ${article.bookmarkCount} 收藏",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
        EditorialHairline(thickness = 0.8.dp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExperienceDetailView(
    article: ExperienceArticle,
    isVerified: Boolean,
    onBack: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleBookmark: () -> Unit,
    onAddComment: (content: String, cohortLabel: String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    var selectedAnswerCohort by remember { mutableStateOf(MockSeedData.seniorCohortOptions[2]) } // default 2024届

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("学长学姐说 · ${article.year}", style = MaterialTheme.typography.titleMedium)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (article.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "收藏"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Column(modifier = Modifier.navigationBarsPadding().imePadding()) {
                    EditorialHairline()
                    // Mandatory cohort selector for responders ("回答的人要写明是哪一届的")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "回答届别：",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(MockSeedData.seniorCohortOptions) { cohort ->
                                MonoTagChip(
                                    text = cohort,
                                    inverted = selectedAnswerCohort == cohort,
                                    onClick = { selectedAnswerCohort = cohort }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = {
                                Text(
                                    if (isVerified) "以「$selectedAnswerCohort」身份回答或补充..."
                                    else "认证央美身份后即可标注届别回答..."
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("exp_comment_input")
                        )
                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank() || !isVerified) {
                                    onAddComment(commentText.ifBlank { "感谢分享！" }, selectedAnswerCohort)
                                    if (isVerified) commentText = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(YangmeiPrimaryGradient)
                                .testTag("exp_comment_send")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "发送回答", tint = PaperWhite)
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
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item(key = "exp_article_body") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExperienceCategoryBadge(category = article.category)
                        CohortNeutralPill(cohort = article.year)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnonymousAvatar(seed = article.author.avatarSeed, size = 34.dp)
                        Column {
                            Text(article.author.nickname, style = MaterialTheme.typography.titleSmall)
                            Text("匿名校友分享 · ${article.year}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                    article.media?.let { media ->
                        PostMediaImage(
                            media = media,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(media.aspectRatio.coerceIn(0.75f, 1.85f))
                        )
                    }
                    Text(article.body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onToggleLike, shape = RoundedCornerShape(10.dp)) {
                            Icon(
                                imageVector = if (article.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "赞同",
                                tint = if (article.isLiked) VermilionRed else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${article.likeCount}")
                        }
                        OutlinedButton(onClick = onToggleBookmark, shape = RoundedCornerShape(10.dp)) {
                            Icon(
                                imageVector = if (article.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "收藏",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${article.bookmarkCount}")
                        }
                    }
                    EditorialHairline()
                }
            }

            item(key = "exp_comments_head") {
                Text(
                    text = "学长学姐回答 (${article.comments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
            }

            items(article.comments, key = { it.id }) { c ->
                val isOp = c.author.id == article.author.id || c.author.nickname == article.author.nickname
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnonymousAvatar(seed = c.author.avatarSeed, size = 30.dp)
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(c.author.nickname, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                if (isOp) {
                                    Text(
                                        text = "楼主",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PaperWhite,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AuroraFuchsia)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                CohortNeutralPill(cohort = c.cohortLabel ?: "往届学长学姐")
                                Text(c.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outlineVariant)
                            }
                            Text(c.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    EditorialHairline(thickness = 0.6.dp)
                }
            }
        }
    }
}

@Composable
private fun ExperienceCategoryBadge(category: ExperienceCategory) {
    val gradient = when (category) {
        ExperienceCategory.STUDY_ABROAD -> Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF0891B2)))
        ExperienceCategory.GRADUATION_WORK -> Brush.horizontalGradient(listOf(Color(0xFFC026D3), Color(0xFF6366F1)))
        ExperienceCategory.GRAD_EXAM -> Brush.horizontalGradient(listOf(Color(0xFF6D28D9), Color(0xFF4338CA)))
        ExperienceCategory.EMPLOYMENT -> Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF0D9488)))
        ExperienceCategory.INTERNSHIP -> Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFEA580C)))
        ExperienceCategory.STUDIO -> Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFFDB2777)))
        ExperienceCategory.MAJOR_STUDY -> Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF4F46E5)))
        ExperienceCategory.ART_PRACTICE -> Brush.horizontalGradient(listOf(Color(0xFF9333EA), Color(0xFFEC4899)))
        ExperienceCategory.ALL -> YangmeiPrimaryGradient
    }
    Text(
        text = category.labelCn,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = PaperWhite,
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(gradient)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}

@Composable
private fun CohortNeutralPill(cohort: String) {
    Text(
        text = cohort,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 3.5.dp)
    )
}
