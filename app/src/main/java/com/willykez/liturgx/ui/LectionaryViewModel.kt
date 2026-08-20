package com.willykez.liturgx.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.willykez.liturgx.core.RegionSettings
import com.willykez.liturgx.data.DayResult
import com.willykez.liturgx.data.LectionaryRepository
import com.willykez.liturgx.data.SettingsStore
import com.willykez.liturgx.notifications.ReminderScheduler
import com.willykez.liturgx.ui.theme.ThemeMode
import java.time.LocalDate

class LectionaryViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = LectionaryRepository(app)
    private val settingsStore = SettingsStore(app)

    var region by mutableStateOf(settingsStore.load())
        private set

    var themeMode by mutableStateOf(settingsStore.loadThemeMode())
        private set

    var reminderEnabled by mutableStateOf(settingsStore.loadReminderEnabled())
        private set

    var reminderHour by mutableStateOf(settingsStore.loadReminderTime().first)
        private set

    var reminderMinute by mutableStateOf(settingsStore.loadReminderTime().second)
        private set

    var today by mutableStateOf(LocalDate.now())
        private set

    var selectedDate by mutableStateOf(LocalDate.now())
        private set

    var todayResult by mutableStateOf(repository.getForDate(today, region))
        private set

    var selectedResult by mutableStateOf(todayResult)
        private set

    fun goToDate(date: LocalDate) {
        selectedDate = date
        selectedResult = repository.getForDate(date, region)
    }

    fun jumpToToday() = goToDate(today)

    fun updateRegion(newRegion: RegionSettings) {
        region = newRegion
        settingsStore.save(newRegion)
        refreshAll()
    }

    fun updateThemeMode(mode: ThemeMode) {
        themeMode = mode
        settingsStore.saveThemeMode(mode)
    }

    /** The permission dance (Android 13+ POST_NOTIFICATIONS) happens in SettingsScreen, which
     *  only calls this once it's actually granted -- this function assumes it's safe to schedule. */
    fun updateReminderEnabled(enabled: Boolean) {
        reminderEnabled = enabled
        settingsStore.saveReminderEnabled(enabled)
        if (enabled) {
            ReminderScheduler.schedule(getApplication(), reminderHour, reminderMinute)
        } else {
            ReminderScheduler.cancel(getApplication())
        }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        reminderHour = hour
        reminderMinute = minute
        settingsStore.saveReminderTime(hour, minute)
        if (reminderEnabled) {
            ReminderScheduler.schedule(getApplication(), hour, minute)
        }
    }

    private fun refreshAll() {
        todayResult = repository.getForDate(today, region)
        selectedResult = repository.getForDate(selectedDate, region)
    }

    fun saintsList() = repository.allSaints()
}
