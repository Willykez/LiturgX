package com.willykez.liturgx.ui.bible

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.willykez.liturgx.ui.components.ShareCardDialog
import com.willykez.liturgx.ui.theme.seasonAccent

/**
 * Renders a chapter verse-by-verse in true reading order (see
 * [BibleBrowseRepository.chapter]'s doc for why that's `rank`, not `position`). When arriving
 * from a search hit, [scrollToVerse] auto-scrolls to and highlights that verse -- the only way
 * a search result is actually useful without forcing the person to re-skim the whole chapter.
 *
 * Tapping any verse selects it and reveals copy / share-as-text / share-as-image actions right
 * under that verse -- this is the "share a verse" feature: the single highest-leverage feature
 * for how people actually discover a Bible app, reusing the same [ShareCardDialog] design
 * already built for Lectionary readings rather than a second card design.
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
    val lines = remember(book.id, chapterNum) { repository.chapter(book.id, chapterNum) }
    val listState = rememberLazyListState()

    var selectedVerse by remember(book.id, chapterNum) { mutableStateOf<Int?>(null) }
    var shareImageVerse by remember(book.id, chapterNum) { mutableStateOf<ChapterLine?>(null) }

    LaunchedEffect(book.id, chapterNum, scrollToVerse) {
        selectedVerse = null
        if (scrollToVerse != null) {
            val index = lines.indexOfFirst { !it.isHeading && it.position == scrollToVerse }
            if (index >= 0) listState.animateScrollToItem(index)
        } else {
            listState.scrollToItem(0)
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
                        bookName = book.name,
                        chapterNum = chapterNum,
                        highlighted = scrollToVerse != null && line.position == scrollToVerse,
                        isSelected = selectedVerse == line.position,
                        accent = accent,
                        onBg = onBg,
                        onBgDim = onBgDim,
                        onToggleSelect = {
                            selectedVerse = if (selectedVerse == line.position) null else line.position
                        },
                        onShareImage = { shareImageVerse = line }
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    shareImageVerse?.let { line ->
        ShareCardDialog(
            dateText = "${book.name} $chapterNum",
            seasonText = "BIBLIA",
            kindLabel = "Mstari",
            citation = "$chapterNum:${line.position}",
            passage = line.text,
            responseText = null,
            liturgicalColor = color,
            onDismiss = { shareImageVerse = null }
        )
    }
}

@Composable
private fun VerseLine(
    line: ChapterLine,
    bookName: String,
    chapterNum: Int,
    highlighted: Boolean,
    isSelected: Boolean,
    accent: Color,
    onBg: Color,
    onBgDim: Color,
    onToggleSelect: () -> Unit,
    onShareImage: () -> Unit
) {
    val context = LocalContext.current
    val emphasized = highlighted || isSelected

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (emphasized) accent.copy(alpha = 0.16f) else Color.Transparent)
            .clickable(onClick = onToggleSelect)
            .padding(vertical = 3.dp, horizontal = if (emphasized) 6.dp else 0.dp)
    ) {
        Row {
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

        AnimatedVisibility(visible = isSelected) {
            Row(
                Modifier.padding(start = 26.dp, top = 2.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { copyVerseToClipboard(context, bookName, chapterNum, line) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Nakili mstari", tint = onBgDim, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = { shareVerseAsText(context, bookName, chapterNum, line) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.IosShare, contentDescription = "Shiriki kama maandishi", tint = onBgDim, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onShareImage, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Image, contentDescription = "Shiriki kama picha", tint = onBgDim, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

private fun verseShareText(bookName: String, chapterNum: Int, line: ChapterLine): String =
    "$bookName $chapterNum:${line.position}\n\n${line.text}\n\nImetumwa kutoka LiturgX"

private fun copyVerseToClipboard(context: Context, bookName: String, chapterNum: Int, line: ChapterLine) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Andiko", verseShareText(bookName, chapterNum, line)))
}

private fun shareVerseAsText(context: Context, bookName: String, chapterNum: Int, line: ChapterLine) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, verseShareText(bookName, chapterNum, line))
    }
    context.startActivity(Intent.createChooser(intent, "Shiriki Mstari"))
}
