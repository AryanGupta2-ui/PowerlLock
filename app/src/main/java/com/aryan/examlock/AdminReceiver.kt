package com.aryan.examlock

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

/**
 * Required boilerplate receiver for Device Admin / Device Owner APIs.
 * This app must be provisioned as Device Owner via ADB (see README) for
 * lock-task power-menu suppression to work.
 */
class AdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }
}
