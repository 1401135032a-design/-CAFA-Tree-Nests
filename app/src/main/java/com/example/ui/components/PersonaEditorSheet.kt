package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AnonymousNicknameLexicon
import com.example.ui.theme.*

/**
 * Interactive ModalBottomSheet on ProfileScreen allowing users to:
 * 1. Type and save their own custom anonymous nickname (2..16 chars),
 * 2. Pick or roll non-repeating nicknames from the 3,648-word CAFA lexicon,
 * 3. Browse & switch unique generative art avatars.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PersonaEditorBottomSheet(
    currentNickname: String,
    currentAvatarSeed: Int,
    usedCommunityNicknames: Set<String>,
    onDismiss: () -> Unit,
    onSave: (nickname: String, avatarSeed: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDarkTheme = MaterialTheme.colorScheme.background.red < 0.2f
    val accentColor = if (isDarkTheme) AuroraLightIndigo else AuroraFocusIndigo

    var draftNickname by remember { mutableStateOf(currentNickname) }
    var draftSeed by remember { mutableIntStateOf(currentAvatarSeed) }

    var suggestedNicknames by remember {
        mutableStateOf(
            AnonymousNicknameLexicon.sampleBatch(
                count = 6,
                exclude = usedCommunityNicknames + currentNickname
            )
        )
    }

    var avatarCandidates by remember {
        mutableStateOf(
            listOf(currentAvatarSeed) +
                AnonymousNicknameLexicon.sampleDistinctAvatarSeeds(count = 9, excludeSeed = currentAvatarSeed)
        )
    }

    fun rollSingleNickname() {
        val next = AnonymousNicknameLexicon.nextUniqueRandom(
            exclude = usedCommunityNicknames + draftNickname
        )
        draftNickname = next
    }

    fun refreshNicknameBatch() {
        suggestedNicknames = AnonymousNicknameLexicon.sampleBatch(
            count = 6,
            exclude = usedCommunityNicknames + draftNickname
        )
    }

    fun refreshAvatarBatch() {
        val fresh = AnonymousNicknameLexicon.sampleDistinctAvatarSeeds(count = 10, excludeSeed = draftSeed)
        avatarCandidates = fresh
        draftSeed = fresh.first()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "自定义昵称与形象",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("persona_editor_close_btn")
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = "关闭")
                }
            }

            // Live Preview Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                    .border(1.2.dp, AuroraBorderGradient, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AnonymousAvatar(seed = draftSeed, size = 64.dp)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "实时预览",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = draftNickname.ifBlank { "请输入你的匿名昵称" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "形象编号 #${draftSeed % 10000}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, AuroraBorderGradient, RoundedCornerShape(10.dp))
                        .clickable {
                            rollSingleNickname()
                            draftSeed = AnonymousNicknameLexicon.sampleDistinctAvatarSeeds(1, excludeSeed = draftSeed).first()
                        }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("persona_editor_roll_both_btn"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "随机一套",
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Section 1: Custom Nickname Input + Lexicon Suggestions
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "修改匿名昵称",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${draftNickname.trim().length} / 16",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (draftNickname.trim().length in 2..16) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            VermilionRed
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = draftNickname,
                        onValueChange = { if (it.length <= 16) draftNickname = it },
                        placeholder = { Text("输入 2~16 字自定义昵称") },
                        singleLine = true,
                        trailingIcon = {
                            if (draftNickname.isNotEmpty()) {
                                IconButton(onClick = { draftNickname = "" }) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        contentDescription = "清空昵称",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("persona_nickname_input")
                    )

                    FilledTonalIconButton(
                        onClick = { rollSingleNickname() },
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("persona_roll_nickname_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "从词库随机抽取昵称",
                            tint = accentColor
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "词库不重复灵感推荐",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { refreshNicknameBatch() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("persona_refresh_lexicon_btn"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "换一批昵称",
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "换一批",
                            style = MaterialTheme.typography.labelMedium,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    suggestedNicknames.forEach { suggestion ->
                        MonoTagChip(
                            text = suggestion,
                            inverted = draftNickname == suggestion,
                            onClick = { draftNickname = suggestion }
                        )
                    }
                }
            }

            EditorialHairline(thickness = 0.6.dp)

            // Section 2: Avatar Gallery Picker (10 distinct procedural art looks per batch)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "更换匿名形象",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { refreshAvatarBatch() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("persona_refresh_avatars_btn"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "换一批形象",
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "换一批形象",
                            style = MaterialTheme.typography.labelMedium,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // 2 rows x 5 columns grid of distinct generative avatars
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    avatarCandidates.chunked(5).forEach { rowSeeds ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowSeeds.forEach { seed ->
                                val isSelected = seed == draftSeed
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(2.2.dp, YangmeiPrimaryGradient, RoundedCornerShape(14.dp))
                                            } else {
                                                Modifier.border(
                                                    0.8.dp,
                                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                    RoundedCornerShape(14.dp)
                                                )
                                            }
                                        )
                                        .clickable { draftSeed = seed }
                                        .padding(3.dp)
                                        .testTag("persona_avatar_option_$seed"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnonymousAvatar(seed = seed, size = 48.dp)
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(YangmeiPrimaryGradient),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "已选中",
                                                tint = PaperWhite,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Save Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(YangmeiPrimaryGradient)
                    .clickable { onSave(draftNickname, draftSeed) }
                    .testTag("save_persona_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "保存昵称与形象",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PaperWhite
                )
            }
        }
    }
}
