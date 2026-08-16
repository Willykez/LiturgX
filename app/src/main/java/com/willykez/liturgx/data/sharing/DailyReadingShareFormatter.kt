package com.willykez.liturgx.data.sharing

import com.willykez.liturgx.core.ReadingItem
import com.willykez.liturgx.data.DayResult
import com.willykez.liturgx.data.bible.BiblePassage

/**
 * Renders a day's readings as the plain-text block used when sharing to WhatsApp, Notes, etc. —
 * dashed dividers and the standard Mass-reading responses, the way a Tanzanian parish bulletin
 * lays a day's readings out. This is what the reading engine's verse text is *for*: before it,
 * sharing a day only ever meant sharing bare citations (see the old [ReadingBlock] copy-only
 * behaviour); now the full Scripture text travels with it.
 *
 * @param passages resolved Swahili verse text for each item, keyed by [ReadingItem.citation] —
 *   as looked up via [com.willykez.liturgx.data.bible.BibleRepository.getPassage]. A missing or
 *   null entry (an unresolvable citation, e.g. a Deuterocanonical First Reading) falls back to
 *   the bare citation text, same as the on-screen card does.
 */
object DailyReadingShareFormatter {

    private const val DIVIDER = "----------------------------------------"

    fun format(
        dayResult: DayResult,
        dateLine: String,
        items: List<ReadingItem>,
        passages: Map<String, BiblePassage?>
    ): String {
        val resolved = dayResult.resolved
        val sb = StringBuilder()

        sb.appendLine((resolved.overridingSaint?.jina ?: resolved.label).uppercase())
        sb.appendLine(dateLine)
        sb.appendLine(
            "Rangi ya Liturujia: ${resolved.color.swahili}"
                + (resolved.cycleYear?.let { " · Mwaka $it" } ?: "")
                + (resolved.weekdayCycle?.let { " · Mzunguko $it" } ?: "")
        )
        sb.appendLine()

        items.forEach { item ->
            sb.appendLine(DIVIDER)
            sb.appendLine("${item.label.uppercase()}: ${item.citation}")
            sb.appendLine(DIVIDER)
            val body = passages[item.citation]?.renderedText() ?: item.citation
            sb.appendLine(body)
            sb.appendLine()
            when (item.kindKey) {
                "SOMO_LA_KWANZA", "SOMO_LA_PILI" -> {
                    sb.appendLine("Neno la Bwana.")
                    sb.appendLine("S: Tumshukuru Mungu.")
                    sb.appendLine()
                }
                "INJILI" -> {
                    sb.appendLine("Injili ya Bwana Yesu Kristo.")
                    sb.appendLine("S: Sifa kwako Ee Kristo.")
                    sb.appendLine()
                }
            }
        }

        sb.appendLine(DIVIDER)
        sb.append("Imetumwa kutoka LiturgX")
        return sb.toString().trimEnd()
    }
}
