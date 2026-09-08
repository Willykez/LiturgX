package com.willykez.liturgx.data.bible

import android.content.Context

/** Scripture body font choice, mirrored from BibliaApp's "Aina ya maandishi" setting. */
enum class ScriptureFontStyle { SERIF, SANS, MONO }

/**
 * Bible-reading preferences (font style, verse numbers, paragraph mode) - the USOMAJI section
 * that BibliaApp has and LiturgX's own Bible reader previously didn't. Plain SharedPreferences,
 * same pattern as [com.willykez.liturgx.data.SettingsStore]: these are read fresh inside
 * [com.willykez.liturgx.ui.bible.ChapterReaderScreen] on every recomposition, so a change made
 * in the Settings sheet is visible as soon as the sheet closes.
 */
class ReadingPrefsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("liturgx_reading_prefs", Context.MODE_PRIVATE)

    fun loadFontStyle(): ScriptureFontStyle = when (prefs.getString(KEY_FONT_STYLE, "serif")) {
        "sans" -> ScriptureFontStyle.SANS
        "mono" -> ScriptureFontStyle.MONO
        else -> ScriptureFontStyle.SERIF
    }

    fun saveFontStyle(style: ScriptureFontStyle) {
        val value = when (style) {
            ScriptureFontStyle.SERIF -> "serif"
            ScriptureFontStyle.SANS -> "sans"
            ScriptureFontStyle.MONO -> "mono"
        }
        prefs.edit().putString(KEY_FONT_STYLE, value).apply()
    }

    fun loadVerseNumbersVisible(): Boolean = prefs.getBoolean(KEY_VERSE_NUMBERS, true)

    fun saveVerseNumbersVisible(visible: Boolean) {
        prefs.edit().putBoolean(KEY_VERSE_NUMBERS, visible).apply()
    }

    /** "Hali ya kusoma: Aya" - a denser, book-like flow: no number gutter, tighter line spacing. */
    fun loadParagraphMode(): Boolean = prefs.getBoolean(KEY_PARAGRAPH_MODE, false)

    fun saveParagraphMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PARAGRAPH_MODE, enabled).apply()
    }

    /** The last chapter actually open in the reader, so leaving the app mid-chapter (without
     *  backing out to the book list first) resumes there next time the Bible tab opens --
     *  rather than always restarting at the book list. Cleared once the person backs all the
     *  way out to the book list themselves, since that's a deliberate "I'm done here" signal. */
    fun saveLastLocation(bookId: Int, chapterNum: Int) {
        prefs.edit().putInt(KEY_LAST_BOOK_ID, bookId).putInt(KEY_LAST_CHAPTER, chapterNum).apply()
    }

    fun loadLastLocation(): Pair<Int, Int>? {
        val bookId = prefs.getInt(KEY_LAST_BOOK_ID, -1)
        val chapterNum = prefs.getInt(KEY_LAST_CHAPTER, -1)
        return if (bookId >= 0 && chapterNum >= 1) bookId to chapterNum else null
    }

    fun clearLastLocation() {
        prefs.edit().remove(KEY_LAST_BOOK_ID).remove(KEY_LAST_CHAPTER).apply()
    }

    companion object {
        private const val KEY_FONT_STYLE = "font_style"
        private const val KEY_VERSE_NUMBERS = "verse_numbers_visible"
        private const val KEY_PARAGRAPH_MODE = "paragraph_mode"
        private const val KEY_LAST_BOOK_ID = "last_book_id"
        private const val KEY_LAST_CHAPTER = "last_chapter"
    }
}
