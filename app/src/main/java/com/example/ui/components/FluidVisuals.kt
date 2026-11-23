package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CategoryAccent
import com.example.data.IdentityType
import com.example.data.PostCategory
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modern futuristic fluid-gray generative surface ("流动的灰色空间").
 * Smooth metallic silver-gray liquid ribbons and soft luminous depth without visual clutter.
 */
@Composable
fun FluidGrayHeroCanvas(
    modifier: Modifier = Modifier,
    darkVariant: Boolean = false,
    seed: Int = 1,
    showGrid: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fluid_gray")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fluid_phase"
    )

    val isDarkTheme = MaterialTheme.colorScheme.background.red < 0.2f

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Unified Aurora fluid canvas — replaces jarring dark charcoal/slate blocks with cohesive brand aurora tones
        val baseBrush = when {
            darkVariant -> Brush.linearGradient(
                colors = listOf(Color(0xFF5458E8), Color(0xFFA84AE0), Color(0xFF0E9BB8)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
            isDarkTheme -> Brush.linearGradient(
                colors = listOf(Color(0xFF1B1F3A), Color(0xFF261C40), Color(0xFF16283E)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
            else -> Brush.linearGradient(
                colors = listOf(Color(0xFFF3F2FF), Color(0xFFFAF0FF), Color(0xFFECFAFD)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        }
        drawRect(brush = baseBrush)

        // Luminous aurora spheres drifting slowly
        val driftX = cos(phase + seed) * w * 0.08f
        val driftY = sin(phase * 0.8f + seed * 0.6f) * h * 0.07f

        val orb1 = when {
            darkVariant -> Color(0xFF818CF8).copy(alpha = 0.45f)
            isDarkTheme -> AuroraIndigo.copy(alpha = 0.28f)
            else -> AuroraIndigo.copy(alpha = 0.16f)
        }
        val orb2 = when {
            darkVariant -> Color(0xFFE879F9).copy(alpha = 0.36f)
            isDarkTheme -> AuroraFuchsia.copy(alpha = 0.22f)
            else -> AuroraFuchsia.copy(alpha = 0.13f)
        }
        val orb3 = when {
            darkVariant -> Color(0xFF22D3EE).copy(alpha = 0.34f)
            isDarkTheme -> AuroraCyan.copy(alpha = 0.22f)
            else -> AuroraCyan.copy(alpha = 0.15f)
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(orb1, Color.Transparent),
                center = Offset(w * 0.78f + driftX, h * 0.24f + driftY),
                radius = w * 0.72f
            ),
            radius = w * 0.72f,
            center = Offset(w * 0.78f + driftX, h * 0.24f + driftY)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(orb2, Color.Transparent),
                center = Offset(w * 0.22f - driftX, h * 0.74f - driftY),
                radius = w * 0.68f
            ),
            radius = w * 0.68f,
            center = Offset(w * 0.22f - driftX, h * 0.74f - driftY)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(orb3, Color.Transparent),
                center = Offset(w * 0.52f + driftY, h * 0.5f - driftX * 0.5f),
                radius = w * 0.55f
            ),
            radius = w * 0.55f,
            center = Offset(w * 0.52f + driftY, h * 0.5f - driftX * 0.5f)
        )

        // Translucent liquid ribbon silk bands
        val ribbonFill = if (darkVariant || isDarkTheme) Color.White.copy(alpha = 0.06f) else AuroraIndigo.copy(alpha = 0.045f)
        val ribbonPath = Path().apply {
            moveTo(-w * 0.1f, h * 0.62f)
            cubicTo(
                w * 0.28f, h * (0.35f + sin(phase + seed).toFloat() * 0.07f),
                w * 0.65f, h * (0.82f + cos(phase + seed).toFloat() * 0.06f),
                w * 1.1f, h * 0.44f
            )
            lineTo(w * 1.1f, h * 0.68f)
            cubicTo(
                w * 0.68f, h * (0.98f + cos(phase + seed).toFloat() * 0.05f),
                w * 0.30f, h * (0.56f + sin(phase + seed).toFloat() * 0.06f),
                -w * 0.1f, h * 0.84f
            )
            close()
        }
        drawPath(path = ribbonPath, color = ribbonFill)

        // Flowing fluid contour strokes
        val lineColor = if (darkVariant || isDarkTheme) Color.White.copy(alpha = 0.18f) else AuroraIndigo.copy(alpha = 0.12f)
        for (layer in 0..5) {
            val path = Path()
            val yBase = h * (0.20f + layer * 0.125f)
            val amp = h * (0.036f + layer * 0.008f)
            path.moveTo(-20f, yBase)
            val steps = 28
            for (s in 0..steps) {
                val t = s.toFloat() / steps
                val x = t * (w + 40f) - 20f
                val wave = sin(t * PI * 2.0 + phase * 0.65 + layer * 0.55 + seed) * amp +
                    cos(t * PI * 3.1 - phase * 0.4 + layer * 0.8) * (amp * 0.42f)
                path.lineTo(x, (yBase + wave).toFloat())
            }
            drawPath(
                path = path,
                color = lineColor.copy(alpha = if (layer == 2 || layer == 3) lineColor.alpha * 1.45f else lineColor.alpha * 0.7f),
                style = Stroke(width = if (layer == 2) 2.2f else 1.0f, cap = StrokeCap.Round)
            )
        }

        if (showGrid) {
            val gridColor = if (darkVariant || isDarkTheme) Color.White.copy(alpha = 0.05f) else AuroraIndigo.copy(alpha = 0.04f)
            for (i in 1..4) {
                val x = w * (i / 5f)
                drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
            }
        }
    }
}

private fun avatarHash(input: Int): Int {
    var x = input xor 0x27D4EB2D
    x = (x xor (x ushr 15)) * 0x85EBCA6B.toInt()
    x = (x xor (x ushr 13)) * 0xC2B2AE35.toInt()
    return x xor (x ushr 16)
}

private data class AvatarPalette(
    val bgStart: Color,
    val bgEnd: Color,
    val primary: Color,
    val secondary: Color,
    val accent: Color
)

private val avatarPalettes = listOf(
    AvatarPalette(Color(0xFFE0E7FF), Color(0xFFF3E8FF), Color(0xFF4338CA), Color(0xFF818CF8), Color(0xFFF43F5E)),
    AvatarPalette(Color(0xFFFFEDD5), Color(0xFFFCE7F3), Color(0xFFEA580C), Color(0xFFFB7185), Color(0xFF6366F1)),
    AvatarPalette(Color(0xFFD1FAE5), Color(0xFFE0F2FE), Color(0xFF047857), Color(0xFF38BDF8), Color(0xFFF59E0B)),
    AvatarPalette(Color(0xFFFEF3C7), Color(0xFFFFE4E6), Color(0xFFB45309), Color(0xFFF472B6), Color(0xFF0EA5E9)),
    AvatarPalette(Color(0xFFE0F2FE), Color(0xFFEDE9FE), Color(0xFF0369A1), Color(0xFFA78BFA), Color(0xFF10B981)),
    AvatarPalette(Color(0xFFFCE7F3), Color(0xFFE0E7FF), Color(0xFFBE185D), Color(0xFF6366F1), Color(0xFF06B6D4)),
    AvatarPalette(Color(0xFFECFCCB), Color(0xFFD1FAE5), Color(0xFF3F6212), Color(0xFF34D399), Color(0xFFEC4899)),
    AvatarPalette(Color(0xFFEDE9FE), Color(0xFFCFFAFE), Color(0xFF6D28D9), Color(0xFF22D3EE), Color(0xFFF97316)),
    AvatarPalette(Color(0xFFFFE4E6), Color(0xFFFEF9C3), Color(0xFF9F1239), Color(0xFFFBBF24), Color(0xFF4F46E5)),
    AvatarPalette(Color(0xFFCFFAFE), Color(0xFFE0E7FF), Color(0xFF0E7490), Color(0xFF818CF8), Color(0xFFE11D48)),
    AvatarPalette(Color(0xFFF3E8FF), Color(0xFFFFEDD5), Color(0xFF7E22CE), Color(0xFFFB923C), Color(0xFF14B8A6)),
    AvatarPalette(Color(0xFFDCFCE7), Color(0xFFFEF3C7), Color(0xFF166534), Color(0xFFF59E0B), Color(0xFF9333EA))
)

/**
 * Unique Procedural Art-School Anonymous Avatar.
 * Deterministically combines 12 gradient palettes x 10 distinct character/art motifs x sub-variations
 * via avalanche hashing so every single user automatically receives a completely different look.
 */
@Composable
fun AnonymousAvatar(
    seed: Int,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val h1 = kotlin.math.abs(avatarHash(seed))
    val h2 = kotlin.math.abs(avatarHash(seed * 31 + 97))
    val h3 = kotlin.math.abs(avatarHash(seed * 131 - 53))

    val palette = avatarPalettes[h1 % avatarPalettes.size]
    val motif = h2 % 10
    val variant = h3 % 4

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .border(0.7.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Soft diagonal gradient canvas unique to palette
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(palette.bgStart, palette.bgEnd),
                    start = if (variant % 2 == 0) Offset.Zero else Offset(w, 0f),
                    end = if (variant % 2 == 0) Offset(w, h) else Offset(0f, h)
                )
            )

            when (motif) {
                0 -> {
                    // Bauhaus Minimalist Portrait with beret
                    drawCircle(palette.primary, radius = w * 0.28f, center = Offset(w * 0.5f, h * 0.56f))
                    drawArc(
                        color = palette.accent,
                        startAngle = 195f,
                        sweepAngle = 165f,
                        useCenter = true,
                        topLeft = Offset(w * 0.18f, h * 0.16f),
                        size = Size(w * 0.64f, h * 0.42f)
                    )
                    drawCircle(Color.White, radius = w * 0.05f, center = Offset(w * 0.41f, h * 0.54f))
                    drawCircle(Color.White, radius = w * 0.05f, center = Offset(w * 0.59f, h * 0.54f))
                    drawLine(
                        palette.bgStart,
                        Offset(w * 0.43f, h * 0.67f),
                        Offset(w * 0.57f, h * 0.67f),
                        strokeWidth = w * 0.045f,
                        cap = StrokeCap.Round
                    )
                }
                1 -> {
                    // Studio Cat character
                    val leftEar = Path().apply {
                        moveTo(w * 0.22f, h * 0.46f); lineTo(w * 0.28f, h * 0.16f); lineTo(w * 0.46f, h * 0.34f); close()
                    }
                    val rightEar = Path().apply {
                        moveTo(w * 0.78f, h * 0.46f); lineTo(w * 0.72f, h * 0.16f); lineTo(w * 0.54f, h * 0.34f); close()
                    }
                    drawPath(leftEar, palette.primary)
                    drawPath(rightEar, palette.primary)
                    drawCircle(palette.primary, radius = w * 0.30f, center = Offset(w * 0.5f, h * 0.58f))
                    drawCircle(Color.White, radius = w * 0.055f, center = Offset(w * 0.39f, h * 0.54f))
                    drawCircle(Color.White, radius = w * 0.055f, center = Offset(w * 0.61f, h * 0.54f))
                    drawCircle(palette.accent, radius = w * 0.045f, center = Offset(w * 0.5f, h * 0.63f))
                }
                2 -> {
                    // Orbiting Planet & Crescent Stars
                    drawCircle(palette.primary, radius = w * 0.26f, center = Offset(w * 0.48f, h * 0.52f))
                    drawOval(
                        color = palette.accent,
                        topLeft = Offset(w * 0.08f, h * 0.38f),
                        size = Size(w * 0.84f, h * 0.26f),
                        style = Stroke(width = w * 0.06f)
                    )
                    drawCircle(palette.secondary, radius = w * 0.08f, center = Offset(w * 0.78f, h * 0.24f))
                    drawCircle(Color.White, radius = w * 0.04f, center = Offset(w * 0.22f, h * 0.24f))
                }
                3 -> {
                    // Modernist Archway & Sunburst
                    val arch = Path().apply {
                        moveTo(w * 0.22f, h)
                        lineTo(w * 0.22f, h * 0.46f)
                        cubicTo(w * 0.22f, h * 0.16f, w * 0.78f, h * 0.16f, w * 0.78f, h * 0.46f)
                        lineTo(w * 0.78f, h)
                        close()
                    }
                    drawPath(arch, palette.primary)
                    drawCircle(palette.accent, radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.48f))
                    drawRect(palette.secondary, topLeft = Offset(w * 0.38f, h * 0.72f), size = Size(w * 0.24f, h * 0.28f))
                }
                4 -> {
                    // Geometric Huajiadi Bird
                    val body = Path().apply {
                        moveTo(w * 0.18f, h * 0.68f)
                        cubicTo(w * 0.22f, h * 0.26f, w * 0.66f, h * 0.20f, w * 0.78f, h * 0.52f)
                        lineTo(w * 0.58f, h * 0.82f)
                        close()
                    }
                    drawPath(body, palette.primary)
                    val beak = Path().apply {
                        moveTo(w * 0.74f, h * 0.40f); lineTo(w * 0.92f, h * 0.47f); lineTo(w * 0.74f, h * 0.54f); close()
                    }
                    drawPath(beak, palette.accent)
                    drawCircle(Color.White, radius = w * 0.055f, center = Offset(w * 0.62f, h * 0.43f))
                    drawCircle(palette.secondary, radius = w * 0.12f, center = Offset(w * 0.42f, h * 0.58f))
                }
                5 -> {
                    // Ceramic Pottery Vase & Leaf
                    val vase = Path().apply {
                        moveTo(w * 0.36f, h * 0.42f)
                        lineTo(w * 0.64f, h * 0.42f)
                        cubicTo(w * 0.80f, h * 0.60f, w * 0.74f, h * 0.88f, w * 0.62f, h * 0.90f)
                        lineTo(w * 0.38f, h * 0.90f)
                        cubicTo(w * 0.26f, h * 0.88f, w * 0.20f, h * 0.60f, w * 0.36f, h * 0.42f)
                        close()
                    }
                    drawPath(vase, palette.primary)
                    drawLine(palette.secondary, Offset(w * 0.5f, h * 0.42f), Offset(w * 0.5f, h * 0.16f), strokeWidth = w * 0.05f, cap = StrokeCap.Round)
                    drawOval(palette.accent, topLeft = Offset(w * 0.50f, h * 0.14f), size = Size(w * 0.26f, h * 0.16f))
                    drawOval(palette.secondary, topLeft = Offset(w * 0.24f, h * 0.20f), size = Size(w * 0.24f, h * 0.14f))
                }
                6 -> {
                    // Painter's Palette & Pigment Dots
                    drawCircle(palette.primary, radius = w * 0.34f, center = Offset(w * 0.5f, h * 0.5f))
                    drawCircle(palette.bgStart, radius = w * 0.09f, center = Offset(w * 0.66f, h * 0.66f))
                    drawCircle(palette.accent, radius = w * 0.08f, center = Offset(w * 0.36f, h * 0.38f))
                    drawCircle(Color.White, radius = w * 0.075f, center = Offset(w * 0.58f, h * 0.34f))
                    drawCircle(palette.secondary, radius = w * 0.08f, center = Offset(w * 0.34f, h * 0.62f))
                }
                7 -> {
                    // Geometric Art Totem Mask
                    val diamond = Path().apply {
                        moveTo(w * 0.5f, h * 0.14f); lineTo(w * 0.84f, h * 0.5f)
                        lineTo(w * 0.5f, h * 0.86f); lineTo(w * 0.16f, h * 0.5f); close()
                    }
                    drawPath(diamond, palette.primary)
                    drawCircle(palette.secondary, radius = w * 0.18f, center = Offset(w * 0.5f, h * 0.5f))
                    drawCircle(palette.accent, radius = w * 0.08f, center = Offset(w * 0.5f, h * 0.5f))
                    drawCircle(Color.White, radius = w * 0.045f, center = Offset(w * 0.5f, h * 0.27f))
                }
                8 -> {
                    // Cozy Bear/Bunny Ears Character
                    drawCircle(palette.secondary, radius = w * 0.14f, center = Offset(w * 0.28f, h * 0.28f))
                    drawCircle(palette.secondary, radius = w * 0.14f, center = Offset(w * 0.72f, h * 0.28f))
                    drawCircle(palette.primary, radius = w * 0.30f, center = Offset(w * 0.5f, h * 0.56f))
                    drawCircle(Color.White, radius = w * 0.05f, center = Offset(w * 0.40f, h * 0.52f))
                    drawCircle(Color.White, radius = w * 0.05f, center = Offset(w * 0.60f, h * 0.52f))
                    drawCircle(palette.accent.copy(alpha = 0.85f), radius = w * 0.06f, center = Offset(w * 0.32f, h * 0.62f))
                    drawCircle(palette.accent.copy(alpha = 0.85f), radius = w * 0.06f, center = Offset(w * 0.68f, h * 0.62f))
                }
                else -> {
                    // Fluid Topographic Hills & Floating Prism
                    val wave1 = Path().apply {
                        moveTo(0f, h * 0.64f)
                        cubicTo(w * 0.32f, h * 0.34f, w * 0.68f, h * 0.82f, w, h * 0.50f)
                        lineTo(w, h); lineTo(0f, h); close()
                    }
                    val wave2 = Path().apply {
                        moveTo(0f, h * 0.78f)
                        cubicTo(w * 0.38f, h * 0.56f, w * 0.70f, h * 0.94f, w, h * 0.68f)
                        lineTo(w, h); lineTo(0f, h); close()
                    }
                    drawPath(wave1, palette.secondary)
                    drawPath(wave2, palette.primary)
                    val prism = Path().apply {
                        moveTo(w * 0.52f, h * 0.14f); lineTo(w * 0.70f, h * 0.34f)
                        lineTo(w * 0.52f, h * 0.48f); lineTo(w * 0.34f, h * 0.34f); close()
                    }
                    drawPath(prism, palette.accent)
                }
            }
        }
    }
}

