package com.willykez.liturgx.data

import android.content.Context
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.core.LiturgicalDayResolver
import com.willykez.liturgx.core.Reading
import com.willykez.liturgx.core.RegionSettings
import com.willykez.liturgx.core.ResolvedDay
import com.willykez.liturgx.core.Saint
import com.willykez.liturgx.core.Season
import com.willykez.liturgx.core.SwahiliDate
import java.time.LocalDate

/** Everything the UI needs to render a single day. */
data class DayResult(
    val resolved: ResolvedDay,
    val readings: List<Reading>,
    val optionalMemorial: Saint?
)

class LectionaryRepository(context: Context) {

    private val dao = LectionaryDao(DatabaseProvider.get(context.applicationContext))

    fun getForDate(date: LocalDate, region: RegionSettings): DayResult {
        val seasonal = LiturgicalDayResolver.resolveSeasonal(date, region)
        val skipFixed = LiturgicalDayResolver.skipsFixedCalendar(date, seasonal.season)
        val fullDate = SwahiliDate.full(date)

        // Rule 2/3: proper-of-time solemnities & sikukuu_maalum can override an Ordinary-Time slot.
        // (Advent/Lent/Easter Sundays and the Triduum are already resolved directly by season logic
        // and are never weaker than a sikukuu_maalum row, so we only ever apply this in Ordinary Time.)
        if (!skipFixed && seasonal.season == Season.MUDA_WA_KAWAIDA) {
            val allHits = dao.sikukuuMaalumFor(fullDate)
            if (allHits.isNotEmpty()) {
                // BUGFIX: a handful of sikukuu_maalum rows (e.g. Kubadilika Sura kwa Bwana /
                // Transfiguration) are split into three rows, one per Sunday-cycle year, via
                // `mwaka_liturujia`. The old code returned every matching row regardless of
                // cycle, which meant all three years' worth of readings got stacked together
                // on screen every year instead of just the current one. Filter to the current
                // cycle when the hit set actually varies by year; rows with no year tag (the
                // overwhelming majority of sikukuu_maalum entries) are unaffected.
                val sikukuu = if (allHits.any { it.mwakaLiturujia != null }) {
                    allHits.filter { it.mwakaLiturujia == null || it.mwakaLiturujia == seasonal.cycleYear }
                } else {
                    allHits
                }
                val overridden = seasonal.copy(
                    season = Season.SIKUKUU_MAALUM,
                    periodKey = sikukuu.first().periodKey,
                    label = prettyLabel(sikukuu.first().periodKey),
                    entryKeys = sikukuu.mapNotNull { it.entryKey ?: "" },
                    color = LiturgicalColor.NYEUPE
                )
                return DayResult(overridden, sikukuu, findOptionalMemorial(dao, fullDate, skipFixed))
            }
        }

        // Rule 6/7: an obligatory memorial/feast changes the day's identity (name + colour) but this
        // dataset has no "Common of Saints" text bank, so the underlying seasonal readings are kept —
        // exactly the fallback APP_LOGIC.md §9 recommends for any kalenda_ya_watakatifu lookup miss,
        // applied here even on a hit, since there's no proper text to substitute.
        var resolvedDay = seasonal
        if (!skipFixed) {
            val saintHits = dao.saintFor(fullDate)
            val obligatory = saintHits.firstOrNull { it.daraja != "Kumbukumbu ya Hiari" }
            if (obligatory != null) {
                resolvedDay = seasonal.copy(
                    overridingSaint = obligatory,
                    color = LiturgicalColor.fromSwahili(obligatory.rangi)
                )
            }
        }

        val readings = dao.readings(seasonal.season.key, seasonal.periodKey, seasonal.entryKeys, seasonal.dayFilter)
        return DayResult(resolvedDay, readings, findOptionalMemorial(dao, fullDate, skipFixed))
    }

    private fun findOptionalMemorial(dao: LectionaryDao, fullDate: String, skipFixed: Boolean): Saint? {
        if (skipFixed) return null
        return dao.saintFor(fullDate).firstOrNull { it.daraja == "Kumbukumbu ya Hiari" }
    }

    fun allSaints(): List<Saint> = dao.allSaints()

    /** Pentecost Vigil's default first reading + the three alternates, offered as a picker. */
    fun pentecostVigilAlternates(): List<Reading> =
        dao.readings("pasaka", "pentekoste", listOf("vigilia", "vigilia_chaguo_1", "vigilia_chaguo_2", "vigilia_chaguo_3"))

    /** Pentecost Day Mass optional Second-Reading/Gospel substitutions for Years B & C. */
    fun pentecostDayOptional(cycle: String): List<Reading> =
        if (cycle == "B" || cycle == "C") dao.readings("pasaka", "pentekoste", listOf("siku_hiari_mwaka_$cycle")) else emptyList()

    private fun prettyLabel(periodKey: String): String = periodKey
        .replace('_', ' ')
        .replaceFirstChar { it.uppercase() }
}
