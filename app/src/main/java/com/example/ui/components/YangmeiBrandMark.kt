package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.R
import com.example.ui.theme.InkBlack
import kotlinx.coroutines.delay

private object LogoBitmapCache {
    var darkSurfaceBitmap: ImageBitmap? = null
    var lightSurfaceBitmap: ImageBitmap? = null
}

private fun lerpRgb(a: Int, b: Int, t: Float): Int {
    val ct = t.coerceIn(0f, 1f)
    val ar = (a shr 16) and 0xFF
    val ag = (a shr 8) and 0xFF
    val ab = a and 0xFF
    val br = (b shr 16) and 0xFF
    val bg = (b shr 8) and 0xFF
    val bb = b and 0xFF
    val r = (ar + (br - ar) * ct).toInt().coerceIn(0, 255)
    val g = (ag + (bg - ag) * ct).toInt().coerceIn(0, 255)
    val bl = (ab + (bb - ab) * ct).toInt().coerceIn(0, 255)
    return (r shl 16) or (g shl 8) or bl
}

private fun sampleIridescentColor(nx: Float, ny: Float, onDarkSurface: Boolean): Int {
    // Diagonal iridescent aurora sweep + subtle angular shimmer
    val stops = if (onDarkSurface) {
        intArrayOf(
            0x818CF8, // Luminous Indigo
            0xC084FC, // Vivid Violet
            0xF472B6, // Neon Pink-Fuchsia
            0x22D3EE, // Electric Cyan
            0x34D399, // Iridescent Mint-Aqua
            0x818CF8  // Indigo loop
        )
    } else {
        intArrayOf(
            0x4F46E5, // Deep Indigo
            0x9333EA, // Rich Violet
            0xDB2777, // Vivid Magenta-Rose
            0x0891B2, // Rich Cyan
            0x0EA5E9, // Vivid Sky-Cyan
            0x4F46E5  // Indigo loop
        )
    }
    val u = (nx * 0.62f + ny * 0.58f + kotlin.math.sin((nx - ny) * 3.14159f) * 0.08f)
        .coerceIn(0f, 0.9999f)
    val scaled = u * (stops.size - 1)
    val idx = scaled.toInt().coerceIn(0, stops.size - 2)
    return lerpRgb(stops[idx], stops[idx + 1], scaled - idx)
}

/**
 * Loads `R.drawable.ic_yangmei_logo`, auto-crops empty margins so the mark fills its bounds,
 * hollows out every filled stroke into a crisp transparent-interior outline ("镂空设计"),
 * and renders the contours in a luminous multi-stop iridescent aurora gradient ("炫彩").
 */