/**
 * Minimalist geometric emblem for Discover category cards.
 */
@Composable
fun CategoryAbstractEmblem(
    category: PostCategory,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    val accentColor = when (category.accentType) {
        CategoryAccent.ACID -> AcidYellow
        CategoryAccent.GREEN -> FluorescentGreen
        CategoryAccent.RED -> VermilionRed
        CategoryAccent.SILVER -> FluidSilver
        CategoryAccent.MIST -> MidGray
        CategoryAccent.INK -> MaterialTheme.colorScheme.onSurface
    }
    val ink = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        when (category.ordinal % 7) {
            0 -> {
                drawRoundRect(ink, Offset(w * 0.12f, h * 0.15f), Size(w * 0.48f, h * 0.48f), CornerRadius(4f, 4f), style = Stroke(2.2f))
                drawCircle(accentColor, w * 0.17f, Offset(w * 0.72f, h * 0.72f))
            }
            1 -> {
                for (i in 0..2) {
                    drawLine(ink, Offset(w * 0.14f, h * (0.25f + i * 0.25f)), Offset(w * 0.86f, h * (0.25f + i * 0.25f)), 2.4f, cap = StrokeCap.Round)
                }
                drawCircle(accentColor, w * 0.12f, Offset(w * 0.76f, h * 0.25f))
            }
            2 -> {
                drawRoundRect(accentColor.copy(alpha = 0.85f), Offset(w * 0.14f, h * 0.52f), Size(w * 0.72f, h * 0.32f), CornerRadius(4f, 4f))
                drawRoundRect(ink, Offset(w * 0.14f, h * 0.14f), Size(w * 0.44f, h * 0.26f), CornerRadius(4f, 4f), style = Stroke(2.2f))
            }
            3 -> {
                val p = Path().apply {
                    moveTo(w * 0.16f, h * 0.80f)
                    lineTo(w * 0.50f, h * 0.16f)
                    lineTo(w * 0.84f, h * 0.80f)
                    close()
                }
                drawPath(p, ink, style = Stroke(2.2f))
                drawCircle(accentColor, w * 0.12f, Offset(w * 0.50f, h * 0.58f))
            }
            4 -> {
                drawCircle(muted, w * 0.30f, Offset(w * 0.40f, h * 0.50f), style = Stroke(2.2f))
                drawCircle(accentColor, w * 0.20f, Offset(w * 0.66f, h * 0.50f))
            }
            5 -> {
                val wave = Path().apply {
                    moveTo(w * 0.10f, h * 0.56f)
                    cubicTo(w * 0.36f, h * 0.14f, w * 0.64f, h * 0.90f, w * 0.90f, h * 0.42f)
                }
                drawPath(wave, ink, style = Stroke(2.6f, cap = StrokeCap.Round))
                drawCircle(accentColor, w * 0.11f, Offset(w * 0.22f, h * 0.26f))
            }
            else -> {
                drawLine(ink, Offset(w * 0.50f, h * 0.14f), Offset(w * 0.50f, h * 0.86f), 2.3f, cap = StrokeCap.Round)
                drawLine(ink, Offset(w * 0.14f, h * 0.50f), Offset(w * 0.86f, h * 0.50f), 2.3f, cap = StrokeCap.Round)
                drawCircle(accentColor, w * 0.14f, Offset(w * 0.72f, h * 0.72f))
            }
        }
    }
}

