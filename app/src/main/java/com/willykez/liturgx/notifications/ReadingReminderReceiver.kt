package com.willykez.liturgx.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.willykez.liturgx.core.ReadingPresenter
import com.willykez.liturgx.data.LectionaryRepository
import com.willykez.liturgx.data.SettingsStore
import java.time.LocalDate

/**
 * Fires once at the scheduled reminder time, shows "today's Gospel is ready", then immediately
 * re-arms tomorrow's alarm -- [ReminderScheduler]'s inexact alarms fire exactly once, they
 * don't repeat on their own, so the chain has to be kept alive from inside itself.
 */
class ReadingReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val settingsStore = SettingsStore(context)

        if (settingsStore.loadReminderEnabled()) {
            val repository = LectionaryRepository(context)
            val today = LocalDate.now()
            val dayResult = repository.getForDate(today, settingsStore.load())
            val readingItems = ReadingPresenter.present(dayResult.readings)
            NotificationHelper.showDailyReadingNotification(context, dayResult, readingItems)

            val (hour, minute) = settingsStore.loadReminderTime()
            ReminderScheduler.scheduleNext(context, hour, minute, ReminderRequestCodes.DAILY_READING, ReadingReminderReceiver::class.java)
        }
    }
}
