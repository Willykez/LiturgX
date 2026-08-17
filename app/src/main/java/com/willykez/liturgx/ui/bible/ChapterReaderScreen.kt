package com.willykez.liturgx.ui.bible

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.data.bible.BibleBookInfo
import com.willykez.liturgx.data.bible.BibleBrowseRepository
import com.willykez.liturgx.data.bible.ChapterLine
import com.willykez.liturgx.ui.theme.seasonAccent

/**
 * Renders a chapter verse-by-verse in true reading order (see
 * [BibleBrowseRepository.chapter]'s doc for why that's `rank`, not `position`). When arriving
 * from a search hit, [scrollToVerse] auto-scrolls to and highlights that verse -- the only way
 * a search result is actually useful without forcing the person to re-skim the whole chapter.
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
    val lines = remember(book.id, chapterNum) { repository.chapter(book.id, chapterNum) }
    val listState = rememberLazyListState()

    LaunchedEffect(book.id, chapterNum, scrollToVerse) {
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
                    VerseLine(line, highlighted = scrollToVerse != null && line.position == scrollToVerse, accent = accent, onBg = onBg)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun VerseLine(
    line: ChapterLine,
    highlighted: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    onBg: androidx.compose.ui.graphics.Color
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (highlighted) accent.copy(alpha = 0.16f) else androidx.compose.ui.graphics.Color.Transparent)
            .padding(vertical = 3.dp, horizontal = if (highlighted) 6.dp else 0.dp)
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
