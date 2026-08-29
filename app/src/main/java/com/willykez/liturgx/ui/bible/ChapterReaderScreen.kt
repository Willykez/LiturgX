package com.willykez.liturgx.ui.bible

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.data.bible.BibleBookInfo
import com.willykez.liturgx.data.bible.BibleBrowseRepository
import com.willykez.liturgx.data.bible.ChapterLine
import com.willykez.liturgx.data.sharing.DailyReadingPdfGenerator
import com.willykez.liturgx.data.sharing.DayCardReading
import com.willykez.liturgx.ui.components.PdfPreviewDialog
import com.willykez.liturgx.ui.components.ShareCardDialog
import com.willykez.liturgx.ui.theme.seasonAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Renders a chapter verse-by-verse in true reading order (see
 * [BibleBrowseRepository.chapter]'s doc for why that's `rank`, not `position`). When arriving
 * from a search hit, [scrollToVerse] auto-scrolls to and highlights that verse.
 *
 * Tapping a verse toggles it into a selection set; tapping further verses adds them too --
 * contiguous taps merge into one range, a distant tap starts a second one, so a real Lectionary
 * citation shape like "Zaburi 33:12-13, 18-19, 20-21" (several separate verse groups, not one
 * span) is directly reachable by tapping exactly those verses. [groupIntoRanges] does the
 * merging; nothing about it is order-dependent, so deselecting and re-selecting verses always
 * settles into the same minimal citation. The action row appears inline, right under whichever
 * verse was tapped most recently -- same placement as the single-verse version had.
 */
