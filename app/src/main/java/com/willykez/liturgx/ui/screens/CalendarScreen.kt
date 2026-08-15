package com.willykez.liturgx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.liturgx.data.engine.LiturgicalCalendarEngine
import com.willykez.liturgx.ui.viewmodel.LiturgicalViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: LiturgicalViewModel,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    var displayMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "LiturgX",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Month Header Controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { displayMonth = displayMonth.minusMonths(1) },
                            modifier = Modifier.testTag("prev_month_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                        }

                        Text(
                            text = displayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { displayMonth = displayMonth.plusMonths(1) },
                            modifier = Modifier.testTag("next_month_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Days of Week Header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                        for (day in daysOfWeek) {
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid Days
                    val firstDayOfMonth = displayMonth.atDay(1)
                    val daysInMonth = displayMonth.lengthOfMonth()
                    val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value % 7

                    val totalCells = dayOfWeekOffset + daysInMonth

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        userScrollEnabled = false
                    ) {
                        items(totalCells) { index ->
                            if (index >= dayOfWeekOffset) {
                                val dayNum = index - dayOfWeekOffset + 1
                                val currentDate = displayMonth.atDay(dayNum)
                                val isSelected = currentDate == selectedDate
                                val dayInfo = LiturgicalCalendarEngine.dayInfo(currentDate)
                                val litColor = Color(dayInfo.color.hexColor)

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else litColor.copy(alpha = 0.15f)
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else litColor.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            viewModel.selectDate(currentDate)
                                            onDateSelected(currentDate)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNum.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else litColor)
                                        )
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.aspectRatio(1f))
                            }
                        }
                    }
                }
            }

            // Major Upcoming Liturgical Solemnities & Feasts Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Upcoming Solemnities & Feasts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val today = LocalDate.now()
                    val anchors = LiturgicalCalendarEngine.anchorsFor(today.year)
                    val nextAnchors = LiturgicalCalendarEngine.anchorsFor(today.year + 1)
                    val dateFmt = DateTimeFormatter.ofPattern("MMM d")

                    // Fixed-date solemnities (same calendar date every year) + this year's/next year's
                    // movable feasts computed from the Easter-relative anchors — no hardcoded dates.
                    val candidates = listOf(
                        LocalDate.of(today.year, 1, 1) to "Solemnity of Mary, Mother of God",
                        LocalDate.of(today.year, 3, 19) to "Solemnity of St. Joseph",
                        LocalDate.of(today.year, 3, 25) to "The Annunciation of the Lord",
                        anchors.palmSunday to "Palm Sunday",
                        anchors.easterSunday to "Easter Sunday",
                        anchors.pentecost to "Pentecost Sunday",
                        anchors.trinitySunday to "The Most Holy Trinity",
                        LocalDate.of(today.year, 8, 15) to "Assumption of the Blessed Virgin Mary",
                        LocalDate.of(today.year, 11, 1) to "All Saints Day",
                        LocalDate.of(today.year, 12, 8) to "Immaculate Conception",
                        LocalDate.of(today.year, 12, 25) to "Nativity of the Lord (Christmas)",
                        LocalDate.of(today.year + 1, 1, 1) to "Solemnity of Mary, Mother of God",
                        nextAnchors.easterSunday to "Easter Sunday"
                    )

                    val upcoming = candidates
                        .filter { (date, _) -> !date.isBefore(today) }
                        .sortedBy { it.first }
                        .distinctBy { it.second }
                        .take(6)

                    upcoming.forEach { (date, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = date.format(dateFmt),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
