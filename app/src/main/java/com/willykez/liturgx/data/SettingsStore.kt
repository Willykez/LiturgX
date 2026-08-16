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
}
