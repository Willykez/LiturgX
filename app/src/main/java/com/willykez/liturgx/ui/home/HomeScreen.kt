package com.willykez.liturgx.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.ReadingPresenter
import com.willykez.liturgx.data.DayResult
import com.willykez.liturgx.data.ProgressStore
import com.willykez.liturgx.data.bible.BibleRepository
import com.willykez.liturgx.ui.components.DailyReadingsView
import com.willykez.liturgx.ui.components.VerseOfTheDayCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class VerseOfDay(val citation: String, val text: String)

@Composable
fun HomeScreen(todayResult: DayResult, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val progressStore = remember { ProgressStore(context) }
    val bibleRepository = remember { BibleRepository(context.applicationContext) }

    // Home only ever shows today, so viewing it IS "opened today's reading" -- the one event
    // the streak feature cares about. The streak card itself no longer shows on Home, but the
    // underlying data still accumulates here in case it resurfaces elsewhere later.
    LaunchedEffect(todayResult.resolved.date) {
        progressStore.recordOpen(todayResult.resolved.date)
    }

    // Uses the day's Shangilio (Gospel Acclamation) citation, which is already a single short
    // verse by design -- falls back to whichever reading is first if there's no Shangilio for
    // the day (placeholder entries like "[hakuna mstari maalum]" are already filtered out of
    // `items` upstream in ReadingPresenter, so this only sees real citations).
    val verseOfDay by produceState<VerseOfDay?>(initialValue = null, todayResult) {
        value = withContext(Dispatchers.IO) {
            val items = ReadingPresenter.present(todayResult.readings)
            val chosen = items.firstOrNull { it.kindKey == "SHANGILIO" } ?: items.firstOrNull()
            chosen?.let { item ->
                val passage = bibleRepository.getPassage(item.citation)
                val firstVerse = passage?.verses?.firstOrNull()
                if (passage != null && firstVerse != null) {
                    VerseOfDay(
                        citation = "${passage.book} ${firstVerse.chapter}:${firstVerse.verse}",
                        text = firstVerse.text
                    )
                } else null
            }
        }
    }

    DailyReadingsView(
        dayResult = todayResult,
        extraHeaderContent = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                verseOfDay?.let { verse ->
                    VerseOfTheDayCard(
                        citation = verse.citation,
                        text = verse.text,
                        color = todayResult.resolved.color
                    )
                }
            }
        },
        modifier = modifier
    )
}
