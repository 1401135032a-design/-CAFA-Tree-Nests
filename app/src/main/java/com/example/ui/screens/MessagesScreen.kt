package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NotificationItem
import com.example.data.NotificationType
import com.example.ui.components.AnonymousAvatar
import com.example.ui.components.AuroraPageBackground
import com.example.ui.components.MonoTagChip
import com.example.ui.components.ObserveScrollFabVisibility
import com.example.ui.components.ProgressiveBlurTitle
import com.example.ui.components.RedNumberBadge
import com.example.ui.theme.AuroraBorderGradient
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.VermilionRed
import com.example.ui.theme.YangmeiPrimaryGradient
import com.example.viewmodel.YangmeiUiState

private enum class MessageFilter(val label: String) {
    ALL("全部"),
    COMMENTS("评论与回复"),
    REACTIONS("点赞与收藏"),
    NOTES("匿名留言"),
    SYSTEM("系统与审核")
}

@Composable
fun MessagesScreen(
    uiState: YangmeiUiState,
    onNotificationClick: (NotificationItem) -> Unit,
    onMarkAllRead: () -> Unit,
    onFabVisibilityChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(MessageFilter.ALL) }
    val listState = rememberLazyListState()
    val filteredItems = remember(uiState.notifications, selectedFilter) {
        when (selectedFilter) {
            MessageFilter.ALL -> uiState.notifications
            MessageFilter.COMMENTS -> uiState.notifications.filter {
                it.type == NotificationType.COMMENT || it.type == NotificationType.REPLY
            }
            MessageFilter.REACTIONS -> uiState.notifications.filter {
                it.type == NotificationType.LIKE || it.type == NotificationType.BOOKMARK
            }
            MessageFilter.NOTES -> uiState.notifications.filter { it.type == NotificationType.ANONYMOUS_NOTE }
            MessageFilter.SYSTEM -> uiState.notifications.filter {
                it.type == NotificationType.SYSTEM || it.type == NotificationType.VERIFICATION
            }
        }
    }

    ObserveScrollFabVisibility(
        listState = listState,
        onFabVisibilityChange = onFabVisibilityChange
    )

    Box(modifier = modifier.fillMaxSize()) {
        AuroraPageBackground(seed = 3, headerWashHeight = 215.dp)

        Column(modifier = Modifier.fillMaxSize()) {
            // Header sits cleanly above the progressive-blur aurora mask
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        ProgressiveBlurTitle(text = "消息", fontSize = 26.sp)
                        if (uiState.unreadNotificationCount > 0) {
                            RedNumberBadge(
                                count = uiState.unreadNotificationCount,
                                modifier = Modifier.testTag("messages_header_unread_badge")
                            )
                        }
                    }

                    if (uiState.unreadNotificationCount > 0) {
                        Row(
                            modifier = Modifier
                                .heightIn(min = 40.dp)
                                .clickable(onClick = onMarkAllRead)
                                .padding(horizontal = 6.dp)
                                .testTag("mark_all_read_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DoneAll,
                                contentDescription = "全部已读",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "全部已读",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Horizontally scrollable filter pills with numbered red badges for unread comments & likes
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("messages_filter_tabs")
                ) {
                    items(MessageFilter.entries) { filter ->
                        val unreadForFilter = when (filter) {
                            MessageFilter.ALL -> uiState.unreadNotificationCount
                            MessageFilter.COMMENTS -> uiState.unreadCommentCount
                            MessageFilter.REACTIONS -> uiState.unreadLikeCount
                            MessageFilter.NOTES -> uiState.unreadNoteCount
                            MessageFilter.SYSTEM -> uiState.unreadSystemCount
                        }
                        val isSelected = selectedFilter == filter
                        val pillShape = RoundedCornerShape(10.dp)

                        Row(
                            modifier = Modifier
                                .clip(pillShape)
                                .then(
                                    if (isSelected) {
                                        Modifier.background(YangmeiPrimaryGradient)
                                    } else {
                                        Modifier
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f))
                                            .border(1.1.dp, AuroraBorderGradient, pillShape)
                                    }
                                )
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("messages_filter_${filter.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) PaperWhite else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            if (unreadForFilter > 0) {
                                RedNumberBadge(
                                    count = unreadForFilter,
                                    modifier = Modifier.testTag("messages_badge_${filter.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("messages_list"),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 124.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredItems.isEmpty()) {
                    item(key = "msg_empty") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 80.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsNone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(34.dp)
                            )
                            Text(
                                text = "暂无消息",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredItems, key = { it.id }) { item ->
                        MessageRowItem(
                            item = item,
                            onClick = { onNotificationClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageRowItem(
    item: NotificationItem,
    onClick: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.red < 0.2f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("message_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (isDarkTheme) 0.dp else 1.dp,
        border = BorderStroke(
            width = if (item.isUnread) 1.dp else 0.8.dp,
            color = if (item.isUnread) {
                VermilionRed.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = if (isDarkTheme) 0.85f else 0.72f)
            }
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box {
                AnonymousAvatar(seed = item.sender.avatarSeed, size = 40.dp)
                if (item.isUnread) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = (-3).dp)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(VermilionRed)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.sender.nickname,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        MonoTagChip(text = item.type.labelCn)
                    }
                    Text(
                        text = item.createdAt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!item.quoteSnippet.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = item.quoteSnippet,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
