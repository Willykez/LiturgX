package com.willykez.liturgx.ui.bible

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import com.willykez.liturgx.data.bible.ReadingPrefsStore
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
    val readingPrefs = remember { ReadingPrefsStore(context) }
    val books = remember { repository.allBooks() }

    // Resume mid-chapter if the person left the app without backing out to the book list
    // first (see the LaunchedEffect below, which is what saves/clears this) -- otherwise
    // start fresh at the book list, same as before.
    var route by remember {
        val resumed = readingPrefs.loadLastLocation()?.let { (bookId, chapterNum) ->
            books.firstOrNull { it.id == bookId }?.let { book -> BibleRoute.Reader(book, chapterNum) }
        }
        mutableStateOf(resumed ?: BibleRoute.Books)
    }

    // Persist (or clear) the resume point every time the route actually changes -- covers
    // every way of getting to a chapter (tapping through, chevrons, swipe, a Saved-tab jump)
    // in one place, rather than each of those call sites remembering to do it themselves.
    // Backing all the way out to the book list clears it: that's a deliberate "done reading"
    // signal, not a spot to resume into next time.
    LaunchedEffect(route) {
        when (val r = route) {
            is BibleRoute.Reader -> readingPrefs.saveLastLocation(r.book.id, r.chapterNum)
            is BibleRoute.Books -> readingPrefs.clearLastLocation()
            else -> Unit
        }
    }

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

    // Back steps up through this tab's own drill-down (Reader -> Chapters -> Books, Search ->
    // Books) before it ever reaches the app-level "go to Leo" handler in LiturgXApp -- so
    // backing out of a chapter lands on that book's chapter grid, not straight on Leo.
    BackHandler(enabled = route !is BibleRoute.Books) {
        route = when (val r = route) {
            is BibleRoute.Reader -> BibleRoute.Chapters(r.book)
            is BibleRoute.Chapters -> BibleRoute.Books
            is BibleRoute.Search -> BibleRoute.Books
            is BibleRoute.Books -> BibleRoute.Books
        }
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
            is BibleRoute.Reader -> AnimatedContent(
                targetState = r,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    // A book change (arriving via search or a Saved-tab jump) has no natural
                    // left/right direction, so it just crossfades; a same-book chapter change
                    // slides -- forward (next chapter, or a jump to a later one) goes right to
                    // left, backward goes left to right, matching the swipe/chevron direction.
                    if (targetState.book.id != initialState.book.id) {
                        fadeIn(tween(220)).togetherWith(fadeOut(tween(180)))
                    } else if (targetState.chapterNum >= initialState.chapterNum) {
                        (slideInHorizontally(tween(280)) { width -> width } + fadeIn(tween(220)))
                            .togetherWith(slideOutHorizontally(tween(280)) { width -> -width } + fadeOut(tween(180)))
                    } else {
                        (slideInHorizontally(tween(280)) { width -> -width } + fadeIn(tween(220)))
                            .togetherWith(slideOutHorizontally(tween(280)) { width -> width } + fadeOut(tween(180)))
                    }
                },
                label = "chapterReader"
            ) { target ->
                ChapterReaderScreen(
                    book = target.book,
                    chapterNum = target.chapterNum,
                    scrollToVerse = target.scrollToVerse,
                    color = currentColor,
                    repository = repository,
                    onBack = { route = BibleRoute.Chapters(target.book) },
                    onPrevChapter = { if (target.chapterNum > 1) route = BibleRoute.Reader(target.book, target.chapterNum - 1) },
                    onNextChapter = { if (target.chapterNum < target.book.chapterCount) route = BibleRoute.Reader(target.book, target.chapterNum + 1) }
                )
            }
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
