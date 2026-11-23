package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Lively Futuristic Fluid Gray & Cool Slate Palette (2-hue restrained system, no abrupt black boxes)
val WarmGrayWhite = Color(0xFFE8ECF2)   // Cool silver-gray canvas so cards float crisply
val PaperWhite = Color(0xFFFFFFFF)      // Pure crisp card surface
val StoneSurface = Color(0xFFEEF1F6)    // Soft pill & chip fill
val HairlineGray = Color(0xFFD6DCE6)    // Subtle structural border
val MidGray = Color(0xFF8B94A3)
val BrandGray = Color(0xFF606B7B)
val DeepGray = Color(0xFF424D5E)
val InkBlack = Color(0xFF1F2937)        // Deep slate ink for high-contrast text (not pure harsh black)
val CharcoalSurface = Color(0xFF2B384B) // Soft deep slate for dark hero surfaces
val PureBlack = Color(0xFF141C27)

// 2-Hue Slate & Mist Gradient Tokens
val SlatePrimary = Color(0xFF364860)
val SlateGradientStart = Color(0xFF2F3F56)
val SlateGradientEnd = Color(0xFF4C607C)
val HeroSlateDeep = Color(0xFF18212E)
val HeroSlateMid = Color(0xFF263447)
val HeroSlateLight = Color(0xFF3A4C63)
val MistGradientStart = Color(0xFFF6F8FC)
val MistGradientEnd = Color(0xFFE7EDF6)

// Fluid Metallic & Restrained Accents
val FluidSilver = Color(0xFFC6CEDB)
val CoolMist = Color(0xFFDCE2EC)
val LiquidGraphite = Color(0xFF3C4B60)
val AcidYellow = Color(0xFFD8EB47)
val AcidTintBg = Color(0xFFF3F8D0)
val FluorescentGreen = Color(0xFF26C266)
val MintTintBg = Color(0xFFE0F6E9)
val VermilionRed = Color(0xFFE0483E)
val SoftRoseTint = Color(0xFFFBEAE8)
val SlateTintBg = Color(0xFFE7ECF5)
val SlateAccent = Color(0xFF445873)

// Unified Aurora Brand Palette (.hero-aurora & .gb-card)
val AuroraIndigo = Color(0xFF6366F1)
val AuroraFuchsia = Color(0xFFD946EF)
val AuroraCyan = Color(0xFF06B6D4)
val AuroraFocusIndigo = Color(0xFF4F46E5)
val AuroraShadowFuchsia = Color(0x73D946EF)     // rgba(217, 70, 239, 0.45)
val AuroraShadowFuchsiaHover = Color(0x99D946EF) // rgba(217, 70, 239, 0.60)

// Reusable Gradient Brushes (135deg linear-gradient(#6366f1, #d946ef 50%, #06b6d4))
val AuroraBorderGradient: Brush = Brush.linearGradient(
    colorStops = arrayOf(
        0.0f to AuroraIndigo,
        0.5f to AuroraFuchsia,
        1.0f to AuroraCyan
    )
)

val YangmeiPrimaryGradient: Brush = Brush.linearGradient(
    colorStops = arrayOf(
        0.0f to AuroraIndigo,
        0.5f to AuroraFuchsia,
        1.0f to AuroraCyan
    )
)

val YangmeiHeroGradient: Brush = Brush.linearGradient(
    colors = listOf(HeroSlateDeep, HeroSlateMid, HeroSlateLight)
)

val YangmeiFeaturedCardGradient: Brush = Brush.linearGradient(
    colors = listOf(Color(0xFFFAF8FF), Color(0xFFF3F8FF))
)

// Dark Mode Companion Surfaces — keeps exact .hero-aurora (#07070C) canvas while elevating cards distinctly
val DarkCanvas = Color(0xFF07070C)
val DarkPaper = Color(0xFF181C30)          // Distinctly elevated indigo-slate card surface over #07070C
val DarkSurfaceElevated = Color(0xFF232844) // Elevated chips, inputs, and nested containers in dark mode
val DarkHairline = Color(0xFF333B5E)       // Crisp subtle card/input frame outline in dark mode
val DarkOnSurface = Color(0xFFF4F6FB)
val DarkSecondaryText = Color(0xFFA7B0C8)
val AuroraLightIndigo = Color(0xFFA5B4FC)
