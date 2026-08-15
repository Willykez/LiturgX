package com.willykez.liturgx.data.sharing

import com.willykez.liturgx.data.bible.BiblePassage
import com.willykez.liturgx.data.engine.LiturgicalCalendarEngine
import com.willykez.liturgx.model.AppLanguage
import com.willykez.liturgx.model.LiturgicalDay
import com.willykez.liturgx.model.ReadingItem
import com.willykez.liturgx.model.ReadingLabels
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Renders a [LiturgicalDay] as the plain-text block format used when sharing readings to
 * another app (WhatsApp, Notes, etc.) — dashed section dividers and the standard Mass-reading
 * formulas ("Neno la Bwana... / S: Tumshukuru Mungu." or their English equivalents), matching
 * how a Tanzanian parish bulletin typically lays the readings out.
 *
 * Section LABELS, the day heading, and the fixed liturgical RESPONSES follow [format]'s
 * [language] parameter. The reading BODY text itself always comes from [passages] — the
 * bundled Swahili Bible, resolved by the caller via BibleRepository before calling [format] —
 * since that's the only full verse text this app has, regardless of the label language.
 */
object LectionaryShareFormatter {

    private const val DIVIDER = "--------------------------------------------------"

    /**
     * @param passages resolved Swahili verse text for each citation, keyed by [ReadingItem.citation],
     *   as looked up via BibleRepository.getPassage(). A missing/null entry falls back to
     *   [ReadingItem.text] (whatever language that happens to be in).
     * @param language which language the section LABELS and day heading are shown in. The
     *   Scripture body text itself is unaffected — it's always whatever [passages] resolved
     *   (Swahili, from the bundled Bible database), since that's the only full verse text this
     *   app has. In English mode a short note is appended making that explicit.
     */
    fun format(
        day: LiturgicalDay,
        date: LocalDate,
        passages: Map<String, BiblePassage?> = emptyMap(),
        language: AppLanguage = AppLanguage.SWAHILI
    ): String {
        val sb = StringBuilder()
        val sw = language == AppLanguage.SWAHILI

        val heading = if (sw) SwahiliLiturgicalLabels.dayHeading(date) else day.title.uppercase(Locale.ROOT)
        sb.appendLine(heading)
        sb.appendLine(date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)).uppercase(Locale.ROOT))
        sb.appendLine()

        val firstLabel = if (sw) "SOMO LA KWANZA" else "FIRST READING"
        val secondLabel = if (sw) "SOMO LA PILI" else "SECOND READING"
        val psalmLabel = if (sw) "WIMBO WA KATIKATI" else "RESPONSORIAL PSALM"
        val gospelLabel = if (sw) "INJILI" else "GOSPEL"
        val readingClosing = if (sw) "Neno la Bwana..." to "S: Tumshukuru Mungu." else "The word of the Lord." to "R. Thanks be to God."
        val gospelClosing = if (sw) "Injili ya Bwana..." to "S: Sifa kwako Ee Kristo." else "The Gospel of the Lord." to "R. Praise to you, Lord Jesus Christ."

        appendSection(sb, firstLabel, day.firstReading, passages)
        appendSimpleClosing(sb, readingClosing.first, readingClosing.second)

        appendPsalmSection(sb, psalmLabel, day.responsorialPsalm, passages, if (sw) "Kuitikia" else "Response")

        day.secondReading?.let { second ->
            appendSection(sb, secondLabel, second, passages)
            appendSimpleClosing(sb, readingClosing.first, readingClosing.second)
        }

        sb.appendLine(DIVIDER)
        sb.appendLine(if (sw) "SHANGILIO LA INJILI (HALELUYA)" else "GOSPEL ACCLAMATION (ALLELUIA)")
        sb.appendLine(DIVIDER)
        sb.appendLine("Haleluya, Haleluya.")
        if (day.gospel.headline.isNotBlank()) {
            sb.appendLine("\"${day.gospel.headline}\"")
        }
        sb.appendLine("Haleluya.")
        sb.appendLine()

        appendSection(sb, gospelLabel, day.gospel, passages, citationPrefix = gospelLabel)
        appendSimpleClosing(sb, gospelClosing.first, gospelClosing.second)

        if (!sw) {
            sb.appendLine(ReadingLabels.swahiliTextNote(AppLanguage.ENGLISH))
        }

        return sb.toString().trimEnd()
    }

    private fun appendSection(
        sb: StringBuilder,
        label: String,
        reading: ReadingItem,
        passages: Map<String, BiblePassage?>,
        citationPrefix: String = label
    ) {
        sb.appendLine(DIVIDER)
        sb.appendLine("$citationPrefix: ${reading.citation}")
        sb.appendLine(DIVIDER)
        val resolved = passages[reading.citation]
        val bodyText = if (resolved != null && resolved.verses.isNotEmpty()) resolved.renderedText() else reading.text
        sb.appendLine(bodyText)
        sb.appendLine()
    }

    private fun appendPsalmSection(
        sb: StringBuilder,
        sectionLabel: String,
        psalm: ReadingItem,
        passages: Map<String, BiblePassage?>,
        responseLabel: String
    ) {
        sb.appendLine(DIVIDER)
        sb.appendLine("$sectionLabel: ${psalm.citation}")
        sb.appendLine(DIVIDER)
        if (psalm.responsorialVerse.isNotBlank()) {
            sb.appendLine("$responseLabel: ${psalm.responsorialVerse}")
            sb.appendLine()
        }
        val resolved = passages[psalm.citation]
        val bodyText = if (resolved != null && resolved.verses.isNotEmpty()) resolved.renderedText() else psalm.text
        // Break the psalm body into numbered stanzas at sentence boundaries, with the
        // refrain marker (K) after each — mirrors how a Missal typically sets the responsorial psalm.
        val sentences = bodyText.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
        val stanzaSize = maxOf(1, (sentences.size + 2) / 3) // roughly 3 stanzas
        sentences.chunked(stanzaSize).forEachIndexed { index, chunk ->
            sb.appendLine("${index + 1}. ${chunk.joinToString(" ")} (K)")
            sb.appendLine()
        }
    }

    private fun appendSimpleClosing(sb: StringBuilder, closing: String, response: String) {
        sb.appendLine(closing)
        sb.appendLine(response)
        sb.appendLine()
    }
}

