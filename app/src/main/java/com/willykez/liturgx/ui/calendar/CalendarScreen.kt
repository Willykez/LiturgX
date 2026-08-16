package com.willykez.liturgx.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.data.DayResult
import com.willykez.liturgx.ui.components.DailyReadingsView
import com.willykez.liturgx.ui.components.SeasonBackdrop
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    selectedResult: DayResult,
    onSelectDate: (LocalDate) -> Unit,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onJumpToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize()) {
        SeasonBackdrop(selectedResult.resolved.color)
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showPicker = true }) {
                    Icon(Icons.Filled.EditCalendar, contentDescription = null)
                    Text("  Chagua Tarehe")
                }
            }
            DailyReadingsView(
                dayResult = selectedResult,
                showDateNav = true,
                onPrevDay = onPrevDay,
                onNextDay = onNextDay,
                onJumpToToday = onJumpToToday,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = selectedResult.resolved.date
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
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
