package com.willykez.liturgx.data

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.willykez.liturgx.core.Reading
import com.willykez.liturgx.core.Saint

class LectionaryDao(private val db: SQLiteDatabase) {

    private fun Cursor.toReading(): Reading = Reading(
        id = getInt(getColumnIndexOrThrow("id")),
        season = getString(getColumnIndexOrThrow("season")),
        periodKey = getString(getColumnIndexOrThrow("period_key")),
        entryKey = getString(getColumnIndexOrThrow("entry_key")),
        day = getString(getColumnIndexOrThrow("day")),
        somoLaKwanza = getString(getColumnIndexOrThrow("somo_la_kwanza")),
        wimboLaKatikati = getString(getColumnIndexOrThrow("wimbo_la_katikati")),
        somoLaPili = getString(getColumnIndexOrThrow("somo_la_pili")),
        shangilio = getString(getColumnIndexOrThrow("shangilio")),
        injili = getString(getColumnIndexOrThrow("injili")),
        mwakaLiturujia = getString(getColumnIndexOrThrow("mwaka_liturujia"))
    )

    /** Fetch one or more entry_key rows for a period, optionally filtered by the `day` column. */
    fun readings(season: String, periodKey: String, entryKeys: List<String>, day: String? = null): List<Reading> {
        val placeholders = entryKeys.joinToString(",") { "?" }
        val args = mutableListOf(season, periodKey).apply { addAll(entryKeys) }
        var sql = "SELECT * FROM readings WHERE season=? AND period_key=? AND entry_key IN ($placeholders)"
        if (day != null) {
            sql += " AND day=?"
            args.add(day)
        }
        val results = mutableListOf<Reading>()
        db.rawQuery(sql, args.toTypedArray()).use { c ->
            while (c.moveToNext()) results.add(c.toReading())
        }
        // Preserve the caller's requested entryKey order (matters for e.g. Easter Vigil OT readings).
        return results.sortedBy { entryKeys.indexOf(it.entryKey) }
    }

    /** Fixed-date solemnity/feast lookup, keyed by the Swahili "Mwezi N" day string. */
    fun sikukuuMaalumFor(dayString: String): List<Reading> {
        val results = mutableListOf<Reading>()
        db.rawQuery(
            "SELECT * FROM readings WHERE season='sikukuu_maalum' AND day=?",
            arrayOf(dayString)
        ).use { c -> while (c.moveToNext()) results.add(c.toReading()) }
        return results
    }

    fun saintFor(tarehe: String): List<Saint> {
        val results = mutableListOf<Saint>()
        db.rawQuery("SELECT * FROM watakatifu WHERE tarehe=?", arrayOf(tarehe)).use { c ->
            while (c.moveToNext()) {
                results.add(
                    Saint(
                        id = c.getInt(c.getColumnIndexOrThrow("id")),
                        tarehe = c.getString(c.getColumnIndexOrThrow("tarehe")),
                        jina = c.getString(c.getColumnIndexOrThrow("jina")),
                        daraja = c.getString(c.getColumnIndexOrThrow("daraja")),
                        rangi = c.getString(c.getColumnIndexOrThrow("rangi")),
                        wasifu = c.getString(c.getColumnIndexOrThrow("wasifu"))
                    )
                )
            }
        }
        return results
    }

    fun allSaints(): List<Saint> {
        val results = mutableListOf<Saint>()
        db.rawQuery("SELECT * FROM watakatifu ORDER BY id", null).use { c ->
            while (c.moveToNext()) {
                results.add(
                    Saint(
                        id = c.getInt(c.getColumnIndexOrThrow("id")),
                        tarehe = c.getString(c.getColumnIndexOrThrow("tarehe")),
                        jina = c.getString(c.getColumnIndexOrThrow("jina")),
                        daraja = c.getString(c.getColumnIndexOrThrow("daraja")),
                        rangi = c.getString(c.getColumnIndexOrThrow("rangi")),
                        wasifu = c.getString(c.getColumnIndexOrThrow("wasifu"))
                    )
                )
            }
        }
        return results
    }
}
