package com.willykez.liturgx.core

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.floor

/** One resolved "address" inside `readings`, before any saint/solemnity override is applied. */
data class SeasonalSpot(
    val periodKey: String,
    val entryKeys: List<String>,
    val dayFilter: String? = null,
    val label: String,
    val color: LiturgicalColor
)

object PeriodResolver {

    fun resolve(
        date: LocalDate,
        season: Season,
        region: RegionSettings,
        sundayCycle: String,
        weekdayCycle: String
    ): SeasonalSpot = when (season) {
        Season.MAJILIO -> majilio(date, region, sundayCycle)
        Season.NOELI -> noeli(date, region, sundayCycle)
        Season.KWARESIMA -> kwaresima(date, sundayCycle)
        Season.PASAKA -> pasaka(date, region, sundayCycle)
        Season.MUDA_WA_KAWAIDA -> mudaWaKawaida(date, region, sundayCycle, weekdayCycle)
        Season.SIKUKUU_MAALUM -> throw IllegalStateException("sikukuu_maalum is resolved as an override, not a season spot")
    }

    private fun weeksBetween(start: LocalDate, date: LocalDate): Int =
        floor(ChronoUnit.DAYS.between(start, date) / 7.0).toInt()

    // ---------------------------------------------------------------- Majilio (Advent)

    private fun majilio(date: LocalDate, region: RegionSettings, cycle: String): SeasonalSpot {
        val advent1 = Computus.anchorsFor(date.year).firstSundayAdvent
        if (date.monthValue == 12 && date.dayOfMonth in 17..24) {
            val dayStr = if (date.dayOfMonth == 24) "Des 24 asubuhi" else SwahiliDate.abbrev(date)
            return SeasonalSpot(
                periodKey = "desemba_17_hadi_24",
                entryKeys = listOf("tarehe_maalum"),
                dayFilter = dayStr,
                label = "Majilio — ${SwahiliDate.full(date)}",
                color = LiturgicalColor.ZAMBARAU
            )
        }
        val week = weeksBetween(advent1, date) + 1
        val wk = "wiki_%02d".format(week.coerceIn(1, 4))
        val isGaudete = week == 3 && date.dayOfWeek == java.time.DayOfWeek.SUNDAY
        return if (date.dayOfWeek == java.time.DayOfWeek.SUNDAY) {
            SeasonalSpot(wk, listOf("dominika_mwaka_$cycle"), null,
                "Dominika ya $week ya Majilio (Mwaka $cycle)",
                if (isGaudete) LiturgicalColor.WARIDI else LiturgicalColor.ZAMBARAU)
        } else {
            SeasonalSpot(wk, listOf("siku_za_wiki"), SwahiliDate.weekdayCode(date),
                "Wiki ya $week ya Majilio", LiturgicalColor.ZAMBARAU)
        }
    }

    // --------------------------------------------------------------- Noeli (Christmas)

    private fun noeli(date: LocalDate, region: RegionSettings, cycle: String): SeasonalSpot {
        val christmasYear = if (date.monthValue == 12) date.year else date.year - 1
        val christmas = LocalDate.of(christmasYear, 12, 25)
        val holyFamily = Computus.holyFamily(christmasYear)
        val epiphany = Computus.epiphanyDate(christmasYear + 1, region)
        val baptism = Computus.baptismOfTheLord(christmasYear + 1, region)

        return when {
            date == christmas -> SeasonalSpot("mchana", listOf("mchana"), null,
                "Sikukuu ya Kuzaliwa kwa Bwana", LiturgicalColor.NYEUPE)
            date == LocalDate.of(christmasYear + 1, 1, 1) -> SeasonalSpot(
                "maria_mama_wa_mungu", listOf("maria_mama_wa_mungu"), null,
                "Maria Mama wa Mungu", LiturgicalColor.NYEUPE)
            date == holyFamily -> SeasonalSpot("familia_takatifu", listOf("mwaka_$cycle"), null,
                "Familia Takatifu (Mwaka $cycle)", LiturgicalColor.NYEUPE)
            date == epiphany -> SeasonalSpot("epifania", listOf("epifania"), null,
                "Epifania ya Bwana", LiturgicalColor.NYEUPE)
            date == baptism -> SeasonalSpot("ubatizo_wa_bwana", listOf("mwaka_$cycle"), null,
                "Ubatizo wa Bwana (Mwaka $cycle)", LiturgicalColor.NYEUPE)
            date.monthValue == 12 && date.dayOfMonth in 26..31 -> SeasonalSpot(
                "oktava", listOf("oktava"), SwahiliDate.abbrev(date),
                "Oktava ya Noeli — ${SwahiliDate.full(date)}", LiturgicalColor.NYEUPE)
            date.monthValue == 1 && date.dayOfMonth in 2..5 && date.isBefore(epiphany) -> SeasonalSpot(
                "oktava", listOf("oktava"), SwahiliDate.abbrev(date),
                "Kabla ya Epifania — ${SwahiliDate.full(date)}", LiturgicalColor.NYEUPE)
            date.isAfter(epiphany) && date.isBefore(baptism) -> SeasonalSpot(
                "baada_ya_epifania", listOf("baada_ya_epifania"), SwahiliDate.weekdayCode(date),
                "Baada ya Epifania", LiturgicalColor.NYEUPE)
            else -> SeasonalSpot("mchana", listOf("mchana"), null,
                "Noeli", LiturgicalColor.NYEUPE) // safe fallback within the octave window
        }
    }

