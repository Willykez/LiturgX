package com.willykez.liturgx.ui.bible

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.data.bible.BibleBookInfo
import com.willykez.liturgx.data.bible.BibleBrowseRepository
import com.willykez.liturgx.data.bible.Testament
import com.willykez.liturgx.ui.BibleJumpTarget
import com.willykez.liturgx.ui.theme.seasonAccent
import com.willykez.liturgx.ui.theme.seasonAccentSoft

/**
 * Route within the Bible tab's own small internal navigation -- state-driven rather than a
 * nested NavHost, matching how the rest of the app keeps screen flow simple (see
 * [com.willykez.liturgx.ui.calendar.CalendarScreen] for the same pattern applied to dates).
 */
private sealed class BibleRoute {
    data object Books : BibleRoute()
    data class Chapters(val book: BibleBookInfo) : BibleRoute()
    data class Reader(val book: BibleBookInfo, val chapterNum: Int, val scrollToVerse: Int? = null) : BibleRoute()
    data object Search : BibleRoute()
}

@Composable
fun BibleScreen(
    currentColor: LiturgicalColor,
    pendingJump: BibleJumpTarget? = null,
    onJumpHandled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { BibleBrowseRepository(context.applicationContext) }
    val books = remember { repository.allBooks() }
    var route by remember { mutableStateOf<BibleRoute>(BibleRoute.Books) }

    // A bookmark/highlight/note tapped on the Saved tab arrives here as a plain address
    // (bookId/chapter/verse) rather than a BibleRoute, since Saved has no reason to know about
    // this tab's internal route type -- resolve it to a BibleBookInfo and jump straight to it.
    LaunchedEffect(pendingJump) {
        val jump = pendingJump ?: return@LaunchedEffect
        val book = books.firstOrNull { it.id == jump.bookId }
        if (book != null) {
            route = BibleRoute.Reader(book, jump.chapterNum, jump.verseNum)
        }
        onJumpHandled()
    }

    Box(modifier.fillMaxSize()) {
        when (val r = route) {
            is BibleRoute.Books -> BookListScreen(
                books = books,
                color = currentColor,
                onSelectBook = { route = BibleRoute.Chapters(it) },
                onSearch = { route = BibleRoute.Search }
            )
            is BibleRoute.Chapters -> ChapterGridScreen(
                book = r.book,
                color = currentColor,
                onBack = { route = BibleRoute.Books },
                onSelectChapter = { chNum -> route = BibleRoute.Reader(r.book, chNum) }
            )
            is BibleRoute.Reader -> ChapterReaderScreen(
                book = r.book,
                chapterNum = r.chapterNum,
                scrollToVerse = r.scrollToVerse,
                color = currentColor,
                repository = repository,
                onBack = { route = BibleRoute.Chapters(r.book) },
                onPrevChapter = { if (r.chapterNum > 1) route = BibleRoute.Reader(r.book, r.chapterNum - 1) },
                onNextChapter = { if (r.chapterNum < r.book.chapterCount) route = BibleRoute.Reader(r.book, r.chapterNum + 1) }
            )
            is BibleRoute.Search -> BibleSearchScreen(
                color = currentColor,
                repository = repository,
                books = books,
                onBack = { route = BibleRoute.Books },
                onSelectResult = { result ->
                    val book = books.first { it.id == result.bookId }
                    route = BibleRoute.Reader(book, result.chapterNum, result.verseNum)
                }
            )
        }
    }
}

@Composable
private fun BookListScreen(
    books: List<BibleBookInfo>,
    color: LiturgicalColor,
    onSelectBook: (BibleBookInfo) -> Unit,
    onSearch: () -> Unit
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = seasonAccent(color)
    val oldTestament = books.filter { it.testament == Testament.AGANO_LA_KALE }
    val newTestament = books.filter { it.testament == Testament.AGANO_JIPYA }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Biblia", style = MaterialTheme.typography.headlineSmall, color = onBg)
                    Text("Vitabu 66 kwa Kiswahili", style = MaterialTheme.typography.labelMedium, color = onBgDim)
                }
                IconButton(onClick = onSearch) {
                    Icon(Icons.Filled.Search, contentDescription = "Tafuta andiko", tint = accent)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        item { SectionHeader("Agano la Kale", accent) }
        items(oldTestament) { book -> BookRow(book, color, onClick = { onSelectBook(book) }) }

        item {
            Spacer(Modifier.height(10.dp))
            SectionHeader("Agano Jipya", accent)
        }
        items(newTestament) { book -> BookRow(book, color, onClick = { onSelectBook(book) }) }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun SectionHeader(text: String, accent: androidx.compose.ui.graphics.Color) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = accent,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
private fun BookRow(book: BibleBookInfo, color: LiturgicalColor, onClick: () -> Unit) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(seasonAccentSoft(color))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(book.name, style = MaterialTheme.typography.bodyLarge, color = onBg)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Sura ${book.chapterCount}", style = MaterialTheme.typography.labelSmall, color = onBgDim)
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = onBgDim, modifier = Modifier.size(18.dp))
        }
    }
}
