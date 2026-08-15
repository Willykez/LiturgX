package com.willykez.liturgx.data.local

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * One-time seeder for the `lectionary_readings` table from a bundled JSON dataset at
 * `assets/lectionary/lectionary.json`. This is intentionally decoupled from app code: to add
 * real Lectionary content, drop a JSON file at that path (see the shape below) — no Kotlin
 * changes needed, and the app runs perfectly well with the table empty (the repository falls
 * back to the existing offline sample content on a miss).
 *
 * Expected JSON shape — an array of rows matching [LectionaryEntity]'s columns:
 * ```json
 * [
 *   {
 *     "season": "ORDINARY_TIME",
 *     "weekNumber": 19,
 *     "dayOfWeek": "SUNDAY",
 *     "cycle": "C",
 *     "firstReadingCitation": "Wisdom 18:6-9",
 *     "firstReadingText": "...",
 *     "psalmCitation": "Psalm 33:1,12,18-19,20-22",
 *     "psalmResponse": "Blessed the people the Lord has chosen to be his own.",
 *     "psalmText": "...",
 *     "secondReadingCitation": "Hebrews 11:1-2,8-19",
 *     "secondReadingText": "...",
 *     "gospelCitation": "Luke 12:32-48",
 *     "gospelText": "..."
 *   }
 * ]
 * ```
 */
object LectionaryImporter {
    private const val ASSET_PATH = "lectionary/lectionary.json"
    private const val PREFS_NAME = "lectionary_importer"
    private const val KEY_DATASET_VERSION = "dataset_version"

    /**
     * Bump this whenever assets/lectionary/lectionary.json is replaced with meaningfully
     * different content. seedIfNeeded then wipes and reimports instead of trusting a
     * non-empty table left over from an older build — otherwise an install that already has
     * *any* rows (even a handful from a partial earlier dataset) would silently keep that
     * old content forever, since a non-empty table normally short-circuits reseeding.
     *
     * v2: full Ordinary Time dataset (Sundays A/B/C + weekdays I/II) from the Swahili
     * lectionary, replacing whatever (if anything) shipped before.
     */
    private const val CURRENT_DATASET_VERSION = 2

    /** Imports the bundled dataset if needed and the asset file exists. Safe to call repeatedly. */
    suspend fun seedIfNeeded(context: Context, dao: LectionaryDao, readingDao: ReadingDao? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedVersion = prefs.getInt(KEY_DATASET_VERSION, 0)
        val needsReseed = storedVersion != CURRENT_DATASET_VERSION

        if (dao.count() > 0 && !needsReseed) return

        val json = try {
            context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            return // No dataset bundled yet — that's fine, the repository falls back to sample content.
        }

        val entries = try {
            parse(json)
        } catch (e: Exception) {
            return // Malformed dataset shouldn't crash the app.
        }

        if (entries.isNotEmpty()) {
            if (needsReseed) {
                dao.clearAll()
                // Readings cached from the old (missing/partial) dataset would otherwise be
                // returned forever from the cache check that runs before the lectionary
                // lookup — clear them too so dates get re-resolved against the new data.
                readingDao?.clearAll()
            }
            dao.insertAll(entries)
            prefs.edit().putInt(KEY_DATASET_VERSION, CURRENT_DATASET_VERSION).apply()
        }
    }

    private fun parse(json: String): List<LectionaryEntity> {
        val array = JSONArray(json)
        val result = mutableListOf<LectionaryEntity>()
        for (i in 0 until array.length()) {
            val obj: JSONObject = array.getJSONObject(i)
            result.add(
                LectionaryEntity(
                    season = obj.getString("season"),
                    weekNumber = obj.optInt("weekNumber", 0),
                    dayOfWeek = obj.getString("dayOfWeek"),
                    cycle = obj.optString("cycle", "ALL"),
                    firstReadingCitation = obj.getString("firstReadingCitation"),
                    firstReadingText = obj.getString("firstReadingText"),
                    psalmCitation = obj.getString("psalmCitation"),
                    psalmResponse = obj.optString("psalmResponse", ""),
                    psalmText = obj.getString("psalmText"),
                    secondReadingCitation = obj.optString("secondReadingCitation", null),
                    secondReadingText = obj.optString("secondReadingText", null),
                    gospelCitation = obj.getString("gospelCitation"),
                    gospelText = obj.getString("gospelText")
                )
            )
        }
        return result
    }
}
