package com.willykez.liturgx.data.engine

import com.willykez.liturgx.model.LiturgicalColor
import com.willykez.liturgx.model.LiturgicalSeason
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Pure date-math liturgical calendar engine (Roman Rite, General Calendar).
 * No network or database access — every value here is derived algorithmically
 * from a [LocalDate], so it is always correct offline and never goes stale.
 *
 * What this engine gives you for free, for any date in any year:
 *  - Easter Sunday (Gregorian computus)
 *  - The liturgical season and its proper color
 *  - Whether the day is a Sunday/solemnity-rank day or a ferial weekday
 *  - The Sunday reading cycle (A / B / C) and weekday cycle (I / II)
 *  - Key fixed points of the liturgical year (Ash Wednesday, Pentecost, Advent 1, etc.)
 *
 * What it deliberately does NOT attempt: the actual saint-of-the-day / feast
 * calendar (which has centuries of precedence rules and per-region variation).
 * Reading text comes from a bundled lectionary dataset — see LiturgicalRepository.
 */
object LiturgicalCalendarEngine {

    enum class SundayCycle { A, B, C }
    enum class WeekdayCycle { I, II }
    enum class DayRank { SOLEMNITY, SUNDAY, FEAST, MEMORIAL, OPTIONAL_MEMORIAL, WEEKDAY }

    data class LiturgicalDayInfo(
        val date: LocalDate,
        val season: LiturgicalSeason,
        val color: LiturgicalColor,
        val rank: DayRank,
        val weekOfSeason: Int,        // 1-based week number within the season
        val sundayCycle: SundayCycle, // Year A/B/C — applies regardless of day-of-week
        val weekdayCycle: WeekdayCycle, // Year I/II — applies regardless of day-of-week
        val isSunday: Boolean,
        val displayLabel: String      // e.g. "4th Sunday of Advent" / "Saturday of the 3rd Week in Ordinary Time"
    )

    /** Anchor points, computed once per year and cached for the lifetime of the process. */
    data class YearAnchors(
        val year: Int,
        val easterSunday: LocalDate,
        val ashWednesday: LocalDate,
        val palmSunday: LocalDate,
        val holyThursday: LocalDate,
        val goodFriday: LocalDate,
        val pentecost: LocalDate,
        val trinitySunday: LocalDate,
        val corpusChristi: LocalDate,
        val christTheKing: LocalDate,      // last Sunday before Advent 1 (of the SAME liturgical year, i.e. previous Nov)
        val advent1: LocalDate,            // 4th Sunday before Christmas — start of the NEXT liturgical year
        val baptismOfTheLord: LocalDate
    )

    private val anchorCache = HashMap<Int, YearAnchors>()

    /** Gregorian Easter via the anonymous Meeus/Jones/Butcher algorithm. */
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

    fun anchorsFor(year: Int): YearAnchors = anchorCache.getOrPut(year) {
        val easter = easterSunday(year)
        val ash = easter.minusDays(46)
        val palm = easter.minusDays(7)
        val holyThu = easter.minusDays(3)
        val goodFri = easter.minusDays(2)
        val pentecost = easter.plusDays(49)
        val trinity = easter.plusDays(56)
        val corpusChristi = easter.plusDays(60) // Thursday; most regions (incl. USA) transfer to the following Sunday +3
        val christmas = LocalDate.of(year, 12, 25)
        // Advent 1 = 4th Sunday before Christmas
        val christmasSunday = christmas.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val advent1 = christmasSunday.minusWeeks(3)
        val christTheKing = advent1.minusWeeks(1)
        // Epiphany (as celebrated, transferred to a Sunday): the Sunday falling between
        // Jan 2 and Jan 8 inclusive — that window always contains exactly one Sunday, found
        // by taking the next-or-same Sunday from Jan 2 (NOT from Jan 6 — starting from Jan 6
        // can skip forward a full week and land outside the Jan 2–8 window).
        val epiphanySunday = LocalDate.of(year, 1, 2).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val baptism = if (epiphanySunday.dayOfMonth in 7..8) {
            // Epiphany fell on Jan 7 or 8 itself, so Baptism of the Lord moves to the next day (Monday).
            epiphanySunday.plusDays(1)
        } else {
            // Otherwise Baptism of the Lord is the following Sunday.
            epiphanySunday.plusWeeks(1)
        }
        YearAnchors(
            year = year,
            easterSunday = easter,
            ashWednesday = ash,
            palmSunday = palm,
            holyThursday = holyThu,
            goodFriday = goodFri,
            pentecost = pentecost,
            trinitySunday = trinity,
            corpusChristi = corpusChristi,
            christTheKing = christTheKing,
            advent1 = advent1,
            baptismOfTheLord = baptism
        )
    }

