package com.willykez.liturgx.data

import android.content.Context
import java.time.LocalDate

/**
 * Records which days the person actually opened today's readings — the "on this day" streak
 * feature. One entry per date, stored as its ISO-8601 string (e.g. "2026-08-17") in a plain
 * SharedPreferences string set; there's no need for anything heavier than that for a set of
 * dates that only ever grows by one entry a day.
 *
 * [HomeScreen] calls [recordOpen] with today's date every time it's shown — Home always shows
 * *today*, never a browsed date, so "opened Home" and "opened today's reading" are the same
 * event and there's no risk of Calendar-browsing a past date accidentally inflating the streak.
 */
class ProgressStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("liturgx_progress", Context.MODE_PRIVATE)

    fun recordOpen(date: LocalDate) {
        val key = date.toString()
        val current = prefs.getStringSet(KEY_OPENED_DATES, emptySet()) ?: emptySet()
        if (key !in current) {
            // getStringSet returns a live-ish set some OEMs mutate in place; copy defensively
            // before handing it back to SharedPreferences, per the documented contract.
            prefs.edit().putStringSet(KEY_OPENED_DATES, current + key).apply()
        }
    }

    fun openedDates(): Set<LocalDate> =
        (prefs.getStringSet(KEY_OPENED_DATES, emptySet()) ?: emptySet())
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            .toSet()

    companion object {
        private const val KEY_OPENED_DATES = "opened_dates"

        /** Consecutive days ending today (or the most recent opened day, if today isn't opened yet). */
        fun currentStreak(opened: Set<LocalDate>, today: LocalDate): Int {
            var count = 0
            var day = today
            while (opened.contains(day)) {
                count++
                day = day.minusDays(1)
            }
            return count
        }

        /** The longest run of consecutive days ever recorded, for a small "record" note. */
        fun longestStreak(opened: Set<LocalDate>): Int {
            if (opened.isEmpty()) return 0
            val sorted = opened.sorted()
            var longest = 1
            var run = 1
            for (i in 1 until sorted.size) {
                run = if (sorted[i] == sorted[i - 1].plusDays(1)) run + 1 else 1
                if (run > longest) longest = run
            }
            return longest
        }
    }
}
