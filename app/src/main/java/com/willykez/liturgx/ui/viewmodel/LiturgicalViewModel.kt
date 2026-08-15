package com.willykez.liturgx.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.willykez.liturgx.data.local.BookmarkEntity
import com.willykez.liturgx.data.local.LiturgicalDatabase
import com.willykez.liturgx.data.local.UserPreferencesEntity
import com.willykez.liturgx.data.repository.LiturgicalRepository
import com.willykez.liturgx.model.AppLanguage
import com.willykez.liturgx.model.LiturgicalDay
import com.willykez.liturgx.model.Prayer
import com.willykez.liturgx.model.PrayerCategory
import com.willykez.liturgx.notifications.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LiturgicalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LiturgicalRepository

    val selectedDate = MutableStateFlow(LocalDate.now())
    
    private val _currentReading = MutableStateFlow<LiturgicalDay?>(null)
    val currentReading: StateFlow<LiturgicalDay?> = _currentReading.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userPreferences = MutableStateFlow(UserPreferencesEntity())
    val userPreferences: StateFlow<UserPreferencesEntity> = _userPreferences.asStateFlow()

    val bookmarks: StateFlow<List<BookmarkEntity>>

    // Prayer & Rosary State
    val selectedPrayerCategory = MutableStateFlow(PrayerCategory.HOURS)
    val rosaryDecade = MutableStateFlow(1)
    val rosaryBead = MutableStateFlow(0)

    // Reading display settings
    val fontScale = MutableStateFlow(1.0f)
    val useSerifFont = MutableStateFlow(true)
    val distractionFreeMode = MutableStateFlow(false)
    val interfaceLanguage = MutableStateFlow(AppLanguage.SWAHILI)

    init {
        val database = LiturgicalDatabase.getDatabase(application)
        repository = LiturgicalRepository(
            context = application,
            readingDao = database.readingDao(),
            bookmarkDao = database.bookmarkDao(),
            preferencesDao = database.userPreferencesDao(),
            lectionaryDao = database.lectionaryDao()
        )

        bookmarks = repository.allBookmarks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.userPreferences.collectLatest { prefs ->
                if (prefs != null) {
                    _userPreferences.value = prefs
                    fontScale.value = prefs.fontScale
                    useSerifFont.value = prefs.useSerifFont
                    interfaceLanguage.value = AppLanguage.from(prefs.interfaceLanguage)
                }
            }
        }

        loadReadingForDate(selectedDate.value)
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
        loadReadingForDate(date)
    }

    fun nextDay() {
        val next = selectedDate.value.plusDays(1)
        selectDate(next)
    }

    fun previousDay() {
        val prev = selectedDate.value.minusDays(1)
        selectDate(prev)
    }

    fun jumpToToday() {
        selectDate(LocalDate.now())
    }

    private fun loadReadingForDate(date: LocalDate) {
        viewModelScope.launch {
            _isLoading.value = true
            val reading = repository.getReadingForDate(date)
            _currentReading.value = reading
            _isLoading.value = false
        }
    }

    fun toggleBookmarkCurrentReading() {
        val reading = _currentReading.value ?: return
        val dateStr = reading.date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val bookmark = BookmarkEntity(
            id = "reading_$dateStr",
            type = "READING",
            title = reading.title,
            subtitle = reading.gospel.citation,
            content = reading.gospel.text,
            dateOrCategory = dateStr
        )
        viewModelScope.launch {
            repository.toggleBookmark(bookmark)
        }
    }

    fun toggleBookmarkPrayer(prayer: Prayer) {
        val bookmark = BookmarkEntity(
            id = "prayer_${prayer.id}",
            type = "PRAYER",
            title = prayer.title,
            subtitle = prayer.subtitle,
            content = prayer.text,
            dateOrCategory = prayer.category.title
        )
        viewModelScope.launch {
            repository.toggleBookmark(bookmark)
        }
    }

    fun deleteBookmark(id: String) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
        }
    }

    fun getOfflinePrayers(): List<Prayer> = repository.getOfflinePrayers()

    fun updateFontScale(newScale: Float) {
        fontScale.value = newScale.coerceIn(0.8f, 1.6f)
        savePreferencesInternal()
    }

    fun toggleSerifFont() {
        useSerifFont.value = !useSerifFont.value
        savePreferencesInternal()
    }

    fun toggleDistractionFreeMode() {
        distractionFreeMode.value = !distractionFreeMode.value
    }

    fun updateInterfaceLanguage(language: AppLanguage) {
        interfaceLanguage.value = language
        val updated = _userPreferences.value.copy(interfaceLanguage = language.name)
        _userPreferences.value = updated
        viewModelScope.launch { repository.savePreferences(updated) }
    }

    fun updateThemeMode(mode: String) {
        val updated = _userPreferences.value.copy(darkMode = mode)
        _userPreferences.value = updated
        viewModelScope.launch { repository.savePreferences(updated) }
    }

    fun updateDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
        val updated = _userPreferences.value.copy(
            dailyReminderEnabled = enabled,
            dailyReminderHour = hour,
            dailyReminderMinute = minute
        )
        _userPreferences.value = updated
        viewModelScope.launch {
            repository.savePreferences(updated)
            if (enabled) {
                NotificationScheduler.scheduleDailyReminder(
                    getApplication(),
                    hour,
                    minute,
                    1001,
                    "Daily Mass Readings & Meditation",
                    "Today's readings and gospel reflection are ready for your quiet prayer."
                )
            } else {
                NotificationScheduler.cancelReminder(getApplication(), 1001)
            }
        }
    }

    fun updateEveningReminder(enabled: Boolean, hour: Int, minute: Int) {
        val updated = _userPreferences.value.copy(
            eveningReminderEnabled = enabled,
            eveningReminderHour = hour,
            eveningReminderMinute = minute
        )
        _userPreferences.value = updated
        viewModelScope.launch {
            repository.savePreferences(updated)
            if (enabled) {
                NotificationScheduler.scheduleDailyReminder(
                    getApplication(),
                    hour,
                    minute,
                    1002,
                    "Evening Prayer & Night Contemplation",
                    "Take a peaceful pause for Vespers and Night Prayer."
                )
            } else {
                NotificationScheduler.cancelReminder(getApplication(), 1002)
            }
        }
    }

    fun updateFeastAlerts(enabled: Boolean) {
        val updated = _userPreferences.value.copy(feastAlertsEnabled = enabled)
        _userPreferences.value = updated
        viewModelScope.launch { repository.savePreferences(updated) }
    }

    fun incrementRosaryBead() {
        if (rosaryBead.value < 10) {
            rosaryBead.value += 1
        } else {
            if (rosaryDecade.value < 5) {
                rosaryDecade.value += 1
                rosaryBead.value = 0
            } else {
                // Completed all 5 decades
                resetRosary()
            }
        }
    }

    fun resetRosary() {
        rosaryDecade.value = 1
        rosaryBead.value = 0
    }

    private fun savePreferencesInternal() {
        viewModelScope.launch {
            val updated = _userPreferences.value.copy(
                fontScale = fontScale.value,
                useSerifFont = useSerifFont.value
            )
            repository.savePreferences(updated)
        }
    }
}
