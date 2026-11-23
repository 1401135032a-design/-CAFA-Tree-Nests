package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*

fun categoryIconFor(category: PostCategory): ImageVector = when (category) {
    PostCategory.CAMPUS_RANT -> Icons.Outlined.Forum
    PostCategory.STUDY -> Icons.Outlined.AutoAwesome
    PostCategory.COURSE_ARCHIVE -> Icons.Outlined.MenuBook
    PostCategory.SENIOR_EXPERIENCE -> Icons.Outlined.AutoStories
    PostCategory.RELATIONSHIP -> Icons.Outlined.FavoriteBorder
    PostCategory.TREE_HOLE -> Icons.Outlined.Spa
    PostCategory.OPPORTUNITY -> Icons.Outlined.WorkOutline
    PostCategory.MARKETPLACE -> Icons.Outlined.ShoppingBag
    PostCategory.LOST_FOUND -> Icons.Outlined.FindInPage
    PostCategory.MUTUAL_AID -> Icons.Outlined.Handshake
    PostCategory.EXHIBITION -> Icons.Outlined.Museum
    PostCategory.GRAD_ABROAD -> Icons.Outlined.School
    PostCategory.CAREER -> Icons.Outlined.Badge
    PostCategory.ART_CREATION -> Icons.Outlined.Palette
}

fun formatCompactCount(count: Int): String = when {
    count >= 10000 -> String.format("%.1fw", count / 10000f)
    count >= 1000 -> String.format("%.1fk", count / 1000f)
    else -> "$count"
}

/**
 * Compact, lively rounded-card post layout so multiple posts fit comfortably on one screen.
 * Supports inline text fold/expand, compact thumbnails, category icons, and eye-icon view count.
 */
@Composable
fun EditorialPostCard(
    post: Post,
    onPostClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit,
    onVoteOption: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember(post.id) { mutableStateOf(false) }
    val isFeaturedCard = post.layoutType == PostLayoutType.TOPIC_POSTER
    val isDarkTheme = MaterialTheme.colorScheme.background.red < 0.2f
    val isDarkCard = isDarkTheme

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp)
            .animateContentSize()
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (isDarkTheme) 0.dp else 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 0.75.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDarkTheme) 0.9f else 0.72f)
        ),
        onClick = onPostClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isFeaturedCard && !isDarkTheme) Modifier.background(YangmeiFeaturedCardGradient)
                    else Modifier
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 11.dp, bottom = 7.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Compact single-line header: Avatar + Nickname + Time + Category Pill
                PostCompactHeader(post = post, darkMode = isDarkCard)

                // Direct body text (no big title) with tight folding & compact thumbnail(s)
                val displayText = post.body.ifBlank { post.title }
                val hasSingleThumb = post.mediaList.size == 1
                if (hasSingleThumb) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            PostPriceInlineBadge(post = post)
                            CollapsibleBodyExcerpt(
                                text = displayText,
                                expanded = expanded,
                                darkMode = isDarkCard,
                                collapsedLines = 2,
                                onToggle = { expanded = !expanded }
                            )
                        }
                        PostMediaImage(
                            media = post.mediaList.first(),
                            modifier = Modifier.size(56.dp)
                        )
                    }
                } else {
                    PostPriceInlineBadge(post = post)
                    CollapsibleBodyExcerpt(
                        text = displayText,
                        expanded = expanded,
                        darkMode = isDarkCard,
                        collapsedLines = if (post.mediaList.size > 1 || post.poll != null) 2 else 2,
                        onToggle = { expanded = !expanded }
                    )
                    if (post.mediaList.size > 1) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(post.mediaList.take(3), key = { it.id }) { media ->
                                PostMediaImage(
                                    media = media,
                                    modifier = Modifier.size(width = 74.dp, height = 46.dp)
                                )
                            }
                        }
                    }
                }

                // Ultra-compact single-line poll teaser (expands full voting on tap or inside detail)
                post.poll?.let { poll ->
                    CompactPollPreview(
                        poll = poll,
                        expanded = expanded,
                        darkSurface = isDarkCard,
                        onExpandPoll = { expanded = true },
                        onVoteOption = onVoteOption
                    )
                }

                // Compact Tag + Location / Mood Row (horizontally scrollable for multi-item tags)
                val showMetaStrip = post.topicTags.isNotEmpty() || post.location != null || post.moodTag != null
                if (showMetaStrip) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        post.moodTag?.let { mood ->
                            CompactPillTag(
                                text = mood,
                                icon = Icons.Outlined.Mood,
                                bg = SlateTintBg,
                                fg = SlateAccent
                            )
                        }
                        post.location?.let { loc ->
                            CompactPillTag(
                                text = loc,
                                icon = Icons.Outlined.LocationOn,
                                bg = if (isDarkCard) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                fg = if (isDarkCard) FluidSilver else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        post.topicTags.forEach { tag ->
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (isDarkCard) AcidYellow else SlateAccent,
                                maxLines = 1,
                                modifier = Modifier.clickable { onTopicClick(tag) }
                            )
                        }
                    }
                }
            }

            EditorialHairline(
                thickness = 0.5.dp,
                color = if (isDarkCard) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
            )

            // Compact Footer with Eye View Count + Like + Comment + Bookmark + Share
            PostFooterActions(
                post = post,
                darkMode = isDarkCard,
                onLikeClick = onLikeClick,
                onCommentClick = onPostClick,
                onBookmarkClick = onBookmarkClick,
                onShareClick = onShareClick
            )
        }
    }
}

