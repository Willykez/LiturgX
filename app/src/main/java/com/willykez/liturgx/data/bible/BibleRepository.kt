package com.willykez.liturgx.data.bible

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.willykez.liturgx.data.lectionary.CitationParser
import com.willykez.liturgx.data.lectionary.VersePart
import kotlin.math.abs

/**
 * One resolved verse: its chapter/number and cleaned display text.
 *
 * [startsNewRange] is true for the first verse of a new comma/semicolon-separated citation group
 * (e.g. each of "2-3", "4-5", "6-7", "8-9" in a Psalm citation is its own group) — this is what
 * lets the UI tell a genuine new "stanza" apart from just the next verse in a continuous passage.
 * [hasGapBeforeIt] is true when that new group doesn't pick up immediately after the previous
 * one (a verse or more was skipped, e.g. the "11" in "6-8,9,11-13") — see [BiblePassage.renderedText].
 * [isPoetic] mirrors whether this verse is laid out as poetry in the source text (the bundled
 * Bible marks Psalms and poetic oracles with internal line breaks) — also used for rendering.
 * [part] is which portion of the verse the citation asked for — see [VersePart].
 */
data class BibleVerse(
    val chapter: Int,
    val verse: Int,
    val text: String,
    val startsNewRange: Boolean,
    val hasGapBeforeIt: Boolean = false,
    val isPoetic: Boolean = false,
    val part: VersePart = VersePart.ALL
)

data class BiblePassage(
    val book: String,
    val citation: String,
    val verses: List<BibleVerse>,
    val hasGaps: Boolean
) {
    /**
     * The passage stitched into one readable block, the way a Missal would set it:
     *  - Poetic verses (Psalms, poetic oracles) render one line per verse, with a blank line
     *    between citation groups — each comma-separated group in the citation is its own stanza,
     *    whether or not the verse numbers are actually contiguous (a Psalm citation like
     *    "2-3, 4-5, 6-7, 8-9" is four sung strophes, not one skipped verse).
     *  - Prose verses (Gospel/Epistle/OT narrative) flow as a continuous paragraph; a genuine
     *    skip in the verse numbers (e.g. "31-35, 37-39") shows as a visible "[…]" break instead
     *    of silently disappearing.
     */
    fun renderedText(): String {
        if (verses.isEmpty()) return ""
        val sb = StringBuilder()
        verses.forEachIndexed { index, v ->
            if (index == 0) {
                sb.append(v.text)
                return@forEachIndexed
            }
            val prev = verses[index - 1]
            when {
                v.startsNewRange && (v.isPoetic || prev.isPoetic) -> sb.append("\n\n")
                v.startsNewRange && v.hasGapBeforeIt -> sb.append("\n[…]\n")
                v.startsNewRange -> sb.append(if (v.isPoetic || prev.isPoetic) "\n" else " ")
                v.isPoetic || prev.isPoetic -> sb.append("\n")
                else -> sb.append(' ')
            }
            sb.append(v.text)
        }
        return sb.toString()
    }
}

/**
 * Resolves a Lectionary citation (e.g. "Mathayo 5:13-16") into its actual verse text from the
 * bundled Swahili Bible. This is the piece the reading engine adds on top of
 * [com.willykez.liturgx.data.LectionaryRepository]: that repository already knows *which*
 * citation belongs to a given liturgical day; this turns that citation into words on the page.
 *
 * [getPassage] is deliberately tolerant — it returns null when a citation can't be parsed, or
 * names a book this Bible edition doesn't contain (the Deuterocanonical books — Wisdom, Sirach,
 * Tobit, Judith, Baruch, 1–2 Maccabees — are absent from this 66-book database, and several of
 * those appear as First Readings in the Catholic lectionary). Callers should fall back to
 * showing just the citation text in that case, same as the app already did before this engine
 * existed.
 */
class BibleRepository(private val context: Context) {

