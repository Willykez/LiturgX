package com.willykez.liturgx.data

import android.content.Context

private const val MAX_RECENT = 8

/** Last few Bible search terms, most recent first, for one-tap re-search. Plain
 *  SharedPreferences string is enough for a list this small -- no need for anything heavier. */
class RecentSearchesStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("liturgx_recent_searches", Context.MODE_PRIVATE)

    fun recentSearches(): List<String> =
        (prefs.getString("terms", null) ?: "").split("\u0001").filter { it.isNotBlank() }

    fun record(term: String) {
        val trimmed = term.trim()
        if (trimmed.isEmpty()) return
        val updated = (listOf(trimmed) + recentSearches().filterNot { it.equals(trimmed, ignoreCase = true) })
            .take(MAX_RECENT)
        prefs.edit().putString("terms", updated.joinToString("\u0001")).apply()
    }

    fun clear() {
        prefs.edit().remove("terms").apply()
    }
}
