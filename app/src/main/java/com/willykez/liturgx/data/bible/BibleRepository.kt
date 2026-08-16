package com.willykez.liturgx.data.bible

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.willykez.liturgx.data.lectionary.CitationParser
import com.willykez.liturgx.data.lectionary.VersePart

/**
 * One resolved verse: its chapter/number and cleaned display text.
 * [startsNewRange] is true for the first verse after a citation gap (e.g. the "11" in
 * "6-8,9,11-13") so the UI can render a visible break exactly the way a Missal does.
 * [part] is which portion of the verse the citation asked for — see [VersePart] for why [text]
 * is always the FULL verse regardless of [part] (the database has no sub-verse boundaries).
 */
data class BibleVerse(
    val chapter: Int,
    val verse: Int,
    val text: String,
    val startsNewRange: Boolean,
    val part: VersePart = VersePart.ALL
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
        val references = CitationParser.expandToReferences(parsed.book, parsed)
        val verses = mutableListOf<BibleVerse>()
        var previousChapter: Int? = null
        var previousVerse: Int? = null

        references.forEach { ref ->
            val text = queryVerseText(db, bookId, ref.chapter, ref.verse) ?: return@forEach
            val isGap = previousChapter != null &&
                !(ref.chapter == previousChapter && ref.verse == previousVerse!! + 1)
            verses.add(
                BibleVerse(
                    chapter = ref.chapter,
                    verse = ref.verse,
                    text = text,
                    startsNewRange = isGap,
                    part = ref.part
                )
            )
            previousChapter = ref.chapter
            previousVerse = ref.verse
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
