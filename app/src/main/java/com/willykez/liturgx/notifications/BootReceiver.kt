package com.willykez.liturgx.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.willykez.liturgx.data.SettingsStore

/** Re-arms the daily reminder after a reboot -- AlarmManager alarms don't survive one on their own. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val settingsStore = SettingsStore(context)
        if (settingsStore.loadReminderEnabled()) {
            val (hour, minute) = settingsStore.loadReminderTime()
            ReminderScheduler.schedule(context, hour, minute)
        }
    }
}
