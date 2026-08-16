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

    @Volatile
    private var database: SQLiteDatabase? = null

    fun getDatabase(context: Context): SQLiteDatabase {
        database?.let { return it }
        synchronized(this) {
            database?.let { return it }
            val dest = File(context.getDatabasePath(DB_NAME).path)
            if (!dest.exists()) {
                dest.parentFile?.mkdirs()
                context.assets.open(ASSET_PATH).use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
            }
            val opened = SQLiteDatabase.openDatabase(dest.path, null, SQLiteDatabase.OPEN_READONLY)
            database = opened
            return opened
        }
    }
}