private fun getTransparentLogoBitmap(context: Context, onDarkSurface: Boolean): ImageBitmap {
    if (onDarkSurface) {
        LogoBitmapCache.darkSurfaceBitmap?.let { return it }
    } else {
        LogoBitmapCache.lightSurfaceBitmap?.let { return it }
    }

    val opts = BitmapFactory.Options().apply {
        inScaled = false
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    val raw = BitmapFactory.decodeResource(context.resources, R.drawable.ic_yangmei_logo, opts)
    // Scale down to 320px working square for fast, anti-aliased morphological hollowing
    val workSize = 320
    val scaled = if (raw.width > workSize || raw.height > workSize) {
        Bitmap.createScaledBitmap(raw, workSize, workSize, true).also {
            if (!raw.isRecycled) raw.recycle()
        }
    } else {
        raw
    }

    val w = scaled.width
    val h = scaled.height
    val rawPixels = IntArray(w * h)
    scaled.getPixels(rawPixels, 0, w, 0, 0, w, h)
    if (!scaled.isRecycled) scaled.recycle()

    // 1. Key out uniform dark charcoal backdrop into soft foreground mask (0..255) & find tight bounding box
    val mask = IntArray(w * h)
    var minX = w
    var minY = h
    var maxX = 0
    var maxY = 0
    for (y in 0 until h) {
        val rowOffset = y * w
        for (x in 0 until w) {
            val c = rawPixels[rowOffset + x]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            val maxC = maxOf(r, g, b)
            val a = when {
                maxC <= 50 -> 0
                maxC >= 104 -> 255
                else -> ((maxC - 50) * 255 / 54).coerceIn(0, 255)
            }
            mask[rowOffset + x] = a
            if (a > 36) {
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
            }
        }
    }

    if (minX >= maxX || minY >= maxY) {
        minX = 0; minY = 0; maxX = w - 1; maxY = h - 1
    }

    // 2. Square tight crop with subtle 4% breathing room so the logo looks generously sized
    val boxW = maxX - minX + 1
    val boxH = maxY - minY + 1
    val side = (maxOf(boxW, boxH) * 1.08f).toInt().coerceAtMost(minOf(w, h))
    val cx = (minX + maxX) / 2
    val cy = (minY + maxY) / 2
    val cropX = (cx - side / 2).coerceIn(0, (w - side).coerceAtLeast(0))
    val cropY = (cy - side / 2).coerceIn(0, (h - side).coerceAtLeast(0))

    // 3. Hollow-out contour extraction: keep a crisp outline ring along every boundary of the artwork
    //    (interior of thick bands/square/dot becomes transparent hollow!)
    val strokeRadius = (side * 0.015f).toInt().coerceIn(2, 4)
    val outPixels = IntArray(side * side)

    for (oy in 0 until side) {
        val sy = cropY + oy
        val ny = oy.toFloat() / (side - 1).coerceAtLeast(1)
        for (ox in 0 until side) {
            val sx = cropX + ox
            val centerMask = mask[sy * w + sx]
            if (centerMask <= 10) {
                outPixels[oy * side + ox] = 0
                continue
            }

            // Find minimum mask value in circular neighborhood of radius `strokeRadius`
            // If every neighbor is solidly inside foreground (minNeighbor ~ 255), this pixel is interior -> hollow!
            var minNeighbor = 255
            val r2 = strokeRadius * strokeRadius
            var dy = -strokeRadius
            while (dy <= strokeRadius && minNeighbor > 0) {
                val nyIdx = (sy + dy).coerceIn(0, h - 1) * w
                var dx = -strokeRadius
                while (dx <= strokeRadius) {
                    if (dx * dx + dy * dy <= r2) {
                        val nxIdx = (sx + dx).coerceIn(0, w - 1)
                        val v = mask[nyIdx + nxIdx]
                        if (v < minNeighbor) {
                            minNeighbor = v
                            if (minNeighbor == 0) break
                        }
                    }
                    dx++
                }
                dy++
            }

            // Outline alpha = center foreground minus eroded interior (crisp hollow contour!)
            val rawOutlineAlpha = (centerMask - (minNeighbor * 0.94f).toInt()).coerceIn(0, 255)
            // Boost anti-aliased edge clarity so fine lines stay bright and crisp at any size
            val outlineAlpha = if (rawOutlineAlpha > 14) {
                (rawOutlineAlpha * 1.32f).toInt().coerceIn(0, 255)
            } else {
                0
            }

            if (outlineAlpha == 0) {
                outPixels[oy * side + ox] = 0
            } else {
                val nx = ox.toFloat() / (side - 1).coerceAtLeast(1)
                val rgb = sampleIridescentColor(nx, ny, onDarkSurface)
                outPixels[oy * side + ox] = (outlineAlpha shl 24) or rgb
            }
        }
    }

    val out = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
    out.setPixels(outPixels, 0, side, 0, 0, side, side)

    val imageBitmap = out.asImageBitmap()
    if (onDarkSurface) {
        LogoBitmapCache.darkSurfaceBitmap = imageBitmap
    } else {
        LogoBitmapCache.lightSurfaceBitmap = imageBitmap
    }
    return imageBitmap
}

/**
 * Iridescent hollowed-out Yangmei Tree brand logo (auto-cropped, transparent interior, colorful aurora contours).
 */
@Composable
fun YangmeiTransparentLogo(
    size: Dp,
    onDarkSurface: Boolean,
    modifier: Modifier = Modifier,
    contentDescription: String? = "杨梅树窝 Logo"
) {
    val context = LocalContext.current
    val logoBitmap = remember(onDarkSurface) {
        getTransparentLogoBitmap(context, onDarkSurface)
    }

    Image(
        bitmap = logoBitmap,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size)
    )
}

