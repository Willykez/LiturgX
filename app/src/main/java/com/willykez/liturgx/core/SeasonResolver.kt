package com.willykez.liturgx.core

import java.time.LocalDate

object SeasonResolver {

    /**
     * Season boundaries per APP_LOGIC.md §4. `sikukuu_maalum` is never returned here —
     * fixed-date solemnities/feasts are resolved as an *override* on top of whichever
     * season the date naturally falls in (see PrecedenceEngine), exactly as the dataset
     * itself only stores `sikukuu_maalum` as a lookup table, not a date range.
     */
    fun detect(date: LocalDate, region: RegionSettings): Season {
        // Advent can only be reached going forward from its own year's anchor.
        val adventThisYear = Computus.anchorsFor(date.year).firstSundayAdvent
        if (!date.isBefore(adventThisYear) && !date.isAfter(LocalDate.of(date.year, 12, 24))) {
            return Season.MAJILIO
        }

        // Christmas season may belong to "last year's" anchors (Jan) or "this year's" (Dec).
        if (isInChristmasWindow(date, region)) return Season.NOELI

        val anchors = Computus.anchorsFor(date.year)
        if (!date.isBefore(anchors.ashWednesday) && date.isBefore(anchors.holyThursday)) {
            return Season.KWARESIMA
        }
        if (!date.isBefore(anchors.holyThursday) && !date.isAfter(anchors.pentecost)) {
            return Season.PASAKA
        }
        return Season.MUDA_WA_KAWAIDA
    }

    private fun isInChristmasWindow(date: LocalDate, region: RegionSettings): Boolean {
        // Try both "Christmas started last civil year" and "starts this civil year" framings.
        for (christmasYear in listOf(date.year, date.year - 1)) {
            val start = LocalDate.of(christmasYear, 12, 25)
            val end = Computus.baptismOfTheLord(christmasYear + 1, region)
            if (!date.isBefore(start) && !date.isAfter(end)) return true
        }
        return false
    }
}
