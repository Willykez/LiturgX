package com.willykez.liturgx.data.bible

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream

/**
 * The bundled bible_swahili.sqlite (assets/database/bible_swahili.sqlite) can't be queried
 * directly from the APK's asset stream — SQLite needs a real file path. This copies it into
 * the app's private storage once, then hands back a read-only [SQLiteDatabase] handle.
 *
 * Schema (as shipped):
 *   chapters(_id, title, num, mode, short_title, ntitle)  — one row per BOOK; _id is the book id,
 *                                                            title is the Swahili book name, num is
 *                                                            the chapter count in that book.
 *   texts(chapter_id, chapter_num, position, text, head, ...) — one row per verse (or per section
 *                                                            heading, when head=1). chapter_id
 *                                                            references chapters._id (the book),
 *                                                            chapter_num is the chapter number
 *                                                            within the book, position is the verse
 *                                                            number within the chapter.
 *   text = "<Swahili verse text> <br/><i>English reference text</i>" — see [BiblePassage.cleanVerseText].
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
            val dbFile = File(context.getDatabasePath(DB_NAME).path)
            if (!dbFile.exists()) {
                dbFile.parentFile?.mkdirs()
                context.assets.open(ASSET_PATH).use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            val db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
            database = db
            return db
        }
    }
}
