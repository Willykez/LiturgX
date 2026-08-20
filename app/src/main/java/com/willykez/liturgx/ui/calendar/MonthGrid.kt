package com.willykez.liturgx.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.liturgx.core.RegionSettings
import com.willykez.liturgx.core.SwahiliDate
import com.willykez.liturgx.data.LectionaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields

private val CELL_SIZE = 40.dp
private val WEEK_GUTTER_WIDTH = 20.dp
private const val WEEK_ROWS = 6 // always render 6 rows so every month page is the same height

/**
 * One month page of the calendar (Sunday-first, matching the platform Calendar app this screen
 * is deliberately modeled on). Always renders exactly [WEEK_ROWS] week-rows regardless of how
 * many the month actually needs (4-6) so swiping between months never jumps in height.
 *
 * The small dot under a day marks a "notable day" -- one with a named saint/feast overriding
 * the plain weekday reading ([com.willykez.liturgx.core.ResolvedDay.overridingSaint]) or an
 * optional memorial offered alongside it. That's up to [WEEK_ROWS] * 7 = 42 lectionary lookups
 * per page, each a real SQLite query -- computed off the main thread via [produceState] rather
 * than during composition, so swiping between months can't stutter waiting on them; the dots
 * simply fill in a frame or two after the page appears instead of blocking it.
 */
@Composable
fun MonthGrid(
    month: YearMonth,
    repository: LectionaryRepository,
    region: RegionSettings,
    selectedDate: LocalDate,
    today: LocalDate,
    accent: Color,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant

    val notableDays by produceState(initialValue = emptySet<Int>(), month, region) {
        value = withContext(Dispatchers.IO) {
            (1..month.lengthOfMonth()).filter { day ->
                val result = repository.getForDate(month.atDay(day), region)
                result.resolved.overridingSaint != null || result.optionalMemorial != null
            }.toSet()
        }
    }

    val firstOfMonth = month.atDay(1)
    // Sunday-first column index: Sunday -> 0, Monday -> 1, ... Saturday -> 6.
    val firstDayColumn = firstOfMonth.dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()

    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(WEEK_GUTTER_WIDTH))
            listOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY).forEach { dow ->
                Text(
                    SwahiliDate.weekdayName(dow).take(3),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = onBgDim
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = onBgDim.copy(alpha = 0.2f))
        Spacer(Modifier.height(4.dp))

        for (week in 0 until WEEK_ROWS) {
            val weekStartCell = week * 7
            val weekStartDate = firstOfMonth.plusDays((weekStartCell - firstDayColumn).toLong())
            val isoWeekNumber = weekStartDate.get(WeekFields.ISO.weekOfWeekBasedYear())

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    isoWeekNumber.toString(),
                    modifier = Modifier.width(WEEK_GUTTER_WIDTH),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = onBgDim.copy(alpha = 0.5f)
                )
                for (column in 0 until 7) {
                    val dayOfMonth = weekStartCell + column - firstDayColumn + 1
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (dayOfMonth in 1..daysInMonth) {
                            val date = month.atDay(dayOfMonth)
                            DayCell(
                                day = dayOfMonth,
                                isSelected = date == selectedDate,
                                isToday = date == today,
                                hasNotableDay = notableDays.contains(dayOfMonth),
                                accent = accent,
                                onBg = onBg,
                                onClick = { onDayClick(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasNotableDay: Boolean,
    accent: Color,
    onBg: Color,
    onClick: () -> Unit
) {
    val background = MaterialTheme.colorScheme.background
    Column(
        Modifier
            .size(CELL_SIZE)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (isSelected) onBg else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                day.toString(),
                color = when {
                    isSelected -> background
                    isToday -> accent
                    else -> onBg
                },
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(if (hasNotableDay) accent else Color.Transparent)
        )
    }
}
