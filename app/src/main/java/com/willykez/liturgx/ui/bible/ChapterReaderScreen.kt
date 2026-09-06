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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.data.bible.BibleBookInfo
import com.willykez.liturgx.data.bible.BibleBrowseRepository
import com.willykez.liturgx.data.bible.BibleUserDataStore
import com.willykez.liturgx.data.bible.ChapterLine
import com.willykez.liturgx.data.bible.ReadingPrefsStore
import com.willykez.liturgx.data.bible.ScriptureFontStyle
import com.willykez.liturgx.data.bible.verseKey
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
 * settles into the same minimal citation. The action row expands/collapses in place right under
 * whichever verse was tapped most recently, with the same fade+expand transition as the
 * original single-verse version -- now carrying a verse count, a clear-selection button, and
 * bookmark/highlight/note actions alongside the existing copy/share ones.
 *
 * Reading preferences (font style, verse numbers, paragraph mode) and the bookmark/highlight/
 * note actions in the selection row are read straight from [ReadingPrefsStore] and
 * [BibleUserDataStore] on every recomposition -- deliberately not lifted into a ViewModel, to
 * match this screen's existing lightweight, store-backed state style (see [BibleScreen]'s doc).
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

    val readingPrefs = remember { ReadingPrefsStore(context) }
    val userData = remember { BibleUserDataStore(context) }
    val haptics = LocalHapticFeedback.current
    val fontStyle = readingPrefs.loadFontStyle()
    val verseNumbersVisible = readingPrefs.loadVerseNumbersVisible()
    val paragraphMode = readingPrefs.loadParagraphMode()
    val scriptureFont = when (fontStyle) {
        ScriptureFontStyle.SERIF -> FontFamily.Serif
        ScriptureFontStyle.SANS -> FontFamily.SansSerif
        ScriptureFontStyle.MONO -> FontFamily.Monospace
    }

    var selectedVerses by remember(book.id, chapterNum) { mutableStateOf<Set<Int>>(emptySet()) }
    var lastTappedVerse by remember(book.id, chapterNum) { mutableStateOf<Int?>(null) }
    var isPreparingPdf by remember { mutableStateOf(false) }
    var showImageShare by remember { mutableStateOf(false) }
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var showPdfPreview by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    // Bumped after any bookmark/highlight/note write so the rows relying on the store recompose
    // (SharedPreferences reads aren't observable to Compose on their own).
    var userDataVersion by remember { mutableStateOf(0) }

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
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        selectedVerses = if (position in selectedVerses) selectedVerses - position else selectedVerses + position
        lastTappedVerse = if (selectedVerses.isEmpty()) null else position
    }

    fun keyFor(position: Int) = verseKey(book.id, chapterNum, position)

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
    val allSelectedBookmarked = userDataVersion.let {
        selectedVerses.isNotEmpty() && selectedVerses.all { pos -> userData.isBookmarked(keyFor(pos)) }
    }
    val allSelectedHighlighted = userDataVersion.let {
        selectedVerses.isNotEmpty() && selectedVerses.all { pos -> userData.isHighlighted(keyFor(pos)) }
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
            verticalArrangement = Arrangement.spacedBy(if (paragraphMode) 0.dp else 2.dp)
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
                    val key = keyFor(line.position)
                    Column {
                        VerseLine(
                            line = line,
                            emphasized = (scrollToVerse != null && line.position == scrollToVerse) ||
                                line.position in selectedVerses,
                            isHighlighted = userDataVersion.let { userData.isHighlighted(key) },
                            isBookmarked = userDataVersion.let { userData.isBookmarked(key) },
                            hasNote = userDataVersion.let { userData.getNote(key) != null },
                            showVerseNumber = verseNumbersVisible,
                            paragraphMode = paragraphMode,
                            scriptureFont = scriptureFont,
                            accent = accent,
                            onBg = onBg,
                            onTap = { toggleVerse(line.position) },
                            onLongPress = { toggleVerse(line.position) }
                        )
                        // Right back where it was before: the action row expands/collapses in
                        // place under whichever verse was tapped most recently, rather than
                        // living in a fixed toolbar -- that's the transition you liked. It keeps
                        // the newer additions (clear button, verse count, bookmark/highlight/
                        // note) that the fixed-toolbar experiment added along the way.
                        AnimatedVisibility(
                            visible = lastTappedVerse == line.position && selectionCitation != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            if (selectionCitation != null) {
                                SelectionActionRow(
                                    citation = selectionCitation,
                                    verseCount = selectedVerses.size,
                                    text = selectionText,
                                    isPreparingPdf = isPreparingPdf,
                                    isBookmarked = allSelectedBookmarked,
                                    isHighlighted = allSelectedHighlighted,
                                    accent = accent,
                                    onBg = onBg,
                                    onBgDim = onBgDim,
                                    onClear = { selectedVerses = emptySet(); lastTappedVerse = null },
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
                                    },
                                    onToggleBookmark = {
                                        val makeBookmarked = !allSelectedBookmarked
                                        selectedVerses.forEach { pos -> userData.setBookmarked(keyFor(pos), makeBookmarked) }
                                        userDataVersion++
                                    },
                                    onToggleHighlight = {
                                        val makeHighlighted = !allSelectedHighlighted
                                        selectedVerses.forEach { pos -> userData.setHighlighted(keyFor(pos), makeHighlighted) }
                                        userDataVersion++
                                    },
                                    onAddNote = { showNoteDialog = true }
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

    val noteVerse = lastTappedVerse
    if (showNoteDialog && noteVerse != null) {
        NoteDialog(
            initialText = userData.getNote(keyFor(noteVerse)) ?: "",
            accent = accent,
            onDismiss = { showNoteDialog = false },
            onSave = { text ->
                userData.setNote(keyFor(noteVerse), text)
                userDataVersion++
                showNoteDialog = false
            }
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
    isHighlighted: Boolean,
    isBookmarked: Boolean,
    hasNote: Boolean,
    showVerseNumber: Boolean,
    paragraphMode: Boolean,
    scriptureFont: FontFamily,
    accent: Color,
    onBg: Color,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    // Highlight always shows (a persistent mark); emphasized (tap-selected / scrolled-to) layers
    // the accent tint on top of it so a highlighted verse that's also selected still reads clearly.
    val background = when {
        emphasized -> accent.copy(alpha = 0.16f)
        isHighlighted -> accent.copy(alpha = 0.22f)
        else -> Color.Transparent
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .combinedClickable(onClick = onTap, onLongClick = onLongPress)
            .padding(
                vertical = if (paragraphMode) 1.dp else 3.dp,
                horizontal = if (emphasized || isHighlighted) 6.dp else 0.dp
            )
    ) {
        if (showVerseNumber && !paragraphMode) {
            Text(
                line.position.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = accent,
                modifier = Modifier
                    .padding(top = 4.dp, end = 6.dp)
                    .width(20.dp)
            )
        }
        Text(
            line.text,
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = scriptureFont),
            color = onBg,
            modifier = Modifier.weight(1f)
        )
        if (isBookmarked || hasNote) {
            Row(modifier = Modifier.padding(start = 4.dp, top = 4.dp)) {
                if (isBookmarked) {
                    Icon(Icons.Filled.Bookmark, contentDescription = "Imewekwa alama", tint = accent, modifier = Modifier.size(14.dp))
                }
                if (hasNote) {
                    Icon(Icons.Filled.NoteAlt, contentDescription = "Ina dokezo", tint = accent, modifier = Modifier.size(14.dp).padding(start = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun SelectionActionRow(
    citation: String,
    verseCount: Int,
    text: String,
    isPreparingPdf: Boolean,
    isBookmarked: Boolean,
    isHighlighted: Boolean,
    accent: Color,
    onBg: Color,
    onBgDim: Color,
    onClear: () -> Unit,
    onCopy: () -> Unit,
    onShareText: () -> Unit,
    onShareImage: () -> Unit,
    onSharePdf: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleHighlight: () -> Unit,
    onAddNote: () -> Unit
) {
    Column(Modifier.padding(start = 26.dp, end = 4.dp, top = 2.dp, bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(citation, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = accent)
                if (verseCount > 1) {
                    Text("Mistari $verseCount imechaguliwa", style = MaterialTheme.typography.labelSmall, color = onBgDim)
                }
            }
            IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Ondoa uchaguzi", tint = onBgDim, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(onClick = onToggleBookmark, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isBookmarked) "Ondoa alama" else "Weka alama",
                    tint = if (isBookmarked) accent else onBgDim,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(onClick = onToggleHighlight, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Highlight,
                    contentDescription = if (isHighlighted) "Ondoa mwangaza" else "Angazia",
                    tint = if (isHighlighted) accent else onBgDim,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(onClick = onAddNote, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.NoteAlt, contentDescription = "Ongeza dokezo", tint = onBgDim, modifier = Modifier.size(16.dp))
            }
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

@Composable
private fun NoteDialog(
    initialText: String,
    accent: Color,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dokezo") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Andika dokezo lako kuhusu mstari huu...") },
                keyboardOptions = KeyboardOptions.Default,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text("Hifadhi", color = accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ghairi")
            }
        }
    )
}
