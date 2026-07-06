package com.aryan.examlock

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.app.admin.DevicePolicyManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar

class MainActivity : Activity() {

    private var chosenHour = -1
    private var chosenMinute = -1
    private lateinit var durationInput: EditText
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        refreshStatus()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }

        statusText = TextView(this).apply { textSize = 14f }

        val pickTimeBtn = Button(this).apply {
            text = "Pick Start Time"
            setOnClickListener { showTimePicker() }
        }

        durationInput = EditText(this).apply {
            hint = "Duration in minutes (e.g. 180)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val scheduleBtn = Button(this).apply {
            text = "Schedule Lockdown"
            setOnClickListener { scheduleLockdown() }
        }

        val startNowBtn = Button(this).apply {
            text = "Start Lockdown Now (test)"
            setOnClickListener {
                val mins = durationInput.text.toString().toIntOrNull() ?: 30
                startActivity(Intent(this@MainActivity, LockActivity::class.java)
                    .putExtra(LockActivity.EXTRA_DURATION_MIN, mins))
            }
        }

        val deviceOwnerInfo = TextView(this).apply {
            textSize = 12f
            setPadding(0, 32, 0, 0)
        }
        checkDeviceOwnerStatus(deviceOwnerInfo)

        root.addView(statusText)
        root.addView(pickTimeBtn)
        root.addView(durationInput)
        root.addView(scheduleBtn)
        root.addView(startNowBtn)
        root.addView(deviceOwnerInfo)
        setContentView(root)
    }

    private fun checkDeviceOwnerStatus(label: TextView) {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        label.text = if (dpm.isDeviceOwnerApp(packageName)) {
            "Device Owner: ACTIVE — power menu suppression will work."
        } else {
            "Device Owner: NOT SET. Power menu will still show during lockdown.\n" +
            "Run the ADB command in the README to enable it."
        }
    }

    private fun showTimePicker() {
        val now = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            chosenHour = hour
            chosenMinute = minute
            refreshStatus()
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
    }

    private fun refreshStatus() {
        statusText.text = if (chosenHour >= 0) {
            String.format("Start time set: %02d:%02d", chosenHour, chosenMinute)
        } else {
            "No start time chosen yet."
        }
    }

    private fun scheduleLockdown() {
        if (chosenHour < 0) {
            Toast.makeText(this, "Pick a start time first", Toast.LENGTH_SHORT).show()
            return
        }
        val durationMin = durationInput.text.toString().toIntOrNull()
        if (durationMin == null || durationMin <= 0) {
            Toast.makeText(this, "Enter a valid duration", Toast.LENGTH_SHORT).show()
            return
        }

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, chosenHour)
            set(Calendar.MINUTE, chosenMinute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra(LockActivity.EXTRA_DURATION_MIN, durationMin)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent
        )

        Toast.makeText(this, "Lockdown scheduled for ${cal.time}", Toast.LENGTH_LONG).show()
    }

    companion object {
        const val REQUEST_CODE = 1001
    }
}
