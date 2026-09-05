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
import com.willykez.liturgx.data.bible.BiblePassage

/**
 * Two independent notification types, two independent channels -- so a person can mute or
 * retime one without affecting the other from system Settings, which isn't possible if they
 * shared a channel.
 */
object NotificationHelper {
    private const val READING_CHANNEL_ID = "daily_reading"
    private const val READING_NOTIFICATION_ID = 1001
    private const val VERSE_CHANNEL_ID = "verse_of_day"
    private const val VERSE_NOTIFICATION_ID = 1002

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(READING_CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    READING_CHANNEL_ID,
                    "Kikumbusho cha Masomo",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Kikumbusho cha kila siku kwamba masomo ya leo yapo tayari" }
            )
        }
        if (manager.getNotificationChannel(VERSE_CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    VERSE_CHANNEL_ID,
                    "Neno la Kila Siku",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Andiko fupi la kila siku kwa ajili ya kutafakari" }
            )
        }
    }

    /**
     * Now lists every reading's reference for the day (First Reading, Psalm, Second Reading
     * when there is one, Gospel), not just the Gospel citation on its own -- a person glancing
     * at the notification shade gets the full set of references to look up, the same as
     * opening the app to Leo would show them, instead of just one of several readings.
     */
    fun showDailyReadingNotification(context: Context, dayResult: DayResult, readingItems: List<ReadingItem>) {
        ensureChannel(context)
        if (!hasNotificationPermission(context)) return

        val title = "Masomo ya Leo Yapo Tayari"
        val body = if (readingItems.isNotEmpty()) {
            readingItems.joinToString("\n") { "${it.label}: ${it.citation}" }
        } else {
            dayResult.resolved.overridingSaint?.jina ?: dayResult.resolved.label
        }

        notify(context, READING_CHANNEL_ID, READING_NOTIFICATION_ID, title, body)
    }

    fun showVerseNotification(context: Context, passage: BiblePassage?) {
        ensureChannel(context)
        if (!hasNotificationPermission(context) || passage == null) return

        val firstVerse = passage.verses.firstOrNull() ?: return
        val title = "Neno la Leo"
        val body = "${firstVerse.text}\n— ${passage.book} ${firstVerse.chapter}:${firstVerse.verse}"

        notify(context, VERSE_CHANNEL_ID, VERSE_NOTIFICATION_ID, title, body)
    }

    private fun notify(context: Context, channelId: String, notificationId: Int, title: String, body: String) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        // Reminder was scheduled while permission was granted, but the person could have
        // revoked it since from system Settings -- fail quietly rather than crash.
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }
}
