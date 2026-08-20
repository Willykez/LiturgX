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
import com.willykez.liturgx.data.sharing.DayCardReading
import com.willykez.liturgx.ui.theme.seasonAccent

/**
 * The whole day's readings as one shareable card -- [LiturgicalCard]'s single-reading design,
 * extended to lay out every reading of the day in one continuous parchment sheet (a graphical
 * counterpart to what [com.willykez.liturgx.data.sharing.DailyReadingShareFormatter] produces
 * as plain text). Tall by nature when a day has four full readings -- that's expected and fine
 * for a shared image the same way a screenshotted note is; it's read top-to-bottom, not framed
 * as a single-glance square.
 */
@Composable
fun DailyLiturgicalCard(
    dateText: String,
    seasonText: String,
    readings: List<DayCardReading>,
    liturgicalColor: LiturgicalColor,
    brandName: String = "LiturgX",
    modifier: Modifier = Modifier
) {
    val accent = seasonAccent(liturgicalColor)
    val cardBg = Color(0xFFFBF6EA)
    val ink = Color(0xFF231D2E)
    val inkDim = Color(0xFF5D5568)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .padding(24.dp)
    ) {
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

        readings.forEachIndexed { index, reading ->
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = inkDim.copy(alpha = 0.2f))
            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    reading.kindLabel.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Text(
                    reading.citation,
                    style = MaterialTheme.typography.bodySmall,
                    color = inkDim
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                reading.passageText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Serif,
                    lineHeight = 21.sp
                ),
                color = ink
            )
            reading.responseText?.let { response ->
                Spacer(Modifier.height(10.dp))
                Text(
                    response,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = inkDim
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Imetumwa kutoka $brandName",
                style = MaterialTheme.typography.labelSmall,
                color = inkDim,
                fontStyle = FontStyle.Italic
            )
        }
    }
}