    /**
     * The liturgical year number used for the Sunday cycle: the liturgical year that
     * begins on Advent 1 of (calendarYear - 1) and runs through the Saturday before
     * the following Advent 1 is "liturgical year calendarYear".
     */
    private fun liturgicalYearFor(date: LocalDate): Int {
        val thisYearAnchors = anchorsFor(date.year)
        return if (date.isBefore(thisYearAnchors.advent1)) date.year else date.year + 1
    }

    fun sundayCycleFor(date: LocalDate): SundayCycle {
        val litYear = liturgicalYearFor(date)
        return when (litYear % 3) {
            0 -> SundayCycle.C
            1 -> SundayCycle.A
            else -> SundayCycle.B
        }
    }

    fun weekdayCycleFor(date: LocalDate): WeekdayCycle {
        val litYear = liturgicalYearFor(date)
        return if (litYear % 2 == 0) WeekdayCycle.II else WeekdayCycle.I
    }

    fun seasonAndColor(date: LocalDate): Pair<LiturgicalSeason, LiturgicalColor> {
        val thisYear = anchorsFor(date.year)

        // January dates (before Ash Wednesday) belong to the Christmas season that began the
        // PREVIOUS Dec 25, and Baptism of the Lord is computed from Jan 6 of THIS same calendar year.
        if (date.month.value <= 2 && date.isBefore(thisYear.ashWednesday)) {
            if (!date.isAfter(thisYear.baptismOfTheLord)) {
                return LiturgicalSeason.CHRISTMAS to LiturgicalColor.WHITE
            }
        }

        return when {
            // Holy Week: Palm Sunday through Holy Saturday (Easter Vigil counted as the start of Easter below)
            !date.isBefore(thisYear.palmSunday) && date.isBefore(thisYear.easterSunday) ->
                LiturgicalSeason.HOLY_WEEK to (if (date == thisYear.palmSunday || date == thisYear.goodFriday) LiturgicalColor.RED else LiturgicalColor.PURPLE)
            // Easter season: Easter Sunday through Pentecost inclusive
            !date.isBefore(thisYear.easterSunday) && !date.isAfter(thisYear.pentecost) ->
                LiturgicalSeason.EASTER to (if (date == thisYear.pentecost) LiturgicalColor.RED else LiturgicalColor.WHITE)
            // Lent: Ash Wednesday through the day before Palm Sunday
            !date.isBefore(thisYear.ashWednesday) && date.isBefore(thisYear.palmSunday) ->
                LiturgicalSeason.LENT to (if (isGaudeteOrLaetare(date, thisYear)) LiturgicalColor.ROSE else LiturgicalColor.PURPLE)
            // Christmas: Dec 25 through Dec 31 of this calendar year (Jan 1–Baptism handled above)
            !date.isBefore(LocalDate.of(date.year, 12, 25)) ->
                LiturgicalSeason.CHRISTMAS to LiturgicalColor.WHITE
            // Advent: this year's Advent 1 through Dec 24
            !date.isBefore(thisYear.advent1) && date.isBefore(LocalDate.of(date.year, 12, 25)) ->
                LiturgicalSeason.ADVENT to (if (isGaudeteOrLaetare(date, thisYear)) LiturgicalColor.ROSE else LiturgicalColor.PURPLE)
            else ->
                LiturgicalSeason.ORDINARY_TIME to LiturgicalColor.GREEN
        }
    }

    private fun isGaudeteOrLaetare(date: LocalDate, anchors: YearAnchors): Boolean {
        val gaudete = anchors.advent1.plusWeeks(2) // 3rd Sunday of Advent
        val laetare = anchors.easterSunday.minusDays(21) // 4th Sunday of Lent
        return date == gaudete || date == laetare
    }

    fun rankFor(date: LocalDate): DayRank = if (date.dayOfWeek == DayOfWeek.SUNDAY) DayRank.SUNDAY else DayRank.WEEKDAY

