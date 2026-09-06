package com.willykez.liturgx.data.bible

import android.content.Context

/** A single verse's address, stable across app runs: "bookId:chapter:verse". */
fun verseKey(bookId: Int, chapterNum: Int, verse: Int): String = "$bookId:$chapterNum:$verse"

/**
 * Bookmarks, highlights and per-verse notes for the Bible reader - BibliaApp's "DATA YAKO"
 * (Alama / Iliyoangaziwa / Dokezo) feature set, previously missing from LiturgX entirely.
 *
 * Bookmarks and highlights are each a plain SharedPreferences string set of verse keys.
 * Notes need actual text per verse, which SharedPreferences string sets can't hold, so each
 * note is its own "note_<key>" string entry, and a separate string set tracks which keys have
 * one (so counting and clearing don't require scanning every preference).
 */
class BibleUserDataStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("liturgx_bible_user_data", Context.MODE_PRIVATE)

    // --- Bookmarks -----------------------------------------------------------------
    fun isBookmarked(key: String): Boolean = key in bookmarkKeys()

    fun setBookmarked(key: String, bookmarked: Boolean) {
        val current = bookmarkKeys()
        val updated = if (bookmarked) current + key else current - key
        prefs.edit().putStringSet(KEY_BOOKMARKS, updated).apply()
    }

    fun bookmarkCount(): Int = bookmarkKeys().size

    fun clearBookmarks() {
        prefs.edit().putStringSet(KEY_BOOKMARKS, emptySet()).apply()
    }

    private fun bookmarkKeys(): Set<String> = prefs.getStringSet(KEY_BOOKMARKS, emptySet()) ?: emptySet()

    // --- Highlights ------------------------------------------------------------------
    fun isHighlighted(key: String): Boolean = key in highlightKeys()

    fun setHighlighted(key: String, highlighted: Boolean) {
        val current = highlightKeys()
        val updated = if (highlighted) current + key else current - key
        prefs.edit().putStringSet(KEY_HIGHLIGHTS, updated).apply()
    }

    fun highlightCount(): Int = highlightKeys().size

    fun clearHighlights() {
        prefs.edit().putStringSet(KEY_HIGHLIGHTS, emptySet()).apply()
    }

    private fun highlightKeys(): Set<String> = prefs.getStringSet(KEY_HIGHLIGHTS, emptySet()) ?: emptySet()

    // --- Notes -------------------------------------------------------------------------
    fun getNote(key: String): String? = prefs.getString(noteContentKey(key), null)

    fun setNote(key: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            removeNote(key)
            return
        }
        val keys = noteKeys()
        prefs.edit()
            .putString(noteContentKey(key), trimmed)
            .putStringSet(KEY_NOTE_KEYS, keys + key)
            .apply()
    }

    fun removeNote(key: String) {
        val keys = noteKeys()
        prefs.edit()
            .remove(noteContentKey(key))
            .putStringSet(KEY_NOTE_KEYS, keys - key)
            .apply()
    }

    fun noteCount(): Int = noteKeys().size

    fun clearNotes() {
        val editor = prefs.edit()
        noteKeys().forEach { editor.remove(noteContentKey(it)) }
        editor.putStringSet(KEY_NOTE_KEYS, emptySet())
        editor.apply()
    }

    private fun noteKeys(): Set<String> = prefs.getStringSet(KEY_NOTE_KEYS, emptySet()) ?: emptySet()

    private fun noteContentKey(key: String) = "note_$key"

    companion object {
        private const val KEY_BOOKMARKS = "bookmark_keys"
        private const val KEY_HIGHLIGHTS = "highlight_keys"
        private const val KEY_NOTE_KEYS = "note_keys"
    }
}
