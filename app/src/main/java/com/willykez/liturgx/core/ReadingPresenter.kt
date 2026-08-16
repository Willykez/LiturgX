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
                    r.shangilio?.let { items += ReadingItem("SHANGILIO", "Shangilio", it) }
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
        r.shangilio?.let { items += ReadingItem("SHANGILIO", "Shangilio", it) }
        r.injili?.let { items += ReadingItem("INJILI", "Injili", it) }
    }
}
