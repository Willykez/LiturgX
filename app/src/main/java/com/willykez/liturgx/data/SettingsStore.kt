package com.willykez.liturgx.data

import android.content.Context
import com.willykez.liturgx.core.EpiphanyMode
import com.willykez.liturgx.core.RegionSettings

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
}
