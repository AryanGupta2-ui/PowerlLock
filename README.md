# Exam Lock — Scheduled Power-Off Prevention (Android)

## What this actually does
- At a time you schedule, the app launches a full-screen "exam session" lock
  screen that stays on top and blocks Home/Recents (Lock Task Mode).
- If the app is set as **Device Owner** (one-time ADB setup below), it also
  hides the power-button long-press menu (Power off / Restart), so a normal
  power-button press during the session does nothing.
- The session auto-ends after your set duration, or you can end it early from
  inside the app.

## What it CANNOT do (hardware limitation, not a bug)
Holding **Power + Volume Down for ~10 seconds** triggers a hardware-level
forced shutdown that is handled by the phone's firmware, completely outside
Android's control. No app — Device Owner or otherwise — can intercept this.
This is true for every phone, including official exam-center kiosk devices.
So: this stops accidental/casual power-off through the UI, not a determined
hard reset.

## One-time setup: making this the Device Owner
Device Owner mode only works on a device/profile with **no Google account
signed in**. Easiest path: use a spare device, or factory reset the one
you'll use for mock tests.

1. Build and install the APK, but don't open it yet:
   ```
   adb install app-debug.apk
   ```
2. Set it as device owner:
   ```
   adb shell dpm set-device-owner com.aryan.examlock/.AdminReceiver
   ```
   If this fails with "not allowed", it means an account is already added,
   or another device admin exists — remove accounts / factory reset and retry.
3. Open the app — it will show "Device Owner: ACTIVE" at the bottom.

## How to build
Open the `ExamLockApp` folder in Android Studio (Hedgehog or newer), let it
sync Gradle, then Build → Build APK. Or from command line with the Android
SDK installed:
```
./gradlew assembleDebug
```

## Using it
1. Open the app, pick a start time and duration in minutes.
2. Tap "Schedule Lockdown."
3. At that time, the lock screen launches automatically and the power menu
   is suppressed (if Device Owner is active).
4. It ends automatically after the duration, or tap "End Session Early."

## Suggested next improvements
- Persist the schedule in SharedPreferences and re-arm it in `BootReceiver`
  so a reboot before the scheduled time doesn't lose the alarm.
- Add a PIN to "End Session Early" so you can't easily bail on yourself.
- Log violation attempts (like your CBT simulator's violation tracker) if
  Home/Recents is pressed during the session.
