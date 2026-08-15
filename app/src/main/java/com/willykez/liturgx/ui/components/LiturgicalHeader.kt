package com.willykez.liturgx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.liturgx.data.sharing.SwahiliLiturgicalLabels
import com.willykez.liturgx.model.AppLanguage
import com.willykez.liturgx.model.LiturgicalColor
import com.willykez.liturgx.model.LiturgicalDay
import com.willykez.liturgx.model.ReadingLabels
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Editorial masthead — no card, no chrome, just a small dated eyebrow, the day's title set
 * in serif, a row of quick-read badges (season, week, cycle), and a hairline rule beneath.
 * The season color survives as a small dot next to the season/rank line and tints the cycle badge.
 */
@Composable
fun LiturgicalHeader(
    reading: LiturgicalDay?,
    selectedDate: LocalDate,
    language: AppLanguage,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onTodayClick: () -> Unit,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val litColor = reading?.color ?: LiturgicalColor.GREEN

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Date row — plain text, thin chevrons either side, tap the date to open the calendar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousDate, modifier = Modifier.testTag("prev_date_button").size(32.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Day",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)).uppercase(Locale.ROOT),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable { onOpenCalendar() }
                    .testTag("calendar_date_picker_trigger")
            )

            IconButton(onClick = onNextDate, modifier = Modifier.testTag("next_date_button").size(32.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Day",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title — the official Swahili day heading ("DOMINIKA YA 19 YA MWAKA A") in Swahili
        // mode, or the plain-English engine label ("19th Sunday in Ordinary Time") in English mode.
        val title = if (language == AppLanguage.SWAHILI) {
            SwahiliLiturgicalLabels.dayHeading(selectedDate)
        } else {
            reading?.title ?: "Loading…"
        }
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Season + color dot + rank, one quiet line
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(litColor.hexColor))
            )
            Spacer(modifier = Modifier.width(8.dp))
            val subtitle = listOfNotNull(
                reading?.season?.displayName,
                reading?.rank?.takeIf { it.isNotBlank() }
            ).joinToString("  ·  ")
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (reading != null && (reading.weekOfSeason > 0 || reading.cycle.isNotBlank())) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (reading.weekOfSeason > 0) {
                    HeaderBadge(
                        text = "${ReadingLabels.weekLabel(language)} ${reading.weekOfSeason}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (reading.cycle.isNotBlank()) {
                    HeaderBadge(
                        text = "${ReadingLabels.yearLabel(language)} ${reading.cycle}",
                        color = Color(litColor.hexColor)
                    )
                }
            }
        }

        if (reading?.saintOfTheDay?.isNotEmpty() == true) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = reading.saintOfTheDay,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )
    }
}

/** A small pill — e.g. "Juma 19" or "Mwaka A" — used for the header's quick-read badges. */
@Composable
private fun HeaderBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontSize = 12.sp
        )
    }
}