    /** Full computed profile for a single date — the offline source of truth. */
    fun dayInfo(date: LocalDate): LiturgicalDayInfo {
        val (season, color) = seasonAndColor(date)
        val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY
        val weekOfSeason = weekOfSeason(date, season)
        val label = buildLabel(date, season, weekOfSeason, isSunday)
        return LiturgicalDayInfo(
            date = date,
            season = season,
            color = color,
            rank = rankFor(date),
            weekOfSeason = weekOfSeason,
            sundayCycle = sundayCycleFor(date),
            weekdayCycle = weekdayCycleFor(date),
            isSunday = isSunday,
            displayLabel = label
        )
    }

    private fun weekOfSeason(date: LocalDate, season: LiturgicalSeason): Int {
        val anchors = anchorsFor(date.year)
        return when (season) {
            LiturgicalSeason.ADVENT -> {
                (java.time.temporal.ChronoUnit.DAYS.between(anchors.advent1, date) / 7).toInt() + 1
            }
            LiturgicalSeason.LENT -> {
                val firstSunday = anchors.ashWednesday.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                if (date.isBefore(firstSunday)) 0
                else (java.time.temporal.ChronoUnit.DAYS.between(firstSunday, date) / 7).toInt() + 1
            }
            LiturgicalSeason.EASTER -> (java.time.temporal.ChronoUnit.DAYS.between(anchors.easterSunday, date) / 7).toInt() + 1
            LiturgicalSeason.ORDINARY_TIME -> {
                if (date.isBefore(anchors.ashWednesday)) {
                    // Before Lent: week 1 starts the Monday after Baptism of the Lord, counted forward.
                    val prevAnchors = anchorsFor(date.year - 1)
                    val baptism = if (date.month.value <= 2) prevAnchors.baptismOfTheLord else anchors.baptismOfTheLord
                    val otStart = baptism.plusDays(1)
                    maxOf(1, (java.time.temporal.ChronoUnit.DAYS.between(otStart, date) / 7).toInt() + 1)
                } else {
                    // After Pentecost: Ordinary Time does NOT restart at week 1 — it resumes wherever
                    // Lent interrupted it. Since the exact resumption number depends on how many pre-Lent
                    // weeks occurred (which varies year to year with the date of Easter), the reliable way
                    // to get the correct number is to count backward from the end: the last Sunday of
                    // Ordinary Time (the Saturday before Advent 1) is always week 34, per the General Norms.
                    val lastOrdinaryWeekEnd = anchors.advent1.minusDays(1) // Saturday before Advent 1
                    val weeksFromEnd = (java.time.temporal.ChronoUnit.DAYS.between(date, lastOrdinaryWeekEnd) / 7).toInt()
                    (34 - weeksFromEnd).coerceAtLeast(1)
                }
            }
            else -> 0
        }
    }

    private fun buildLabel(date: LocalDate, season: LiturgicalSeason, week: Int, isSunday: Boolean): String {
        val ordinal = ordinal(week)
        val dayName = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
        return when (season) {
            LiturgicalSeason.ADVENT -> if (isSunday) "$ordinal Sunday of Advent" else "$dayName of the $ordinal Week of Advent"
            LiturgicalSeason.CHRISTMAS -> if (isSunday) "Sunday in the Christmas Season" else "$dayName in the Christmas Season"
            LiturgicalSeason.LENT -> if (isSunday) "$ordinal Sunday of Lent" else "$dayName of the $ordinal Week of Lent"
            LiturgicalSeason.HOLY_WEEK -> "$dayName of Holy Week"
            LiturgicalSeason.EASTER -> if (isSunday) "$ordinal Sunday of Easter" else "$dayName of the $ordinal Week of Easter"
            LiturgicalSeason.ORDINARY_TIME -> if (isSunday) "$ordinal Sunday in Ordinary Time" else "$dayName of the $ordinal Week in Ordinary Time"
        }
    }

    private fun ordinal(n: Int): String {
        if (n <= 0) return "1st"
        val suffix = when {
            n % 100 in 11..13 -> "th"
            n % 10 == 1 -> "st"
            n % 10 == 2 -> "nd"
            n % 10 == 3 -> "rd"
            else -> "th"
        }
        return "$n$suffix"
    }
}
