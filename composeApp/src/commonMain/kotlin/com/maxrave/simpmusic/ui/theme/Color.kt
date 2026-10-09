package com.maxrave.simpmusic.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================================
// THEME SELECTION
// Yahan se theme change karein: "LUXE_GOLDEN" ya "CHRONO_GRAPHITE"
// ==========================================================
val currentTheme = "LUXE_GOLDEN"

// ==========================================================
// BRAND SEED COLORS (Theme Generator)
// ==========================================================
val seed = when (currentTheme) {
    "LUXE_GOLDEN" -> Color(0xFFD4AF37)      // Royal Gold
    "CHRONO_GRAPHITE" -> Color(0xFF6E6E73)  // Metallic Graphite Grey
    else -> Color(0xFF8ECAE6)               // Original Default Blue
}

// ==========================================================
// PREMIUM THEME PALETTES (For Custom UI Components)
// ==========================================================
// Luxe Golden Colors
val luxeBackground = Color(0xFF0A1128)
val luxeSurface = Color(0xFF13203A)
val luxePrimary = Color(0xFFD4AF37)

// Chrono Graphite Colors
val graphiteBackground = Color(0xFF0F0F0F)
val graphiteSurface = Color(0xFF1C1C1E)
val graphitePrimary = Color(0xFF9E9E9E)

// ==========================================================
// SEMANTIC COLORS (Standard App Colors)
// ==========================================================
/** Liked/favorite state (heart buttons, favorite tiles). */
val favoriteColor = Color(0xFFFF4081)

/** Currently playing lyric line. */
val lyricActiveColor = Color(0xFFFFFF00)

val shimmerBackground = Color(0x7E383737)
val shimmerLine = Color(0xFF4D4848)

// Light-theme counterparts of the shimmer tokens.
val shimmerBackgroundLight = Color(0x7EDCD8D8)
val shimmerLineLight = Color(0xFFCFC8C8)

val overlay = Color(0x32242424)
val blackMoreOverlay = Color(0x8f242424)

// ==========================================================
// DESKTOP SHELL COLORS
// ==========================================================
val desktopWindowDark = Color(0xFF000000)
val desktopWindowLight = Color(0xFFFFFFFF)
val desktopPanelDark = Color(0xFF121212)

// ==========================================================
// DESKTOP WINDOW CONTROLS (macOS Traffic Lights)
// ==========================================================
val windowCloseButton = Color(0xFFFF605C)
val windowCloseButtonHover = Color(0xFFE54942)
val windowMinimiseButton = Color(0xFFFFBD44)
val windowMinimiseButtonHover = Color(0xFFE5A93D)
val windowMaximiseButton = Color(0xFF00CA4E)
val windowMaximiseButtonHover = Color(0xFF00B344)

// ==========================================================
// LEGACY COLORS (Do not add new usages)
// ==========================================================
@Deprecated("Legacy storage bar color only — use MaterialTheme.colorScheme.primary in new code")
val md_theme_dark_primary = Color(0xFFB2C5FF)