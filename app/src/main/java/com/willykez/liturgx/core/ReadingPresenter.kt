package com.willykez.liturgx.core

/** One citation ready to render, already labeled for its context (handles Palm Sunday's two
 *  Gospels and the Easter Vigil's numbered Old Testament readings, per APP_LOGIC.md §1/§5). */
data class ReadingItem(val kindKey: String, val label: String, val citation: String)

object ReadingPresenter {

    fun present(readings: List<Reading>): List<ReadingItem> {
        val items = mutableListOf<ReadingItem>()
        for (r in readings) {
            val entry = r.entryKey.orEmpty()
            when {
                entry.startsWith("agano_la_kale_") -> {
                    val n = entry.removePrefix("agano_la_kale_")
                    r.somoLaKwanza?.let { items += ReadingItem("SOMO_LA_KWANZA", "Somo la $n", it) }
                    r.wimboLaKatikati?.let { items += ReadingItem("WIMBO", "Zaburi baada ya Somo la $n", it) }
                }
                entry.endsWith("_msafara") -> {
                    r.injili?.let { items += ReadingItem("INJILI", "Injili ya Maandamano", it) }
                }
                entry.endsWith("_misa") -> {
                    r.somoLaKwanza?.let { items += ReadingItem("SOMO_LA_KWANZA", "Somo la Kwanza", it) }
                    r.wimboLaKatikati?.let { items += ReadingItem("WIMBO", "Wimbo wa Katikati", it) }
                    r.somoLaPili?.let { items += ReadingItem("SOMO_LA_PILI", "Somo la Pili", it) }
                    r.shangilio?.takeIf(::isRealCitation)?.let { items += ReadingItem("SHANGILIO", "Shangilio", it) }
                    r.injili?.let { items += ReadingItem("INJILI", "Injili ya Misa (Simulizi la Mateso)", it) }
                }
                entry == "vigilia" -> {
                    r.somoLaKwanza?.let { items += ReadingItem("SOMO_LA_KWANZA", "Somo la Kwanza (Chaguo Kuu)", it) }
                    addCommon(items, r, skipFirst = true)
                }
                entry.startsWith("vigilia_chaguo_") -> {
                    val n = entry.removePrefix("vigilia_chaguo_")
                    r.somoLaKwanza?.let { items += ReadingItem("SOMO_LA_KWANZA", "Somo la Kwanza — Chaguo Mbadala $n", it) }
                }
                entry.startsWith("siku_hiari_mwaka_") -> {
                    r.somoLaPili?.let { items += ReadingItem("SOMO_LA_PILI", "Somo la Pili (Hiari)", it) }
                    r.injili?.let { items += ReadingItem("INJILI", "Injili (Hiari)", it) }
                }
                else -> addCommon(items, r)
            }
        }
        return items
    }

    private fun addCommon(items: MutableList<ReadingItem>, r: Reading, skipFirst: Boolean = false) {
        if (!skipFirst) r.somoLaKwanza?.let { items += ReadingItem("SOMO_LA_KWANZA", "Somo la Kwanza", it) }
        r.wimboLaKatikati?.let { items += ReadingItem("WIMBO", "Wimbo wa Katikati", it) }
        r.somoLaPili?.let { items += ReadingItem("SOMO_LA_PILI", "Somo la Pili", it) }
        r.shangilio?.takeIf(::isRealCitation)?.let { items += ReadingItem("SHANGILIO", "Shangilio", it) }
        r.injili?.let { items += ReadingItem("INJILI", "Injili", it) }
    }

    /**
     * 29 rows in the lectionary DB store an annotation in `shangilio` instead of an actual
     * citation: "[hakuna mstari maalum]" ("no specific verse"), "[Angalia Te Deum]", etc.,
     * for days where the Gospel Acclamation verse is genuinely unspecified or replaced by a
     * named hymn. A bracket-wrapped value like that isn't a citation Verse Part could ever
     * parse, and showing it as a reading card / share-text section with empty body text under
     * it is worse than just omitting the line, so it's filtered out here, once, for every
     * consumer (`present()` feeds the UI, plain-text share, image cards, and the PDF alike).
     */
    private fun isRealCitation(value: String): Boolean = value.isNotBlank() && !value.trimStart().startsWith("[")

    /**
     * The standard spoken Mass response after a reading, keyed by [ReadingItem.kindKey].
     * Only the two Scripture readings and the Gospel get one -- the Responsorial Psalm/Wimbo
     * and the Gospel Acclamation/Shangilio are themselves sung responses, not readings that are
     * responded *to*. Centralized here (rather than duplicated per call site) since every
     * sharing surface -- [com.willykez.liturgx.ui.components.ReadingBlock]'s image card, the
     * plain-text day share, the full-day image card, and the PDF export -- needs the same text.
     */
    fun massResponseFor(kindKey: String): String? = when (kindKey) {
        "SOMO_LA_KWANZA", "SOMO_LA_PILI" -> "Neno la Bwana.\nS: Tumshukuru Mungu."
        "INJILI" -> "Injili ya Bwana Yesu Kristo.\nS: Sifa kwako Ee Kristo."
        else -> null
    }
}
