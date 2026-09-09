package com.willykez.liturgx.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.core.ReadingPresenter
import com.willykez.liturgx.core.RegionSettings
import com.willykez.liturgx.core.SwahiliDate
import com.willykez.liturgx.data.DayResult
import com.willykez.liturgx.data.LectionaryRepository
import com.willykez.liturgx.data.ProgressStore
import com.willykez.liturgx.data.bible.BibleRepository
import com.willykez.liturgx.ui.components.DailyReadingsView
import com.willykez.liturgx.ui.components.LiturgicalSeal
import com.willykez.liturgx.ui.components.VerseOfTheDayCard
import com.willykez.liturgx.ui.theme.seasonAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

private data class VerseOfDay(val citation: String, val text: String)
private data class UpcomingHoliday(val date: LocalDate, val title: String, val color: LiturgicalColor)
private data class UpcomingSaint(val date: LocalDate, val name: String, val rank: String)

private const val UPCOMING_HORIZON_DAYS = 45
private const val UPCOMING_MAX_ITEMS = 5

/** Same fixed-solemnity/major-feast detection [com.willykez.liturgx.data.sharing.YearlyLectionaryPdfGenerator]
 *  uses for its "special day" filter -- duplicated locally (small, and the two call sites have
 *  no other reason to share a dependency) rather than exported from that file just for this. */
private val HOLIDAY_PERIOD_KEYS = setOf(
    "mchana", "epifania", "maria_mama_wa_mungu", "jumatano_ya_majivu",
    "alhamisi_kuu", "ijumaa_kuu", "vigilia_ya_pasaka", "kupaa_kwa_bwana",
    "fungu_takatifu_la_mwili_na_damu_ya_kristo", "familia_takatifu",
    "ubatizo_wa_bwana", "moyo_mtakatifu_wa_yesu", "utatu_mtakatifu"
)
private val MAJOR_SAINT_RANKS = setOf("Sikukuu", "Sikukuu Kuu")

/** Walks forward from tomorrow, sorting each day into "holiday" (a solemnity/major feast --
 *  the same test the yearly PDF export uses, minus the "every Sunday counts" part, since an
 *  ordinary Sunday isn't what "upcoming holidays" means here) or "saint" (any other day with a
 *  saint attached) -- a day only lands in one list, not both, so a major feast doesn't also
 *  show up as a duplicate saint entry. Stops early once both lists are full, so a light month
 *  doesn't mean walking the full 45-day horizon for nothing. */
private fun findUpcoming(repository: LectionaryRepository, from: LocalDate, region: RegionSettings): Pair<List<UpcomingHoliday>, List<UpcomingSaint>> {
    val holidays = mutableListOf<UpcomingHoliday>()
    val saints = mutableListOf<UpcomingSaint>()
    var date = from.plusDays(1)
    val end = from.plusDays(UPCOMING_HORIZON_DAYS.toLong())
    while (!date.isAfter(end) && (holidays.size < UPCOMING_MAX_ITEMS || saints.size < UPCOMING_MAX_ITEMS)) {
        try {
            val resolved = repository.getForDate(date, region).resolved
            val saintRank = resolved.overridingSaint?.daraja
            val isMajor = saintRank != null && saintRank in MAJOR_SAINT_RANKS
            val isHoliday = resolved.periodKey in HOLIDAY_PERIOD_KEYS || resolved.season.key == "sikukuu_maalum" || isMajor
            when {
                isHoliday && holidays.size < UPCOMING_MAX_ITEMS ->
                    holidays += UpcomingHoliday(date, resolved.overridingSaint?.jina ?: resolved.label, resolved.color)
                !isHoliday && resolved.overridingSaint != null && saints.size < UPCOMING_MAX_ITEMS ->
                    saints += UpcomingSaint(date, resolved.overridingSaint.jina, saintRank ?: "")
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeUpcoming", "Skipping $date", e)
        }
        date = date.plusDays(1)
    }
    return holidays to saints
}

@Composable
fun HomeScreen(todayResult: DayResult, region: RegionSettings, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val progressStore = remember { ProgressStore(context) }
    val bibleRepository = remember { BibleRepository(context.applicationContext) }
    val lectionaryRepository = remember { LectionaryRepository(context.applicationContext) }

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

    val upcoming by produceState(initialValue = emptyList<UpcomingHoliday>() to emptyList<UpcomingSaint>(), todayResult.resolved.date, region) {
        value = withContext(Dispatchers.IO) {
            findUpcoming(lectionaryRepository, todayResult.resolved.date, region)
        }
    }
    val (upcomingHolidays, upcomingSaints) = upcoming

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
        extraFooterContent = {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                if (upcomingHolidays.isNotEmpty()) {
                    UpcomingSection(title = "Sikukuu Zinazokuja") {
                        upcomingHolidays.forEachIndexed { index, holiday ->
                            UpcomingHolidayRow(holiday)
                            if (index != upcomingHolidays.lastIndex) UpcomingDivider()
                        }
                    }
                }
                if (upcomingSaints.isNotEmpty()) {
                    UpcomingSection(title = "Watakatifu Wanaokuja") {
                        upcomingSaints.forEachIndexed { index, saint ->
                            UpcomingSaintRow(saint)
                            if (index != upcomingSaints.lastIndex) UpcomingDivider()
                        }
                    }
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun UpcomingSection(title: String, content: @Composable () -> Unit) {
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    Column {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = onBgDim,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun UpcomingDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
}

/** "Jumapili, 14 Sep" -- a short weekday+day+month form, distinct from [DailyReadingsView]'s
 *  full "Jumapili, 14 Septemba 2026" header line, since this repeats several times in a list. */
private fun shortDateLabel(date: LocalDate): String =
    "${SwahiliDate.weekdayName(date.dayOfWeek)}, ${date.dayOfMonth} ${SwahiliDate.monthName(date.monthValue).take(3)}"

@Composable
private fun UpcomingHolidayRow(holiday: UpcomingHoliday) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiturgicalSeal(holiday.color, size = 28.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(holiday.title, style = MaterialTheme.typography.titleSmall, color = onBg)
            Text(shortDateLabel(holiday.date), style = MaterialTheme.typography.labelMedium, color = onBgDim)
        }
    }
}

@Composable
private fun UpcomingSaintRow(saint: UpcomingSaint) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = onBgDim, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(saint.name, style = MaterialTheme.typography.titleSmall, color = onBg)
            Text(
                "${saint.rank} · ${shortDateLabel(saint.date)}",
                style = MaterialTheme.typography.labelMedium,
                color = onBgDim
            )
        }
    }
}
