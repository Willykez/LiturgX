package com.willykez.liturgx.data.bible

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * The bundled Swahili Bible (assets/database/bible_swahili.sqlite) can't be queried straight out
 * of the assets folder, so — same pattern as [com.willykez.liturgx.data.DatabaseProvider] for the
 * lectionary — it's copied byte-for-byte into app-internal storage on first use, then opened
 * read-only from there.
 *
 * Schema (as shipped):
 *   chapters(_id, title, num, mode, short_title, ntitle) — one row per BOOK; _id is the book id
 *                                                           ([BibleBooks] maps citation book names
 *                                                           to it), title is the Swahili book name.
 *   texts(chapter_id, chapter_num, position, text, head, ...) — one row per verse (or per section
 *                                                           heading, when head=1). chapter_id
 *                                                           references chapters._id, chapter_num is
 *                                                           the chapter number, position is the
 *                                                           verse number within that chapter.
 *   text = "<Swahili verse text> <br/><i>English reference text</i>" — see
 *          [BibleRepository.cleanVerseText], which strips everything but the Swahili portion.
 */
object BibleDatabaseHelper {
    private const val ASSET_PATH = "database/bible_swahili.sqlite"
    private const val DB_NAME = "bible_swahili.sqlite"
    private const val PREFS_NAME = "liturgx_db_meta"
    private const val KEY_VERSION = "bible_db_version"

    /** Same versioning fix as [com.willykez.liturgx.data.DatabaseProvider] -- bump this if the
     *  bundled Bible database is ever replaced, so existing installs actually pick it up. */
    const val DB_VERSION = 1

    @Volatile
    private var database: SQLiteDatabase? = null

    fun getDatabase(context: Context): SQLiteDatabase {
        database?.let { return it }
        synchronized(this) {
            database?.let { return it }
            val app = context.applicationContext
            val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val dest = File(app.getDatabasePath(DB_NAME).path)
            val installedVersion = prefs.getInt(KEY_VERSION, -1)

            if (!dest.exists() || installedVersion != DB_VERSION) {
                dest.parentFile?.mkdirs()
                if (dest.exists()) dest.delete()
                app.assets.open(ASSET_PATH).use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
                prefs.edit().putInt(KEY_VERSION, DB_VERSION).apply()
            }

            val opened = SQLiteDatabase.openDatabase(dest.path, null, SQLiteDatabase.OPEN_READONLY)
            database = opened
            return opened
        }
    }
}
