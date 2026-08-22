package com.willykez.liturgx.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.ReadingPresenter
import com.willykez.liturgx.data.DayResult
import com.willykez.liturgx.data.ProgressStore
import com.willykez.liturgx.data.bible.BibleRepository
import com.willykez.liturgx.ui.components.DailyReadingsView
import com.willykez.liturgx.ui.components.StreakCard
import com.willykez.liturgx.ui.components.VerseOfTheDayCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

private data class VerseOfDay(val citation: String, val text: String)

@Composable
fun HomeScreen(todayResult: DayResult, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val progressStore = remember { ProgressStore(context) }
    val bibleRepository = remember { BibleRepository(context.applicationContext) }
    var openedDates by remember { mutableStateOf<Set<LocalDate>>(emptySet()) }

    // Home only ever shows today, so viewing it IS "opened today's reading" -- the one event
    // the streak feature cares about. Re-runs if the calendar day rolls over mid-session.
    LaunchedEffect(todayResult.resolved.date) {
        progressStore.recordOpen(todayResult.resolved.date)
        openedDates = progressStore.openedDates()
    }

    // Trimmed to the Gospel's FIRST verse only -- a whole Gospel reading isn't a "verse of the
    // day", and this stays true to the actual day rather than a hand-curated list disconnected
    // from the Lectionary the rest of the app is built around. Falls back to whichever reading
    // is first if there's genuinely no Gospel entry for the day (rare structural edge case).
    val verseOfDay by produceState<VerseOfDay?>(initialValue = null, todayResult) {
        value = withContext(Dispatchers.IO) {
            val items = ReadingPresenter.present(todayResult.readings)
            val chosen = items.firstOrNull { it.kindKey == "INJILI" } ?: items.firstOrNull()
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
                StreakCard(
                    openedDates = openedDates,
                    today = todayResult.resolved.date,
                    color = todayResult.resolved.color
                )
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
