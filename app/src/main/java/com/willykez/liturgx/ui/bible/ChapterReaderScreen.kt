package com.willykez.liturgx.ui.bible

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
 * Long-press a verse to start a selection; tapping any other verse afterward extends it to that
 * verse (like the anchor/focus of a text selection) -- a plain tap does nothing when no
 * selection is active, so casually tapping a verse while reading never pops up a toolbar by
 * accident. The citation and combined text regenerate from the selected range automatically,
 * the same way a Lectionary citation like "Yohana 3:16-18" names a range, not a single verse.
 * The share button on the resulting selection bar mirrors [com.willykez.liturgx.ui.components.
 * DailyReadingsView]'s three-way menu (text / image / PDF) rather than jumping straight to one
 * action, reusing that exact same PDF generator with a single-item reading list.
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

    var selectionAnchor by remember(book.id, chapterNum) { mutableStateOf<Int?>(null) }
    var selectionFocus by remember(book.id, chapterNum) { mutableStateOf<Int?>(null) }
    var showShareMenu by remember { mutableStateOf(false) }
    var showImageShare by remember { mutableStateOf(false) }
    var isPreparingPdf by remember { mutableStateOf(false) }
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var showPdfPreview by remember { mutableStateOf(false) }

    LaunchedEffect(book.id, chapterNum, scrollToVerse) {
        selectionAnchor = null
        selectionFocus = null
        if (scrollToVerse != null) {
            val index = lines.indexOfFirst { !it.isHeading && it.position == scrollToVerse }
            if (index >= 0) listState.animateScrollToItem(index)
        } else {
            listState.scrollToItem(0)
        }
    }

    val selectionRange: IntRange? = if (selectionAnchor != null && selectionFocus != null) {
        minOf(selectionAnchor!!, selectionFocus!!)..maxOf(selectionAnchor!!, selectionFocus!!)
    } else null

    val selectedLines = selectionRange?.let { range -> lines.filter { !it.isHeading && it.position in range } }.orEmpty()
    val selectionIsPoetic = selectedLines.any { it.text.contains('\n') }
    val selectionText = selectedLines.joinToString(if (selectionIsPoetic) "\n" else " ") { it.text }
    val selectionCitation = selectionRange?.let { range ->
        if (range.first == range.last) "${book.name} $chapterNum:${range.first}"
        else "${book.name} $chapterNum:${range.first}-${range.last}"
    }

    fun clearSelection() {
        selectionAnchor = null
        selectionFocus = null
    }

    fun onVerseLongClick(position: Int) {
        selectionAnchor = position
        selectionFocus = position
    }

    fun onVerseTap(position: Int) {
        if (selectionAnchor != null) {
            selectionFocus = position
        }
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

        if (selectionRange != null && selectionCitation != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    selectionCitation,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    modifier = Modifier.weight(1f)
                )
                if (isPreparingPdf) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp).padding(end = 8.dp), strokeWidth = 2.dp, color = accent)
                }
                IconButton(onClick = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("Andiko", "$selectionCitation\n\n$selectionText"))
                }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Nakili", tint = onBgDim, modifier = Modifier.size(16.dp))
                }
                Box {
                    IconButton(onClick = { showShareMenu = true }, enabled = !isPreparingPdf, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.IosShare, contentDescription = "Shiriki", tint = onBgDim, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = showShareMenu, onDismissRequest = { showShareMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Shiriki kama Maandishi") },
                            leadingIcon = { Icon(Icons.Filled.TextSnippet, contentDescription = null) },
                            onClick = {
                                showShareMenu = false
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "$selectionCitation\n\n$selectionText\n\nImetumwa kutoka LiturgX")
                                }
                                context.startActivity(Intent.createChooser(intent, "Shiriki Andiko"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Shiriki kama Picha") },
                            leadingIcon = { Icon(Icons.Outlined.Image, contentDescription = null) },
                            onClick = { showShareMenu = false; showImageShare = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Hifadhi kama PDF") },
                            leadingIcon = { Icon(Icons.Filled.PictureAsPdf, contentDescription = null) },
                            onClick = {
                                showShareMenu = false
                                isPreparingPdf = true
                                scope.launch {
                                    val file = withContext(Dispatchers.IO) {
                                        DailyReadingPdfGenerator.generate(
                                            context = context,
                                            dateText = book.name,
                                            seasonText = "BIBLIA",
                                            color = color,
                                            readings = listOf(
                                                DayCardReading(
                                                    kindLabel = "Andiko",
                                                    citation = selectionCitation,
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
                        )
                    }
                }
                IconButton(onClick = { clearSelection() }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Funga uteuzi", tint = onBgDim, modifier = Modifier.size(16.dp))
                }
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
                    VerseLine(
                        line = line,
                        emphasized = (scrollToVerse != null && line.position == scrollToVerse) ||
                            (selectionRange != null && line.position in selectionRange),
                        accent = accent,
                        onBg = onBg,
                        onTap = { onVerseTap(line.position) },
                        onLongPress = { onVerseLongClick(line.position) }
                    )
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

@Composable
private fun VerseLine(
    line: ChapterLine,
    emphasized: Boolean,
    accent: Color,
    onBg: Color,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (emphasized) accent.copy(alpha = 0.16f) else Color.Transparent)
            .combinedClickable(onClick = onTap, onLongClick = onLongPress)
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
