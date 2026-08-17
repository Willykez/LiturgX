package com.willykez.liturgx.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.willykez.liturgx.data.DayResult
import com.willykez.liturgx.data.ProgressStore
import com.willykez.liturgx.ui.components.DailyReadingsView
import com.willykez.liturgx.ui.components.SeasonBackdrop
import com.willykez.liturgx.ui.components.StreakCard
import java.time.LocalDate

@Composable
fun HomeScreen(todayResult: DayResult, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val progressStore = remember { ProgressStore(context) }
    var openedDates by remember { mutableStateOf<Set<LocalDate>>(emptySet()) }

    // Home only ever shows today, so viewing it IS "opened today's reading" -- the one event
    // the streak feature cares about. Re-runs if the calendar day rolls over mid-session.
    LaunchedEffect(todayResult.resolved.date) {
        progressStore.recordOpen(todayResult.resolved.date)
        openedDates = progressStore.openedDates()
    }

    Box(modifier.fillMaxSize()) {
        SeasonBackdrop(todayResult.resolved.color)
        DailyReadingsView(
            dayResult = todayResult,
            showDateNav = false,
            extraHeaderContent = {
                StreakCard(
                    openedDates = openedDates,
                    today = todayResult.resolved.date,
                    color = todayResult.resolved.color
                )
            }
        )
    }
}