/**
 * Checks Android's system High Text Contrast accessibility setting —
 * native counterpart to CSS `@media (forced-colors: active)`.
 */
@Composable
private fun rememberHighTextContrastEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Secure.getInt(
                context.contentResolver,
                "high_text_contrast_enabled",
                0
            ) == 1
        }.getOrDefault(false)
    }
}

/**
 * Seamless 4-stop gradient palette matching CSS `.grad-text`:
 * `linear-gradient(90deg, #4f46e5, #c026d3, #0e7490, #4f46e5)`
 * (luminous equivalent on dark #07070C backdrops so every hue stays vibrant).
 * Start and end colors are identical so 0% -> 200% sweep matches first & last frames seamlessly.
 */
private fun gradTextColors(onDarkSurface: Boolean): List<Color> =
    if (onDarkSurface) {
        listOf(
            Color(0xFF818CF8), // Luminous Indigo
            Color(0xFFE879F9), // Vivid Fuchsia
            Color(0xFF22D3EE), // Bright Cyan
            Color(0xFF818CF8)  // Seamless return to Indigo
        )
    } else {
        listOf(
            Color(0xFF4F46E5), // #4f46e5
            Color(0xFFC026D3), // #c026d3
            Color(0xFF0E7490), // #0e7490
            Color(0xFF4F46E5)  // #4f46e5 seamless loop
        )
    }

/**
 * Compose translation of `.grad-text`:
 * - `font-weight: 800`, `line-height: 1.1`, `letter-spacing: -0.04em`
 * - `linear-gradient(90deg, #4f46e5, #c026d3, #0e7490, #4f46e5)` at `200%` width (`TileMode.Repeated`)
 * - Smooth `4.2s linear 1` flow shifting `0% -> 200%` (first and last frames identical)
 * - Respects `prefers-reduced-motion` and high-contrast `forced-colors` solid CanvasText fallback.
 */
@Composable
fun GradientFlowTitle(
    text: String,
    fontSize: TextUnit,
    onDarkSurface: Boolean,
    modifier: Modifier = Modifier
) {
    val highContrast = rememberHighTextContrastEnabled()
    val prefersReducedMotion = rememberPrefersReducedMotion()
    var widthPx by remember { mutableFloatStateOf(0f) }

    val flowProgress = remember(prefersReducedMotion, highContrast) { Animatable(0f) }
    LaunchedEffect(prefersReducedMotion, highContrast) {
        if (!prefersReducedMotion && !highContrast) {
            flowProgress.snapTo(0f)
            flowProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 4200, easing = LinearEasing)
            )
        } else {
            flowProgress.snapTo(0f)
        }
    }

    val colors = remember(onDarkSurface) { gradTextColors(onDarkSurface) }
    val span = (widthPx * 2f).coerceAtLeast(320f)
    val shift = flowProgress.value * span
    val gradBrush = Brush.linearGradient(
        colors = colors,
        start = Offset(-shift, 0f),
        end = Offset(span - shift, 0f),
        tileMode = TileMode.Repeated
    )

    val baseStyle = MaterialTheme.typography.displayLarge.copy(
        fontSize = fontSize,
        lineHeight = fontSize * 1.1f,
        fontWeight = FontWeight(800),
        letterSpacing = (-0.04).em
    )

    if (highContrast) {
        Text(
            text = text,
            style = baseStyle,
            color = if (onDarkSurface) Color.White else InkBlack,
            maxLines = 1,
            modifier = modifier
        )
    } else {
        Text(
            text = text,
            style = baseStyle.copy(brush = gradBrush),
            maxLines = 1,
            modifier = modifier.onSizeChanged { size ->
                if (size.width > 0) widthPx = size.width.toFloat()
            }
        )
    }
}

