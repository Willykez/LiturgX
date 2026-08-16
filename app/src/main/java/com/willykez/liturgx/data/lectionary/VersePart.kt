package com.willykez.liturgx.data.lectionary

/**
 * Which portion of a verse a Lectionary citation selects.
 *
 * IMPORTANT LIMITATION — read before using this for anything beyond display: the bundled Bible
 * database (assets/database/bible_swahili.sqlite, table `texts`) stores one row per WHOLE verse
 * only. There is no `partA`/`partB` column and no sub-verse boundary metadata anywhere in the
 * schema (verified directly against the shipped .sqlite — see BibleDatabaseHelper's doc comment
 * for the full schema). That means:
 *
 *   - This engine CAN tell you a citation asked for "verse 6, part a" (that's what this enum is
 *     for — the letter is parsed and preserved, not discarded).
 *   - This engine CANNOT give you the *exact* "a" half of verse 6's text from data alone — there
 *     is no data source that records precisely where "a" ends and "b" begins.
 *
 * [com.willykez.liturgx.data.bible.BibleRepository] resolves [part] anyway, with a best-effort
 * heuristic split of the whole verse's text (by its internal poetic line break when there is
 * exactly one, otherwise by sentence or clause boundary nearest the midpoint) — good enough for
 * display, but not a source of truth for exact verse-subdivision boundaries. If official
 * verse-subdivision metadata is ever bundled (e.g. a `verse_parts` table with start/end
 * character or word offsets), that heuristic can be swapped for an exact lookup without any
 * change to the parser or to this enum.
 */
enum class VersePart {
    /** No letter suffix — the whole verse, e.g. "12:1". */
    ALL,
    /** Trailing "a", e.g. "12:6a". */
    A,
    /** Trailing "b", e.g. "12:10b". */
    B,
    /** Trailing "ab" or any other multi-letter combination, e.g. "12:10ab", "67:8abcd". */
    AB;

    companion object {
        /** Classifies a lettered suffix (already lowercased) into one of the four values above. */
        fun fromSuffix(suffix: String): VersePart = when (suffix) {
            "" -> ALL
            "a" -> A
            "b" -> B
            else -> AB // "ab", or any other combination — see class doc for why this collapses to AB
        }
    }
}/**
 * One fully-resolved position in a liturgical citation: a specific book, chapter, verse, and
 * which part of that verse was requested. This is the atomic unit [CitationParser] expands a
 * citation into — see [CitationParser.expandToReferences].
 */
data class LiturgicalVerseReference(
    val book: String,
    val chapter: Int,
    val verse: Int,
    val part: VersePart = VersePart.ALL
)

/** The lettered suffix this part reconstructs as in a citation string, e.g. A -> "a". */
fun VersePart.suffix(): String = when (this) {
    VersePart.ALL -> ""
    VersePart.A -> "a"
    VersePart.B -> "b"
    VersePart.AB -> "ab"
}
