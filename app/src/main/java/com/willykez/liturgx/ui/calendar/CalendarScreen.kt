package com.willykez.liturgx.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
 * A real visual month calendar (Monday-first grid, swipe or tap the chevrons to change months,
 * today/selected markers, every day's own liturgical color as a small dot) with the tapped
 * day's full readings docked below it -- modeled on the platform Calendar app's month view + day
 * agenda, rather than a bare [DatePickerDialog] (kept here only as a secondary "jump to a
 * specific date" shortcut, since swiping month-by-month to a date a year away is impractical).
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
    // the "Leo" button, the chevrons, or the date-picker shortcut below.
    LaunchedEffect(selectedDate) {
        val targetPage = pageForMonth(YearMonth.from(selectedDate))
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    val visibleMonth = monthForPage(pagerState.currentPage)

    fun stepMonth(delta: Long) {
        scope.launch {
            pagerState.animateScrollToPage((pagerState.currentPage + delta).toInt().coerceIn(0, PAGE_COUNT - 1))
        }
    }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${SwahiliDate.monthName(visibleMonth.monthValue)} ${visibleMonth.year}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = onBg
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showPicker = true }) {
                        Icon(Icons.Filled.EditCalendar, contentDescription = "Chagua tarehe mahususi", tint = onBgDim)
                    }
                    IconButton(onClick = { stepMonth(-1) }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Mwezi uliopita", tint = onBg)
                    }
                    IconButton(onClick = { stepMonth(1) }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Mwezi ujao", tint = onBg)
                    }
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

            Spacer(Modifier.height(10.dp))
            DaySummaryRow(selectedResult, accent, onBg, onBgDim)
            Spacer(Modifier.height(4.dp))

            DailyReadingsView(
                dayResult = selectedResult,
                showHeader = false,
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

/** The compact date + title + color-pill summary shown between the grid and the full readings
 *  below -- the day's title comes straight from [DayResult], same source [DailyReadingsView]'s
 *  own (now-suppressed-here-via-`showHeader=false`) header would have used. */
@Composable
private fun DaySummaryRow(
    dayResult: DayResult,
    accent: androidx.compose.ui.graphics.Color,
    onBg: androidx.compose.ui.graphics.Color,
    onBgDim: androidx.compose.ui.graphics.Color
) {
    val resolved = dayResult.resolved
    val d = resolved.date
    val dateHead = "${SwahiliDate.weekdayName(d.dayOfWeek)}, ${d.dayOfMonth} ${SwahiliDate.monthName(d.monthValue)} ${d.year}"
    val title = resolved.overridingSaint?.jina ?: resolved.label
    val colorName = resolved.color.swahili.replaceFirstChar { it.uppercase() }

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(Modifier.weight(1f)) {
            Text(dateHead, style = MaterialTheme.typography.labelMedium, color = onBgDim)
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, color = onBg)
        }
        Spacer(Modifier.width(12.dp))
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(accent.copy(alpha = 0.16f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(6.dp))
            Text(colorName, style = MaterialTheme.typography.labelMedium, color = accent)
        }
    }
}
