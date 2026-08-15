package com.willykez.liturgx.data.lectionary

/**
 * Parses Scripture citations in the form the Lectionary prints them, e.g.:
 *   "Matthew 20:1-16"
 *   "Matthew 20:6-8,9,11-13"          (verse 10 skipped)
 *   "Mark 1:14-20;2:1-3"              (cross-chapter span)
 *   "Ephesians 1:3-6,15-18 [3-6]"    (optional shorter form in brackets)
 *   "Zaburi 29:1-2, 3-4, 3b+9b-10"   (Swahili Psalm citation: "+" joins
 *                                     non-contiguous partial verses, and a
 *                                     trailing letter like "3b" marks a
 *                                     partial verse — both collapse to their
 *                                     whole-verse number for DB lookup)
 *
 * This is pure text parsing — it doesn't resolve verses to actual Bible text.
 * Pair it with a verse-lookup source (a bundled bible database, a Bible API,
 * etc.) to render the full passage; on its own it's useful for validating
 * citations, building a "read more / short form" toggle, and driving a
 * verse-by-verse renderer that shows a break wherever verses are skipped.
 */
object CitationParser {

    /** A single verse or contiguous range within one chapter, e.g. 6-8 or 9. */
    data class VerseRange(val chapter: Int, val startVerse: Int, val endVerse: Int) {
        fun verses(): IntRange = startVerse..endVerse
    }

    data class ParsedCitation(
        val book: String,
        val ranges: List<VerseRange>,
        /** The optional bracketed short form, e.g. "3-6" in "Eph 1:3-6,15-18 [3-6]" — null if absent. */
        val shortForm: List<VerseRange>? = null
    ) {
        /** True if there's at least one gap between consecutive ranges within the same chapter. */
        fun hasSkips(): Boolean = ranges.zipWithNext().any { (a, b) ->
            a.chapter == b.chapter && b.startVerse > a.endVerse + 1
        }

        /** Human-readable form, reconstructed from the parsed ranges (useful for round-trip validation). */
        fun canonicalCitation(): String {
            val byChapter = LinkedHashMap<Int, MutableList<VerseRange>>()
            for (r in ranges) byChapter.getOrPut(r.chapter) { mutableListOf() }.add(r)
            val chapterParts = byChapter.entries.joinToString(";") { (chapter, rs) ->
                val verseParts = rs.joinToString(",") { if (it.startVerse == it.endVerse) "${it.startVerse}" else "${it.startVerse}-${it.endVerse}" }
                "$chapter:$verseParts"
            }
            return "$book $chapterParts"
        }
    }

    private val VERSE_TOKEN = """\d+[a-zA-Z]*"""
    /** The end of a dash range may itself carry a "chapter:" prefix — a cross-chapter span. */
    private val RANGE_END = """(?:\d+\s*:\s*)?$VERSE_TOKEN"""
    private val VERSE_ITEM = """$VERSE_TOKEN(?:-$RANGE_END)?"""
    private val VERSE_SPEC = """$VERSE_ITEM(?:\s*[,+]\s*$VERSE_ITEM)*"""
    private val CHAPTER_SPEC = """(?:\d+\s*:\s*)?$VERSE_SPEC"""
    private val BODY_PATTERN = """$CHAPTER_SPEC(?:\s*;\s*$CHAPTER_SPEC)*"""
    private val CITATION_REGEX = Regex("""^(.+?)\s+($BODY_PATTERN)(?:\s*\[($VERSE_SPEC)])?$""")
    private val BARE_BODY_REGEX = Regex("""^\s*$BODY_PATTERN\s*$""")

    /**
     * A generous upper bound used as the end-verse when a dash range crosses into a new
     * chapter (e.g. "25-14:1" means "verse 25 through the end of this chapter, then verses
     * 1 through 1 of chapter 14"). No chapter in Scripture has this many verses (the longest,
     * Psalm 119, has 176), and the Bible lookup already skips any verse number that doesn't
     * exist — see [com.willykez.liturgx.data.bible.BibleRepository] — so padding to this sentinel is a
     * safe way to say "to the end of the chapter" without needing to know its real length here.
     */
    private const val END_OF_CHAPTER_SENTINEL = 200

    /**
     * Parses a citation string. Returns null if it doesn't match the expected grammar
     * (e.g. it's a free-text label rather than a real citation).
     */
    fun parse(citation: String): ParsedCitation? {
        val trimmed = citation.trim()
        val match = CITATION_REGEX.matchEntire(trimmed) ?: return null
        val book = match.groupValues[1].trim()
        val body = match.groupValues[2].trim()
        val bracketed = match.groupValues[3].takeIf { it.isNotBlank() }

        val ranges = parseChapterVerseBody(body) ?: return null
        val shortRanges = bracketed?.let { parseChapterVerseBody(it, defaultChapter = ranges.firstOrNull()?.chapter) }

        return ParsedCitation(book = book, ranges = ranges, shortForm = shortRanges)
    }

