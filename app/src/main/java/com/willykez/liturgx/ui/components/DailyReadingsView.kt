package com.willykez.liturgx.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.ReadingPresenter
import com.willykez.liturgx.data.DayResult
import com.willykez.liturgx.data.bible.BibleRepository
import com.willykez.liturgx.data.sharing.DailyReadingShareFormatter
import com.willykez.liturgx.ui.theme.seasonAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    extraHeaderContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val resolved = dayResult.resolved
    val accent = seasonAccent(resolved.color)
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    val d = resolved.date
    val dateLine = "${swWeekdays[d.dayOfWeek.value - 1]}, ${d.dayOfMonth} ${swMonths[d.monthValue - 1]} ${d.year}"

    val context = LocalContext.current
    val repository = remember { BibleRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var isSharing by remember(dayResult) { mutableStateOf(false) }
    val items = ReadingPresenter.present(dayResult.readings)

    fun shareDay() {
        if (isSharing) return
        isSharing = true
        scope.launch {
            val passages = withContext(Dispatchers.IO) {
                items.associate { it.citation to repository.getPassage(it.citation) }
            }
            val text = DailyReadingShareFormatter.format(dayResult, dateLine, items, passages)
            isSharing = false
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Shiriki Masomo"))
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showDateNav) {
                        IconButton(onClick = onPrevDay) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = "Siku iliyopita", tint = onBg)
                        }
                        TextButton(onClick = onJumpToToday) {
                            Icon(Icons.Filled.Today, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Leo", color = accent, style = MaterialTheme.typography.labelLarge)
                        }
                        IconButton(onClick = onNextDay) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = "Siku ijayo", tint = onBg)
                        }
                        Spacer(Modifier.weight(1f))
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    IconButton(onClick = { shareDay() }, enabled = !isSharing) {
                        if (isSharing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = accent)
                        } else {
                            Icon(Icons.Filled.IosShare, contentDescription = "Shiriki masomo ya siku", tint = onBgDim)
                        }
                    }
                }

                Text(dateLine, style = MaterialTheme.typography.labelMedium, color = onBgDim)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiturgicalSeal(resolved.color, size = 40.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            resolved.overridingSaint?.jina ?: resolved.label,
                            style = MaterialTheme.typography.headlineSmall,
                            color = onBg
                        )
                        Text(
                            "Rangi ya Liturujia: ${resolved.color.swahili}"
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
                        Icon(Icons.Filled.Star, contentDescription = null, tint = onBgDim, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Leo pia ni ukumbusho wa hiari wa ${memorial.jina}",
                            style = MaterialTheme.typography.labelMedium,
                            color = onBgDim,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }

        extraHeaderContent?.let { content ->
            item { content() }
        }

        items(items) { item ->
            ReadingBlock(
                kind = kindFor(item.kindKey),
                citation = item.citation,
                color = resolved.color,
                dateText = dateLine,
                seasonLabel = resolved.overridingSaint?.jina ?: resolved.label,
                label = item.label
            )
        }

        if (items.isEmpty()) {
            item {
                Text(
                    "Hamna masomo yaliyopatikana kwa siku hii kwenye hifadhidata.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onBgDim
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

private fun kindFor(key: String) = ReadingKind.entries.first { it.name == key }
