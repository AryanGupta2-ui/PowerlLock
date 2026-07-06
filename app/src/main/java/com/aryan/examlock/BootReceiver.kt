package com.aryan.examlock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Placeholder: if the phone reboots before a scheduled alarm fires, exact
 * alarms set via AlarmManager are cleared by the OS. For a production version,
 * persist the scheduled time (e.g. SharedPreferences) and re-register it here.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // TODO: read saved schedule from SharedPreferences and re-arm AlarmManager
        }
    }
}
