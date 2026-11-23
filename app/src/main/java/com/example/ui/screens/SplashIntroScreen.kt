package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.SweepGradient
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.floatingColorShadow
import com.example.ui.theme.AuroraBorderGradient
import com.example.ui.theme.AuroraFuchsia
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Full-bleed Aurora Splash Screen (`.hero-aurora`) with:
 * 1. `.blur-text` staggered character blur-in animation on "杨梅树窝" (40sp–56sp, 780 weight, -0.035em tracking)
 * 2. Snappy staggered reveal: Title -> Subtitles -> Orbiting-border transparent `.beam-btn`
 * 3. Deep #07070C backdrop + drifting tri-color aurora glow + tiled noise texture + reduced-motion support.
 */
@Composable
fun SplashIntroScreen(
    onEnterTree: () -> Unit
) {
    val context = LocalContext.current
    val prefersReducedMotion = remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        }.getOrDefault(false)
    }

    // Fast, snappy choreography: Title blur-in (0ms) -> Subtitles (320ms) -> Button (540ms)
    var showSubtitle by remember { mutableStateOf(prefersReducedMotion) }
    var showButton by remember { mutableStateOf(prefersReducedMotion) }
    var isTransitioning by remember { mutableStateOf(false) }

    LaunchedEffect(prefersReducedMotion) {
        if (!prefersReducedMotion) {
            delay(320)
            showSubtitle = true
            delay(220)
            showButton = true
        }
    }

    // 16s ease-in-out alternate infinite aurora drift (disabled under reduced motion)
    val driftProgress: Float = if (prefersReducedMotion) {
        0.35f
    } else {
        val infinite = rememberInfiniteTransition(label = "hero_aurora_drift")
        val anim by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 16000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "aurora_drift_t"
        )
        anim
    }

    val exitAlpha by animateFloatAsState(
        targetValue = if (isTransitioning) 0f else 1f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "splash_exit_alpha"
    )

    LaunchedEffect(isTransitioning) {
        if (isTransitioning) {
            delay(280)
            onEnterTree()
        }
    }

    // Pre-computed 120x120 tiled SVG-turbulence-style film-grain noise shader
    val noiseBrush = remember { createTiledNoiseBrush(sizePx = 120) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF07070C),
                        Color(0xFF0C0D17),
                        Color(0xFF07070C)
                    )
                )
            )
            .alpha(exitAlpha)
            .testTag("splash_intro_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Refined, smaller editorial title scale for 4 CJK characters on mobile
        val titleFontSize = (maxWidth.value * 0.092f).coerceIn(30f, 38f).sp

        // Layer 1: Oversized drifting tri-color aurora glow (.hero-aurora__glow)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(64.dp)
        ) {
            val w = size.width
            val h = size.height
            val glowW = w * 1.8f
            val glowH = h * 1.8f
            val originX = -w * 0.4f
            val originY = -h * 0.4f

            val tx = w * (-0.02f + 0.05f * driftProgress)
            val ty = h * (-0.01f + 0.03f * driftProgress)
            val rotationDeg = 6f * driftProgress

            withTransform({
                translate(left = tx, top = ty)
                rotate(degrees = rotationDeg, pivot = Offset(w * 0.5f, h * 0.5f))
            }) {
                // Orb 1: Indigo at (20%, 30%)
                val c1 = Offset(originX + glowW * 0.20f, originY + glowH * 0.30f)
                val r1 = maxOf(glowW, glowH) * 0.42f
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFF6366F1).copy(alpha = 0.90f),
                            0.55f to Color(0xFF6366F1).copy(alpha = 0.32f),
                            1.0f to Color.Transparent
                        ),
                        center = c1,
                        radius = r1
                    ),
                    radius = r1,
                    center = c1
                )

                // Orb 2: Fuchsia at (75%, 25%)
                val c2 = Offset(originX + glowW * 0.75f, originY + glowH * 0.25f)
                val r2 = maxOf(glowW, glowH) * 0.40f
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFFD946EF).copy(alpha = 0.60f),
                            0.55f to Color(0xFFD946EF).copy(alpha = 0.22f),
                            1.0f to Color.Transparent
                        ),
                        center = c2,
                        radius = r2
                    ),
                    radius = r2,
                    center = c2
                )

                // Orb 3: Cyan at (50%, 80%)
                val c3 = Offset(originX + glowW * 0.50f, originY + glowH * 0.80f)
                val r3 = maxOf(glowW, glowH) * 0.45f
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFF06B6D4).copy(alpha = 0.55f),
                            0.55f to Color(0xFF06B6D4).copy(alpha = 0.20f),
                            1.0f to Color.Transparent
                        ),
                        center = c3,
                        radius = r3
                    ),
                    radius = r3,
                    center = c3
                )
            }
        }

        // Layer 2: Film-grain noise overlay (.hero-aurora__noise)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(brush = noiseBrush, alpha = 0.16f)
        }

        // Layer 3: Transparent Logo + Staggered Outline-Stack Blur Title -> Unified Subtitles -> Orbiting Beam Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 0) Transparent App Brand Logo (black square background cleanly removed)
            val logoAlpha = remember(prefersReducedMotion) { Animatable(if (prefersReducedMotion) 1f else 0f) }
            LaunchedEffect(prefersReducedMotion) {
                if (!prefersReducedMotion) {
                    logoAlpha.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
                }
            }
            com.example.ui.components.YangmeiTransparentLogo(
                size = 108.dp,
                onDarkSurface = true,
                modifier = Modifier.alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1) Flowing `.grad-text` (800 weight, -0.04em, 4.2s linear 0%->200% seamless gradient)
            com.example.ui.components.BlurGradientFlowTitle(
                text = "杨梅树窝",
                fontSize = titleFontSize,
                onDarkSurface = true,
                prefersReducedMotion = prefersReducedMotion
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2) Subtitle lines appear next — identical font size & color across both lines
            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(tween(340, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        initialOffsetY = { 14 },
                        animationSpec = tween(340, easing = FastOutSlowInEasing)
                    )
            ) {
                val unifiedSubtitleStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.5.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.Normal
                )
                val unifiedSubtitleColor = Color.White.copy(alpha = 0.76f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "在杨梅树下，聊聊央美的生活。",
                        style = unifiedSubtitleStyle,
                        color = unifiedSubtitleColor,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "关于学习、生活、艺术、情绪，和只有央美人懂的事情",
                        style = unifiedSubtitleStyle,
                        color = unifiedSubtitleColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            // 3) Transparent `.beam-btn` appears last with continuously orbiting border beam
            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn(tween(340, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(340, easing = FastOutSlowInEasing)
                    )
            ) {
                BeamEnterButton(
                    text = "进入树窝",
                    prefersReducedMotion = prefersReducedMotion,
                    onClick = { isTransitioning = true },
                    modifier = Modifier.testTag("splash_enter_button")
                )
            }
        }
    }
}

