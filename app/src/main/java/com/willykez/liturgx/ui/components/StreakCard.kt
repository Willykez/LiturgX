package com.willykez.liturgx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.data.ProgressStore
import com.willykez.liturgx.ui.theme.seasonAccent
import com.willykez.liturgx.ui.theme.seasonAccentSoft
import java.time.LocalDate

/**
 * The "on this day" retention nudge: a streak count plus a GitHub-style contribution heatmap
 * of which days the person actually opened their readings, over the last [weeks] weeks. Shown
 * only on [com.willykez.liturgx.ui.home.HomeScreen] — a streak is about today's habit, so it
 * has no business following you into Kalenda while you're browsing some other date.
 */
@Composable
fun StreakCard(
    openedDates: Set<LocalDate>,
    today: LocalDate,
    color: LiturgicalColor,
    weeks: Int = 12,
    modifier: Modifier = Modifier
) {
    val accent = seasonAccent(color)
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant

    val streak = remember(openedDates, today) { ProgressStore.currentStreak(openedDates, today) }
    val longest = remember(openedDates) { ProgressStore.longestStreak(openedDates) }

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(seasonAccentSoft(color))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                if (streak == 0) "Anza mfululizo wako leo" else if (streak == 1) "Siku 1 mfululizo" else "Siku $streak mfululizo",
                style = MaterialTheme.typography.titleMedium,
                color = onBg
            )
            Spacer(Modifier.weight(1f))
            if (longest > 1) {
                Text("Rekodi: siku $longest", style = MaterialTheme.typography.labelSmall, color = onBgDim)
            }
        }
        Spacer(Modifier.height(12.dp))
        ReadingHeatmap(openedDates = openedDates, today = today, weeks = weeks, accent = accent)
    }
}

@Composable
private fun ReadingHeatmap(
    openedDates: Set<LocalDate>,
    today: LocalDate,
    weeks: Int,
    accent: androidx.compose.ui.graphics.Color
) {
    val emptyCell = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f)
    val currentWeekMonday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val firstMonday = currentWeekMonday.minusWeeks((weeks - 1).toLong())

    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        for (w in 0 until weeks) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                val weekStart = firstMonday.plusWeeks(w.toLong())
                for (d in 0 until 7) {
                    val date = weekStart.plusDays(d.toLong())
                    val cellColor = when {
                        date.isAfter(today) -> androidx.compose.ui.graphics.Color.Transparent
                        date in openedDates -> accent
                        else -> emptyCell
                    }
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(cellColor)
                    )
                }
            }
        }
    }
}
