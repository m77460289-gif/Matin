package com.example.reminder

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Keeps the daily local-time reminder aligned after device and app state changes.
 *
 * RTC alarms use wall-clock time, so changes to the clock or timezone require
 * rebuilding the next trigger from the current local calendar.
 */
internal fun isReminderRescheduleAction(action: String?): Boolean = when (action) {
    Intent.ACTION_BOOT_COMPLETED,
    Intent.ACTION_TIME_CHANGED,
    Intent.ACTION_TIMEZONE_CHANGED,
    Intent.ACTION_MY_PACKAGE_REPLACED,
    AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
    ACTION_TIMEZONE_OFFSET_CHANGED -> true
    else -> false
}

/**
 * API 37 introduced this broadcast for offset changes such as daylight-saving
 * transitions. Keep the action as a string so the app can receive it while
 * compiling against an earlier SDK.
 */
private const val ACTION_TIMEZONE_OFFSET_CHANGED =
    "android.intent.action.TIMEZONE_OFFSET_CHANGED"

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (!ReminderScheduler.isEnabled(context)) return
        if (isReminderRescheduleAction(intent?.action)) {
            ReminderScheduler.scheduleDaily(context)
        }
    }
}