/** Best-effort Swahili day/season heading, e.g. "DOMINIKA YA 4 YA MWAKA A". */
object SwahiliLiturgicalLabels {

    private val SEASON_NAMES = mapOf(
        com.willykez.liturgx.model.LiturgicalSeason.ADVENT to "MAJILIO",
        com.willykez.liturgx.model.LiturgicalSeason.CHRISTMAS to "NOELI",
        com.willykez.liturgx.model.LiturgicalSeason.LENT to "KWARESIMA",
        com.willykez.liturgx.model.LiturgicalSeason.HOLY_WEEK to "WIKI KUU",
        com.willykez.liturgx.model.LiturgicalSeason.EASTER to "PASAKA",
        com.willykez.liturgx.model.LiturgicalSeason.ORDINARY_TIME to "MWAKA"
    )

    private val WEEKDAY_NAMES_SW = mapOf(
        java.time.DayOfWeek.MONDAY to "JUMATATU",
        java.time.DayOfWeek.TUESDAY to "JUMANNE",
        java.time.DayOfWeek.WEDNESDAY to "JUMATANO",
        java.time.DayOfWeek.THURSDAY to "ALHAMISI",
        java.time.DayOfWeek.FRIDAY to "IJUMAA",
        java.time.DayOfWeek.SATURDAY to "JUMAMOSI"
    )

    fun dayHeading(date: LocalDate): String {
        val info = LiturgicalCalendarEngine.dayInfo(date)
        val seasonName = SEASON_NAMES[info.season] ?: "MWAKA"
        return if (info.isSunday) {
            when (info.season) {
                com.willykez.liturgx.model.LiturgicalSeason.ORDINARY_TIME ->
                    "DOMINIKA YA ${info.weekOfSeason} YA MWAKA ${info.sundayCycle.name}"
                else ->
                    "DOMINIKA YA ${info.weekOfSeason} YA $seasonName"
            }
        } else {
            val weekdayName = WEEKDAY_NAMES_SW[date.dayOfWeek] ?: date.dayOfWeek.name
            "$weekdayName YA JUMA LA ${info.weekOfSeason} YA $seasonName"
        }
    }
}
