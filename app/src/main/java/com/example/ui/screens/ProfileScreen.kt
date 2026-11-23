package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.YangmeiUiState

enum class ProfileCollectionType(val titleCn: String) {
    MY_POSTS("我的帖子"),
    MY_COMMENTS("我的评论"),
    MY_BOOKMARKS("我的收藏"),
    MY_DRAFTS("我的草稿"),
    MY_TRADES("我的交易"),
    MY_HELP("我的求助")
}

/**
 * Clean user center ("我的") — the dedicated home for CAFA identity verification,
 * anonymous persona management, user content collections, and settings.
 */
@Composable
fun ProfileScreen(
    uiState: YangmeiUiState,
    onOpenVerification: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCollection: (ProfileCollectionType) -> Unit,
    onRandomizeIdentity: () -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onFabVisibilityChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDarkTheme = uiState.settings.darkTheme
    val listState = rememberLazyListState()
    val myPostsCount = remember(uiState.posts) { uiState.posts.count { it.isMine } }
    val myCommentsCount = remember(uiState.posts, uiState.currentPersona) {
        uiState.posts.sumOf { post -> post.comments.count { it.author.id == uiState.currentPersona.id } }
    }
    val myBookmarksCount = remember(uiState.posts, uiState.courses, uiState.experiences) {
        uiState.posts.count { it.isBookmarked } +
            uiState.courses.count { it.isBookmarked } +
            uiState.experiences.count { it.isBookmarked }
    }
    val myDraftsCount = uiState.drafts.size
    val myTradesCount = remember(uiState.posts) {
        uiState.posts.count { it.isMine && it.category == PostCategory.MARKETPLACE }
    }
    val myHelpCount = remember(uiState.posts) {
        uiState.posts.count {
            it.isMine && (it.category == PostCategory.LOST_FOUND || it.category == PostCategory.MUTUAL_AID)
        }
    }

    ObserveScrollFabVisibility(
        listState = listState,
        onFabVisibilityChange = onFabVisibilityChange
    )

    Box(modifier = modifier.fillMaxSize()) {
        AuroraPageBackground(seed = 4, headerWashHeight = 260.dp)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("profile_screen_list"),
            contentPadding = PaddingValues(bottom = 124.dp)
        ) {
            // 1. Pure Anonymous Identity Header above the Progressive Blur mask
            item(key = "profile_identity_hero") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProgressiveBlurTitle(text = "我的", fontSize = 26.sp)
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("profile_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "设置",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AnonymousAvatar(seed = uiState.currentPersona.avatarSeed, size = 64.dp)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = uiState.currentPersona.nickname,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VerifiedIdentityBadge(
                                    identityType = uiState.currentPersona.identityType,
                                    isVerified = uiState.isVerified
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "社区内仅展示匿名形象",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "换一套形象",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkTheme) AuroraLightIndigo else AuroraFocusIndigo,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
                                .border(1.3.dp, AuroraBorderGradient, RoundedCornerShape(10.dp))
                                .clickable(onClick = onRandomizeIdentity)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("randomize_persona_btn")
                        )
                    }
                }
            }

            // 2. Campus Verification Card (sole dedicated verification hub in "我的")
            item(key = "profile_verification_card") {
                val statusColor = when (uiState.verificationStatus) {
                    VerificationStatus.VERIFIED -> FluorescentGreen
                    VerificationStatus.REVIEWING -> AcidYellow
                    VerificationStatus.REJECTED -> VermilionRed
                    VerificationStatus.UNVERIFIED -> FluidSilver
                }
                GradientBorderCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("profile_verification_entry"),
                    outerCornerRadius = 16.dp,
                    borderWidth = 2.dp,
                    onClick = onOpenVerification
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        FluidGrayHeroCanvas(
                            modifier = Modifier.matchParentSize(),
                            darkVariant = true,
                            seed = 99
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                    Text(
                                        text = "校园身份认证 · ${uiState.verificationStatus.labelCn}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PaperWhite
                                    )
                                }
                                Text(
                                    text = when (uiState.verificationStatus) {
                                        VerificationStatus.UNVERIFIED -> "认证中央美术学院在读或校友身份，开启发帖、评论与点赞"
                                        VerificationStatus.REVIEWING -> "认证材料审核中，点击查看状态"
                                        VerificationStatus.VERIFIED -> "已通过校园核验，实名资料严格隔离不公开"
                                        VerificationStatus.REJECTED -> "认证未通过，点击重新提交材料"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WarmGrayWhite.copy(alpha = 0.74f)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "进入认证",
                                tint = PaperWhite
                            )
                        }
                    }
                }
            }

            // 3. Stats Summary Strip (.gb-card unified style)
            item(key = "profile_stats_row") {
                GradientBorderCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    outerCornerRadius = 16.dp,
                    borderWidth = 1.8.dp,
                    shadowStrength = 0.85f
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatColumn("帖子", myPostsCount) { onOpenCollection(ProfileCollectionType.MY_POSTS) }
                        ProfileStatColumn("收藏", myBookmarksCount) { onOpenCollection(ProfileCollectionType.MY_BOOKMARKS) }
                        ProfileStatColumn("草稿", myDraftsCount) { onOpenCollection(ProfileCollectionType.MY_DRAFTS) }
                        ProfileStatColumn("评论", myCommentsCount) { onOpenCollection(ProfileCollectionType.MY_COMMENTS) }
                    }
                }
            }

            // 4. Management Rows (.gb-card unified style) + Direct Dark Mode Switch
            item(key = "profile_entries") {
                GradientBorderCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    outerCornerRadius = 16.dp,
                    borderWidth = 1.8.dp,
                    shadowStrength = 0.85f
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Direct Dark Mode Toggle matching the opening aurora backdrop (#07070C)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 58.dp)
                                .clickable { onToggleDarkTheme(!isDarkTheme) }
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                                .testTag("profile_dark_mode_toggle"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                                    contentDescription = null,
                                    tint = if (isDarkTheme) AuroraLightIndigo else AuroraFocusIndigo,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text(
                                        text = "深色模式",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isDarkTheme) "已开启 · 开屏同款极光夜幕" else "切换为开屏同款深邃极光背景",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = onToggleDarkTheme,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PaperWhite,
                                    checkedTrackColor = AuroraIndigo,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                        EditorialHairline(thickness = 0.6.dp)

                        ProfileMenuRow("我的帖子", "$myPostsCount") { onOpenCollection(ProfileCollectionType.MY_POSTS) }
                        ProfileMenuRow("我的评论", "$myCommentsCount") { onOpenCollection(ProfileCollectionType.MY_COMMENTS) }
                        ProfileMenuRow("我的收藏", "$myBookmarksCount") { onOpenCollection(ProfileCollectionType.MY_BOOKMARKS) }
                        ProfileMenuRow("我的草稿", "$myDraftsCount") { onOpenCollection(ProfileCollectionType.MY_DRAFTS) }
                        ProfileMenuRow("我的交易", "$myTradesCount") { onOpenCollection(ProfileCollectionType.MY_TRADES) }
                        ProfileMenuRow("我的求助", "$myHelpCount") { onOpenCollection(ProfileCollectionType.MY_HELP) }
                        ProfileMenuRow("认证信息", uiState.verificationStatus.labelCn) { onOpenVerification() }
                        ProfileMenuRow("设置", "", showDivider = false) { onOpenSettings() }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatColumn(label: String, count: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileMenuRow(
    title: String,
    trailingText: String,
    showDivider: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("profile_menu_$title")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (trailingText.isNotEmpty()) {
                    Text(
                        text = trailingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        if (showDivider) EditorialHairline(thickness = 0.6.dp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCollectionScreen(
    collectionType: ProfileCollectionType,
    uiState: YangmeiUiState,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenDraft: (Post) -> Unit,
    onDeleteDraft: (String) -> Unit,
    onOpenTopic: (String) -> Unit,
    onLikePost: (String) -> Unit,
    onBookmarkPost: (String) -> Unit,
    onSharePost: (Post) -> Unit,
    onVotePoll: (String, String) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(collectionType.titleCn, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(innerPadding),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
        ) {
            when (collectionType) {
                ProfileCollectionType.MY_DRAFTS -> {
                    if (uiState.drafts.isEmpty()) {
                        item { EmptyCollectionNotice("暂无草稿", "发布页面支持随时暂存未写完的想法。") }
                    } else {
                        items(uiState.drafts, key = { it.id }) { draft ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { onOpenDraft(draft) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        MonoTagChip(text = "${draft.category.titleCn} · 草稿")
                                        IconButton(onClick = { onDeleteDraft(draft.id) }) {
                                            Icon(Icons.Outlined.Delete, contentDescription = "删除草稿", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Text(
                                        text = draft.title.ifBlank { "无标题草稿" },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = draft.body,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${draft.createdAt} · 点击继续编辑",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                                EditorialHairline()
                            }
                        }
                    }
                }
                ProfileCollectionType.MY_COMMENTS -> {
                    val myComments = uiState.posts.flatMap { post ->
                        post.comments.filter { it.author.id == uiState.currentPersona.id }.map { post to it }
                    }
                    if (myComments.isEmpty()) {
                        item { EmptyCollectionNotice("暂无评论记录", "完成认证后即可在帖子或树洞下参与交流。") }
                    } else {
                        items(myComments, key = { it.second.id }) { (post, comment) ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { onOpenPost(post.id) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "回复于「${post.title.ifBlank { "树洞内容" }}」",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = comment.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = comment.createdAt,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                                EditorialHairline()
                            }
                        }
                    }
                }
                else -> {
                    val itemsList = when (collectionType) {
                        ProfileCollectionType.MY_POSTS -> uiState.posts.filter { it.isMine }
                        ProfileCollectionType.MY_BOOKMARKS -> uiState.posts.filter { it.isBookmarked }
                        ProfileCollectionType.MY_TRADES -> uiState.posts.filter { it.isMine && it.category == PostCategory.MARKETPLACE }
                        ProfileCollectionType.MY_HELP -> uiState.posts.filter {
                            it.isMine && (it.category == PostCategory.LOST_FOUND || it.category == PostCategory.MUTUAL_AID)
                        }
                        else -> emptyList()
                    }
                    if (itemsList.isEmpty()) {
                        item {
                            EmptyCollectionNotice(
                                title = "这里暂时空着",
                                subtitle = "当你发布或收藏内容后，记录会汇集在这里。"
                            )
                        }
                    } else {
                        items(itemsList, key = { it.id }) { post ->
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

@Composable
private fun EmptyCollectionNotice(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 72.dp, horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
