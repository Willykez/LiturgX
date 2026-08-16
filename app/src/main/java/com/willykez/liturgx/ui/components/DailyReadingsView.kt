package com.willykez.liturgx.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.ReadingPresenter
import com.willykez.liturgx.data.DayResult
import com.willykez.liturgx.ui.theme.Parchment
import com.willykez.liturgx.ui.theme.ParchmentDim
import com.willykez.liturgx.ui.theme.seasonAccent

private val swMonths = listOf(
    "Januari", "Februari", "Machi", "Aprili", "Mei", "Juni",
    "Julai", "Agosti", "Septemba", "Oktoba", "Novemba", "Desemba"
)
private val swWeekdays = listOf("Jumatatu", "Jumanne", "Jumatano", "Alhamisi", "Ijumaa", "Jumamosi", "Jumapili")

@Composable
fun DailyReadingsView(
    dayResult: DayResult,
    showDateNav: Boolean = false,
    onPrevDay: () -> Unit = {},
    onNextDay: () -> Unit = {},
    onJumpToToday: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val resolved = dayResult.resolved
    val accent = seasonAccent(resolved.color)
    val d = resolved.date
    val dateLine = "${swWeekdays[d.dayOfWeek.value - 1]}, ${d.dayOfMonth} ${swMonths[d.monthValue - 1]} ${d.year}"

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                if (showDateNav) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onPrevDay) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = "Siku iliyopita", tint = Parchment)
                        }
                        TextButton(onClick = onJumpToToday) {
                            Icon(Icons.Filled.Today, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Leo", color = accent, style = MaterialTheme.typography.labelLarge)
                        }
                        IconButton(onClick = onNextDay) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = "Siku ijayo", tint = Parchment)
                        }
                    }
                }

                Text(dateLine, style = MaterialTheme.typography.labelMedium, color = ParchmentDim)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiturgicalSeal(resolved.color, size = 40.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            resolved.overridingSaint?.jina ?: resolved.label,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Parchment
                        )
                        Text(
                            "Rangi ya LiturgX: ${resolved.color.swahili}"
                                    + (resolved.cycleYear?.let { " · Mwaka $it" } ?: "")
                                    + (resolved.weekdayCycle?.let { " · Mzunguko $it" } ?: ""),
                            style = MaterialTheme.typography.labelSmall,
                            color = accent
                        )
                    }
                }

                resolved.overridingSaint?.let { saint ->
                    Spacer(Modifier.height(10.dp))
                    AssistChip(text = "${saint.daraja} — ${resolved.label}", accentHex = accent)
                }
                dayResult.optionalMemorial?.let { memorial ->
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = ParchmentDim, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Leo pia ni ukumbusho wa hiari wa ${memorial.jina}",
                            style = MaterialTheme.typography.labelMedium,
                            color = ParchmentDim,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }

        val items = ReadingPresenter.present(dayResult.readings)
        items(items) { item ->
            ReadingBlock(
                kind = kindFor(item.kindKey),
                citation = item.citation,
                color = resolved.color,
                label = item.label
            )
        }

        if (items.isEmpty()) {
            item {
                Text(
                    "Hamna masomo yaliyopatikana kwa siku hii kwenye hifadhidata.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentDim
                )
            }
        }
    }
}

@Composable
private fun AssistChip(text: String, accentHex: androidx.compose.ui.graphics.Color) {
    Surface(
        color = accentHex.copy(alpha = 0.18f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = accentHex
        )
    }
}

private fun kindFor(key: String) = com.willykez.liturgx.ui.components.ReadingKind.entries.first {
    it.name == key
}
