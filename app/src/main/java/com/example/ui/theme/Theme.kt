package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val YangmeiLightColorScheme = lightColorScheme(
    primary = SlatePrimary,
    onPrimary = PaperWhite,
    primaryContainer = StoneSurface,
    onPrimaryContainer = InkBlack,
    secondary = DeepGray,
    onSecondary = PaperWhite,
    secondaryContainer = CoolMist,
    onSecondaryContainer = InkBlack,
    tertiary = BrandGray,
    onTertiary = PaperWhite,
    tertiaryContainer = StoneSurface,
    onTertiaryContainer = InkBlack,
    background = WarmGrayWhite,
    onBackground = InkBlack,
    surface = PaperWhite,
    onSurface = InkBlack,
    surfaceVariant = StoneSurface,
    onSurfaceVariant = DeepGray,
    outline = HairlineGray,
    outlineVariant = MidGray,
    error = VermilionRed,
    onError = PaperWhite,
    errorContainer = SoftRoseTint,
    onErrorContainer = VermilionRed
)

private val YangmeiDarkColorScheme = darkColorScheme(
    primary = DarkOnSurface,
    onPrimary = PureBlack,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = DarkOnSurface,
    secondary = MidGray,
    onSecondary = PureBlack,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = DarkOnSurface,
    tertiary = BrandGray,
    onTertiary = DarkOnSurface,
    tertiaryContainer = DarkSurfaceElevated,
    onTertiaryContainer = DarkOnSurface,
    background = DarkCanvas,
    onBackground = DarkOnSurface,
    surface = DarkPaper,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkHairline,
    outlineVariant = BrandGray,
    error = VermilionRed,
    onError = PaperWhite
)

// Clean modern geometric shapes with subtle fluidity
val YangmeiShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun YangmeiTreeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) YangmeiDarkColorScheme else YangmeiLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = YangmeiTypography,
        shapes = YangmeiShapes,
        content = content
    )
}
