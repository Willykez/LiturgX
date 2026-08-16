package com.willykez.liturgx.core

import java.time.LocalDate

object LiturgicalDayResolver {

    /** Pure seasonal resolution — no saint/solemnity override applied yet (APP_LOGIC.md §2 steps 1-4). */
    fun resolveSeasonal(date: LocalDate, region: RegionSettings): ResolvedDay {
        val season = SeasonResolver.detect(date, region)
        val liturgicalYear = CycleResolver.liturgicalYearFor(date)
        val sundayCycle = CycleResolver.sundayCycle(liturgicalYear)
        val weekdayCycle = CycleResolver.weekdayCycle(date.year)
        val spot = PeriodResolver.resolve(date, season, region, sundayCycle, weekdayCycle)

        return ResolvedDay(
            date = date,
            season = season,
            periodKey = spot.periodKey,
            label = spot.label,
            cycleYear = sundayCycle,
            weekdayCycle = weekdayCycle,
            entryKeys = spot.entryKeys,
            dayFilter = spot.dayFilter,
            color = spot.color
        )
    }

    /** True if a fixed-calendar lookup (sikukuu_maalum / kalenda_ya_watakatifu) must be skipped
     *  outright, per precedence rule 5: Advent Dec 17-24 and all Lent weekdays outrank them. */
    fun skipsFixedCalendar(date: LocalDate, season: Season): Boolean =
        season == Season.KWARESIMA || (season == Season.MAJILIO && date.monthValue == 12 && date.dayOfMonth in 17..24)
}
