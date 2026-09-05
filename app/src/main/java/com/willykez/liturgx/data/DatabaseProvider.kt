package com.willykez.liturgx.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * The lectionary ships as a pre-populated SQLite file in assets/. Android can't query a
 * database straight out of the assets folder, so on first launch we copy it byte-for-byte
 * into app-internal storage and open it read-only from there.
 *
 * [DB_VERSION] guards against a real bug this class used to have: it only ever copied the
 * asset in when the internal-storage file was completely missing, so anyone who already had
 * the app installed would go on using the OLD bundled data forever, even after updating to a
 * build that ships a corrected/expanded `lectionary_swahili.db` -- the new asset would just
 * sit there unused. Bump [DB_VERSION] any time the bundled database content changes; a
 * mismatch against the version recorded in SharedPreferences forces a fresh copy.
 */
object DatabaseProvider {

    private const val DB_NAME = "lectionary_swahili.db"
    private const val PREFS_NAME = "liturgx_db_meta"
    private const val KEY_VERSION = "lectionary_db_version"

    /** Bump this whenever assets/lectionary_swahili.db is replaced with new content. */
    const val DB_VERSION = 2

    @Volatile private var db: SQLiteDatabase? = null

    fun get(context: Context): SQLiteDatabase {
        db?.let { return it }
        synchronized(this) {
            db?.let { return it }
            val app = context.applicationContext
            val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val dest = File(app.getDatabasePath(DB_NAME).path)
            val installedVersion = prefs.getInt(KEY_VERSION, -1)

            if (!dest.exists() || installedVersion != DB_VERSION) {
                dest.parentFile?.mkdirs()
                if (dest.exists()) dest.delete()
                app.assets.open(DB_NAME).use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
                prefs.edit().putInt(KEY_VERSION, DB_VERSION).apply()
            }

            val opened = SQLiteDatabase.openDatabase(dest.path, null, SQLiteDatabase.OPEN_READONLY)
            db = opened
            return opened
        }
    }
}
