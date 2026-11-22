package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * Daily 17:00 reminder: "۲ ساعت مطالعه امروز شروع شود؟"
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Do not show or re-schedule reminders after the user has opted out.
        if (!ReminderScheduler.isEnabled(context)) {
            ReminderScheduler.cancel(context)
            return
        }

        val channelId = CHANNEL_ID
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "یادآور مطالعه روزانه",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏰ ۲ ساعت مطالعه امروز")
            .setContentText("۲ ساعت مطالعه امروز شروع شود؟")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try { manager.notify(NOTIFICATION_ID, notification) } catch (_: Exception) {}

        // Schedule the next day's alarm
        ReminderScheduler.scheduleNext(context)
    }

    companion object {
        const val CHANNEL_ID = "daily_study_reminder"
        const val NOTIFICATION_ID = 2001
    }
}
