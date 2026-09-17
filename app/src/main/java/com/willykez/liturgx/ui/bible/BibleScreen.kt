package com.willykez.liturgx.ui.bible

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.data.bible.BibleBookInfo
import com.willykez.liturgx.data.bible.BibleBrowseRepository
import com.willykez.liturgx.data.bible.ReadingPrefsStore
import com.willykez.liturgx.data.bible.Testament
import com.willykez.liturgx.ui.BibleJumpTarget
import com.willykez.liturgx.ui.components.OneUiGroupCard
import com.willykez.liturgx.ui.components.OneUiIconRow
import com.willykez.liturgx.ui.components.OneUiRowDivider
import com.willykez.liturgx.ui.components.OneUiSectionLabel
import com.willykez.liturgx.ui.theme.seasonAccent

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
    onHeaderTextChange: (title: String, subtitle: String?) -> Unit = { _, _ -> },
    onSearchTriggerReady: (() -> Unit) -> Unit = {},
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

    // Reports the book-list's title/subtitle up to the shared collapsing top bar (see
    // Navigation.kt) -- only from the book-list (landing) state. The chapter grid, chapter
    // reader, and search screen keep their own in-content headers untouched rather than also
    // reporting up: the reader in particular already shows its own "{book} {chapter}" header
    // with prev/next chevrons, and reporting the same text to the shared bar would just
    // recreate the exact duplicate-heading problem fixed a few rounds back. Leaving those three
    // un-reported means the shared bar simply keeps showing "Biblia" while browsing deeper,
    // rather than trying (and risking getting it wrong) to mirror every sub-screen.
    LaunchedEffect(route) {
        if (route is BibleRoute.Books) {
            onHeaderTextChange("Biblia", "Vitabu 66 kwa Kiswahili")
        }
    }

    // The search trigger is reported exactly once, regardless of which route this tab happens
    // to start on (it can start straight in a resumed chapter, not the book list -- see the
    // resume logic above) -- unlike the title/subtitle, which only make sense while looking at
    // the book list, "jump to search" is valid from anywhere in this tab, so it doesn't need
    // the `route is BibleRoute.Books` guard the effect above has, and lives in its own callback
    // entirely rather than sharing one with the title -- an earlier version merged them into a
    // single 3-arg callback, and every return to the book list re-fired it with a null search
    // action, silently overwriting the real one Navigation.kt had already stored. Two separate
    // callbacks means neither can step on the other's last-reported value. The closure captures
    // the `route` setter itself, not a route value, so it stays correct no matter how `route`
    // changes after this fires.
    LaunchedEffect(Unit) {
        onSearchTriggerReady { route = BibleRoute.Search }
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
                onSelectBook = { route = BibleRoute.Chapters(it) }
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
    onSelectBook: (BibleBookInfo) -> Unit
) {
    val accent = seasonAccent(color)
    val oldTestament = books.filter { it.testament == Testament.AGANO_LA_KALE }
    val newTestament = books.filter { it.testament == Testament.AGANO_JIPYA }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                OneUiSectionLabel("AGANO LA KALE", modifier = Modifier.padding(start = 4.dp))
                OneUiGroupCard {
                    oldTestament.forEachIndexed { index, book ->
                        BookRow(book, accent, onClick = { onSelectBook(book) })
                        if (index != oldTestament.lastIndex) OneUiRowDivider()
                    }
                }
            }
        }

        item {
            Column {
                OneUiSectionLabel("AGANO JIPYA", modifier = Modifier.padding(start = 4.dp))
                OneUiGroupCard {
                    newTestament.forEachIndexed { index, book ->
                        BookRow(book, accent, onClick = { onSelectBook(book) })
                        if (index != newTestament.lastIndex) OneUiRowDivider()
                    }
                }
            }
        }

        item { Spacer(Modifier.height(4.dp)) }
    }
}

@Composable
private fun BookRow(book: BibleBookInfo, accent: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    OneUiIconRow(
        icon = Icons.Filled.MenuBook,
        iconBackground = accent,
        title = book.name,
        onClick = onClick,
    ) {
        Text("Sura ${book.chapterCount}", style = MaterialTheme.typography.labelSmall, color = onBgDim)
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = onBgDim, modifier = Modifier.size(18.dp))
    }
}
