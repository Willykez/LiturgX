package com.willykez.liturgx.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.willykez.liturgx.core.LiturgicalColor

/**
 * The whole app is themed around a single idea: the liturgical colour of *today* IS the
 * app's accent colour. Everything — the hero card, the nav bar glow, the reading-card
 * borders — derives from whichever LiturgicalColor the resolver returns, so the app
 * visually changes its mood with the calendar, the way vestments do in church.
 *
 * That idea now works in two registers: a near-black "night vestment" dark theme, and a
 * warm-paper "open missal" light theme. Screens should never reach for a hardcoded colour —
 * always MaterialTheme.colorScheme.background / onBackground / onSurfaceVariant — so both
 * adapt automatically.
 */

/** User-facing choice, persisted via SettingsStore; SYSTEM follows the device setting. */
enum class ThemeMode(val label: String) {
    SYSTEM("Fuata Mfumo"),
    LIGHT("Mwanga"),
    DARK("Giza")
}

// --- Dark palette: near-black base, gives every season's colour room to glow ---
val DarkBackground = Color(0xFF120F1A)
val DarkSurface = Color(0xFF1B1726)
val DarkOnBackground = Color(0xFFF6EFE1)       // warm parchment text
val DarkOnSurfaceVariant = Color(0xFFCFC6B8)

// --- Light palette: warm paper base, ink-toned text — an open missal in daylight ---
val LightBackground = Color(0xFFFBF6EA)
val LightSurface = Color(0xFFF1E7D3)
val LightOnBackground = Color(0xFF231D2E)      // deep plum-ink, not pure black
val LightOnSurfaceVariant = Color(0xFF5D5568)

fun seasonAccent(color: LiturgicalColor): Color = Color(color.hex)

fun seasonAccentSoft(color: LiturgicalColor): Color = seasonAccent(color).copy(alpha = 0.16f)

@Composable
fun isDarkThemeActive(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun LiturgXTheme(accent: LiturgicalColor, darkTheme: Boolean, textScale: TextScale = TextScale.WASTANI, content: @Composable () -> Unit) {
    val accentColor = seasonAccent(accent)
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = accentColor,
            onPrimary = DarkOnBackground,
            secondary = accentColor,
            background = DarkBackground,
            onBackground = DarkOnBackground,
            surface = DarkSurface,
            onSurface = DarkOnBackground,
            surfaceVariant = DarkSurface,
            onSurfaceVariant = DarkOnSurfaceVariant,
            outline = DarkOnSurfaceVariant.copy(alpha = 0.3f)
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            onPrimary = LightBackground,
            secondary = accentColor,
            background = LightBackground,
            onBackground = LightOnBackground,
            surface = LightSurface,
            onSurface = LightOnBackground,
            surfaceVariant = LightSurface,
            onSurfaceVariant = LightOnSurfaceVariant,
            outline = LightOnSurfaceVariant.copy(alpha = 0.35f)
        )
    }
    MaterialTheme(colorScheme = scheme, typography = scaledTypography(textScale), content = content)
}
