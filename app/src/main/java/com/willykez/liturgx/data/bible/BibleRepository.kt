package com.willykez.liturgx.data.bible

import android.content.Context
import com.willykez.liturgx.data.lectionary.CitationParser

/**
 * One resolved verse: its chapter/number and cleaned display text.
 * [startsNewRange] is true for the first verse after a citation gap (e.g. the "11" in
 * "6-8,9,11-13") so the UI can render a visible "…" break exactly the way a Missal does.
 */
data class BibleVerse(
    val chapter: Int,
    val verse: Int,
    val text: String,
    val startsNewRange: Boolean
)

data class BiblePassage(
    val book: String,
    val citation: String,
    val verses: List<BibleVerse>,
    val hasGaps: Boolean
) {
    /** The passage stitched into one block of text, with a visible break wherever verses were skipped. */
    fun renderedText(gapMarker: String = "\n[…]\n"): String {
        val sb = StringBuilder()
        verses.forEachIndexed { index, v ->
            if (index > 0 && v.startsNewRange) sb.append(gapMarker) else if (index > 0) sb.append(' ')
            sb.append(v.text)
        }
        return sb.toString()
    }
}

class BibleRepository(private val context: Context) {

    /**
     * Resolves a lectionary citation (e.g. "Matthew 5:13-16", or "12:1-3" when [fallbackBook]
     * supplies the book from context) against the bundled Swahili Bible.
     *
     * Returns null when the citation can't be parsed, or when it names a book this Bible edition
     * doesn't contain (the Deuterocanonical books — Wisdom, Sirach, Tobit, Judith, Baruch, 1–2
     * Maccabees — are absent from this database, and several of those are common Lectionary First
     * Readings). Callers should fall back to showing just the citation text in that case.
     */
    fun getPassage(citation: String, fallbackBook: String? = null): BiblePassage? {
        val parsed = CitationParser.parseWithFallback(citation, fallbackBook) ?: return null
        val bookId = BibleBooks.resolveId(parsed.book) ?: return null

        val db = BibleDatabaseHelper.getDatabase(context)
        val verses = mutableListOf<BibleVerse>()

        parsed.ranges.forEachIndexed { rangeIndex, range ->
            range.verses().forEachIndexed { verseIndexInRange, verseNum ->
                val text = queryVerseText(db, bookId, range.chapter, verseNum) ?: return@forEachIndexed
                verses.add(
                    BibleVerse(
                        chapter = range.chapter,
                        verse = verseNum,
                        text = text,
                        startsNewRange = verseIndexInRange == 0 && rangeIndex > 0
                    )
                )
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

    private fun queryVerseText(db: android.database.sqlite.SQLiteDatabase, bookId: Int, chapter: Int, verse: Int): String? {
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
         * Each verse is stored as "<Swahili text> <br/><i>English reference text</i>".
         * We only want the Swahili portion: cut at the first <br, then strip any stray tags.
         */
        fun cleanVerseText(raw: String): String {
            val cutAtBr = raw.substringBefore("<br")
            val noTags = cutAtBr.replace(Regex("<[^>]*>"), "")
            return noTags
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .trim()
        }
    }
}
