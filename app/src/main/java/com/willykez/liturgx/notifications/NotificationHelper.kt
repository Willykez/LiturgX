package com.willykez.liturgx.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.willykez.liturgx.MainActivity
import com.willykez.liturgx.R
import com.willykez.liturgx.core.ReadingItem
import com.willykez.liturgx.data.DayResult

/**
 * The daily "today's Gospel is ready" push. One channel, one notification id — a new day's
 * notification simply replaces yesterday's if it's somehow still showing.
 */
object NotificationHelper {
    private const val CHANNEL_ID = "daily_reading"
    private const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Kikumbusho cha Masomo",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Kikumbusho cha kila siku kwamba masomo ya leo yapo tayari"
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun showDailyReadingNotification(context: Context, dayResult: DayResult, gospel: ReadingItem?) {
        ensureChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Reminder was scheduled while permission was granted, but the person could have
            // revoked it since from system Settings -- fail quietly rather than crash.
            return
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Masomo ya Leo Yapo Tayari"
        val body = gospel?.let { "Injili: ${it.citation}" }
            ?: (dayResult.resolved.overridingSaint?.jina ?: dayResult.resolved.label)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