@Composable
fun EditorialHairline(
    modifier: Modifier = Modifier,
    thickness: Dp = 0.7.dp,
    color: Color = MaterialTheme.colorScheme.outline
) {
    HorizontalDivider(modifier = modifier, thickness = thickness, color = color)
}

@Composable
fun MonoTagChip(
    text: String,
    modifier: Modifier = Modifier,
    inverted: Boolean = false,
    accentDot: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val fg = if (inverted) PaperWhite else MaterialTheme.colorScheme.onSurfaceVariant
    val bgModifier = if (inverted) {
        Modifier.background(YangmeiPrimaryGradient)
    } else {
        Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(bgModifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (accentDot != null) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(accentDot)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = fg
        )
    }
}

@Composable
fun VerifiedIdentityBadge(
    identityType: IdentityType,
    isVerified: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isVerified) FluorescentGreen else MidGray)
        )
        Text(
            text = if (isVerified) "已认证 · ${identityType.labelCn}" else "未认证",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Clean verification gate dialog shown when an unverified visitor taps an interactive action.
 */
@Composable
fun VerificationGateDialog(
    actionLabel: String,
    onDismiss: () -> Unit,
    onGoVerify: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GradientBorderCard(
            modifier = Modifier.fillMaxWidth(),
            outerCornerRadius = 16.dp,
            borderWidth = 2.dp
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp)
                ) {
                    FluidGrayHeroCanvas(
                        modifier = Modifier.fillMaxSize(),
                        darkVariant = true,
                        seed = 7
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "认证央美身份后即可互动",
                            style = MaterialTheme.typography.headlineMedium,
                            color = PaperWhite
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "「$actionLabel」仅面向通过中央美术学院在读学生或毕业校友认证的成员开放。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "实名核验资料严格保密，社区内仅以独立匿名身份发声。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("gate_dismiss_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("先逛逛")
                        }
                        Button(
                            onClick = onGoVerify,
                            modifier = Modifier
                                .weight(1.3f)
                                .heightIn(min = 48.dp)
                                .testTag("gate_verify_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("去认证", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Progressive Blur Aurora Backdrop echoing the `.hero-aurora` palette (Indigo / Fuchsia / Cyan)
 * for inner screens.
 *
 * Implements the 3-layer progressive blur stack inside an overflow-clipped container,
 * topped with the exact progressive vertical mask:
 *   linear-gradient(to top, fadeColor 4%, fadeColor@92% 26%, fadeColor@35% 54%, transparent 76%)
 * so header titles & cards float cleanly above a luminous artistic atmosphere.
 */
@Composable
fun AuroraProgressiveBlurBackdrop(
    modifier: Modifier = Modifier,
    fadeToColor: Color = MaterialTheme.colorScheme.background,
    seed: Int = 1
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val veilColor = if (isDark) fadeToColor else Color.White

    Box(
        modifier = modifier
            .clipToBounds()
            .background(fadeToColor)
    ) {
        // Layer 1 (Crispest top band): soft aurora base echoing SplashIntroScreen
        AuroraWashLayer(
            modifier = Modifier
                .matchParentSize()
                .blur(12.dp),
            isDark = isDark,
            seed = seed
        )

        // Layer 2 (Mid-band stronger blur — covers lower 68%)
        AuroraWashLayer(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f)
                .align(Alignment.BottomCenter)
                .blur(22.dp),
            isDark = isDark,
            seed = seed
        )

        // Layer 3 (Bottom-band deepest blur — covers lower 40%)
        AuroraWashLayer(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.40f)
                .align(Alignment.BottomCenter)
                .blur(34.dp),
            isDark = isDark,
            seed = seed
        )

        // Progressive linear-gradient veil mask (exact stops: 4% -> 100%, 26% -> 92%, 54% -> 35%, 76% -> 0% from bottom)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Transparent,
                            0.24f to Color.Transparent,
                            0.46f to veilColor.copy(alpha = 0.35f),
                            0.74f to veilColor.copy(alpha = 0.92f),
                            0.94f to veilColor.copy(alpha = 0.98f),
                            1.00f to fadeToColor
                        )
                    )
                )
        )
    }
}

