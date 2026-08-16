package com.willykez.liturgx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.ui.theme.seasonAccent

/** A small glowing "wax seal" — the day's liturgical colour, rendered like vestment cloth. */
@Composable
fun LiturgicalSeal(color: LiturgicalColor, size: Dp = 48.dp, modifier: Modifier = Modifier) {
    val accent = seasonAccent(color)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.95f), accent.copy(alpha = 0.55f)),
                    radius = size.value * 1.4f
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center
    ) {}
}
