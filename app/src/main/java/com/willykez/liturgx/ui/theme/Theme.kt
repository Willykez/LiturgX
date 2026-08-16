package com.willykez.liturgx.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.willykez.liturgx.core.LiturgicalColor

/**
 * The whole app is themed around a single idea: the liturgical colour of *today* IS the
 * app's accent colour. Everything — the hero card, the nav bar glow, the reading-card
 * borders — derives from whichever LiturgicalColor the resolver returns, so the app
 * visually changes its mood with the calendar, the way vestments do in church.
 */

val Ink = Color(0xFF120F1A)          // near-black base, gives every season's colour room to glow
val InkElevated = Color(0xFF1B1726)
val Parchment = Color(0xFFF6EFE1)    // warm off-white for scripture text — reads like a page, not a screen
val ParchmentDim = Color(0xFFCFC6B8)

fun seasonAccent(color: LiturgicalColor): Color = Color(color.hex)

fun seasonAccentSoft(color: LiturgicalColor): Color = seasonAccent(color).copy(alpha = 0.16f)

@Composable
fun LiturgXTheme(accent: LiturgicalColor, content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = seasonAccent(accent),
        onPrimary = Parchment,
        secondary = seasonAccent(accent),
        background = Ink,
        onBackground = Parchment,
        surface = InkElevated,
        onSurface = Parchment,
        surfaceVariant = InkElevated,
        onSurfaceVariant = ParchmentDim,
        outline = ParchmentDim.copy(alpha = 0.3f)
    )
    MaterialTheme(colorScheme = scheme, typography = LiturgXTypography, content = content)
}
