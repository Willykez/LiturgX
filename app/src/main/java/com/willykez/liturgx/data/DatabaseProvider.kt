package com.willykez.liturgx.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * The lectionary ships as a pre-populated SQLite file in assets/. Android can't query a
 * database straight out of the assets folder, so on first launch we copy it byte-for-byte
 * into app-internal storage and open it read-only from there.
 */
object DatabaseProvider {

    private const val DB_NAME = "lectionary_swahili.db"
    @Volatile private var db: SQLiteDatabase? = null

    fun get(context: Context): SQLiteDatabase {
        db?.let { return it }
        synchronized(this) {
            db?.let { return it }
            val dest = File(context.getDatabasePath(DB_NAME).path)
            if (!dest.exists()) {
                dest.parentFile?.mkdirs()
                context.assets.open(DB_NAME).use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
            }
            val opened = SQLiteDatabase.openDatabase(dest.path, null, SQLiteDatabase.OPEN_READONLY)
            db = opened
            return opened
        }
    }
}