    /**
     * Parses a citation that may or may not include a book name, e.g. "Mt 12:1-3",
     * "12:1-3", or "12:2-3,5,8-10". When the book is omitted, [fallbackBook] is used —
     * handy for a second citation in the same reading that only shows "12:1-3" because
     * the book was already established by the first one.
     */
    fun parseWithFallback(citation: String, fallbackBook: String?): ParsedCitation? {
        val trimmed = citation.trim()
        if (BARE_BODY_REGEX.matches(trimmed) && fallbackBook != null) {
            val ranges = parseChapterVerseBody(trimmed) ?: return null
            return ParsedCitation(book = fallbackBook, ranges = ranges)
        }
        return parse(trimmed)
    }

    /**
     * Body grammar: Chapter:VerseSpec[;Chapter:VerseSpec...]
     * VerseSpec: comma-separated list of "n" or "n-m"
     * A verse spec segment without an explicit "chapter:" prefix inherits the
     * most recently seen chapter (used by the bracketed short-form, and by
     * same-chapter continuations after a ';').
     */
    private fun parseChapterVerseBody(body: String, defaultChapter: Int? = null): List<VerseRange>? {
        val segments = body.split(";").map { it.trim() }.filter { it.isNotEmpty() }
        if (segments.isEmpty()) return null

        val result = mutableListOf<VerseRange>()
        var currentChapter = defaultChapter

        for (segment in segments) {
            val colonIdx = segment.indexOf(':')
            val chapter: Int
            val verseSpec: String
            if (colonIdx >= 0) {
                chapter = segment.substring(0, colonIdx).trim().toIntOrNull() ?: return null
                verseSpec = segment.substring(colonIdx + 1).trim()
            } else {
                // No "chapter:" prefix. Either a continuation of the previous segment's
                // chapter, or — for a one-chapter book cited by verse alone, e.g. "Yuda 17,
                // 20b-25" or "Filemoni 9-10" — an implicit chapter 1.
                chapter = currentChapter ?: 1
                verseSpec = segment
            }
            currentChapter = chapter

            for (part in verseSpec.split(Regex("[,+]")).map { it.trim() }.filter { it.isNotEmpty() }) {
                if ("-" in part) {
                    val (startStr, endStr) = part.split("-", limit = 2)
                    val start = leadingVerseNumber(startStr) ?: return null
                    val endColonIdx = endStr.indexOf(':')
                    if (endColonIdx >= 0) {
                        // Cross-chapter range, e.g. "25-14:1": rest of the current chapter,
                        // then the start of the next chapter through the given end verse.
                        val endChapter = endStr.substring(0, endColonIdx).trim().toIntOrNull() ?: return null
                        val endVerse = leadingVerseNumber(endStr.substring(endColonIdx + 1)) ?: return null
                        result.add(VerseRange(chapter, start, END_OF_CHAPTER_SENTINEL))
                        result.add(VerseRange(endChapter, 1, endVerse))
                        currentChapter = endChapter
                    } else {
                        val end = leadingVerseNumber(endStr) ?: return null
                        result.add(VerseRange(chapter, start, end))
                    }
                } else {
                    val v = leadingVerseNumber(part) ?: return null
                    result.add(VerseRange(chapter, v, v))
                }
            }
        }
        return result.takeIf { it.isNotEmpty() }
    }

    /**
     * Extracts the leading whole-verse number from a token that may carry a lettered
     * sub-verse suffix (e.g. "4bcd" -> 4, "8a" -> 8, "3b" -> 3). The bundled Bible database
     * only stores whole verses, so a lettered suffix — which in the printed Lectionary marks
     * a partial-verse selection — collapses to its parent verse number for lookup purposes.
     */
    private fun leadingVerseNumber(token: String): Int? =
        Regex("""^(\d+)""").find(token.trim())?.groupValues?.get(1)?.toIntOrNull()

    /**
     * Expands a parsed citation's ranges into a flat, ordered list of
     * (chapter, verse) pairs, useful for driving a verse-lookup query.
     */
    fun expand(parsed: ParsedCitation): List<Pair<Int, Int>> =
        parsed.ranges.flatMap { r -> r.verses().map { v -> r.chapter to v } }
}
