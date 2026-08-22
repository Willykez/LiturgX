package com.willykez.liturgx.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.willykez.liturgx.data.SettingsStore
import com.willykez.liturgx.data.bible.BibleRepository
import java.time.LocalDate

private const val PSALM_COUNT = 150

/**
 * Fires at the person's chosen time with a rotating verse -- deliberately independent content
 * from [ReadingReminderReceiver]'s "today's Gospel is ready", so the two reminders don't just
 * repeat each other. Rotates through all 150 Psalms by day-of-year
 * (`dayOfYear % 150 + 1`), rather than a hand-picked list: every citation it can ever produce is
 * guaranteed to resolve (Psalms are fully present in the bundled Bible), the rotation is
 * deterministic day-to-day without needing any new data, and cycling the full Psalter is itself
 * a reasonable devotional rotation, not just a filler mechanism.
 */
class VerseReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val settingsStore = SettingsStore(context)

        if (settingsStore.loadVerseReminderEnabled()) {
            val repository = BibleRepository(context)
            val psalmNumber = (LocalDate.now().dayOfYear % PSALM_COUNT) + 1
            val passage = repository.getPassage("Zaburi $psalmNumber:1")
            NotificationHelper.showVerseNotification(context, passage)

            val (hour, minute) = settingsStore.loadVerseReminderTime()
            ReminderScheduler.scheduleNext(context, hour, minute, ReminderRequestCodes.VERSE_OF_DAY, VerseReminderReceiver::class.java)
        }
    }
}
