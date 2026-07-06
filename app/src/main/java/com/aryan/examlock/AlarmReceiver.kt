package com.aryan.examlock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val duration = intent.getIntExtra(LockActivity.EXTRA_DURATION_MIN, 180)
        val lockIntent = Intent(context, LockActivity::class.java).apply {
            putExtra(LockActivity.EXTRA_DURATION_MIN, duration)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(lockIntent)
    }
}
