package com.willykez.liturgx.core

import java.time.LocalDate

/**
 * APP_LOGIC.md §6. The bundled `mizunguko_ya_miaka` table only covers 2024–2035;
 * beyond that we compute on the fly from the known anchor: liturgical year 2026 = Sunday
 * Cycle A (verified against the USCCB 2026 Ordo), cycles repeat A→B→C→A each liturgical year.
 * Weekday cycle is simply odd civil year = I, even = II.
 */
object CycleResolver {

    private val sundayCycleOrder = listOf("A", "B", "C")

    fun liturgicalYearFor(date: LocalDate): Int {
        val advent1 = Computus.anchorsFor(date.year).firstSundayAdvent
        return if (!date.isBefore(advent1)) date.year + 1 else date.year
    }

    fun sundayCycle(liturgicalYear: Int): String {
        val offset = ((liturgicalYear - 2026) % 3 + 3) % 3
        return sundayCycleOrder[offset]
    }

    fun weekdayCycle(civilYear: Int): String = if (civilYear % 2 == 0) "II" else "I"
}
