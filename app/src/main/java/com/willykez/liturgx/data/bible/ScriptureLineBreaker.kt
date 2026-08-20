package com.willykez.liturgx.data.bible

/**
 * Breaks a prose passage into one line per sentence — an approximation of the "sense line"
 * typesetting printed lectionaries use (each clause read as its own line, so a reader's eye and
 * breath land on natural pauses). It's a deliberate approximation, not a reproduction: real
 * lectionary sense-lines are an editorial choice baked into how a specific translation was
 * typeset, and this Bible database carries no such markup for prose books — only Psalms and
 * poetic oracles have real internal line breaks (see [BibleRepository.cleanVerseText]). This
 * only breaks at sentence-ending punctuation (`. ! ? ;`), deliberately never at commas, so it
 * can't fragment a clause mid-thought the way a naive "split on every comma" approach would.
 */
object ScriptureLineBreaker {
    private val SENTENCE_BOUNDARY = Regex("""(?<=[.!?;])\s+""")

    fun toSenseLines(text: String): String {
        val sentences = text.split(SENTENCE_BOUNDARY).map { it.trim() }.filter { it.isNotEmpty() }
        return if (sentences.size <= 1) text else sentences.joinToString("\n")
    }
}
