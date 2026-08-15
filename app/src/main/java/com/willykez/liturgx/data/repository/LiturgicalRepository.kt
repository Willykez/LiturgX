package com.willykez.liturgx.data.repository

import com.willykez.liturgx.data.engine.LiturgicalCalendarEngine
import com.willykez.liturgx.data.local.BookmarkDao
import com.willykez.liturgx.data.local.BookmarkEntity
import com.willykez.liturgx.data.local.CachedReadingEntity
import com.willykez.liturgx.data.local.LectionaryDao
import com.willykez.liturgx.data.local.LectionaryEntity
import com.willykez.liturgx.data.local.LectionaryImporter
import com.willykez.liturgx.data.local.ReadingDao
import com.willykez.liturgx.data.local.UserPreferencesDao
import com.willykez.liturgx.data.local.UserPreferencesEntity
import com.willykez.liturgx.model.LiturgicalColor
import com.willykez.liturgx.model.LiturgicalDay
import com.willykez.liturgx.model.LiturgicalSeason
import com.willykez.liturgx.model.Prayer
import com.willykez.liturgx.model.ReadingItem
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LiturgicalRepository(
    private val context: Context,
    private val readingDao: ReadingDao,
    private val bookmarkDao: BookmarkDao,
    private val preferencesDao: UserPreferencesDao,
    private val lectionaryDao: LectionaryDao
) {

    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
    val userPreferences: Flow<UserPreferencesEntity?> = preferencesDao.getPreferencesFlow()

    private var lectionarySeeded = false

    fun isBookmarkedFlow(id: String): Flow<Boolean> = bookmarkDao.isBookmarkedFlow(id)

    suspend fun getReadingForDate(date: LocalDate): LiturgicalDay = withContext(Dispatchers.IO) {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        // Reseed (and, if the dataset version changed, clear stale reading caches) BEFORE the
        // cache check below — otherwise a date already cached from an older/partial dataset
        // would short-circuit on the next line and never see the cleanup at all.
        if (!lectionarySeeded) {
            LectionaryImporter.seedIfNeeded(context, lectionaryDao, readingDao)
            lectionarySeeded = true
        }

        // 1. Check local cache in Room (short-circuits the lectionary lookup and the offline generator)
        val cached = readingDao.getReadingByDate(dateString)
        if (cached != null) {
            return@withContext cached.toDomainModel(date)
        }

        // 2. Compute the algorithmically-correct season/week/day/cycle for this date.
        //    This is always right, offline, for any date in any year — see LiturgicalCalendarEngine
        //    for why this is more reliable than deriving the weekday cycle from the raw calendar
        //    year: the weekday cycle actually flips at Advent 1, not January 1.
        val engineInfo = LiturgicalCalendarEngine.dayInfo(date)

        // 3. Reading TEXT: prefer a real match from the curated `lectionary_readings` table
        //    (populated from assets/lectionary/lectionary.json — now bundled with the full
        //    Swahili Ordinary Time dataset), falling back to the bundled offline sample content
        //    when there's no match. Season/color/title/rank all come from the pure offline
        //    LiturgicalCalendarEngine — no network call, no litcal API dependency.
        val lectionaryMatch = lectionaryLookup(engineInfo, date)
        val offlineContent = OfflineReadingsData.generateReadingForDate(date)

        val resolvedSeason = engineInfo.season
        val resolvedColor = engineInfo.color
        val resolvedTitle = engineInfo.displayLabel
        val resolvedRank = if (engineInfo.isSunday) "Sunday / Lord's Day" else "Weekday"
        val resolvedCycle = if (engineInfo.isSunday) engineInfo.sundayCycle.name else engineInfo.weekdayCycle.name

        val day = (lectionaryMatch ?: offlineContent).copy(
            title = resolvedTitle,
            season = resolvedSeason,
            color = resolvedColor,
            rank = resolvedRank,
            cycle = resolvedCycle,
            weekOfSeason = engineInfo.weekOfSeason,
            saintOfTheDay = offlineContent.saintOfTheDay,
            holyDayOfObligation = offlineContent.holyDayOfObligation
        )

        // Only persist a genuine lectionary match. Caching the offline placeholder would lock
        // this date to sample content forever — the cache check above returns it before ever
        // reaching the lectionary lookup again, even after the real dataset becomes available.
        if (lectionaryMatch != null) {
            readingDao.insertReading(day.toEntity())
        }
        return@withContext day
    }

    /** Queries the curated lectionary table using the engine-computed key; returns null on a miss. */
    private suspend fun lectionaryLookup(
        engineInfo: LiturgicalCalendarEngine.LiturgicalDayInfo,
        date: LocalDate
    ): LiturgicalDay? {
        val dayOfWeekName = date.dayOfWeek.name // MONDAY, TUESDAY, ...
        val cycle = if (engineInfo.isSunday) engineInfo.sundayCycle.name else engineInfo.weekdayCycle.name
        val entity = lectionaryDao.getReadingForDay(
            season = engineInfo.season.name,
            weekNumber = engineInfo.weekOfSeason,
            dayOfWeek = dayOfWeekName,
            cycle = cycle
        ) ?: return null
        return entity.toDomainModel(date, engineInfo)
    }

    private fun LectionaryEntity.toDomainModel(date: LocalDate, engineInfo: LiturgicalCalendarEngine.LiturgicalDayInfo): LiturgicalDay {
        return LiturgicalDay(
            date = date,
            title = engineInfo.displayLabel,
            season = engineInfo.season,
            color = engineInfo.color,
            rank = if (engineInfo.isSunday) "Sunday / Lord's Day" else "Weekday",
            firstReading = ReadingItem("First Reading", firstReadingCitation, "", firstReadingText),
            responsorialPsalm = ReadingItem("Responsorial Psalm", psalmCitation, "", psalmText, psalmResponse),
            secondReading = if (!secondReadingCitation.isNullOrEmpty()) {
                ReadingItem("Second Reading", secondReadingCitation, "", secondReadingText ?: "")
            } else null,
            gospel = ReadingItem("Gospel Reading", gospelCitation, "", gospelText),
            reflection = "",
            saintOfTheDay = "",
            holyDayOfObligation = false
        )
    }

    suspend fun toggleBookmark(bookmark: BookmarkEntity) = withContext(Dispatchers.IO) {
        val exists = bookmarkDao.isBookmarked(bookmark.id)
        if (exists) {
            bookmarkDao.deleteBookmarkById(bookmark.id)
        } else {
            bookmarkDao.insertBookmark(bookmark)
        }
    }

    suspend fun deleteBookmark(id: String) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmarkById(id)
    }

    fun getOfflinePrayers(): List<Prayer> {
        return OfflinePrayersData.prayersList
    }

    suspend fun savePreferences(prefs: UserPreferencesEntity) = withContext(Dispatchers.IO) {
        preferencesDao.savePreferences(prefs)
    }

    private fun CachedReadingEntity.toDomainModel(date: LocalDate): LiturgicalDay {
        return LiturgicalDay(
            date = date,
            title = title,
            season = try { LiturgicalSeason.valueOf(season) } catch (e: Exception) { LiturgicalSeason.ORDINARY_TIME },
            color = try { LiturgicalColor.valueOf(color) } catch (e: Exception) { LiturgicalColor.GREEN },
            rank = rank,
            cycle = cycle,
            weekOfSeason = weekOfSeason,
            firstReading = ReadingItem("First Reading", firstReadingCitation, firstReadingHeadline, firstReadingText),
            responsorialPsalm = ReadingItem("Responsorial Psalm", psalmCitation, "", psalmText, psalmResponse),
            secondReading = if (!secondReadingCitation.isNullOrEmpty()) {
                ReadingItem("Second Reading", secondReadingCitation!!, secondReadingHeadline ?: "", secondReadingText ?: "")
            } else null,
            gospel = ReadingItem("Gospel Reading", gospelCitation, gospelHeadline, gospelText),
            reflection = reflection,
            saintOfTheDay = saintOfTheDay,
            holyDayOfObligation = holyDayOfObligation
        )
    }

    private fun LiturgicalDay.toEntity(): CachedReadingEntity {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return CachedReadingEntity(
            dateString = dateString,
            title = title,
            season = season.name,
            color = color.name,
            rank = rank,
            cycle = cycle,
            weekOfSeason = weekOfSeason,
            firstReadingCitation = firstReading.citation,
            firstReadingHeadline = firstReading.headline,
            firstReadingText = firstReading.text,
            psalmCitation = responsorialPsalm.citation,
            psalmResponse = responsorialPsalm.responsorialVerse,
            psalmText = responsorialPsalm.text,
            secondReadingCitation = secondReading?.citation,
            secondReadingHeadline = secondReading?.headline,
            secondReadingText = secondReading?.text,
            gospelCitation = gospel.citation,
            gospelHeadline = gospel.headline,
            gospelText = gospel.text,
            reflection = reflection,
            saintOfTheDay = saintOfTheDay,
            holyDayOfObligation = holyDayOfObligation
        )
    }
}
