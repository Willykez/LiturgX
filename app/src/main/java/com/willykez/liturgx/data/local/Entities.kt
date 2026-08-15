package com.willykez.liturgx.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_readings")
data class CachedReadingEntity(
    @PrimaryKey val dateString: String, // YYYY-MM-DD
    val title: String,
    val season: String,
    val color: String,
    val rank: String,
    val cycle: String = "",
    val weekOfSeason: Int = 0,
    val firstReadingCitation: String,
    val firstReadingHeadline: String,
    val firstReadingText: String,
    val psalmCitation: String,
    val psalmResponse: String,
    val psalmText: String,
    val secondReadingCitation: String?,
    val secondReadingHeadline: String?,
    val secondReadingText: String?,
    val gospelCitation: String,
    val gospelHeadline: String,
    val gospelText: String,
    val reflection: String,
    val saintOfTheDay: String,
    val holyDayOfObligation: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String, // e.g. "reading_2026-08-12" or "prayer_rosary"
    val type: String,           // "READING", "REFLECTION", "PRAYER"
    val title: String,
    val subtitle: String,
    val content: String,
    val dateOrCategory: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val darkMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK, EVENING
    val fontScale: Float = 1.0f,
    val useSerifFont: Boolean = true,
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderHour: Int = 7,
    val dailyReminderMinute: Int = 0,
    val eveningReminderEnabled: Boolean = true,
    val eveningReminderHour: Int = 21,
    val eveningReminderMinute: Int = 0,
    val feastAlertsEnabled: Boolean = true,
    val interfaceLanguage: String = "SWAHILI" // "SWAHILI" | "ENGLISH" — see com.willykez.liturgx.model.AppLanguage
)

/**
 * Structured Lectionary lookup table: one row per (season, week, weekday, cycle) combination,
 * exactly the shape the Church actually publishes the Lectionary in — a fixed table lookup, not
 * something derivable from a date. [com.willykez.liturgx.data.engine.LiturgicalCalendarEngine] computes
 * the (season, weekNumber, dayOfWeek, cycle) KEY for any date; this table holds the VALUE.
 *
 * `cycle` is "A"/"B"/"C" for Sunday rows, "I"/"II" for weekday rows, or "ALL" for a row that's
 * the same text regardless of cycle (common on solemnities and many weekdays outside Lent/Advent).
 * `weekNumber` is 0 for days that aren't numbered within a season (e.g. Holy Week).
 *
 * This table ships empty by default — see LectionaryImporter for how it's populated from a
 * bundled dataset (assets/lectionary/lectionary.json), which is a data-acquisition project of its
 * own and not something bundled here. Until that dataset is supplied, lookups simply miss and the
 * repository falls back to the existing offline sample content.
 */
@Entity(tableName = "lectionary_readings")
data class LectionaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val season: String,          // matches LiturgicalSeason.name — ADVENT, CHRISTMAS, LENT, HOLY_WEEK, EASTER, ORDINARY_TIME
    val weekNumber: Int,         // 1..34, or 0 when not applicable
    val dayOfWeek: String,       // matches DayOfWeek.name — SUNDAY, MONDAY, ...
    val cycle: String,           // "A" | "B" | "C" | "I" | "II" | "ALL"
    val firstReadingCitation: String,
    val firstReadingText: String,
    val psalmCitation: String,
    val psalmResponse: String,
    val psalmText: String,
    val secondReadingCitation: String? = null,
    val secondReadingText: String? = null,
    val gospelCitation: String,
    val gospelText: String
)