    fun getPassage(citation: String, fallbackBook: String? = null): BiblePassage? {
        val parsed = CitationParser.parseWithFallback(citation, fallbackBook) ?: return null
        val bookId = BibleBooks.resolveId(parsed.book) ?: return null

        val db = BibleDatabaseHelper.getDatabase(context)
        val verses = mutableListOf<BibleVerse>()
        var previousChapter: Int? = null
        var previousVerse: Int? = null

        parsed.ranges.forEach { range ->
            val verseNumbers = range.verses().toList()
            verseNumbers.forEachIndexed { i, verseNum ->
                val part = when {
                    verseNumbers.size == 1 ->
                        if (range.startPart != VersePart.ALL) range.startPart else range.endPart
                    i == 0 -> range.startPart
                    i == verseNumbers.lastIndex -> range.endPart
                    else -> VersePart.ALL
                }
                val rawText = queryVerseText(db, bookId, range.chapter, verseNum) ?: return@forEachIndexed
                val isPoetic = rawText.contains('\n')
                val displayText = splitVersePart(rawText, part)

                val startsNewRange = i == 0
                val hasGap = previousChapter != null &&
                    !(range.chapter == previousChapter && verseNum == previousVerse!! + 1)

                verses.add(
                    BibleVerse(
                        chapter = range.chapter,
                        verse = verseNum,
                        text = displayText,
                        startsNewRange = startsNewRange,
                        hasGapBeforeIt = startsNewRange && hasGap,
                        isPoetic = isPoetic,
                        part = part
                    )
                )
                previousChapter = range.chapter
                previousVerse = verseNum
            }
        }

        if (verses.isEmpty()) return null
        return BiblePassage(
            book = parsed.book,
            citation = parsed.canonicalCitation(),
            verses = verses,
            hasGaps = parsed.hasSkips()
        )
    }

    private fun queryVerseText(db: SQLiteDatabase, bookId: Int, chapter: Int, verse: Int): String? {
        db.rawQuery(
            "SELECT text FROM texts WHERE chapter_id = ? AND chapter_num = ? AND position = ? AND head = 0 LIMIT 1",
            arrayOf(bookId.toString(), chapter.toString(), verse.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            val raw = cursor.getString(0) ?: return null
            return cleanVerseText(raw)
        }
    }

    companion object {
        /**
         * Each row is stored as "<Swahili text> <br/><i>English reference text</i>" — but poetic
         * verses (Psalms, poetic oracles) ALSO use a bare "<br>" (no slash) as an internal
         * mid-verse line break, e.g. "Na waseme hivi..., <br>Wale aliowakomboa...". Only the
         * slashed "<br/>" marks where the English portion starts; a bare "<br>" belongs to the
         * Swahili text and must become a newline, not a cut point. (A previous version of this
         * function cut at the first "<br" of either kind, which silently dropped the second half
         * of every poetic verse — fixed here.)
         */
        fun cleanVerseText(raw: String): String {
            val swahiliPart = raw.substringBefore("<br/>")
            val withLineBreaks = swahiliPart.replace(Regex("""<br\s*/?>"""), "\n")
            val noTags = withLineBreaks.replace(Regex("<[^>]*>"), "")
            return noTags
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString("\n")
        }

        /**
         * Best-effort trim of a whole verse's text down to its "a" or "b" half. The database has
         * no sub-verse boundaries (see [VersePart]), so this is a heuristic, not exact data:
         *  - If the verse is poetic with exactly one internal line break, that break IS the
         *    a/b boundary almost always (a Psalm verse's two parallel lines).
         *  - Otherwise, split at sentence boundaries if there's more than one sentence, or at
         *    comma/semicolon clause boundaries as a last resort — choosing whichever split point
         *    lands closest to the middle of the verse, since the Lectionary's "a"/"b" split is
         *    always roughly a half, not an exact character count.
         * [VersePart.AB] (and [VersePart.ALL]) return the full text unchanged — "ab" means both
         * halves were selected, which is the whole verse anyway.
         */
        private fun splitVersePart(text: String, part: VersePart): String {
            if (part != VersePart.A && part != VersePart.B) return text

            val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
            val clauses: List<String> = when {
                lines.size >= 2 -> lines
                else -> {
                    val bySentence = splitClauses(text, Regex("""(?<=[.!?])\s+"""))
                    if (bySentence.size >= 2) bySentence else splitClauses(text, Regex("""(?<=[,;])\s+"""))
                }
            }
            if (clauses.size < 2) return text

            val lengths = clauses.map { it.length }
            val total = lengths.sum()
            var cumulative = 0
            var splitIndex = 1
            var bestDiff = Int.MAX_VALUE
            for (i in 1 until clauses.size) {
                cumulative += lengths[i - 1]
                val diff = abs(cumulative - total / 2)
                if (diff < bestDiff) {
                    bestDiff = diff
                    splitIndex = i
                }
            }

            val selected = if (part == VersePart.A) clauses.subList(0, splitIndex) else clauses.subList(splitIndex, clauses.size)
            val joiner = if (lines.size >= 2) "\n" else " "
            return closeClause(selected.joinToString(joiner))
        }

        private fun splitClauses(text: String, delimiter: Regex): List<String> =
            text.split(delimiter).map { it.trim() }.filter { it.isNotEmpty() }

        /** A trimmed-down clause reads as a standalone excerpt, so it should end like a sentence. */
        private fun closeClause(text: String): String {
            val trimmed = text.trim()
            if (trimmed.isEmpty()) return trimmed
            return if (trimmed.last() in ".!?") trimmed else trimmed.trimEnd(',', ';', ':') + "."
        }
    }
}
