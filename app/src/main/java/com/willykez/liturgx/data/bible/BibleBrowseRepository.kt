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

/** Which portion of the Bible a search is restricted to. [Book] pins it to one specific book,
 *  identified by its `chapters._id`. */
sealed class SearchScope {
    data object WholeBible : SearchScope()
    data object OldTestament : SearchScope()
    data object NewTestament : SearchScope()
    data class Book(val bookId: Int, val bookName: String) : SearchScope()
}

enum class SearchMode { PHRASE, ANY_WORD }

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
     * Free-text search across the Bible, optionally restricted to a [scope] (whole Bible, one
     * testament, or one book) and in one of two [mode]s:
     *  - [SearchMode.PHRASE]: the query as one contiguous substring, same as a plain LIKE search.
     *  - [SearchMode.ANY_WORD]: broader recall -- matches a verse containing *any* of the
     *    query's individual words, OR'd together, not requiring them adjacent or even all present.
     *
     * The raw `text` column carries an appended English gloss after `<br/>` (see
     * [BibleRepository.cleanVerseText]'s doc), so a plain SQL `LIKE` would also surface verses
     * where a term only appears in that English tail, not the Swahili verse itself. `LIKE` is
     * used as a fast prefilter, then each candidate is re-checked against only its Swahili
     * portion before being accepted -- precision over relying on the database alone.
     */
    fun search(
        query: String,
        mode: SearchMode = SearchMode.PHRASE,
        scope: SearchScope = SearchScope.WholeBible,
        limit: Int = 100
    ): List<SearchResult> {
        val term = query.trim()
        if (term.length < 2) return emptyList()

        val words = term.split(Regex("\\s+")).filter { it.isNotBlank() }
        val db = BibleDatabaseHelper.getDatabase(context)

        val whereClauses = mutableListOf("t.head = 0")
        val args = mutableListOf<String>()

        when (mode) {
            SearchMode.PHRASE -> {
                whereClauses += "t.text LIKE ? ESCAPE '\\'"
                args += likePattern(term)
            }
            SearchMode.ANY_WORD -> {
                whereClauses += words.joinToString(" OR ", prefix = "(", postfix = ")") { "t.text LIKE ? ESCAPE '\\'" }
                words.forEach { args += likePattern(it) }
            }
        }

        when (scope) {
            is SearchScope.WholeBible -> {}
            is SearchScope.OldTestament -> whereClauses += "c.mode = 1"
            is SearchScope.NewTestament -> whereClauses += "c.mode = 2"
            is SearchScope.Book -> {
                whereClauses += "t.chapter_id = ?"
                args += scope.bookId.toString()
            }
        }

        // Over-fetch: some LIKE hits get filtered out below as English-only matches.
        args += (limit * 3).toString()

        db.rawQuery(
            """
            SELECT c.title, t.chapter_id, t.chapter_num, t.position, t.text
            FROM texts t
            JOIN chapters c ON c._id = t.chapter_id
            WHERE ${whereClauses.joinToString(" AND ")}
            ORDER BY t.chapter_id, t.chapter_num, t.position
            LIMIT ?
            """.trimIndent(),
            args.toTypedArray()
        ).use { cursor ->
            val results = mutableListOf<SearchResult>()
            while (cursor.moveToNext() && results.size < limit) {
                val raw = cursor.getString(4) ?: continue
                val swahiliOnly = raw.substringBefore("<br/>")
                val matches = when (mode) {
                    SearchMode.PHRASE -> swahiliOnly.contains(term, ignoreCase = true)
                    SearchMode.ANY_WORD -> words.any { swahiliOnly.contains(it, ignoreCase = true) }
                }
                if (!matches) continue
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

    /** A single verse's text, or null if that address doesn't exist (e.g. stale saved data
     *  from a Bible database that's since changed). Used by the Saved tab to render bookmarks/
     *  highlights/notes, which only store the address, not a text snapshot. */
    fun verseAt(bookId: Int, chapterNum: Int, position: Int): String? {
        val db = BibleDatabaseHelper.getDatabase(context)
        db.rawQuery(
            "SELECT text FROM texts WHERE chapter_id = ? AND chapter_num = ? AND position = ? AND head = 0 LIMIT 1",
            arrayOf(bookId.toString(), chapterNum.toString(), position.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) BibleRepository.cleanVerseText(cursor.getString(0) ?: return null) else null
        }
    }

    /**
     * If [query] reads as a Bible reference -- "Yohana 3:16", "1 Wafalme 2", "Zaburi 23:1-6",
     * or even an English/abbreviated form like "Jn 3:16" or "1 Cor 13" -- resolves it directly
     * against [BibleBooks]'s existing alias table (the same one citation parsing already uses)
     * rather than relying on it also happening to match as free text. Requires a trailing
     * chapter number (a bare book name alone, with nothing else, is too ambiguous to jump on --
     * it stays a normal typed-so-far query). A verse range keeps only the start verse, since a
     * jump target is one verse, not a span. Returns null for anything that doesn't parse or
     * doesn't resolve to a real book/chapter/verse.
     */
    fun resolveReference(query: String): SearchResult? {
        val trimmed = query.trim()
        val match = REFERENCE_PATTERN.matchEntire(trimmed) ?: return null
        val bookPart = match.groupValues[1].trim()
        val chapterNum = match.groupValues[2].toIntOrNull() ?: return null
        val verseNum = match.groupValues[3].toIntOrNull() ?: 1
        if (bookPart.isBlank()) return null

        val bookId = BibleBooks.resolveId(bookPart) ?: return null
        val book = allBooks().firstOrNull { it.id == bookId } ?: return null
        if (chapterNum < 1 || chapterNum > book.chapterCount) return null

        val text = verseAt(book.id, chapterNum, verseNum) ?: return null
        return SearchResult(bookId = book.id, bookName = book.name, chapterNum = chapterNum, verseNum = verseNum, text = text)
    }

    private fun likePattern(term: String): String {
        val escaped = term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
        return "%$escaped%"
    }

    companion object {
        /** Group 1: book name (letters/spaces, optionally starting with a book-numbering digit
         *  like "1 Yohana" or "1 Cor"). Group 2: chapter. Group 3: optional verse (a trailing
         *  "-N" range is matched but discarded -- only the start verse is kept). */
        private val REFERENCE_PATTERN = Regex("^(\\d?\\s?[\\p{L} .]+?)\\s+(\\d{1,3})(?::(\\d{1,3})(?:[-\u2013]\\d{1,3})?)?$")
    }
}
