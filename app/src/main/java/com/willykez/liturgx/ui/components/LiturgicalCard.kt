package com.willykez.liturgx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.ui.theme.seasonAccent

/**
 * A single reading laid out as a shareable card -- the "post this to WhatsApp Status" version
 * of a [ReadingBlock]. Parchment-on-plum, the same open-missal palette as the rest of the app
 * (not a hardcoded green/gray design), so a shared card is recognizably a LiturgX card.
 */
@Composable
fun LiturgicalCard(
    dateText: String,
    seasonText: String,
    kindLabel: String,
    citation: String,
    passage: String,
    responseText: String? = null,
    liturgicalColor: LiturgicalColor,
    brandName: String = "LiturgX",
    modifier: Modifier = Modifier
) {
    val accent = seasonAccent(liturgicalColor)
    val cardBg = Color(0xFFFBF6EA)   // warm paper, same as LightBackground -- fixed regardless of app theme
    val ink = Color(0xFF231D2E)      // deep plum-ink, same as LightOnBackground
    val inkDim = Color(0xFF5D5568)   // same as LightOnSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .padding(24.dp)
    ) {
        // Liturgical accent bar
        Box(
            Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(accent)
        )

        Spacer(Modifier.height(18.dp))

        Text(
            seasonText.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(dateText, style = MaterialTheme.typography.bodySmall, color = inkDim)

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = inkDim.copy(alpha = 0.25f))
        Spacer(Modifier.height(16.dp))

        Text(
            "${kindLabel.uppercase()}: $citation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ink
        )

        Spacer(Modifier.height(14.dp))

        Text(
            passage,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Serif,
                lineHeight = 24.sp
            ),
            color = ink
        )

        if (!responseText.isNullOrEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text(
                responseText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = inkDim
            )
        }

        Spacer(Modifier.height(24.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Imetumwa kutoka $brandName",
                style = MaterialTheme.typography.labelSmall,
                color = inkDim,
                fontStyle = FontStyle.Italic
            )
        }
    }
}