@Composable
private fun AuroraWashLayer(
    modifier: Modifier,
    isDark: Boolean,
    seed: Int
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val shift = ((seed % 5) - 2) * 0.05f

        val indigoAlpha = if (isDark) 0.56f else 0.36f
        val fuchsiaAlpha = if (isDark) 0.40f else 0.25f
        val cyanAlpha = if (isDark) 0.38f else 0.28f

        // Base atmospheric gradient (Dark mode uses exact .hero-aurora #07070C -> #0C0D17 -> #07070C)
        drawRect(
            brush = Brush.linearGradient(
                colors = if (isDark) {
                    listOf(Color(0xFF07070C), Color(0xFF0C0D17), Color(0xFF07070C))
                } else {
                    listOf(Color(0xFFE7EAFA), Color(0xFFF3E8FA), Color(0xFFE3F4FA))
                },
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        // Indigo orb (left-top)
        val c1 = Offset(w * (0.18f + shift), h * 0.28f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF6366F1).copy(alpha = indigoAlpha), Color.Transparent),
                center = c1,
                radius = maxOf(w, h) * 0.65f
            ),
            radius = maxOf(w, h) * 0.65f,
            center = c1
        )

        // Fuchsia orb (right-top)
        val c2 = Offset(w * (0.82f - shift), h * 0.22f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFD946EF).copy(alpha = fuchsiaAlpha), Color.Transparent),
                center = c2,
                radius = maxOf(w, h) * 0.60f
            ),
            radius = maxOf(w, h) * 0.60f,
            center = c2
        )

        // Cyan orb (center-mid)
        val c3 = Offset(w * (0.52f + shift * 0.5f), h * 0.64f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF06B6D4).copy(alpha = cyanAlpha), Color.Transparent),
                center = c3,
                radius = maxOf(w, h) * 0.58f
            ),
            radius = maxOf(w, h) * 0.58f,
            center = c3
        )
    }
}

