package com.willykez.liturgx.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.RegionSettings
import com.willykez.liturgx.core.SwahiliDate
import com.willykez.liturgx.data.DayResult
import com.willykez.liturgx.data.LectionaryRepository
import com.willykez.liturgx.ui.components.DailyReadingsView
import com.willykez.liturgx.ui.components.SeasonBackdrop
import com.willykez.liturgx.ui.theme.seasonAccent
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

// Wide but bounded window -- swiping to the literal edge just stops, same as any real calendar.
private val PAGER_START_MONTH: YearMonth = YearMonth.of(1970, 1)
private val PAGER_END_MONTH: YearMonth = YearMonth.of(2100, 12)
private val PAGE_COUNT: Int =
    ChronoUnit.MONTHS.between(PAGER_START_MONTH, PAGER_END_MONTH).toInt() + 1

private fun monthForPage(page: Int): YearMonth = PAGER_START_MONTH.plusMonths(page.toLong())
private fun pageForMonth(month: YearMonth): Int =
    ChronoUnit.MONTHS.between(PAGER_START_MONTH, month).toInt().coerceIn(0, PAGE_COUNT - 1)

/**
 * A real visual month calendar (Sunday-first grid, swipe left/right between months, week-number
 * gutter, today/selected/notable-day markers) with the tapped day's full readings docked below
 * it -- modeled directly on the platform Calendar app's month view + day agenda, rather than a
 * bare [DatePickerDialog] (kept here only as a secondary "jump to a specific date" shortcut,
 * since swiping month-by-month to a date a year away is impractical).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    selectedResult: DayResult,
    region: RegionSettings,
    onSelectDate: (LocalDate) -> Unit,
    onJumpToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { LectionaryRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var showPicker by remember { mutableStateOf(false) }

    val selectedDate = selectedResult.resolved.date
    val today = LocalDate.now()
    val accent = seasonAccent(selectedResult.resolved.color)
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant

    val pagerState = rememberPagerState(
        initialPage = pageForMonth(YearMonth.from(selectedDate)),
        pageCount = { PAGE_COUNT }
    )

    // Keep the pager following the selected date when it changes from outside a swipe --
    // the "Leo" button, or the date-picker shortcut below.
    LaunchedEffect(selectedDate) {
        val targetPage = pageForMonth(YearMonth.from(selectedDate))
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    val visibleMonth = monthForPage(pagerState.currentPage)

    Box(modifier.fillMaxSize()) {
        SeasonBackdrop(selectedResult.resolved.color)
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        SwahiliDate.monthName(visibleMonth.monthValue),
                        style = MaterialTheme.typography.headlineMedium,
                        color = onBg
                    )
                    Text(
                        visibleMonth.year.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = onBgDim
                    )
                }
                IconButton(onClick = { showPicker = true }) {
                    Icon(Icons.Filled.EditCalendar, contentDescription = "Chagua tarehe mahususi", tint = onBgDim)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                MonthGrid(
                    month = monthForPage(page),
                    repository = repository,
                    region = region,
                    selectedDate = selectedDate,
                    today = today,
                    accent = accent,
                    onDayClick = onSelectDate,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                relativeDayLabel(selectedDate, today),
                style = MaterialTheme.typography.labelMedium,
                color = onBgDim,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(4.dp))

            DailyReadingsView(
                dayResult = selectedResult,
                modifier = Modifier.weight(1f)
            )
        }

        TextButton(
            onClick = { onJumpToToday(); scope.launch { pagerState.animateScrollToPage(pageForMonth(YearMonth.from(today))) } },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .clip(RoundedCornerShape(50))
                .background(onBg.copy(alpha = 0.08f))
        ) {
            Icon(Icons.Filled.Today, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Leo", color = accent)
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onSelectDate(date)
                    }
                    showPicker = false
                }) { Text("Nenda") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Ghairi") } }
        ) {
            DatePicker(state = state)
        }
    }
}

private fun relativeDayLabel(date: LocalDate, today: LocalDate): String {
    val head = "${SwahiliDate.weekdayName(date.dayOfWeek)}, ${SwahiliDate.monthName(date.monthValue)} ${date.dayOfMonth}"
    val days = ChronoUnit.DAYS.between(date, today)
    return when {
        days == 0L -> "$head, Leo"
        days == 1L -> "$head, jana"
        days == -1L -> "$head, kesho"
        days > 1L -> "$head, siku $days zilizopita"
        else -> "$head, baada ya siku ${-days}"
    }
}