@Composable
fun ChapterReaderScreen(
    book: BibleBookInfo,
    chapterNum: Int,
    scrollToVerse: Int?,
    color: LiturgicalColor,
    repository: BibleBrowseRepository,
    onBack: () -> Unit,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = seasonAccent(color)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lines = remember(book.id, chapterNum) { repository.chapter(book.id, chapterNum) }
    val listState = rememberLazyListState()

    var selectedVerses by remember(book.id, chapterNum) { mutableStateOf<Set<Int>>(emptySet()) }
    var lastTappedVerse by remember(book.id, chapterNum) { mutableStateOf<Int?>(null) }
    var isPreparingPdf by remember { mutableStateOf(false) }
    var showImageShare by remember { mutableStateOf(false) }
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var showPdfPreview by remember { mutableStateOf(false) }

    LaunchedEffect(book.id, chapterNum, scrollToVerse) {
        selectedVerses = emptySet()
        lastTappedVerse = null
        if (scrollToVerse != null) {
            val index = lines.indexOfFirst { !it.isHeading && it.position == scrollToVerse }
            if (index >= 0) listState.animateScrollToItem(index)
        } else {
            listState.scrollToItem(0)
        }
    }

    fun toggleVerse(position: Int) {
        selectedVerses = if (position in selectedVerses) selectedVerses - position else selectedVerses + position
        lastTappedVerse = if (selectedVerses.isEmpty()) null else position
    }

    val selectionGroups = remember(selectedVerses) {
        groupIntoRanges(selectedVerses).map { range -> lines.filter { !it.isHeading && it.position in range } }
    }
    val selectionIsPoetic = selectionGroups.flatten().any { it.text.contains('\n') }
    val selectionText = selectionGroups.joinToString("\n\n") { group ->
        group.joinToString(if (selectionIsPoetic) "\n" else " ") { it.text }
    }
    val selectionCitation = remember(selectedVerses) {
        citationFor(book.name, chapterNum, selectedVerses)
    }

    Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Rudi kwenye sura", tint = onBg)
            }
            Text(
                "${book.name} $chapterNum",
                style = MaterialTheme.typography.titleMedium,
                color = onBg,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onPrevChapter, enabled = chapterNum > 1) {
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = "Sura iliyopita",
                    tint = if (chapterNum > 1) onBg else onBgDim.copy(alpha = 0.3f)
                )
            }
            IconButton(onClick = onNextChapter, enabled = chapterNum < book.chapterCount) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "Sura ijayo",
                    tint = if (chapterNum < book.chapterCount) onBg else onBgDim.copy(alpha = 0.3f)
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(lines) { line ->
                if (line.isHeading) {
                    Text(
                        line.text,
                        style = MaterialTheme.typography.titleSmall,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
                    )
                } else {
                    Column {
                        VerseLine(
                            line = line,
                            emphasized = (scrollToVerse != null && line.position == scrollToVerse) ||
                                line.position in selectedVerses,
                            accent = accent,
                            onBg = onBg,
                            onTap = { toggleVerse(line.position) }
                        )
                        AnimatedVisibility(
                            visible = lastTappedVerse == line.position && selectionCitation != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            if (selectionCitation != null) {
                                SelectionActionRow(
                                    citation = selectionCitation,
                                    text = selectionText,
                                    isPreparingPdf = isPreparingPdf,
                                    accent = accent,
                                    onBgDim = onBgDim,
                                    onCopy = {
                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cm.setPrimaryClip(ClipData.newPlainText("Andiko", "$selectionCitation\n\n$selectionText"))
                                    },
                                    onShareText = {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "$selectionCitation\n\n$selectionText\n\nImetumwa kutoka LiturgX")
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Shiriki Andiko"))
                                    },
                                    onShareImage = { showImageShare = true },
                                    onSharePdf = {
                                        if (!isPreparingPdf) {
                                            isPreparingPdf = true
                                            scope.launch {
                                                val citation = selectionCitation
                                                val file = withContext(Dispatchers.IO) {
                                                    DailyReadingPdfGenerator.generate(
                                                        context = context,
                                                        dateText = book.name,
                                                        seasonText = "BIBLIA",
                                                        color = color,
                                                        readings = listOf(
                                                            DayCardReading(
                                                                kindLabel = "Andiko",
                                                                citation = citation,
                                                                passageText = selectionText,
                                                                responseText = null
                                                            )
                                                        )
                                                    )
                                                }
                                                isPreparingPdf = false
                                                pdfFile = file
                                                showPdfPreview = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showImageShare && selectionCitation != null) {
        ShareCardDialog(
            dateText = book.name,
            seasonText = "BIBLIA",
            kindLabel = "Andiko",
            citation = selectionCitation,
            passage = selectionText,
            responseText = null,
            liturgicalColor = color,
            onDismiss = { showImageShare = false }
        )
    }

    val currentPdfFile = pdfFile
    if (showPdfPreview && currentPdfFile != null) {
        PdfPreviewDialog(
            file = currentPdfFile,
            color = color,
            onDismiss = { showPdfPreview = false }
        )
    }
}

/** Sorted positions collapse into the fewest possible contiguous ranges -- {12,13,18,19} becomes
 *  [12..13, 18..19], never something order-dependent on which verse was tapped first or last. */
private fun groupIntoRanges(positions: Set<Int>): List<IntRange> {
    if (positions.isEmpty()) return emptyList()
    val sorted = positions.sorted()
    val ranges = mutableListOf<IntRange>()
    var start = sorted[0]
    var prev = sorted[0]
    for (i in 1 until sorted.size) {
        val current = sorted[i]
        if (current == prev + 1) {
            prev = current
        } else {
            ranges += start..prev
            start = current
            prev = current
        }
    }
    ranges += start..prev
    return ranges
}

private fun citationFor(bookName: String, chapterNum: Int, positions: Set<Int>): String? {
    val ranges = groupIntoRanges(positions)
    if (ranges.isEmpty()) return null
    val parts = ranges.joinToString(", ") { r -> if (r.first == r.last) "${r.first}" else "${r.first}-${r.last}" }
    return "$bookName $chapterNum:$parts"
}

@Composable
private fun VerseLine(
    line: ChapterLine,
    emphasized: Boolean,
    accent: Color,
    onBg: Color,
    onTap: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (emphasized) accent.copy(alpha = 0.16f) else Color.Transparent)
            .clickable(onClick = onTap)
            .padding(vertical = 3.dp, horizontal = if (emphasized) 6.dp else 0.dp)
    ) {
        Text(
            line.position.toString(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = accent,
            modifier = Modifier
                .padding(top = 4.dp, end = 6.dp)
                .width(20.dp)
        )
        Text(
            line.text,
            style = MaterialTheme.typography.bodyLarge,
            color = onBg,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SelectionActionRow(
    citation: String,
    text: String,
    isPreparingPdf: Boolean,
    accent: Color,
    onBgDim: Color,
    onCopy: () -> Unit,
    onShareText: () -> Unit,
    onShareImage: () -> Unit,
    onSharePdf: () -> Unit
) {
    Column(Modifier.padding(start = 26.dp, top = 2.dp, bottom = 8.dp)) {
        Text(citation, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = accent)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.ContentCopy, contentDescription = "Nakili", tint = onBgDim, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onShareText, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.IosShare, contentDescription = "Shiriki kama maandishi", tint = onBgDim, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onShareImage, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Image, contentDescription = "Shiriki kama picha", tint = onBgDim, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onSharePdf, enabled = !isPreparingPdf, modifier = Modifier.size(32.dp)) {
                if (isPreparingPdf) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = accent)
                } else {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = "Hifadhi kama PDF", tint = onBgDim, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
