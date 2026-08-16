package com.willykez.liturgx.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.willykez.liturgx.data.DayResult
import com.willykez.liturgx.ui.components.DailyReadingsView
import com.willykez.liturgx.ui.components.SeasonBackdrop

@Composable
fun HomeScreen(todayResult: DayResult, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        SeasonBackdrop(todayResult.resolved.color)
        DailyReadingsView(dayResult = todayResult, showDateNav = false)
    }
}
