package com.willykez.liturgx.model

/**
 * The language the app's own interface (labels, headings, section titles) is shown in.
 *
 * This does NOT control what language the Scripture text itself renders in — the only Bible
 * database bundled with the app is Swahili (assets/database/bible_swahili.sqlite), so the
 * actual reading body is always resolved in Swahili regardless of this setting. What this
 * setting changes is everything AROUND the text: "Somo la Kwanza" vs "First Reading", the day
 * heading format, and which label the share sheet uses.
 */
enum class AppLanguage {
    SWAHILI,
    ENGLISH;

    companion object {
        fun from(stored: String): AppLanguage = try {
            valueOf(stored)
        } catch (e: Exception) {
            SWAHILI
        }
    }
}

/** Which of the four reading slots a [com.willykez.liturgx.model.ReadingItem] fills — used to pick its label. */
enum class ReadingSection {
    FIRST_READING,
    PSALM,
    SECOND_READING,
    GOSPEL
}

object ReadingLabels {

    fun sectionLabel(section: ReadingSection, language: AppLanguage): String = when (language) {
        AppLanguage.SWAHILI -> when (section) {
            ReadingSection.FIRST_READING -> "Somo la Kwanza"
            ReadingSection.PSALM -> "Wimbo wa Katikati"
            ReadingSection.SECOND_READING -> "Somo la Pili"
            ReadingSection.GOSPEL -> "Injili"
        }
        AppLanguage.ENGLISH -> when (section) {
            ReadingSection.FIRST_READING -> "First Reading"
            ReadingSection.PSALM -> "Responsorial Psalm"
            ReadingSection.SECOND_READING -> "Second Reading"
            ReadingSection.GOSPEL -> "Gospel"
        }
    }

    fun readingsTabTitle(language: AppLanguage): String =
        if (language == AppLanguage.SWAHILI) "Masomo" else "Readings"

    fun reflectionLabel(language: AppLanguage): String =
        if (language == AppLanguage.SWAHILI) "Tafakari" else "Reflection"

    fun weekLabel(language: AppLanguage): String =
        if (language == AppLanguage.SWAHILI) "Juma" else "Week"

    fun yearLabel(language: AppLanguage): String =
        if (language == AppLanguage.SWAHILI) "Mwaka" else "Year"

    /** Caption shown under a reading's body text explaining why it's Swahili even in English mode. */
    fun swahiliTextNote(language: AppLanguage): String? =
        if (language == AppLanguage.ENGLISH)
            "Reading text is shown in Swahili — no English Bible text is bundled with this app."
        else null
}
