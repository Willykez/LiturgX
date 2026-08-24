package com.willykez.liturgx.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.core.RegionSettings
import com.willykez.liturgx.data.LectionaryRepository
import com.willykez.liturgx.ui.theme.seasonAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val CELL_SIZE = 40.dp
private const val WEEK_ROWS = 6 // always render 6 rows so every month page is the same height

/** Monday-first, matching the reference layout this was rebuilt against -- English 3-letter
 *  abbreviations were an explicit, deliberate choice for this header specifically (unlike the
 *  rest of the app's Swahili UI): Swahili's first four weekday names all start "Juma-", which
 *  abbreviated to 3 Swahili letters collapses four different days down to the same "Jum" label. */
private val WEEKDAYS = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
)
private val WEEKDAY_LABELS = mapOf(
    DayOfWeek.MONDAY to "Mon", DayOfWeek.TUESDAY to "Tue", DayOfWeek.WEDNESDAY to "Wed",
    DayOfWeek.THURSDAY to "Thu", DayOfWeek.FRIDAY to "Fri", DayOfWeek.SATURDAY to "Sat",
    DayOfWeek.SUNDAY to "Sun"
)

/**
 * One month page of the calendar. Always renders exactly [WEEK_ROWS] week-rows regardless of
 * how many the month actually needs (4-6) so swiping between months never jumps in height.
 *
 * Every day gets a small dot in *that day's own* resolved liturgical color -- not just days
 * with a named saint, unlike the previous version -- giving an at-a-glance view of how a
 * season's colors actually run across the month. That's [WEEK_ROWS] * 7 = 42 real lectionary
 * lookups per page, computed off the main thread via [produceState] so swiping can't stutter
 * waiting on them; the dots fill in a frame or two after the page appears instead of blocking it.
 *
 * Today gets a thin ring in its own color; the selected day (if different from today) gets a
 * solid fill in the current app accent, independent of that day's own color -- selection is a
 * UI state, not a liturgical fact, so it deliberately doesn't borrow the day's own color the way
 * the dot does. Today's weekday column header is highlighted in today's color too.
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

    val dayColors by produceState(initialValue = emptyMap<Int, LiturgicalColor>(), month, region) {
        value = withContext(Dispatchers.IO) {
            (1..month.lengthOfMonth()).associateWith { day ->
                repository.getForDate(month.atDay(day), region).resolved.color
            }
        }
    }

    val firstOfMonth = month.atDay(1)
    // Monday-first column index: Monday -> 0, ... Sunday -> 6.
    val firstDayColumn = firstOfMonth.dayOfWeek.value - 1
    val daysInMonth = month.lengthOfMonth()
    val todayIsInThisMonth = YearMonth.from(today) == month

    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            WEEKDAYS.forEach { dow ->
                val isTodayColumn = todayIsInThisMonth && dow == today.dayOfWeek
                Text(
                    WEEKDAY_LABELS.getValue(dow),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isTodayColumn) FontWeight.Bold else FontWeight.Normal,
                    color = if (isTodayColumn) seasonAccent(dayColors[today.dayOfMonth] ?: LiturgicalColor.KIJANI) else onBgDim
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        for (week in 0 until WEEK_ROWS) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                for (column in 0 until 7) {
                    val dayOfMonth = week * 7 + column - firstDayColumn + 1
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (dayOfMonth in 1..daysInMonth) {
                            val date = month.atDay(dayOfMonth)
                            DayCell(
                                day = dayOfMonth,
                                isSelected = date == selectedDate,
                                isToday = date == today,
                                dotColor = dayColors[dayOfMonth]?.let { seasonAccent(it) },
                                selectionAccent = accent,
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
    dotColor: Color?,
    selectionAccent: Color,
    onBg: Color,
    onClick: () -> Unit
) {
    val background = MaterialTheme.colorScheme.background
    val ringColor = dotColor ?: selectionAccent

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
                .then(
                    when {
                        isSelected -> Modifier.background(selectionAccent)
                        isToday -> Modifier.border(1.5.dp, ringColor, CircleShape)
                        else -> Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                day.toString(),
                color = if (isSelected) background else onBg,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(dotColor ?: Color.Transparent)
        )
    }
}