    // ---------------------------------------------------------------- Kwaresima (Lent)

    private fun kwaresima(date: LocalDate, cycle: String): SeasonalSpot {
        val a = Computus.anchorsFor(date.year)
        return when {
            date == a.ashWednesday -> SeasonalSpot("jumatano_ya_majivu", listOf("jumatano_ya_majivu"), null,
                "Jumatano ya Majivu", LiturgicalColor.ZAMBARAU)
            date.isAfter(a.ashWednesday) && date.isBefore(a.ashWednesday.plusDays(4)) -> SeasonalSpot(
                "baada_ya_majivu", listOf("siku_za_wiki"), SwahiliDate.weekdayCode(date),
                "Baada ya Majivu", LiturgicalColor.ZAMBARAU)
            date == a.palmSunday -> SeasonalSpot("dominika_ya_matawi",
                listOf("mwaka_${cycle}_msafara", "mwaka_${cycle}_misa"), null,
                "Dominika ya Matawi (Mwaka $cycle)", LiturgicalColor.NYEKUNDU)
            date.isAfter(a.palmSunday) && date.isBefore(a.holyThursday) -> SeasonalSpot(
                "wiki_takatifu", listOf("siku_za_wiki"), SwahiliDate.weekdayCode(date),
                "Wiki Takatifu", LiturgicalColor.ZAMBARAU)
            else -> {
                val firstSundayOfLent = a.ashWednesday.plusDays(4)
                val week = weeksBetween(firstSundayOfLent, date) + 1
                val wk = "wiki_%02d".format(week.coerceIn(1, 5))
                val isLaetare = week == 4 && date.dayOfWeek == java.time.DayOfWeek.SUNDAY
                if (date.dayOfWeek == java.time.DayOfWeek.SUNDAY) {
                    SeasonalSpot(wk, listOf("dominika_mwaka_$cycle"), null,
                        "Dominika ya $week ya Kwaresima (Mwaka $cycle)",
                        if (isLaetare) LiturgicalColor.WARIDI else LiturgicalColor.ZAMBARAU)
                } else {
                    SeasonalSpot(wk, listOf("siku_za_wiki"), SwahiliDate.weekdayCode(date),
                        "Wiki ya $week ya Kwaresima", LiturgicalColor.ZAMBARAU)
                }
            }
        }
    }

    // ----------------------------------------------------------------- Pasaka (Easter)