/**
 * Compose translation of `.blur-text`:
 * Splits the title into individual characters (4 units <= 20), animating each with
 * 80ms stagger, opacity 0 -> 1, and blur 9dp -> 0dp (ease-out).
 * Provides merged screen-reader semantics for the full phrase.
 */
@Composable
private fun BlurInTitleText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    prefersReducedMotion: Boolean
) {
    val chars = remember(text) { text.toList() }

    Row(
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = text
        },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        chars.forEachIndexed { index, ch ->
            val progress = remember(index, prefersReducedMotion) {
                Animatable(if (prefersReducedMotion) 1f else 0f)
            }
            LaunchedEffect(index, prefersReducedMotion) {
                if (prefersReducedMotion) {
                    progress.snapTo(1f)
                } else {
                    delay(index * 80L)
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 520, easing = CubicBezierEasing(0f, 0f, 0.2f, 1f))
                    )
                }
            }
            val t = progress.value
            val blurRadius = ((1f - t) * 9f).dp

            Text(
                text = ch.toString(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = fontSize,
                    lineHeight = fontSize * 1.12f,
                    fontWeight = FontWeight(780),
                    letterSpacing = (-0.035).em,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.42f * t),
                        offset = Offset(0f, 3f),
                        blurRadius = 20f
                    )
                ),
                color = Color.White,
                modifier = Modifier
                    .alpha(t)
                    .then(if (blurRadius > 0.2.dp) Modifier.blur(blurRadius) else Modifier)
            )
        }
    }
}

