package com.example.ui.components

import android.graphics.BlurMaskFilter
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Helper to check Android system animator duration scale (prefers-reduced-motion equivalent).
 */
@Composable
fun rememberPrefersReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        }.getOrDefault(false)
    }
}

/**
 * Draws a soft, wide CSS-style colored floating shadow behind a rounded box:
 * e.g., `box-shadow: 0 12px 32px -12px rgba(217, 70, 239, .45)`.
 */
fun Modifier.floatingColorShadow(
    color: Color,
    cornerRadius: Dp = 16.dp,
    offsetY: Dp = 10.dp,
    blurRadius: Dp = 22.dp,
    spreadInset: Dp = 8.dp
): Modifier = this.drawBehind {
    if (color.alpha <= 0.005f || size.width <= 0f || size.height <= 0f) return@drawBehind
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.isAntiAlias = true
        frameworkPaint.color = color.toArgb()
        val blurPx = blurRadius.toPx().coerceAtLeast(1f)
        frameworkPaint.maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)

        val insetPx = spreadInset.toPx().coerceAtMost(minOf(size.width, size.height) * 0.35f)
        val dy = offsetY.toPx()
        val r = cornerRadius.toPx()

        canvas.drawRoundRect(
            left = insetPx,
            top = insetPx + dy,
            right = (size.width - insetPx).coerceAtLeast(insetPx + 1f),
            bottom = (size.height - insetPx * 0.4f + dy).coerceAtLeast(insetPx + dy + 1f),
            radiusX = r,
            radiusY = r,
            paint = paint
        )
    }
}

/**
 * Compose translation of `.gb-card` — unified gradient-border card across all pages:
 * - Outer radius `outerCornerRadius` (default 16dp) with `borderWidth` (default 2dp) 135deg gradient
 *   (#6366F1 -> #D946EF 50% -> #06B6D4)
 * - Inner radius = `outerCornerRadius - borderWidth` (14dp) so corners never leak color
 * - Floating fuchsia shadow: `0 12px 32px -12px rgba(217, 70, 239, .45)` resting,
 *   intensifying to `.60` alpha and deeper blur on hover/press
 * - Focus ring: 2dp #4F46E5 with 3dp offset
 * - Respects prefers-reduced-motion
 */
@Composable
fun GradientBorderCard(
    modifier: Modifier = Modifier,
    outerCornerRadius: Dp = 16.dp,
    borderWidth: Dp = 2.dp,
    innerBackground: Color = MaterialTheme.colorScheme.surface,
    innerBackgroundBrush: Brush? = null,
    shadowStrength: Float = 1f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val prefersReducedMotion = rememberPrefersReducedMotion()
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isElevated = isHovered || isPressed

    val targetShadowAlpha = (if (isElevated) 0.44f else 0.28f) * shadowStrength
    val shadowAlpha by animateFloatAsState(
        targetValue = targetShadowAlpha,
        animationSpec = if (prefersReducedMotion) snap() else tween(200, easing = FastOutSlowInEasing),
        label = "gb_card_shadow_alpha"
    )
    val shadowBlur by animateDpAsState(
        targetValue = if (isElevated) 26.dp else 20.dp,
        animationSpec = if (prefersReducedMotion) snap() else tween(200, easing = FastOutSlowInEasing),
        label = "gb_card_shadow_blur"
    )
    val shadowOffsetY by animateDpAsState(
        targetValue = if (isElevated) 13.dp else 10.dp,
        animationSpec = if (prefersReducedMotion) snap() else tween(200, easing = FastOutSlowInEasing),
        label = "gb_card_shadow_y"
    )

    val innerCornerRadius = (outerCornerRadius - borderWidth).coerceAtLeast(0.dp)
    val outerShape = RoundedCornerShape(outerCornerRadius)
    val innerShape = RoundedCornerShape(innerCornerRadius)

    val focusModifier = if (isFocused) {
        Modifier
            .border(2.dp, AuroraFocusIndigo, RoundedCornerShape(outerCornerRadius + 3.dp))
            .padding(3.dp)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(focusModifier)
            .floatingColorShadow(
                color = AuroraFuchsia.copy(alpha = shadowAlpha),
                cornerRadius = outerCornerRadius,
                offsetY = shadowOffsetY,
                blurRadius = shadowBlur,
                spreadInset = 9.dp
            )
            .clip(outerShape)
            .background(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to AuroraIndigo,
                        0.5f to AuroraFuchsia,
                        1.0f to AuroraCyan
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .then(
                if (onClick != null) {
                    Modifier
                        .hoverable(interactionSource)
                        .focusable(interactionSource = interactionSource)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(color = AuroraIndigo),
                            role = Role.Button,
                            onClick = onClick
                        )
                } else {
                    Modifier.hoverable(interactionSource)
                }
            )
            .padding(borderWidth)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(innerShape)
                .then(
                    if (innerBackgroundBrush != null) Modifier.background(innerBackgroundBrush)
                    else Modifier.background(innerBackground)
                ),
            content = content
        )
    }
}

