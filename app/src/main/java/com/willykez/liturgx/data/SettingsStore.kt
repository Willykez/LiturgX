package com.willykez.liturgx.data

import android.content.Context
import com.willykez.liturgx.core.EpiphanyMode
import com.willykez.liturgx.core.RegionSettings
import com.willykez.liturgx.ui.theme.ThemeMode

class SettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("liturgx_settings", Context.MODE_PRIVATE)

    fun load(): RegionSettings = RegionSettings(
        epiphanyMode = if (prefs.getString("epiphany_mode", "transferred") == "fixed_jan6")
            EpiphanyMode.FIXED_JAN6 else EpiphanyMode.TRANSFERRED,
        keepThursdaySolemnities = prefs.getBoolean("keep_thursday", false)
    )

    fun save(settings: RegionSettings) {
        prefs.edit()
            .putString("epiphany_mode", if (settings.epiphanyMode == EpiphanyMode.FIXED_JAN6) "fixed_jan6" else "transferred")
            .putBoolean("keep_thursday", settings.keepThursdaySolemnities)
            .apply()
    }

    fun loadThemeMode(): ThemeMode = when (prefs.getString("theme_mode", "system")) {
        "light" -> ThemeMode.LIGHT
        "dark" -> ThemeMode.DARK
        else -> ThemeMode.SYSTEM
    }

    fun saveThemeMode(mode: ThemeMode) {
        val value = when (mode) {
            ThemeMode.LIGHT -> "light"
            ThemeMode.DARK -> "dark"
            ThemeMode.SYSTEM -> "system"
        }
        prefs.edit().putString("theme_mode", value).apply()
    }

    fun loadReminderEnabled(): Boolean = prefs.getBoolean("reminder_enabled", false)

    fun saveReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("reminder_enabled", enabled).apply()
    }

    /** Defaults to 5:00 AM -- matches the "Today's Gospel is ready" framing this feature was built around. */
    fun loadReminderTime(): Pair<Int, Int> =
        prefs.getInt("reminder_hour", 5) to prefs.getInt("reminder_minute", 0)

    fun saveReminderTime(hour: Int, minute: Int) {
        prefs.edit().putInt("reminder_hour", hour).putInt("reminder_minute", minute).apply()
    }

    fun loadVerseReminderEnabled(): Boolean = prefs.getBoolean("verse_reminder_enabled", false)

    fun saveVerseReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("verse_reminder_enabled", enabled).apply()
    }

    /** Defaults to noon -- a separate time of day from the reading reminder's 5 AM by design,
     *  so the two don't land together even before the person has customized either one. */
    fun loadVerseReminderTime(): Pair<Int, Int> =
        prefs.getInt("verse_reminder_hour", 12) to prefs.getInt("verse_reminder_minute", 0)

    fun saveVerseReminderTime(hour: Int, minute: Int) {
        prefs.edit().putInt("verse_reminder_hour", hour).putInt("verse_reminder_minute", minute).apply()
    }
}
