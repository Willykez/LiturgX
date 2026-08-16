package com.willykez.liturgx.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.ui.theme.seasonAccent

/**
 * Two soft radial "candle" glows — one warm top-left, one the day's liturgical colour
 * bottom-right — over the theme's base surface. Colour animates smoothly when the resolved
 * day (and therefore the season) changes, so browsing the calendar feels alive rather
 * than like flipping static pages. Works in both themes: a rich glow over near-black at
 * night, a soft wash over warm paper in daylight (toned down so it stays legible).
 */
@Composable
fun SeasonBackdrop(color: LiturgicalColor, modifier: Modifier = Modifier) {
    val accent by animateColorAsState(seasonAccent(color), tween(600), label = "seasonAccent")
    val base = MaterialTheme.colorScheme.background
    val isLight = base.luminance() > 0.5f
    val glowStrong = if (isLight) 0.16f else 0.35f
    val glowSoft = if (isLight) 0.10f else 0.22f

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(base)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = glowStrong), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.05f),
                radius = size.maxDimension * 0.6f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = glowSoft), Color.Transparent),
                center = Offset(size.width * 0.1f, size.height * 0.9f),
                radius = size.maxDimension * 0.7f
            )
        )
    }
}
