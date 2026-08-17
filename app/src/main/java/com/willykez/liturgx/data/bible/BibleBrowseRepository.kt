package com.willykez.liturgx.data.bible

import android.content.Context

/**
 * Testament grouping -- `chapters.mode` in the bundled Bible is 1 for the 39 Old Testament
 * books and 2 for the 27 New Testament books (verified directly against the shipped .sqlite).
 */
enum class Testament { AGANO_LA_KALE, AGANO_JIPYA }

/**
 * One book of the Bible. Despite the table being named `chapters`, each row in it is a whole
 * BOOK (see [BibleDatabaseHelper]'s schema doc) -- `chapterCount` comes from that row's `num`
 * column, which stores the book's total chapter count directly (Mwanzo/Genesis = 50, etc.),
 * so no separate query is needed to know how many chapters a book has.
 */
data class BibleBookInfo(
    val id: Int,
    val name: String,
    val chapterCount: Int,
    val testament: Testament
)

/** One line in a rendered chapter: either a verse, or an editorial section heading. */
data class ChapterLine(
    val position: Int,   // verse number; headings that don't belong to one specific verse use 0
    val isHeading: Boolean,
    val text: String
)

data class SearchResult(
    val bookId: Int,
    val bookName: String,
    val chapterNum: Int,
    val verseNum: Int,
    val text: String
)

/**
 * Browsing and free-text search over the bundled Swahili Bible -- separate from
 * [BibleRepository], which resolves a single Lectionary *citation* into text. This is the
 * "read any book, any chapter" and "find every verse mentioning X" side of the same database.
 */
class BibleBrowseRepository(private val context: Context) {

    fun allBooks(): List<BibleBookInfo> {
        val db = BibleDatabaseHelper.getDatabase(context)
        db.rawQuery("SELECT _id, title, num, mode FROM chapters ORDER BY _id", null).use { cursor ->
            val books = mutableListOf<BibleBookInfo>()
            while (cursor.moveToNext()) {
                books += BibleBookInfo(
                    id = cursor.getInt(0),
                    name = cursor.getString(1),
                    chapterCount = cursor.getInt(2),
                    testament = if (cursor.getInt(3) == 1) Testament.AGANO_LA_KALE else Testament.AGANO_JIPYA
                )
            }
            return books
        }
    }

    /**
     * A full chapter, verses and section headings interleaved in true reading order.
     * Order matters here in a way [BibleDatabaseHelper]'s reading-order caution applies to
     * directly: a heading's `position` records which verse it's *nearest*, not necessarily
     * "immediately before" -- some headings fall right after a verse and before the next
     * (e.g. Mwanzo 4:16's "Wazawa wa Kaini" heading comes after verse 16, not before it).
     * `rank` is the column that actually encodes true reading order; sorting by `position`
     * instead reliably gets a handful of headings-per-book placed one verse too early.
     */
    fun chapter(bookId: Int, chapterNum: Int): List<ChapterLine> {
        val db = BibleDatabaseHelper.getDatabase(context)
        db.rawQuery(
            "SELECT position, head, text FROM texts WHERE chapter_id = ? AND chapter_num = ? ORDER BY rank",
            arrayOf(bookId.toString(), chapterNum.toString())
        ).use { cursor ->
            val lines = mutableListOf<ChapterLine>()
            while (cursor.moveToNext()) {
                val raw = cursor.getString(2) ?: continue
                lines += ChapterLine(
                    position = cursor.getInt(0),
                    isHeading = cursor.getInt(1) == 1,
                    text = BibleRepository.cleanVerseText(raw)
                )
            }
            return lines
        }
    }

    /**
     * Free-text search across every verse in the Bible. The raw `text` column carries an
     * appended English gloss after `<br/>` (see [BibleRepository.cleanVerseText]'s doc), so a
     * plain SQL `LIKE` would also surface verses where the term only appears in that English
     * tail, not the Swahili verse itself. `LIKE` is used as a fast prefilter, then each
     * candidate is re-checked against only its Swahili portion before being accepted --
     * precision over relying on the database alone.
     */
    fun search(query: String, limit: Int = 100): List<SearchResult> {
        val term = query.trim()
        if (term.length < 2) return emptyList()

        val db = BibleDatabaseHelper.getDatabase(context)
        val escaped = term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
        val likePattern = "%$escaped%"

        db.rawQuery(
            """
            SELECT c.title, t.chapter_id, t.chapter_num, t.position, t.text
            FROM texts t
            JOIN chapters c ON c._id = t.chapter_id
            WHERE t.head = 0 AND t.text LIKE ? ESCAPE '\'
            ORDER BY t.chapter_id, t.chapter_num, t.position
            LIMIT ?
            """.trimIndent(),
            // Over-fetch: some LIKE hits will be filtered out below as English-only matches.
            arrayOf(likePattern, (limit * 3).toString())
        ).use { cursor ->
            val results = mutableListOf<SearchResult>()
            while (cursor.moveToNext() && results.size < limit) {
                val raw = cursor.getString(4) ?: continue
                val swahiliOnly = raw.substringBefore("<br/>")
                if (!swahiliOnly.contains(term, ignoreCase = true)) continue
                results += SearchResult(
                    bookName = cursor.getString(0),
                    bookId = cursor.getInt(1),
                    chapterNum = cursor.getInt(2),
                    verseNum = cursor.getInt(3),
                    text = BibleRepository.cleanVerseText(raw)
                )
            }
            return results
        }
    }
}
