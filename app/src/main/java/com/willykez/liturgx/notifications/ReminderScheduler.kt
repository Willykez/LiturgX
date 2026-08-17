package com.willykez.liturgx.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Schedules the daily reading reminder using [AlarmManager.setAndAllowWhileIdle] -- inexact,
 * Doze-aware delivery that needs no special "exact alarm" permission (that permission is
 * meant for alarm-clock-grade precision; a devotional reminder firing a few minutes late is
 * completely fine, and avoiding it means one less permission prompt for the person to grant).
 */
object ReminderScheduler {
    private const val REQUEST_CODE = 5501

    /** Call when the reminder is first turned on, or its time is changed. */
    fun schedule(context: Context, hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!target.isAfter(now)) target = target.plusDays(1)
        arm(context, target)
    }

    /** Call from [ReadingReminderReceiver] right after it fires, to queue tomorrow's notification. */
    fun scheduleNext(context: Context, hour: Int, minute: Int) {
        val tomorrow = LocalDateTime.now().plusDays(1).withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        arm(context, tomorrow)
    }

    fun cancel(context: Context) {
        alarmManager(context).cancel(pendingIntent(context))
    }

    private fun arm(context: Context, target: LocalDateTime) {
        val millis = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager(context).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent(context))
    }

    private fun alarmManager(context: Context) =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ReadingReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