/**
 * Full-screen atmospheric page backdrop that echoes the splash aurora colors softly
 * across the entire page while placing a progressive-blur hero wash at the top.
 * In Dark Mode, renders the exact `#07070C` + tri-color aurora atmosphere of the opening screen.
 */
@Composable
fun AuroraPageBackground(
    modifier: Modifier = Modifier,
    seed: Int = 1,
    headerWashHeight: Dp = 230.dp
) {
    val bg = MaterialTheme.colorScheme.background
    val isDark = bg.red < 0.2f

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (isDark) {
                    Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF07070C),
                                Color(0xFF0C0D17),
                                Color(0xFF07070C)
                            )
                        )
                    )
                } else {
                    Modifier.background(bg)
                }
            )
            .clipToBounds()
    ) {
        // Ambient aurora orbs along the viewport (vivid in dark mode matching splash, soft in light mode)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6366F1).copy(alpha = if (isDark) 0.20f else 0.055f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.14f, h * 0.70f),
                    radius = w * 0.82f
                ),
                radius = w * 0.82f,
                center = Offset(w * 0.14f, h * 0.70f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFD946EF).copy(alpha = if (isDark) 0.14f else 0.035f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.84f, h * 0.38f),
                    radius = w * 0.70f
                ),
                radius = w * 0.70f,
                center = Offset(w * 0.84f, h * 0.38f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF06B6D4).copy(alpha = if (isDark) 0.16f else 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.62f, h * 0.88f),
                    radius = w * 0.78f
                ),
                radius = w * 0.78f,
                center = Offset(w * 0.62f, h * 0.88f)
            )
        }

        // Top Progressive-Blur Aurora Hero Wash
        AuroraProgressiveBlurBackdrop(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerWashHeight)
                .align(Alignment.TopCenter),
            fadeToColor = if (isDark) Color(0xFF07070C) else bg,
            seed = seed
        )
    }
}

/**
 * Editorial title sitting above the Progressive Blur mask with exact 730 weight and -0.015em tracking.
 */
@Composable
fun ProgressiveBlurTitle(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 26.sp,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = fontSize,
            fontWeight = FontWeight(730),
            letterSpacing = (-0.015).em
        ),
        color = color,
        modifier = modifier
    )
}

