package com.willykez.liturgx.core

import java.time.LocalDate

/** The nine liturgical seasons/blocks as used throughout the dataset. */
enum class Season(val key: String, val label: String) {
    MAJILIO("majilio", "Majilio"),
    NOELI("noeli", "Noeli"),
    KWARESIMA("kwaresima", "Kwaresima"),
    PASAKA("pasaka", "Pasaka"),
    MUDA_WA_KAWAIDA("muda_wa_kawaida", "Muda wa Kawaida"),
    SIKUKUU_MAALUM("sikukuu_maalum", "Sikukuu Maalum");

    companion object {
        fun fromKey(key: String) = entries.first { it.key == key }
    }
}

/** Liturgical colour — drives the whole visual identity of a given day. */
enum class LiturgicalColor(val swahili: String, val hex: Long) {
    ZAMBARAU("zambarau", 0xFF5B3A8E),   // purple — Advent & Lent
    NYEUPE("nyeupe", 0xFFC9A227),       // white/gold — Christmas, Easter, feasts of the Lord/Mary
    NYEKUNDU("nyekundu", 0xFF9E1B32),   // red — Passion, Pentecost, martyrs
    KIJANI("kijani", 0xFF1F6F4A),       // green — Ordinary Time
    WARIDI("waridi", 0xFFD98CB3);       // rose — Gaudete/Laetare Sundays

    companion object {
        fun fromSwahili(s: String?): LiturgicalColor =
            entries.firstOrNull { it.swahili.equals(s?.trim(), ignoreCase = true) } ?: KIJANI
    }
}

/** Fully resolved "address" of a liturgical day inside the dataset. */
data class ResolvedDay(
    val date: LocalDate,
    val season: Season,
    val periodKey: String,
    val label: String,               // human-readable Swahili description of the day
    val cycleYear: String?,          // "A"/"B"/"C" (Sunday) — null if not a Sunday-cycle day
    val weekdayCycle: String?,       // "I"/"II" — for weekday Ordinary Time / feria lookups
    val entryKeys: List<String>,     // one or more entry_key rows to pull from `readings`
    val dayFilter: String? = null,   // the `day` column filter (Mon..Sat, or a date string)
    val overridingSaint: Saint? = null,   // set when a fixed-calendar entry outranks the season
    val optionalMemorial: Saint? = null,  // set when today ALSO has an optional memorial (not swapped in)
    val color: LiturgicalColor = LiturgicalColor.KIJANI
)

data class Reading(
    val id: Int,
    val season: String,
    val periodKey: String,
    val entryKey: String?,
    val day: String?,
    val somoLaKwanza: String?,
    val wimboLaKatikati: String?,
    val somoLaPili: String?,
    val shangilio: String?,
    val injili: String?,
    val mwakaLiturujia: String?
)

data class Saint(
    val id: Int,
    val tarehe: String,
    val jina: String,
    val daraja: String,   // Sikukuu / Sikukuu Kuu / Kumbukumbu / Kumbukumbu ya Hiari
    val rangi: String?
)

/** User-configurable regional variants flagged explicitly in APP_LOGIC.md §9. */
data class RegionSettings(
    val epiphanyMode: EpiphanyMode = EpiphanyMode.TRANSFERRED,
    val keepThursdaySolemnities: Boolean = false // Ascension/Corpus Christi on Thu instead of Sun
)

enum class EpiphanyMode { TRANSFERRED, FIXED_JAN6 }
