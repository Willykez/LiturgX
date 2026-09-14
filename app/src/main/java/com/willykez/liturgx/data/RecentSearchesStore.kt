package com.willykez.liturgx.data

import android.content.Context

private const val MAX_RECENT = 8

/** Last few search terms for one screen, most recent first, for one-tap re-search. Plain
 *  SharedPreferences string is enough for a list this small -- no need for anything heavier.
 *  [namespace] keeps each screen's history separate (Bible search vs. Saints search, etc.);
 *  the default empty namespace keeps the original file name so existing Bible search history
 *  isn't lost by this becoming shared. */
class RecentSearchesStore(context: Context, namespace: String = "") {
    private val suffix = if (namespace.isEmpty()) "" else "_$namespace"
    private val prefs = context.applicationContext.getSharedPreferences("liturgx_recent_searches$suffix", Context.MODE_PRIVATE)

    fun recentSearches(): List<String> =
        (prefs.getString("terms", null) ?: "").split("\u0001").filter { it.isNotBlank() }

    fun record(term: String) {
        val trimmed = term.trim()
        if (trimmed.isEmpty()) return
        val updated = (listOf(trimmed) + recentSearches().filterNot { it.equals(trimmed, ignoreCase = true) })
            .take(MAX_RECENT)
        prefs.edit().putString("terms", updated.joinToString("\u0001")).apply()
    }

    /** Removes one entry -- the swipe-to-dismiss action on a single recent-search row. */
    fun remove(term: String) {
        val updated = recentSearches().filterNot { it.equals(term, ignoreCase = true) }
        prefs.edit().putString("terms", updated.joinToString("\u0001")).apply()
    }

    fun clear() {
        prefs.edit().remove("terms").apply()
    }
}
