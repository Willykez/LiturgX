package com.willykez.liturgx.model

import java.time.LocalDate

enum class LiturgicalColor(val displayName: String, val hexColor: Long, val lightHexColor: Long) {
    GREEN("Green", 0xFF2E7D32, 0xFF4CAF50),      // Ordinary Time
    PURPLE("Violet / Purple", 0xFF6A1B9A, 0xFF9C27B0), // Lent & Advent
    WHITE("White / Gold", 0xFFD4AF37, 0xFFE0C068),   // Easter, Christmas, Feasts of the Lord
    RED("Red", 0xFFC62828, 0xFFE53935),        // Passion Sunday, Pentecost, Martyrs
    ROSE("Rose", 0xFFAD1457, 0xFFEC407A)       // Gaudete & Laetare Sundays
}

enum class LiturgicalSeason(val displayName: String) {
    ORDINARY_TIME("Ordinary Time"),
    ADVENT("Advent"),
    CHRISTMAS("Christmas Season"),
    LENT("Lent"),
    HOLY_WEEK("Holy Week"),
    EASTER("Easter Season")
}

data class ReadingItem(
    val title: String,         // e.g., "First Reading"
    val citation: String,      // e.g., "Ezekiel 18:21-28"
    val headline: String = "",   // e.g., "If the wicked turn away from their sins, they shall live"
    val text: String,
    val responsorialVerse: String = "" // For Psalms
)

data class LiturgicalDay(
    val date: LocalDate,
    val title: String,                // e.g., "20th Sunday in Ordinary Time" or "Feast of St. Augustine"
    val season: LiturgicalSeason,
    val color: LiturgicalColor,
    val rank: String,                 // Solemnity, Feast, Memorial, Weekday
    val cycle: String = "",           // "A"/"B"/"C" on Sundays, "I"/"II" on weekdays — see LiturgicalCalendarEngine
    val weekOfSeason: Int = 0,        // 1-based week number within the season, 0 when not applicable
    val firstReading: ReadingItem,
    val responsorialPsalm: ReadingItem,
    val secondReading: ReadingItem? = null,
    val gospel: ReadingItem,
    val reflection: String,           // Daily reflection snippet
    val saintOfTheDay: String = "",
    val holyDayOfObligation: Boolean = false
)

enum class PrayerCategory(val title: String, val iconName: String) {
    HOURS("Liturgy of the Hours", "Schedule"),
    ROSARY("Holy Rosary", "Psychology"),
    DIVINE_MERCY("Divine Mercy", "Favorite"),
    TRADITIONAL("Traditional Prayers", "MenuBook"),
    DEVOTIONS("Novenas & Devotions", "AutoAwesome")
}

data class Prayer(
    val id: String,
    val title: String,
    val category: PrayerCategory,
    val subtitle: String = "",
    val text: String,
    val latinText: String? = null,
    val isFavorite: Boolean = false
)
