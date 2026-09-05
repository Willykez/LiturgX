package com.willykez.liturgx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.ui.theme.seasonAccent
import com.willykez.liturgx.ui.theme.seasonAccentSoft

/** The day's Gospel, trimmed to its first verse -- short enough to actually read at a glance,
 *  unlike showing the whole Gospel reading here. Sits alongside [StreakCard] on Home. */
@Composable
fun VerseOfTheDayCard(
    citation: String,
    text: String,
    color: LiturgicalColor,
    modifier: Modifier = Modifier
) {
    val accent = seasonAccent(color)
    val onBg = MaterialTheme.colorScheme.onBackground

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(seasonAccentSoft(color))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.FormatQuote, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Neno la Leo", style = MaterialTheme.typography.titleMedium, color = onBg)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif),
            color = onBg
        )
        Spacer(Modifier.height(8.dp))
        Text(citation, style = MaterialTheme.typography.labelMedium, color = accent)
    }
}
