package com.aryan.examlock

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Button
import android.view.Gravity
import android.view.WindowManager

/**
 * The active "exam session" screen.
 *
 * While this Activity is in the foreground and Lock Task Mode is running:
 *  - Home / Recents buttons are disabled (standard Lock Task behavior)
 *  - If this app is Device Owner, the power-button long-press menu
 *    (Power off / Restart / Emergency) is HIDDEN because we do not
 *    include LOCK_TASK_FEATURE_GLOBAL_ACTIONS in setLockTaskFeatures().
 *
 * IMPORTANT LIMITATION (tell the user, don't hide it):
 * A hardware force-shutdown (holding Power + Volume Down ~10s) is handled
 * entirely by the phone's firmware, below the Android OS. No app, including
 * Device Owner apps, can intercept or block that. This app only prevents
 * casual/accidental power-off via the normal UI.
 */
class LockActivity : Activity() {

    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    private lateinit var timerText: TextView
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, AdminReceiver::class.java)

        buildUi()

        val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MIN, 180)
        startLockdown(durationMinutes)
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(0xFF0B1F3A.toInt())
        }

        val title = TextView(this).apply {
            text = "Exam Session Active"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }

        timerText = TextView(this).apply {
            text = "--:--"
            setTextColor(0xFFFFD54F.toInt())
            textSize = 48f
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Power menu is disabled until the session ends.\nHold Power + Volume Down can still force a hardware shutdown \u2014 this cannot be blocked by any app."
            setTextColor(0xFFB0BEC5.toInt())
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(48, 32, 48, 32)
        }

        val endButton = Button(this).apply {
            text = "End Session Early"
            setOnClickListener { endLockdown() }
        }

        root.addView(title)
        root.addView(timerText)
        root.addView(subtitle)
        root.addView(endButton)
        setContentView(root)
    }

    private fun startLockdown(durationMinutes: Int) {
        // If this app is Device Owner, restrict which system UI features are
        // available during lock task mode. Omitting GLOBAL_ACTIONS hides the
        // power-button long-press menu.
        if (dpm.isDeviceOwnerApp(packageName)) {
            dpm.setLockTaskPackages(adminComponent, arrayOf(packageName))
            dpm.setLockTaskFeatures(
                adminComponent,
                DevicePolicyManager.LOCK_TASK_FEATURE_NONE // excludes GLOBAL_ACTIONS
            )
        }

        try {
            startLockTask()
        } catch (e: Exception) {
            // Lock task not permitted yet — device owner setup likely incomplete.
            e.printStackTrace()
        }

        val millis = durationMinutes * 60_000L
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(msLeft: Long) {
                val min = (msLeft / 1000) / 60
                val sec = (msLeft / 1000) % 60
                timerText.text = String.format("%02d:%02d", min, sec)
            }
            override fun onFinish() {
                endLockdown()
            }
        }.start()
    }

    private fun endLockdown() {
        countDownTimer?.cancel()
        try {
            stopLockTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        finish()
    }

    // Block back button during the session
    override fun onBackPressed() {
        // no-op: swallow back button while locked
    }

    companion object {
        const val EXTRA_DURATION_MIN = "duration_minutes"
    }
}