/**
 * Single direct-action floating '+' button (FAB):
 * - 58dp rounded-16dp squircle, 135deg brand aurora gradient + white '+' icon
 * - Wide floating same-hue shadow (`0 12px 28px -8px` level)
 * - Spring entrance / smooth exit when scrolled to the bottom of long feeds
 */
@Composable
fun AuroraFloatingPlusButton(
    visible: Boolean,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefersReducedMotion = rememberPrefersReducedMotion()

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = if (prefersReducedMotion) fadeIn(snap()) else slideInVertically(
            initialOffsetY = { it + 48 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + scaleIn(
            initialScale = 0.72f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(tween(220)),
        exit = if (prefersReducedMotion) fadeOut(snap()) else slideOutVertically(
            targetOffsetY = { it + 40 },
            animationSpec = tween(220, easing = FastOutSlowInEasing)
        ) + scaleOut(targetScale = 0.78f) + fadeOut(tween(180))
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .floatingColorShadow(
                    color = AuroraFuchsia.copy(alpha = 0.52f),
                    cornerRadius = 16.dp,
                    offsetY = 12.dp,
                    blurRadius = 26.dp,
                    spreadInset = 6.dp
                )
                .floatingColorShadow(
                    color = AuroraIndigo.copy(alpha = 0.34f),
                    cornerRadius = 16.dp,
                    offsetY = 10.dp,
                    blurRadius = 20.dp,
                    spreadInset = 5.dp
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to AuroraIndigo,
                            0.5f to AuroraFuchsia,
                            1.0f to AuroraCyan
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    )
                )
                .clickable(
                    role = Role.Button,
                    onClick = onClick
                )
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

data class SpeedDialActionItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * Single floating action button (FAB) with expandable Speed Dial:
 * - 58dp rounded-16dp squircle, 135deg brand aurora gradient fill + white icon
 * - Wide floating same-hue shadow (`0 12px 28px -8px` level)
 * - Scroll-driven visibility: hidden at initial hero/top viewport, springs in after 600px scroll,
 *   auto-tucks near bottom so last content is never obscured.
 * - Expands into 2-3 mini FABs (speed dial) when tapped.
 */
@Composable
fun AuroraSpeedDialFab(
    visible: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    actions: List<SpeedDialActionItem>,
    modifier: Modifier = Modifier
) {
    val prefersReducedMotion = rememberPrefersReducedMotion()

    LaunchedEffect(visible) {
        if (!visible && expanded) {
            onExpandedChange(false)
        }
    }

    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = if (prefersReducedMotion) snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fab_icon_rotate"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Expandable mini speed-dial items
        AnimatedVisibility(
            visible = visible && expanded,
            enter = if (prefersReducedMotion) fadeIn(snap()) else fadeIn(tween(180)) +
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + scaleIn(initialScale = 0.82f),
            exit = if (prefersReducedMotion) fadeOut(snap()) else fadeOut(tween(140)) +
                slideOutVertically(targetOffsetY = { it / 3 }) + scaleOut(targetScale = 0.85f)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                actions.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .clickable {
                                onExpandedChange(false)
                                item.onClick()
                            }
                            .testTag("speed_dial_${item.id}")
                    ) {
                        // Label pill styled with unified .gb-card gradient border
                        Box(
                            modifier = Modifier
                                .floatingColorShadow(
                                    color = AuroraFuchsia.copy(alpha = 0.28f),
                                    cornerRadius = 12.dp,
                                    offsetY = 6.dp,
                                    blurRadius = 14.dp,
                                    spreadInset = 4.dp
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .background(AuroraBorderGradient)
                                .padding(1.5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.5.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Mini gradient FAB (46dp)
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .floatingColorShadow(
                                    color = AuroraFuchsia.copy(alpha = 0.45f),
                                    cornerRadius = 14.dp,
                                    offsetY = 8.dp,
                                    blurRadius = 18.dp,
                                    spreadInset = 4.dp
                                )
                                .clip(RoundedCornerShape(14.dp))
                                .background(AuroraBorderGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Primary Floating Action Button (58dp, 16dp rounded, floating 0 12px 28px -8px shadow)
        AnimatedVisibility(
            visible = visible,
            enter = if (prefersReducedMotion) fadeIn(snap()) else slideInVertically(
                initialOffsetY = { it + 48 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + scaleIn(
                initialScale = 0.72f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(tween(220)),
            exit = if (prefersReducedMotion) fadeOut(snap()) else slideOutVertically(
                targetOffsetY = { it + 40 },
                animationSpec = tween(220, easing = FastOutSlowInEasing)
            ) + scaleOut(targetScale = 0.78f) + fadeOut(tween(180))
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .floatingColorShadow(
                        color = AuroraFuchsia.copy(alpha = 0.52f),
                        cornerRadius = 16.dp,
                        offsetY = 12.dp,
                        blurRadius = 26.dp,
                        spreadInset = 6.dp
                    )
                    .floatingColorShadow(
                        color = AuroraIndigo.copy(alpha = 0.34f),
                        cornerRadius = 16.dp,
                        offsetY = 10.dp,
                        blurRadius = 20.dp,
                        spreadInset = 5.dp
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colorStops = arrayOf(
                                0.0f to AuroraIndigo,
                                0.5f to AuroraFuchsia,
                                1.0f to AuroraCyan
                            ),
                            start = Offset.Zero,
                            end = Offset.Infinite
                        )
                    )
                    .clickable(
                        role = Role.Button,
                        onClick = { onExpandedChange(!expanded) }
                    )
                    .testTag("main_publish_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = if (expanded) "收起快捷操作" else "展开快捷发布菜单",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(iconRotation)
                )
            }
        }
    }
}

data class AuroraNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Floating glass 4-button Bottom Navigation Bar unified with the Aurora + `.gb-card` aesthetic.
 * Outer 22dp radius + 1.8dp aurora gradient border -> inner 20.2dp frosted surface + floating shadow.
 */
@Composable
fun AuroraBottomNavigationBar(
    tabs: List<AuroraNavTab>,
    selectedRoute: String?,
    unreadCount: Int,
    onSelectTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val barSurface = if (isDark) DarkPaper.copy(alpha = 0.94f) else Color.White.copy(alpha = 0.95f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .floatingColorShadow(
                    color = AuroraFuchsia.copy(alpha = 0.28f),
                    cornerRadius = 22.dp,
                    offsetY = 10.dp,
                    blurRadius = 24.dp,
                    spreadInset = 8.dp
                )
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to AuroraIndigo,
                            0.5f to AuroraFuchsia,
                            1.0f to AuroraCyan
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    )
                )
                .padding(1.8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.2.dp))
                    .background(barSurface)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val selected = selectedRoute == tab.route
                    val itemShape = RoundedCornerShape(15.dp)

                    val activeModifier = if (selected) {
                        Modifier
                            .clip(itemShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        AuroraIndigo.copy(alpha = 0.14f),
                                        AuroraFuchsia.copy(alpha = 0.13f),
                                        AuroraCyan.copy(alpha = 0.14f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        AuroraIndigo.copy(alpha = 0.55f),
                                        AuroraFuchsia.copy(alpha = 0.50f),
                                        AuroraCyan.copy(alpha = 0.55f)
                                    )
                                ),
                                shape = itemShape
                            )
                    } else {
                        Modifier.clip(itemShape)
                    }

                    val activeTint = if (isDark) AuroraLightIndigo else AuroraFocusIndigo

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp)
                            .then(activeModifier)
                            .clickable(
                                role = Role.Tab,
                                onClick = { onSelectTab(tab.route) }
                            )
                            .padding(vertical = 6.dp)
                            .testTag("bottom_nav_${tab.route}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                                tint = if (selected) activeTint else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            if (tab.route == "messages" && unreadCount > 0) {
                                RedNumberBadge(
                                    count = unreadCount,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 10.dp, y = (-5).dp)
                                        .testTag("bottom_nav_messages_badge")
                                )
                            }
                        }

                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) activeTint else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Glowing aurora accent bar under active tab
                        Box(
                            modifier = Modifier
                                .height(2.5.dp)
                                .width(if (selected) 18.dp else 0.dp)
                                .clip(CircleShape)
                                .background(AuroraBorderGradient)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Scroll-driven FAB visibility observer:
 * - Visible immediately so users can always publish without hunting for the '+' button
 * - Auto-tucks cleanly when scrolled all the way to the bottom of a long feed so the final card is unobstructed
 */
@Composable
fun ObserveScrollFabVisibility(
    listState: LazyListState,
    onFabVisibilityChange: (Boolean) -> Unit
) {
    val shouldShowFab by remember(listState) {
        derivedStateOf {
            val isLongFeedAtVeryBottom =
                listState.layoutInfo.totalItemsCount > 5 &&
                    listState.firstVisibleItemIndex >= 2 &&
                    !listState.canScrollForward &&
                    listState.canScrollBackward
            !isLongFeedAtVeryBottom
        }
    }

    LaunchedEffect(shouldShowFab) {
        onFabVisibilityChange(shouldShowFab)
    }
}

/**
 * Crisp red numbered unread badge ("1".."99+") for Messages tab and comment/like filters.
 */
@Composable
fun RedNumberBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count <= 0) return
    val label = if (count > 99) "99+" else count.toString()
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 17.dp, minHeight = 17.dp)
            .clip(CircleShape)
            .background(VermilionRed)
            .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
            .padding(horizontal = if (label.length > 1) 5.dp else 4.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.Bold
            ),
            color = PaperWhite,
            maxLines = 1
        )
    }
}

