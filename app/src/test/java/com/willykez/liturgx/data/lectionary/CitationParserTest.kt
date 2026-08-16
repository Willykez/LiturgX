package com.willykez.liturgx.data.lectionary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Validates [CitationParser] against the four reference citations used throughout the
 * Liturgical Bible Reading Engine spec, plus the individual grammar features (comma,
 * semicolon, range, partial verse, Psalm "+") each one exercises.
 */
class CitationParserTest {

    private fun refs(citation: String): List<LiturgicalVerseReference> {
        val parsed = CitationParser.parse(citation)
        assertNotNull("Expected \"$citation\" to parse", parsed)
        return CitationParser.expandToReferences(parsed!!.book, parsed)
    }

    @Test
    fun `1 Mambo ya Nyakati -- comma ranges then semicolon chapter change`() {
        val result = refs("1 Mambo ya Nyakati 15:3-4, 15-16; 16:1-2")

        val expected = listOf(
            LiturgicalVerseReference("1 Mambo ya Nyakati", 15, 3),
            LiturgicalVerseReference("1 Mambo ya Nyakati", 15, 4),
            LiturgicalVerseReference("1 Mambo ya Nyakati", 15, 15),
            LiturgicalVerseReference("1 Mambo ya Nyakati", 15, 16),
            LiturgicalVerseReference("1 Mambo ya Nyakati", 16, 1),
            LiturgicalVerseReference("1 Mambo ya Nyakati", 16, 2)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Ufunuo -- partial verses a, ranges, and ab combination`() {
        val result = refs("Ufunuo 11:19a; 12:1-6a, 10ab")

        val expected = listOf(
            LiturgicalVerseReference("Ufunuo", 11, 19, VersePart.A),
            LiturgicalVerseReference("Ufunuo", 12, 1, VersePart.ALL),
            LiturgicalVerseReference("Ufunuo", 12, 2, VersePart.ALL),
            LiturgicalVerseReference("Ufunuo", 12, 3, VersePart.ALL),
            LiturgicalVerseReference("Ufunuo", 12, 4, VersePart.ALL),
            LiturgicalVerseReference("Ufunuo", 12, 5, VersePart.ALL),
            LiturgicalVerseReference("Ufunuo", 12, 6, VersePart.A),
            LiturgicalVerseReference("Ufunuo", 12, 10, VersePart.AB)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Zaburi 67 -- plus sign joins non-contiguous verses, not arithmetic`() {
        val result = refs("Zaburi 67:2-3, 5, 6+8")

        val expected = listOf(
            LiturgicalVerseReference("Zaburi", 67, 2),
            LiturgicalVerseReference("Zaburi", 67, 3),
            LiturgicalVerseReference("Zaburi", 67, 5),
            LiturgicalVerseReference("Zaburi", 67, 6),
            LiturgicalVerseReference("Zaburi", 67, 8)
        )
        assertEquals(expected, result)
        // Explicitly guard against the "+" ever being treated as arithmetic (i.e. producing a
        // single verse 14, or a range 6-8 that pulls in verse 7 which was never requested).
        assertEquals(false, result.any { it.verse == 7 })
        assertEquals(false, result.any { it.verse == 14 })
    }

    @Test
    fun `Zaburi 132 -- three independent comma-separated ranges`() {
        val result = refs("Zaburi 132:6-7, 9-10, 13-14")

        val expected = listOf(
            LiturgicalVerseReference("Zaburi", 132, 6),
            LiturgicalVerseReference("Zaburi", 132, 7),
            LiturgicalVerseReference("Zaburi", 132, 9),
            LiturgicalVerseReference("Zaburi", 132, 10),
            LiturgicalVerseReference("Zaburi", 132, 13),
            LiturgicalVerseReference("Zaburi", 132, 14)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `comma does not leak into the next chapter`() {
        // Regression guard for the spec's explicit warning: "10ab" after a comma must stay in
        // the current chapter (12), never be misread as chapter 10.
        val result = refs("Ufunuo 12:1-6a, 10ab")
        assertEquals(true, result.all { it.chapter == 12 })
        assertEquals(10, result.last().verse)
        assertEquals(VersePart.AB, result.last().part)
    }

    @Test
    fun `unparseable free text returns null rather than a wrong guess`() {
        assertEquals(null, CitationParser.parse("Haleluya, Haleluya."))
    }

    @Test
    fun `single letter suffix a vs b vs ab are distinguished`() {
        assertEquals(VersePart.A, refs("Ufunuo 1:1a").single().part)
        assertEquals(VersePart.B, refs("Ufunuo 1:1b").single().part)
        assertEquals(VersePart.AB, refs("Ufunuo 1:1ab").single().part)
        assertEquals(VersePart.ALL, refs("Ufunuo 1:1").single().part)
    }
}
