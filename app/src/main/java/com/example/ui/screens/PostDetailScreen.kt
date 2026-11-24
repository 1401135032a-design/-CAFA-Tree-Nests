package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    post: Post?,
    isVerified: Boolean,
    dmMessages: List<WechatDmMessage> = emptyList(),
    onBack: () -> Unit,
    onTopicClick: (String) -> Unit,
    onLikePost: () -> Unit,
    onBookmarkPost: () -> Unit,
    onSharePost: () -> Unit,
    onVotePoll: (String) -> Unit,
    onAddComment: (content: String, replyTo: String?, comfortTag: String?) -> Unit,
    onToggleCommentLike: (String) -> Unit,
    onBlockAuthor: (String) -> Unit,
    onReportPost: (String, String) -> Unit,
    onRequestWechatDm: (onAllowed: () -> Unit) -> Unit = { it() },
    onSendWechatDm: (text: String, wechatId: String?) -> Unit = { _, _ -> },
    onCopyWechatId: (String) -> Unit = {},
    onDeletePost: (() -> Unit)? = null
) {
    var commentText by remember { mutableStateOf("") }
    var replyTarget by remember { mutableStateOf<String?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showWechatSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (post == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("内容已归档或不存在", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = onBack, shape = RoundedCornerShape(10.dp)) { Text("返回") }
            }
        }
        return
    }

    // Strictly restrict private messaging to 二手交易 (MARKETPLACE) and 校园互助 (MUTUAL_AID) only
    val allowsWechatDm = post.category == PostCategory.MARKETPLACE || post.category == PostCategory.MUTUAL_AID

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = categoryIconFor(post.category),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = post.category.titleCn,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (post.isMine && onDeletePost != null) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("detail_delete_post_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "删除帖子",
                                tint = VermilionRed
                            )
                        }
                    }
                    IconButton(onClick = onSharePost) {
                        Icon(Icons.Outlined.Share, contentDescription = "分享")
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Outlined.MoreHoriz, contentDescription = "更多操作")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("举报该内容") },
                                onClick = {
                                    menuExpanded = false
                                    onReportPost(post.title.ifBlank { post.body }, "不符合社区规范")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("屏蔽该匿名用户") },
                                onClick = {
                                    menuExpanded = false
                                    onBlockAuthor(post.author.nickname)
                                    onBack()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.navigationBarsPadding().imePadding()) {
                    EditorialHairline()
                    if (!post.allowComments) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "作者已关闭此帖评论 · 静静阅读",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    } else {
                        if (replyTarget != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "回复 @$replyTarget",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "取消",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.clickable { replyTarget = null }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = {
                                    Text(
                                        text = if (isVerified) "匿名写下你的回应..." else "认证央美身份后即可评论...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("detail_comment_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(2.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (commentText.isNotBlank() || !isVerified) {
                                        onAddComment(commentText.ifBlank { "路过支持" }, replyTarget, null)
                                        if (isVerified) {
                                            commentText = ""
                                            replyTarget = null
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                    .testTag("detail_comment_send")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "发送评论",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
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
                .testTag("post_detail_scroll"),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Editorial Article Body
            item(key = "detail_article") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Author Anonymous Identity Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AnonymousAvatar(seed = post.author.avatarSeed, size = 42.dp)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = post.author.nickname,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    VerifiedIdentityBadge(
                                        identityType = post.author.identityType,
                                        isVerified = post.author.isVerified
                                    )
                                }
                                Text(
                                    text = post.createdAt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }

                    // Mood / Marketplace / Location Specialized Banners
                    if (post.moodTag != null) {
                        MonoTagChip(text = "树洞情绪 · ${post.moodTag}")
                    }
                    if (post.price != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.9.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(2.dp))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("EXCHANGE PRICE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outlineVariant)
                                Text(post.price, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                            MonoTagChip(text = post.tradeStatus ?: "校内可面交", accentDot = AcidYellow)
                        }
                    }
                    if (post.location != null || post.eventTime != null || post.contactHint != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(2.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            post.location?.let {
                                Text("地点 · $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            post.eventTime?.let {
                                Text("时间 · $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            post.contactHint?.let {
                                Text("联系 · $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Direct Post Body (no big title)
                    Text(
                        text = post.body.ifBlank { post.title },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f)
                    )

                    // Variable aspect ratio images with captions
                    post.mediaList.forEach { media ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            PostMediaImage(
                                media = media,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(media.aspectRatio.coerceIn(0.65f, 1.9f))
                            )
                            if (media.caption.isNotBlank()) {
                                Text(
                                    text = media.caption,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }

                    // Interactive Poll
                    post.poll?.let { poll ->
                        PollPreviewBlock(
                            poll = poll,
                            darkSurface = false,
                            onVoteOption = onVotePoll
                        )
                    }

                    if (post.topicTags.isNotEmpty()) {
                        TopicTagRow(tags = post.topicTags, onTopicClick = onTopicClick)
                    }

                    // Exclusive WeChat Exchange DM entry — ONLY for 二手交易 (MARKETPLACE) & 校园互助 (MUTUAL_AID)
                    if (allowsWechatDm) {
                        val isMarketplace = post.category == PostCategory.MARKETPLACE
                        GradientBorderCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("detail_wechat_dm_btn"),
                            onClick = {
                                onRequestWechatDm {
                                    showWechatSheet = true
                                }
                            },
                            outerCornerRadius = 14.dp,
                            borderWidth = 1.6.dp,
                            shadowStrength = 0.7f
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(AuroraIndigo, AuroraFuchsia, AuroraCyan)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Chat,
                                            contentDescription = "私信互换微信",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = if (isMarketplace) "私信互换微信 · 转移微信交易" else "私信互换微信 · 转移微信对接",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (dmMessages.isEmpty()) {
                                                "仅二手交易与校园互助开放私信，交换微信号后移步微信沟通"
                                            } else {
                                                "已有 ${dmMessages.size} 条私信记录 · 点击继续互换微信"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Surface(
                                    color = AuroraIndigo.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(999.dp)
                                ) {
                                    Text(
                                        text = if (dmMessages.isEmpty()) "去私信" else "查看",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AuroraIndigo,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    EditorialHairline(thickness = 0.7.dp)

                    // Reaction Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = onLikePost,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .heightIn(min = 44.dp)
                                    .testTag("detail_like_btn")
                            ) {
                                Icon(
                                    imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "点赞",
                                    tint = if (post.isLiked) VermilionRed else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${post.likeCount}")
                            }
                            OutlinedButton(
                                onClick = onBookmarkPost,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .heightIn(min = 44.dp)
                                    .testTag("detail_bookmark_btn")
                            ) {
                                Icon(
                                    imageVector = if (post.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "收藏",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${post.bookmarkCount}")
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Visibility,
                                    contentDescription = "浏览量",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = formatCompactCount(post.viewCount),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${post.comments.size} 条回应",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }

                    // Tree Hole gentle quick-comfort buttons
                    if (post.category == PostCategory.TREE_HOLE && post.allowComments) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "给树洞一句温柔回应",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(MockSeedData.treeHoleComfortOptions) { comfort ->
                                    MonoTagChip(
                                        text = comfort,
                                        onClick = {
                                            val defaultMsg = when (comfort) {
                                                "抱抱" -> "隔着屏幕给你一个安静的抱抱，慢慢来就好。"
                                                "我也一样" -> "其实我也常常有同样的瞬间，你不是一个人。"
                                                "给你一点建议" -> "先停下来休息一晚，明天再回头看会轻松很多。"
                                                else -> "随时在这里陪你聊聊，想说多少都可以。"
                                            }
                                            onAddComment(defaultMsg, null, comfort)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    EditorialHairline()
                }
            }

            // Comments Header
            item(key = "comments_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "讨论与回应 (${post.comments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (post.comments.isEmpty()) {
                item(key = "comments_empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "还没有人留言，写下第一条匿名回应吧。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            } else {
                items(post.comments, key = { it.id }) { comment ->
                    val isOp = comment.author.id == post.author.id ||
                        comment.author.nickname == post.author.nickname
                    CommentRowItem(
                        comment = comment,
                        isPostAuthor = isOp,
                        onReplyClick = { replyTarget = comment.author.nickname },
                        onLikeClick = { onToggleCommentLike(comment.id) }
                    )
                }
            }
        }
    }

    if (allowsWechatDm && showWechatSheet) {
        WechatExchangeSheet(
            post = post,
            messages = dmMessages,
            onDismiss = { showWechatSheet = false },
            onSend = onSendWechatDm,
            onCopyWechatId = onCopyWechatId
        )
    }

    if (showDeleteConfirm && onDeletePost != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "确认删除这条帖子？",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "删除后将无法恢复，该帖下的评论与互动记录也会一并移除。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    modifier = Modifier.testTag("detail_cancel_delete_btn")
                ) {
                    Text("取消")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeletePost()
                    },
                    modifier = Modifier.testTag("detail_confirm_delete_btn")
                ) {
                    Text("确认删除", color = VermilionRed)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WechatExchangeSheet(
    post: Post,
    messages: List<WechatDmMessage>,
    onDismiss: () -> Unit,
    onSend: (text: String, wechatId: String?) -> Unit,
    onCopyWechatId: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current

    var myWechatId by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnonymousAvatar(seed = post.author.avatarSeed, size = 34.dp)
                    Column {
                        Text(
                            text = "私信 ${post.author.nickname}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${post.category.titleCn} · 仅支持互换微信后转移微信交易",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "关闭私信窗口")
                }
            }

            // Policy notice banner (unified Aurora tint)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                AuroraIndigo.copy(alpha = 0.10f),
                                AuroraFuchsia.copy(alpha = 0.08f),
                                AuroraCyan.copy(alpha = 0.10f)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Text(
                    text = "平台提示：仅闲置二手交易与校园互助开放私信功能。建议确认意向后互换微信号，转移至微信完成面交或细节沟通。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            EditorialHairline()

            // Scrollable message thread
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("wechat_dm_list"),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.isEmpty()) {
                    item(key = "dm_empty") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = null,
                                tint = AuroraIndigo.copy(alpha = 0.7f),
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "还没有私信记录",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "可在下方填写你的微信号或留言，发给对方互换微信并转移至微信交易。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(messages, key = { it.id }) { msg ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (msg.isFromMe) Alignment.End else Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${msg.senderNickname} · ${msg.createdAt}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            Surface(
                                color = if (msg.isFromMe) {
                                    AuroraIndigo.copy(alpha = 0.14f)
                                } else {
                                    MaterialTheme.colorScheme.background
                                },
                                shape = RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (msg.isFromMe) 14.dp else 4.dp,
                                    bottomEnd = if (msg.isFromMe) 4.dp else 14.dp
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.8.dp,
                                    if (msg.isFromMe) AuroraIndigo.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .widthIn(max = 290.dp)
                                        .padding(horizontal = 13.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!msg.wechatId.isNullOrBlank()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                AuroraIndigo.copy(alpha = 0.4f)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "微信号 · 转移微信沟通",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = AuroraIndigo
                                                    )
                                                    Text(
                                                        text = msg.wechatId,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                TextButton(
                                                    onClick = {
                                                        clipboard.setText(AnnotatedString(msg.wechatId))
                                                        onCopyWechatId(msg.wechatId)
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Text("复制", color = AuroraIndigo, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            EditorialHairline()

            // Bottom composer with optional WeChat ID field
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = myWechatId,
                    onValueChange = { myWechatId = it },
                    placeholder = {
                        Text(
                            "附带你的微信号（选填，便于对方直接加微信）",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Badge,
                            contentDescription = null,
                            tint = AuroraIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wechat_dm_wx_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = {
                            Text(
                                "发条私信，约好时间或互换微信...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wechat_dm_text_input")
                    )
                    val canSend = messageText.isNotBlank() || myWechatId.isNotBlank()
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (canSend) {
                                    Brush.linearGradient(listOf(AuroraIndigo, AuroraFuchsia, AuroraCyan))
                                } else {
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.outlineVariant,
                                            MaterialTheme.colorScheme.outlineVariant
                                        )
                                    )
                                }
                            )
                            .clickable(enabled = canSend) {
                                onSend(messageText, myWechatId.takeIf { it.isNotBlank() })
                                messageText = ""
                                myWechatId = ""
                            }
                            .testTag("wechat_dm_send_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "发送私信",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentRowItem(
    comment: Comment,
    isPostAuthor: Boolean = false,
    onReplyClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            AnonymousAvatar(seed = comment.author.avatarSeed, size = 34.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        Text(
                            text = comment.author.nickname,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isPostAuthor) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(YangmeiPrimaryGradient)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("comment_op_badge_${comment.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "楼主",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PaperWhite
                                )
                            }
                        }
                        comment.comfortTag?.let { tag ->
                            MonoTagChip(text = tag, accentDot = AcidYellow)
                        }
                    }
                    Text(
                        text = comment.createdAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                if (!comment.replyToNickname.isNullOrBlank()) {
                    Text(
                        text = "回复 @${comment.replyToNickname}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable(onClick = onLikeClick)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "点赞评论",
                            tint = if (comment.isLiked) VermilionRed else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${comment.likes}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "回复",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable(onClick = onReplyClick)
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
        EditorialHairline(thickness = 0.6.dp)
    }
}
