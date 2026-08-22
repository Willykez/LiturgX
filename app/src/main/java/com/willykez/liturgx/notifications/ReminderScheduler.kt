package com.willykez.liturgx.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Schedules a daily reminder using [AlarmManager.setAndAllowWhileIdle] -- inexact, Doze-aware
 * delivery that needs no special "exact alarm" permission (that permission is meant for
 * alarm-clock-grade precision; a devotional reminder firing a few minutes late is completely
 * fine, and avoiding it means one less permission prompt for the person to grant).
 *
 * Generalized over which [BroadcastReceiver] fires and a distinct [requestCode] so the same
 * scheduling logic backs both independent reminders this app has -- the daily reading reminder
 * ([ReadingReminderReceiver]) and the rotating verse reminder ([VerseReminderReceiver]) -- as
 * two separate alarms a person can enable/disable/retime independently of each other, rather
 * than duplicating this whole class for a second reminder type.
 */
object ReminderScheduler {

    /** Call when a reminder is first turned on, or its time is changed. */
    fun schedule(context: Context, hour: Int, minute: Int, requestCode: Int, receiver: Class<*>) {
        val now = LocalDateTime.now()
        var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!target.isAfter(now)) target = target.plusDays(1)
        arm(context, target, requestCode, receiver)
    }

    /** Call from the receiver right after it fires, to queue tomorrow's notification. */
    fun scheduleNext(context: Context, hour: Int, minute: Int, requestCode: Int, receiver: Class<*>) {
        val tomorrow = LocalDateTime.now().plusDays(1).withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        arm(context, tomorrow, requestCode, receiver)
    }

    fun cancel(context: Context, requestCode: Int, receiver: Class<*>) {
        alarmManager(context).cancel(pendingIntent(context, requestCode, receiver))
    }

    private fun arm(context: Context, target: LocalDateTime, requestCode: Int, receiver: Class<*>) {
        val millis = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager(context).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent(context, requestCode, receiver))
    }

    private fun alarmManager(context: Context) =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun pendingIntent(context: Context, requestCode: Int, receiver: Class<*>): PendingIntent {
        val intent = Intent(context, receiver)
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

/** Stable request codes -- must stay distinct so the two reminders' PendingIntents never collide. */
object ReminderRequestCodes {
    const val DAILY_READING = 5501
    const val VERSE_OF_DAY = 5502
}
