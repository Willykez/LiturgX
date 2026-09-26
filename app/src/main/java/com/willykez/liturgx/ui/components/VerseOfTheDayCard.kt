package com.willykez.liturgx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.ui.theme.seasonAccent

/** The day's Gospel, trimmed to its first verse -- short enough to actually read at a glance,
 *  unlike showing the whole Gospel reading here. Shown at the top of Home.
 *
 *  Visual language: a soft diagonal wash of the day's liturgical colour behind a raised card,
 *  with an oversized quotation mark bleeding off the top-right corner as a purely decorative
 *  watermark -- the same "vestment mood" idea as [LiturgicalSeal], applied to a card instead of
 *  a circle. The citation reads as a small pill rather than a plain caption, matching the chip
 *  language used elsewhere (saint chips, upcoming rows). */
@Composable
fun VerseOfTheDayCard(
    citation: String,
    text: String,
    color: LiturgicalColor,
    modifier: Modifier = Modifier
) {
    val accent = seasonAccent(color)
    val onBg = MaterialTheme.colorScheme.onBackground
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = accent.copy(alpha = 0.25f),
                spotColor = accent.copy(alpha = 0.30f),
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(accent.copy(alpha = 0.24f), accent.copy(alpha = 0.06f)),
                )
            )
            .border(1.dp, accent.copy(alpha = 0.28f), shape)
    ) {
        // Oversized decorative quote mark, clipped to the card, bleeding past the corner --
        // pure watermark, never read as content, so it carries no contentDescription.
        Icon(
            Icons.Filled.FormatQuote,
            contentDescription = null,
            tint = accent.copy(alpha = 0.14f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = (-20).dp)
                .size(104.dp)
        )

        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.FormatQuote, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "NENO LA LEO",
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif, lineHeight = 27.sp),
                color = onBg
            )
            Spacer(Modifier.height(12.dp))
            Surface(color = accent.copy(alpha = 0.16f), shape = RoundedCornerShape(50)) {
                Text(
                    citation,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
