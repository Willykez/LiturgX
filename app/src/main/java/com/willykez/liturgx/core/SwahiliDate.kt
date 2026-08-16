package com.willykez.liturgx.core

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * The dataset uses three different date-string conventions depending on where a
 * date lives — this object reproduces each one exactly so lookups match rows.
 */
object SwahiliDate {

    private val fullMonths = mapOf(
        1 to "Januari", 2 to "Februari", 3 to "Machi", 4 to "Aprili",
        5 to "Mei", 6 to "Juni", 7 to "Julai", 8 to "Agosti",
        9 to "Septemba", 10 to "Oktoba", 11 to "Novemba", 12 to "Desemba"
    )

    /** Used by `kalenda_ya_watakatifu.tarehe` and `sikukuu_maalum` day fields, e.g. "Februari 5". */
    fun full(date: LocalDate): String = "${fullMonths.getValue(date.monthValue)} ${date.dayOfMonth}"

    /** Used by `noeli.oktava` and `majilio.desemba_17_hadi_24`, e.g. "Des 26", "Jan 2". */
    fun abbrev(date: LocalDate): String = when (date.monthValue) {
        12 -> "Des ${date.dayOfMonth}"
        1 -> "Jan ${date.dayOfMonth}"
        else -> "${date.dayOfMonth}"
    }

    /** Mon..Sat short code used by every `siku_za_wiki` / weekday `day` column. Sundays never index here. */
    fun weekdayCode(date: LocalDate): String? = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> null
    }
}
