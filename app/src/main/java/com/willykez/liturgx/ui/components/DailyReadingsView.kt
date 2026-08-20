package com.willykez.liturgx.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.outlined.Image
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
import com.willykez.liturgx.core.SwahiliDate
import com.willykez.liturgx.data.DayResult
import com.willykez.liturgx.data.bible.BibleRepository
import com.willykez.liturgx.data.sharing.DailyReadingPdfGenerator
import com.willykez.liturgx.data.sharing.DailyReadingShareFormatter
import com.willykez.liturgx.data.sharing.DayCardReading
import com.willykez.liturgx.ui.theme.seasonAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class SharePreparing { NONE, TEXT, PDF }

@Composable
fun DailyReadingsView(
    dayResult: DayResult,
    extraHeaderContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val resolved = dayResult.resolved
    val accent = seasonAccent(resolved.color)
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    val d = resolved.date
    val dateLine = "${SwahiliDate.weekdayName(d.dayOfWeek)}, ${d.dayOfMonth} ${SwahiliDate.monthName(d.monthValue)} ${d.year}"
    val seasonLabel = resolved.overridingSaint?.jina ?: resolved.label

    val context = LocalContext.current
    val repository = remember { BibleRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var preparing by remember(dayResult) { mutableStateOf(SharePreparing.NONE) }
    var showShareMenu by remember(dayResult) { mutableStateOf(false) }
    var showImageCard by remember(dayResult) { mutableStateOf(false) }
    var dayCardReadings by remember(dayResult) { mutableStateOf<List<DayCardReading>?>(null) }
    var pdfFile by remember(dayResult) { mutableStateOf<java.io.File?>(null) }
    var showPdfPreview by remember(dayResult) { mutableStateOf(false) }
    val items = ReadingPresenter.present(dayResult.readings)

    suspend fun resolveDayCardReadings(): List<DayCardReading> =
        withContext(Dispatchers.IO) {
            items.map { item ->
                val passage = repository.getPassage(item.citation)
                DayCardReading(
                    kindLabel = item.label,
                    citation = item.citation,
                    passageText = passage?.renderedText() ?: item.citation,
                    responseText = ReadingPresenter.massResponseFor(item.kindKey)
                )
            }
        }

    fun shareAsText() {
        if (preparing != SharePreparing.NONE) return
        preparing = SharePreparing.TEXT
        scope.launch {
            val passages = withContext(Dispatchers.IO) {
                items.associate { it.citation to repository.getPassage(it.citation) }
            }
            val text = DailyReadingShareFormatter.format(dayResult, dateLine, items, passages)
            preparing = SharePreparing.NONE
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Shiriki Masomo"))
        }
    }

    fun shareAsImage() {
        if (preparing != SharePreparing.NONE) return
        scope.launch {
            dayCardReadings = resolveDayCardReadings()
            showImageCard = true
        }
    }

    fun shareAsPdf() {
        if (preparing != SharePreparing.NONE) return
        preparing = SharePreparing.PDF
        scope.launch {
            val readings = resolveDayCardReadings()
            val file = withContext(Dispatchers.IO) {
                DailyReadingPdfGenerator.generate(context, dateLine, seasonLabel, resolved.color, readings)
            }
            preparing = SharePreparing.NONE
            pdfFile = file
            showPdfPreview = true
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
                    Spacer(Modifier.weight(1f))
                    Box {
                        IconButton(onClick = { showShareMenu = true }, enabled = preparing == SharePreparing.NONE) {
                            if (preparing != SharePreparing.NONE) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = accent)
                            } else {
                                Icon(Icons.Filled.IosShare, contentDescription = "Shiriki masomo ya siku", tint = onBgDim)
                            }
                        }
                        DropdownMenu(expanded = showShareMenu, onDismissRequest = { showShareMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Shiriki kama Maandishi") },
                                leadingIcon = { Icon(Icons.Filled.TextSnippet, contentDescription = null) },
                                onClick = { showShareMenu = false; shareAsText() }
                            )
                            DropdownMenuItem(
                                text = { Text("Shiriki kama Picha") },
                                leadingIcon = { Icon(Icons.Outlined.Image, contentDescription = null) },
                                onClick = { showShareMenu = false; shareAsImage() }
                            )
                            DropdownMenuItem(
                                text = { Text("Hifadhi kama PDF") },
                                leadingIcon = { Icon(Icons.Filled.PictureAsPdf, contentDescription = null) },
                                onClick = { showShareMenu = false; shareAsPdf() }
                            )
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
                            seasonLabel,
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
                seasonLabel = seasonLabel,
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

    val cardReadings = dayCardReadings
    if (showImageCard && cardReadings != null) {
        DailyShareCardDialog(
            dateText = dateLine,
            seasonText = seasonLabel,
            readings = cardReadings,
            liturgicalColor = resolved.color,
            onDismiss = { showImageCard = false }
        )
    }

    val currentPdfFile = pdfFile
    if (showPdfPreview && currentPdfFile != null) {
        PdfPreviewDialog(
            file = currentPdfFile,
            color = resolved.color,
            onDismiss = { showPdfPreview = false }
        )
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