@Composable
private fun PostCompactHeader(
    post: Post,
    darkMode: Boolean
) {
    val primaryColor = if (darkMode) PaperWhite else MaterialTheme.colorScheme.onSurface
    val secondaryColor = if (darkMode) MidGray else MaterialTheme.colorScheme.onSurfaceVariant

    val (pillBg, pillFg) = when (post.category.accentType) {
        CategoryAccent.ACID -> if (darkMode) AcidYellow.copy(alpha = 0.22f) to AcidYellow else AcidTintBg to InkBlack
        CategoryAccent.GREEN -> if (darkMode) FluorescentGreen.copy(alpha = 0.22f) to FluorescentGreen else MintTintBg to Color(0xFF1A7A42)
        CategoryAccent.RED -> if (darkMode) VermilionRed.copy(alpha = 0.24f) to Color(0xFFFF8F87) else SoftRoseTint to VermilionRed
        else -> if (darkMode) Color.White.copy(alpha = 0.12f) to FluidSilver else SlateTintBg to SlateAccent
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.weight(1f)
        ) {
            AnonymousAvatar(seed = post.author.avatarSeed, size = 24.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = post.author.nickname,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = post.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryColor,
                    maxLines = 1
                )
            }
        }

        // Compact category pill with icon
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(pillBg)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = categoryIconFor(post.category),
                contentDescription = null,
                tint = pillFg,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = post.category.titleCn,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = pillFg
            )
        }
    }
}

@Composable
private fun PostPriceInlineBadge(post: Post) {
    post.price?.let { price ->
        Text(
            text = price,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = VermilionRed
        )
    }
}

@Composable
private fun CollapsibleBodyExcerpt(
    text: String,
    expanded: Boolean,
    darkMode: Boolean,
    collapsedLines: Int = 2,
    onToggle: () -> Unit
) {
    val bodyColor = if (darkMode) WarmGrayWhite.copy(alpha = 0.88f) else MaterialTheme.colorScheme.onSurface
    val toggleColor = if (darkMode) AcidYellow else SlateAccent
    val foldThreshold = if (collapsedLines == 1) 24 else 40
    val isLongText = text.length > foldThreshold || text.contains("\n")

    if (expanded) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = bodyColor,
                maxLines = 12,
                overflow = TextOverflow.Ellipsis
            )
            if (isLongText) {
                Text(
                    text = "收起",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = toggleColor,
                    modifier = Modifier.clickable(onClick = onToggle)
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = text.replace("\n", " "),
                style = MaterialTheme.typography.bodyMedium,
                color = bodyColor,
                maxLines = collapsedLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (isLongText) {
                Text(
                    text = "展开",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = toggleColor,
                    modifier = Modifier.clickable(onClick = onToggle)
                )
            }
        }
    }
}