    private fun pasaka(date: LocalDate, region: RegionSettings, cycle: String): SeasonalSpot {
        val a = Computus.anchorsFor(date.year)
        return when {
            date == a.holyThursday -> SeasonalSpot("alhamisi_kuu", listOf("alhamisi_kuu"), null,
                "Alhamisi Kuu — Karamu ya Bwana", LiturgicalColor.NYEUPE)
            date == a.goodFriday -> SeasonalSpot("ijumaa_kuu", listOf("ijumaa_kuu"), null,
                "Ijumaa Kuu — Mateso ya Bwana", LiturgicalColor.NYEKUNDU)
            date == a.easterVigil -> SeasonalSpot("vigilia_ya_pasaka",
                listOf("agano_la_kale_1", "agano_la_kale_2", "agano_la_kale_3", "agano_la_kale_4",
                    "agano_la_kale_5", "agano_la_kale_6", "agano_la_kale_7", "mwaka_$cycle"),
                null, "Vigilia Takatifu ya Pasaka", LiturgicalColor.NYEUPE)
            date == a.easterSunday -> SeasonalSpot("dominika_ya_ufufuo", listOf("dominika_ya_ufufuo"), null,
                "Dominika ya Ufufuo — Pasaka", LiturgicalColor.NYEUPE)
            date.isAfter(a.easterSunday) && date.isBefore(a.easterSunday.plusDays(7)) -> SeasonalSpot(
                "oktava", listOf("oktava"), SwahiliDate.weekdayCode(date),
                "Oktava ya Pasaka", LiturgicalColor.NYEUPE)
            date == a.ascension(region) -> SeasonalSpot("kupaa_kwa_bwana", listOf("mwaka_$cycle"), null,
                "Kupaa kwa Bwana (Mwaka $cycle)", LiturgicalColor.NYEUPE)
            date == a.pentecost -> SeasonalSpot("pentekoste", listOf("siku"), null,
                "Dominika ya Pentekoste (Mwaka $cycle)", LiturgicalColor.NYEKUNDU)
            date == a.pentecost.minusDays(1) -> SeasonalSpot("pentekoste", listOf("vigilia"), null,
                "Vigilia ya Pentekoste", LiturgicalColor.NYEKUNDU)
            date == a.trinitySunday -> SeasonalSpot("utatu_mtakatifu", listOf("mwaka_$cycle"), null,
                "Utatu Mtakatifu (Mwaka $cycle)", LiturgicalColor.NYEUPE)
            date == a.corpusChristi(region) -> SeasonalSpot("fungu_takatifu_la_mwili_na_damu_ya_kristo",
                listOf("mwaka_$cycle"), null, "Fungu Takatifu la Mwili na Damu ya Kristo (Mwaka $cycle)",
                LiturgicalColor.NYEUPE)
            date == a.sacredHeart -> SeasonalSpot("moyo_mtakatifu_wa_yesu", listOf("mwaka_$cycle"), null,
                "Moyo Mtakatifu wa Yesu (Mwaka $cycle)", LiturgicalColor.NYEKUNDU)
            else -> {
                val week = weeksBetween(a.easterSunday, date) + 1
                val wk = when (week) {
                    2 -> "wiki_02_huruma_ya_mungu"
                    4 -> "wiki_04_mchungaji_mwema"
                    else -> "wiki_%02d".format(week.coerceIn(2, 7))
                }
                if (date.dayOfWeek == java.time.DayOfWeek.SUNDAY) {
                    SeasonalSpot(wk, listOf("mwaka_$cycle"), null,
                        "Dominika ya $week ya Pasaka (Mwaka $cycle)", LiturgicalColor.NYEUPE)
                } else {
                    SeasonalSpot(wk, listOf("siku_za_wiki"), SwahiliDate.weekdayCode(date),
                        "Wiki ya $week ya Pasaka", LiturgicalColor.NYEUPE)
                }
            }
        }
    }

    // -------------------------------------------------------- Muda wa Kawaida (Ordinary Time)

    private fun mudaWaKawaida(
        date: LocalDate, region: RegionSettings, cycle: String, weekdayCycle: String
    ): SeasonalSpot {
        val christmasYear = if (date.monthValue <= 6) date.year - 1 else date.year
        val baptism = Computus.baptismOfTheLord(christmasYear + 1, region)
        val a = Computus.anchorsFor(date.year)

        // NOTE (known simplification, flagged like APP_LOGIC.md §9's own weekday-cycle caveat):
        // exact Ordo week-jump at the Pentecost/Christ-the-King seam varies slightly by year.
        // Block 1 counts forward from Baptism of the Lord; Block 2 counts backward from
        // Christ the King (always week 34) — both match the Ordo in the overwhelming majority
        // of years, but a definitive per-year Ordo table would be needed for full precision.
        val week = if (date.isBefore(a.ashWednesday)) {
            weeksBetween(baptism, date) + 2
        } else {
            34 - weeksBetween(date, a.christTheKing)
        }
        val wClamped = week.coerceIn(1, 34)
        val wk = "wiki_%02d".format(wClamped)

        if (date == a.christTheKing) {
            return SeasonalSpot("wiki_34", listOf("dominika_mwaka_$cycle"), null,
                "Kristo Mfalme wa Ulimwengu (Mwaka $cycle)", LiturgicalColor.NYEUPE)
        }

        return if (date.dayOfWeek == java.time.DayOfWeek.SUNDAY) {
            SeasonalSpot(wk, listOf("dominika_mwaka_$cycle"), null,
                "Dominika ya $wClamped ya Muda wa Kawaida (Mwaka $cycle)", LiturgicalColor.KIJANI)
        } else {
            // Ordinary Time weekdays are the one place the I/II weekday cycle splits the row itself.
            SeasonalSpot(wk, listOf("siku_za_wiki_mwaka_$weekdayCycle"), SwahiliDate.weekdayCode(date),
                "Wiki ya $wClamped ya Muda wa Kawaida (Mzunguko $weekdayCycle)", LiturgicalColor.KIJANI)
        }
    }
}