/**
 * Transparent-core `.beam-btn` with continuously orbiting cyan -> fuchsia light beam around
 * the rounded border using native SweepGradient + LocalMatrix rotation on a fixed Stroke.
 * Zero black interior fill so the aurora backdrop shines cleanly right through the button.
 */
@Composable
fun BeamEnterButton(
    text: String,
    prefersReducedMotion: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    // Continuous smooth 360-degree border orbit (pins at 135deg under reduced motion)
    val beamAngle: Float = if (prefersReducedMotion) {
        135f
    } else {
        val infinite = rememberInfiniteTransition(label = "beam_orbit")
        val angle by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = if (isHovered || isPressed) 1600 else 2800,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "beam_orbit_angle"
        )
        angle
    }

    val outerFocusModifier = if (isFocused) {
        Modifier
            .border(2.dp, Color(0xFF22D3EE), RoundedCornerShape(15.dp))
            .padding(3.dp)
    } else {
        Modifier
    }

    val sweepColors = remember {
        intArrayOf(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT,
            0xFF22D3EE.toInt(),
            0xFFC026D3.toInt(),
            android.graphics.Color.TRANSPARENT
        )
    }
    val sweepPositions = remember {
        floatArrayOf(0.0f, 0.64f, 0.80f, 0.93f, 1.0f)
    }

    Box(
        modifier = modifier
            .then(outerFocusModifier)
            .clip(RoundedCornerShape(12.dp))
            .hoverable(interactionSource)
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color(0xFF22D3EE)),
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Orbiting conic-gradient light beam drawn purely on the rounded border Stroke
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokePx = 1.8.dp.toPx()
            val half = strokePx / 2f
            val cornerPx = 12.dp.toPx()
            val rectSize = Size(
                (size.width - strokePx).coerceAtLeast(1f),
                (size.height - strokePx).coerceAtLeast(1f)
            )

            // Resting translucent hairline border so the button edge is always defined
            drawRoundRect(
                color = Color.White.copy(alpha = 0.24f),
                topLeft = Offset(half, half),
                size = rectSize,
                cornerRadius = CornerRadius(cornerPx, cornerPx),
                style = Stroke(width = 1.dp.toPx())
            )

            // Native SweepGradient rotated smoothly around the button center via local matrix
            val cx = size.width / 2f
            val cy = size.height / 2f
            val shader = SweepGradient(cx, cy, sweepColors, sweepPositions)
            val matrix = Matrix().apply { postRotate(beamAngle, cx, cy) }
            shader.setLocalMatrix(matrix)
            val rotatedBrush = ShaderBrush(shader)

            // Soft outer neon halo following the orbiting beam
            drawRoundRect(
                brush = rotatedBrush,
                topLeft = Offset(half, half),
                size = rectSize,
                cornerRadius = CornerRadius(cornerPx, cornerPx),
                alpha = 0.36f,
                style = Stroke(width = 4.5.dp.toPx())
            )

            // Razor-sharp orbiting beam border
            drawRoundRect(
                brush = rotatedBrush,
                topLeft = Offset(half, half),
                size = rectSize,
                cornerRadius = CornerRadius(cornerPx, cornerPx),
                style = Stroke(width = strokePx)
            )
        }

        // Transparent inner span (zero black background)
        Box(
            modifier = Modifier
                .padding(2.dp)
                .clip(RoundedCornerShape(10.dp))
                .height(44.dp)
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 15.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.35f),
                        offset = Offset(0f, 1.5f),
                        blurRadius = 8f
                    )
                ),
                color = Color.White
            )
        }
    }
}

private fun createTiledNoiseBrush(sizePx: Int): ShaderBrush {
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val rng = Random(2026)
    val pixels = IntArray(sizePx * sizePx)
    for (i in pixels.indices) {
        val lum = rng.nextInt(90, 256)
        val a = rng.nextInt(40, 175)
        pixels[i] = (a shl 24) or (lum shl 16) or (lum shl 8) or lum
    }
    bmp.setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
    val shader = ImageShader(
        image = bmp.asImageBitmap(),
        tileModeX = TileMode.Repeated,
        tileModeY = TileMode.Repeated
    )
    return ShaderBrush(shader)
}