@Composable
private fun CompactPillTag(
    text: String,
    icon: ImageVector,
    bg: Color,
    fg: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(bg)
            .padding(horizontal = 5.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = fg, modifier = Modifier.size(11.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CompactPollPreview(
    poll: PollData,
    expanded: Boolean,
    darkSurface: Boolean,
    onExpandPoll: () -> Unit,
    onVoteOption: (String) -> Unit
) {
    val total = poll.totalVotes.coerceAtLeast(1)
    val borderColor = if (darkSurface) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.outline
    val textColor = if (darkSurface) PaperWhite else MaterialTheme.colorScheme.onSurface
    val subColor = if (darkSurface) MidGray else MaterialTheme.colorScheme.onSurfaceVariant

    if (!expanded) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (darkSurface) Color.White.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                .clickable(onClick = onExpandPoll)
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Poll,
                    contentDescription = "投票",
                    tint = if (darkSurface) AcidYellow else SlateAccent,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = poll.question,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "${poll.totalVotes}人投票 ▾",
                style = MaterialTheme.typography.labelSmall,
                color = if (darkSurface) AcidYellow else SlateAccent
            )
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Poll,
                contentDescription = "投票",
                tint = if (darkSurface) AcidYellow else SlateAccent,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "${poll.question} · ${poll.totalVotes}人已投",
                style = MaterialTheme.typography.labelMedium,
                color = subColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        poll.options.take(2).forEach { option ->
            val ratio = option.votes.toFloat() / total
            val isSelected = poll.votedOptionId == option.id
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .border(
                        width = if (isSelected) 1.dp else 0.7.dp,
                        color = if (isSelected) AcidYellow else borderColor,
                        shape = RoundedCornerShape(7.dp)
                    )
                    .clickable(enabled = poll.votedOptionId == null) { onVoteOption(option.id) }
            ) {
                if (poll.votedOptionId != null) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .fillMaxWidth(ratio.coerceIn(0.05f, 1f))
                            .background(if (isSelected) AcidYellow.copy(alpha = 0.26f) else Color.White.copy(alpha = 0.10f))
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (poll.votedOptionId != null) {
                        Text(
                            text = "${(ratio * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) AcidYellow else subColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PollPreviewBlock(
    poll: PollData,
    darkSurface: Boolean,
    onVoteOption: (String) -> Unit
) {
    val total = poll.totalVotes.coerceAtLeast(1)
    val borderColor = if (darkSurface) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.outline
    val textColor = if (darkSurface) PaperWhite else MaterialTheme.colorScheme.onSurface
    val subColor = if (darkSurface) MidGray else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = "${poll.question}（${poll.totalVotes} 人已投）",
            style = MaterialTheme.typography.bodySmall,
            color = subColor
        )
        poll.options.forEach { option ->
            val ratio = option.votes.toFloat() / total
            val isSelected = poll.votedOptionId == option.id
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = if (isSelected) 1.2.dp else 0.7.dp,
                        color = if (isSelected) AcidYellow else borderColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = poll.votedOptionId == null) { onVoteOption(option.id) }
            ) {
                if (poll.votedOptionId != null) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .fillMaxWidth(ratio.coerceIn(0.03f, 1f))
                            .background(
                                if (isSelected) AcidYellow.copy(alpha = 0.24f)
                                else if (darkSurface) Color.White.copy(alpha = 0.08f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    if (poll.votedOptionId != null) {
                        Text(
                            text = "${(ratio * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) textColor else subColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostMediaImage(
    media: PostMedia,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        when {
            media.drawableRes != null -> {
                Image(
                    painter = painterResource(id = media.drawableRes),
                    contentDescription = media.caption.ifBlank { "帖子图片" },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            media.uriString != null -> {
                AsyncImage(
                    model = media.uriString,
                    contentDescription = media.caption.ifBlank { "用户上传图片" },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun TopicTagRow(
    tags: List<String>,
    onTopicClick: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tags.take(3).forEach { tag ->
            Text(
                text = tag,
                style = MaterialTheme.typography.bodySmall,
                color = SlateAccent,
                modifier = Modifier
                    .clickable { onTopicClick(tag) }
                    .padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun PostFooterActions(
    post: Post,
    darkMode: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val defaultTint = if (darkMode) MidGray else MaterialTheme.colorScheme.onSurfaceVariant
    val activeBookTint = if (darkMode) AcidYellow else InkBlack

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Eye icon view count (小眼睛浏览量)
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = "浏览量",
                    tint = defaultTint,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = formatCompactCount(post.viewCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = defaultTint
                )
            }

            FooterActionButton(
                icon = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                label = "${post.likeCount}",
                tint = if (post.isLiked) VermilionRed else defaultTint,
                contentDesc = "点赞",
                testTag = "like_btn_${post.id}",
                onClick = onLikeClick
            )
            FooterActionButton(
                icon = Icons.Outlined.ChatBubbleOutline,
                label = "${post.comments.size}",
                tint = defaultTint,
                contentDesc = "评论",
                testTag = "comment_btn_${post.id}",
                onClick = onCommentClick
            )
            FooterActionButton(
                icon = if (post.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                label = "${post.bookmarkCount}",
                tint = if (post.isBookmarked) activeBookTint else defaultTint,
                contentDesc = "收藏",
                testTag = "bookmark_btn_${post.id}",
                onClick = onBookmarkClick
            )
        }

        IconButton(
            onClick = onShareClick,
            modifier = Modifier
                .size(36.dp)
                .testTag("share_btn_${post.id}")
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "分享",
                tint = defaultTint,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun FooterActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    contentDesc: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .heightIn(min = 36.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = tint,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
    }
}
