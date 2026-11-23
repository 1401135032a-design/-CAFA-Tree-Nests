package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiCampusStatsReport
import com.example.ai.AiStatsUiState
import com.example.ui.theme.*

/**
 * Interactive Gemini AI campus tag & keyword statistics card.
 * Displays Idle prompt, Loading state, Structured Success analytics (top keywords,
 * sentiment/topic bars, clickable recommended tags), and transparent Error feedback.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiCampusStatsCard(
    query: String,
    aiStatsState: AiStatsUiState,
    matchedCount: Int,
    totalTagCount: Int,
    onRunAnalysis: () -> Unit,
    onSelectTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.red < 0.2f
    val cleanQuery = query.trim()
    val expectedScope = if (cleanQuery.isBlank()) "全站校园标签与热词" else "关键词「$cleanQuery」"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("ai_stats_card"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (isDarkTheme) 0.dp else 1.5.dp,
        border = BorderStroke(
            width = 1.dp,
            brush = AuroraBorderGradient
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header row + trigger CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(YangmeiPrimaryGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "AI 统计",
                            tint = PaperWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (cleanQuery.isBlank()) "AI 智能搜索 · 校园标签统计" else "AI 搜索「$cleanQuery」",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (cleanQuery.isBlank()) {
                                "已聚合 $totalTagCount 个校园与自定义标签 · 支持自然语言 AI 搜索"
                            } else {
                                "站内匹配 $matchedCount 条内容 · AI 自动总结回答与趋势统计"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val isLoading = aiStatsState is AiStatsUiState.Loading
                val actionLabel = when {
                    isLoading -> "搜索中"
                    aiStatsState is AiStatsUiState.Success && aiStatsState.report.queryLabel == expectedScope -> "重新生成"
                    else -> "AI 搜索"
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (isLoading) Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                            else Modifier.background(YangmeiPrimaryGradient)
                        )
                        .clickable(enabled = !isLoading, onClick = onRunAnalysis)
                        .padding(horizontal = 11.dp, vertical = 7.dp)
                        .testTag("ai_stats_trigger_btn"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(13.dp),
                            strokeWidth = 1.8.dp,
                            color = AuroraIndigo
                        )
                    } else {
                        Icon(
                            imageVector = if (actionLabel == "重新生成") Icons.Outlined.Refresh else Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = PaperWhite,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLoading) MaterialTheme.colorScheme.onSurfaceVariant else PaperWhite
                    )
                }
            }

            when (aiStatsState) {
                is AiStatsUiState.Idle -> {
                    Text(
                        text = if (cleanQuery.isBlank()) {
                            "输入任意央美问题或关键词即可触发 AI 智能搜索，也可点击右侧「AI 搜索」一键汇总全站热点、选课口碑与自定义标签分布。"
                        } else {
                            "点击「AI 搜索」，让 AI 结合全站帖子、选课评价与学长学姐经验直接总结「$cleanQuery」的答案与热度统计。"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is AiStatsUiState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = AuroraIndigo
                        )
                        Text(
                            text = "正在 AI 搜索并统计${aiStatsState.queryLabel}的相关帖子、选课与经验...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                is AiStatsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SoftRoseTint.copy(alpha = if (isDarkTheme) 0.16f else 0.75f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = VermilionRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "AI 统计暂不可用",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = VermilionRed
                            )
                        }
                        Text(
                            text = aiStatsState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                is AiStatsUiState.Success -> {
                    AiStatsReportContent(
                        report = aiStatsState.report,
                        isDarkTheme = isDarkTheme,
                        onSelectTag = onSelectTag
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiStatsReportContent(
    report: AiCampusStatsReport,
    isDarkTheme: Boolean,
    onSelectTag: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Headline + Summary
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isDarkTheme) Color.White.copy(alpha = 0.06f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = report.headline,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = report.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Top Keyword Statistics
        if (report.topKeywords.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "高频关键词 / 标签统计",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                report.topKeywords.take(5).forEachIndexed { idx, stat ->
                    val badgeColor = when (stat.heatLabel) {
                        "飙升" -> VermilionRed
                        "热门" -> Color(0xFFEA580C)
                        "新锐" -> AuroraFuchsia
                        else -> AuroraIndigo
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectTag(stat.keyword.removePrefix("#")) }
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDarkTheme) 0.42f else 0.48f))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${idx + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stat.keyword,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stat.heatLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(badgeColor)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                            if (stat.insight.isNotBlank()) {
                                Text(
                                    text = stat.insight,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Text(
                            text = "${stat.count} 提及",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme) AuroraLightIndigo else AuroraIndigo
                        )
                    }
                }
            }
        }

        // Sentiment / Topic Distribution Bars
        if (report.sentimentBreakdown.isNotEmpty()) {
            val barGradients = listOf(
                Brush.horizontalGradient(listOf(AuroraIndigo, AuroraCyan)),
                Brush.horizontalGradient(listOf(AuroraFuchsia, AuroraIndigo)),
                Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF06B6D4))),
                Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFD946EF)))
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "话题情绪分布",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                report.sentimentBreakdown.take(4).forEachIndexed { idx, share ->
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = share.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${share.percentage}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((share.percentage / 100f).coerceIn(0.04f, 1f))
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(barGradients[idx % barGradients.size])
                            )
                        }
                    }
                }
            }
        }

        // Recommended Searchable Tags
        if (report.recommendedTags.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "AI 推荐检索标签（点击直达）",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    report.recommendedTags.forEach { tag ->
                        MonoTagChip(
                            text = tag,
                            onClick = { onSelectTag(tag.removePrefix("#")) }
                        )
                    }
                }
            }
        }
    }
}