/**
 * Splash-screen `.grad-text` combined with subtle staggered `.blur-text` reveal:
 * - Renders `"杨梅树窝"` with `800` weight, `1.1` line-height, `-0.04em` tracking
 * - Unified `200%`-wide horizontal gradient (`#4f46e5 -> #c026d3 -> #0e7490 -> #4f46e5`, brightened on dark)
 *   flowing smoothly across all 4 characters in `4.2s linear 1` (0% -> 200%, identical start/end frames)
 * - Respects `prefersReducedMotion` and high-contrast `forced-colors` solid fallback.
 */
@Composable
fun BlurGradientFlowTitle(
    text: String,
    fontSize: TextUnit,
    onDarkSurface: Boolean,
    prefersReducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val chars = remember(text) { text.toList() }
    val highContrast = rememberHighTextContrastEnabled()
    var rowWidthPx by remember { mutableFloatStateOf(0f) }

    val flowProgress = remember(prefersReducedMotion, highContrast) { Animatable(0f) }
    LaunchedEffect(prefersReducedMotion, highContrast) {
        if (!prefersReducedMotion && !highContrast) {
            flowProgress.snapTo(0f)
            flowProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 4200, easing = LinearEasing)
            )
        } else {
            flowProgress.snapTo(0f)
        }
    }

    val colors = remember(onDarkSurface) { gradTextColors(onDarkSurface) }
    val charCount = chars.size.coerceAtLeast(1)
    val totalW = rowWidthPx.coerceAtLeast(360f)
    val span = totalW * 2f
    val shift = flowProgress.value * span

    Row(
        modifier = modifier
            .onSizeChanged { size ->
                if (size.width > 0) rowWidthPx = size.width.toFloat()
            }
            .semantics(mergeDescendants = true) {
                contentDescription = text
            },
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        chars.forEachIndexed { index, ch ->
            val reveal = remember(index, prefersReducedMotion) {
                Animatable(if (prefersReducedMotion) 1f else 0f)
            }
            LaunchedEffect(index, prefersReducedMotion) {
                if (prefersReducedMotion) {
                    reveal.snapTo(1f)
                } else {
                    delay(index * 75L)
                    reveal.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 480,
                            easing = CubicBezierEasing(0f, 0f, 0.2f, 1f)
                        )
                    )
                }
            }

            val t = reveal.value
            val blurRadius = ((1f - t) * 8f).dp
            val charOriginX = (totalW / charCount) * index
            val charBrush = Brush.linearGradient(
                colors = colors,
                start = Offset(-shift - charOriginX, 0f),
                end = Offset(span - shift - charOriginX, 0f),
                tileMode = TileMode.Repeated
            )

            val baseStyle = MaterialTheme.typography.displayLarge.copy(
                fontSize = fontSize,
                lineHeight = fontSize * 1.1f,
                fontWeight = FontWeight(800),
                letterSpacing = (-0.04).em
            )

            val animModifier = Modifier
                .alpha(t)
                .then(if (blurRadius > 0.2.dp) Modifier.blur(blurRadius) else Modifier)

            if (highContrast) {
                Text(
                    text = ch.toString(),
                    style = baseStyle,
                    color = if (onDarkSurface) Color.White else InkBlack,
                    maxLines = 1,
                    modifier = animModifier
                )
            } else {
                Text(
                    text = ch.toString(),
                    style = baseStyle.copy(brush = charBrush),
                    maxLines = 1,
                    modifier = animModifier
                )
            }
        }
    }
}
