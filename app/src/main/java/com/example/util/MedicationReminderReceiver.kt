package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.IvfSettingsManager

class MedicationReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val isMorning = intent.getBooleanExtra(EXTRA_IS_MORNING, true)
    val settings = IvfSettingsManager.getInstance(context).loadSettings()

    val title = if (isMorning) "⏰ Rappel PMA : Médicaments du Matin" else "🌙 Rappel PMA : Injections & Soir"
    val message = if (isMorning) {
      "Prise du matin : ${settings.protocolName}\nDosage : ${settings.dailyDosages}"
    } else {
      "Injection & Prise du soir : ${settings.dailyDosages}"
    }

    val notificationId = if (isMorning) 101 else 102
    NotificationHelper.sendNotification(context, title, message, notificationId)

    // Reschedule for next day
    NotificationHelper.scheduleDailyMedicationReminders(
      context = context,
      morningEnabled = settings.morningReminderEnabled,
      morningTime = settings.morningReminderTime,
      eveningEnabled = settings.eveningReminderEnabled,
      eveningTime = settings.eveningReminderTime,
      protocolSummary = settings.dailyDosages
    )
  }

  companion object {
    const val EXTRA_IS_MORNING = "extra_is_morning"
    const val ACTION_REMINDER = "com.example.ACTION_MEDICATION_REMINDER"
    const val REQUEST_CODE_MORNING = 1001
    const val REQUEST_CODE_EVENING = 1002
  }
}
