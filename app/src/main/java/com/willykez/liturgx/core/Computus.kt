package com.willykez.liturgx.core

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * All movable-feast anchor dates for a given civil year, per APP_LOGIC.md §3.
 * Only Easter itself is computed (Anonymous Gregorian / Meeus algorithm); everything
 * else is a fixed offset from it, or the Sunday nearest a fixed civil date.
 */
data class Anchors(
    val ashWednesday: LocalDate,
    val palmSunday: LocalDate,
    val holyThursday: LocalDate,
    val goodFriday: LocalDate,
    val easterVigil: LocalDate,
    val easterSunday: LocalDate,
    val ascensionThursday: LocalDate,
    val ascensionSunday: LocalDate,
    val pentecost: LocalDate,
    val trinitySunday: LocalDate,
    val corpusChristiThursday: LocalDate,
    val corpusChristiSunday: LocalDate,
    val sacredHeart: LocalDate,
    val firstSundayAdvent: LocalDate,
    val christTheKing: LocalDate
) {
    fun ascension(region: RegionSettings) = if (region.keepThursdaySolemnities) ascensionThursday else ascensionSunday
    fun corpusChristi(region: RegionSettings) = if (region.keepThursdaySolemnities) corpusChristiThursday else corpusChristiSunday
}

object Computus {

    /** Anonymous Gregorian algorithm (Meeus/Jones/Butcher). */
    fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }

    /** Sunday closest to Nov 30 — always falls between Nov 27 and Dec 3. */
    fun firstSundayOfAdvent(year: Int): LocalDate {
        var d = LocalDate.of(year, 11, 27)
        while (d.dayOfWeek != DayOfWeek.SUNDAY) d = d.plusDays(1)
        return d
    }

    private val cache = HashMap<Int, Anchors>()

    fun anchorsFor(year: Int): Anchors = cache.getOrPut(year) {
        val easter = easterSunday(year)
        val advent1 = firstSundayOfAdvent(year)
        Anchors(
            ashWednesday = easter.minusDays(46),
            palmSunday = easter.minusDays(7),
            holyThursday = easter.minusDays(3),
            goodFriday = easter.minusDays(2),
            easterVigil = easter.minusDays(1),
            easterSunday = easter,
            ascensionThursday = easter.plusDays(39),
            ascensionSunday = easter.plusDays(42),
            pentecost = easter.plusDays(49),
            trinitySunday = easter.plusDays(56),
            corpusChristiThursday = easter.plusDays(60),
            corpusChristiSunday = easter.plusDays(63),
            sacredHeart = easter.plusDays(68),
            firstSundayAdvent = advent1,
            christTheKing = advent1.minusDays(7)
        )
    }

    /** Sunday-nearest-Jan-6 rule used by the "transferred" Epiphany mode (Jan 2–8). */
    fun epiphanySunday(year: Int): LocalDate {
        var d = LocalDate.of(year, 1, 2)
        while (d.dayOfWeek != DayOfWeek.SUNDAY) d = d.plusDays(1)
        return d
    }

    fun epiphanyDate(year: Int, region: RegionSettings): LocalDate =
        if (region.epiphanyMode == EpiphanyMode.FIXED_JAN6) LocalDate.of(year, 1, 6)
        else epiphanySunday(year)

    /** Baptism of the Lord: Sunday after Epiphany, UNLESS Epiphany lands Jan 7/8 — then the following Monday. */
    fun baptismOfTheLord(year: Int, region: RegionSettings): LocalDate {
        val epiphany = epiphanyDate(year, region)
        if (region.epiphanyMode == EpiphanyMode.FIXED_JAN6) {
            var d = epiphany.plusDays(1)
            while (d.dayOfWeek != DayOfWeek.SUNDAY) d = d.plusDays(1)
            return d
        }
        return if (epiphany.dayOfMonth >= 7) epiphany.plusDays(1) // following Monday
        else {
            var d = epiphany.plusDays(1)
            while (d.dayOfWeek != DayOfWeek.SUNDAY) d = d.plusDays(1)
            d
        }
    }

    /** The Sunday that carries "Familia Takatifu" (Holy Family) — normally within the Christmas octave. */
    fun holyFamily(year: Int): LocalDate {
        val christmas = LocalDate.of(year, 12, 25)
        if (christmas.dayOfWeek == DayOfWeek.SUNDAY) return LocalDate.of(year, 12, 30) // rule in §7
        var d = christmas.plusDays(1)
        while (d.dayOfWeek != DayOfWeek.SUNDAY) d = d.plusDays(1)
        return d // guaranteed to land by Dec 31
    }
}
