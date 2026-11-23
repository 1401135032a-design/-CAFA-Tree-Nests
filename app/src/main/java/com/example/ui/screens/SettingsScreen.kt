package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.Coil
import com.example.BuildConfig
import com.example.R
import com.example.data.UserSettings
import com.example.ui.components.EditorialHairline
import com.example.ui.components.MonoTagChip
import com.example.viewmodel.YangmeiUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: YangmeiUiState,
    onBack: () -> Unit,
    onUpdateNickname: (String) -> Unit,
    onRandomizeIdentity: () -> Unit,
    onUpdateSettings: ((UserSettings) -> UserSettings) -> Unit,
    onUnblockUser: (String) -> Unit,
    onAddBlockedKeyword: (String) -> Unit,
    onRemoveBlockedKeyword: (String) -> Unit,
    onResetVerificationDemo: () -> Unit,
    onSendFeedback: (String) -> Unit,
    onCacheCleared: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var newKeyword by remember { mutableStateOf("") }
    var feedbackText by remember { mutableStateOf("") }
    var memRefreshTick by remember { mutableIntStateOf(0) }
    val memoryStats = remember(memRefreshTick) {
        val rt = Runtime.getRuntime()
        val usedMb = ((rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L)).coerceAtLeast(1L)
        val maxMb = (rt.maxMemory() / (1024L * 1024L)).coerceAtLeast(usedMb)
        usedMb to maxMb
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("设置", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
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
                .testTag("settings_list"),
            contentPadding = PaddingValues(bottom = 64.dp)
        ) {
            // 1. Notification & Privacy Settings
            item(key = "sec_notify_privacy") {
                Column {
                    SettingsSectionHeader("通知与隐私")
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        SettingsToggleRow(
                            title = "评论与回复通知",
                            subtitle = "有人回应你的帖子或树洞时提醒",
                            checked = uiState.settings.notifyComments,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(notifyComments = v) } }
                        )
                        EditorialHairline(thickness = 0.5.dp)
                        SettingsToggleRow(
                            title = "点赞与收藏通知",
                            subtitle = "收到共鸣与收藏时记录在消息列表",
                            checked = uiState.settings.notifyLikes,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(notifyLikes = v) } }
                        )
                        EditorialHairline(thickness = 0.5.dp)
                        SettingsToggleRow(
                            title = "接收匿名留言",
                            subtitle = "允许通过认证的同学在树洞给你留言",
                            checked = uiState.settings.allowAnonymousNotes,
                            onCheckedChange = { v -> onUpdateSettings { it.copy(allowAnonymousNotes = v) } }
                        )
                    }
                    EditorialHairline()
                }
            }

            // 3. Blocked Users & Keywords
            item(key = "sec_blocking") {
                Column {
                    SettingsSectionHeader("屏蔽管理")
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("已屏蔽匿名用户", style = MaterialTheme.typography.titleSmall)
                        if (uiState.settings.blockedUsers.isEmpty()) {
                            Text("暂无屏蔽用户", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outlineVariant)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(uiState.settings.blockedUsers) { user ->
                                    Row(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                            .clickable { onUnblockUser(user) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(user, style = MaterialTheme.typography.bodySmall)
                                        Icon(Icons.Outlined.Close, contentDescription = "解除屏蔽", modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }

                        EditorialHairline(thickness = 0.6.dp)

                        Text("屏蔽关键词", style = MaterialTheme.typography.titleSmall)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newKeyword,
                                onValueChange = { newKeyword = it },
                                placeholder = { Text("输入想过滤的词...") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (newKeyword.isNotBlank()) {
                                        onAddBlockedKeyword(newKeyword)
                                        newKeyword = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = "添加屏蔽词", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.settings.blockedKeywords) { kw ->
                                MonoTagChip(text = "$kw ×", onClick = { onRemoveBlockedKeyword(kw) })
                            }
                        }
                    }
                    EditorialHairline()
                }
            }

            // 4. Report Records
            item(key = "sec_reports") {
                Column {
                    SettingsSectionHeader("举报记录")
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (uiState.settings.reportHistory.isEmpty()) {
                            Text("暂无举报记录", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outlineVariant)
                        } else {
                            uiState.settings.reportHistory.forEach { rep ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(rep.targetSummary, style = MaterialTheme.typography.titleSmall)
                                        Text("${rep.reason} · ${rep.createdAt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outlineVariant)
                                    }
                                    MonoTagChip(text = rep.status)
                                }
                                EditorialHairline(thickness = 0.5.dp)
                            }
                        }
                    }
                    EditorialHairline()
                }
            }

            // 5. Help, Feedback & Verification Demo Reset
            item(key = "sec_feedback") {
                Column {
                    SettingsSectionHeader("帮助与反馈")
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            label = { Text("给杨梅树窝提建议") },
                            minLines = 2,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (feedbackText.isNotBlank()) {
                                    onSendFeedback(feedbackText)
                                    feedbackText = ""
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        ) {
                            Text("提交匿名反馈")
                        }
                        OutlinedButton(
                            onClick = onResetVerificationDemo,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("settings_reset_verify_btn")
                        ) {
                            Text("重置为未认证状态（演示测试）")
                        }
                    }
                    EditorialHairline()
                }
            }

            // 6. Version, Creator Copyright & Memory Capacity Management
            item(key = "sec_version_memory") {
                Column {
                    SettingsSectionHeader("版本、版权与运行内存")
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("当前版本", style = MaterialTheme.typography.titleSmall)
                            MonoTagChip(text = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("运行内存占用 / 上限", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${memoryStats.first} MB / ${memoryStats.second} MB",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "已启用大堆内存配置、图片 20% 内存上限 + 64MB 磁盘缓存与首页分页窗口化加载，适配 5000+ 人规模流畅浏览与后续版本无缝覆盖升级。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    val loader = Coil.imageLoader(context)
                                    loader.memoryCache?.clear()
                                    loader.diskCache?.clear()
                                }
                                memRefreshTick++
                                onCacheCleared()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("settings_clear_cache_btn")
                        ) {
                            Text("清理图片内存与本地缓存")
                        }

                        EditorialHairline(thickness = 0.6.dp)

                        // Copyright & Original Ownership Statement
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("settings_copyright_card"),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.app_copyright),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.app_copyright_statement),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
