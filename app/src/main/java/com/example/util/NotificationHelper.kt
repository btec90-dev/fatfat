package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R
import java.util.Calendar

object NotificationHelper {
  private const val CHANNEL_ID = "pregnancy_reminders_channel"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = "Rappels PMA, Médicaments & Grossesse"
      val descriptionText = "Rappels quotidiens pour les injections, médicaments et rendez-vous du protocole"
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
        enableVibration(true)
      }
      val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun sendNotification(context: Context, title: String, message: String, notificationId: Int = 1) {
    createNotificationChannel(context)
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)

    try {
      with(NotificationManagerCompat.from(context)) {
        notify(notificationId, builder.build())
      }
    } catch (e: SecurityException) {
      e.printStackTrace()
    }
  }

  fun scheduleDailyMedicationReminders(
    context: Context,
    morningEnabled: Boolean,
    morningTime: String,
    eveningEnabled: Boolean,
    eveningTime: String,
    protocolSummary: String
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

    if (morningEnabled) {
      val (mHour, mMin) = parseTimeString(morningTime, 8, 0)
      scheduleAlarm(context, alarmManager, mHour, mMin, MedicationReminderReceiver.REQUEST_CODE_MORNING, isMorning = true)
    } else {
      cancelAlarm(context, alarmManager, MedicationReminderReceiver.REQUEST_CODE_MORNING)
    }

    if (eveningEnabled) {
      val (eHour, eMin) = parseTimeString(eveningTime, 20, 0)
      scheduleAlarm(context, alarmManager, eHour, eMin, MedicationReminderReceiver.REQUEST_CODE_EVENING, isMorning = false)
    } else {
      cancelAlarm(context, alarmManager, MedicationReminderReceiver.REQUEST_CODE_EVENING)
    }
  }

  private fun scheduleAlarm(
    context: Context,
    alarmManager: AlarmManager,
    hour: Int,
    minute: Int,
    requestCode: Int,
    isMorning: Boolean
  ) {
    val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
      action = MedicationReminderReceiver.ACTION_REMINDER
      putExtra(MedicationReminderReceiver.EXTRA_IS_MORNING, isMorning)
    }

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      if (before(Calendar.getInstance())) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }

    try {
      alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
      )
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun cancelAlarm(context: Context, alarmManager: AlarmManager, requestCode: Int) {
    val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
      action = MedicationReminderReceiver.ACTION_REMINDER
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
      alarmManager.cancel(pendingIntent)
      pendingIntent.cancel()
    }
  }

  private fun parseTimeString(timeStr: String, defaultHour: Int, defaultMinute: Int): Pair<Int, Int> {
    return try {
      val parts = timeStr.trim().split(":")
      val hour = parts.getOrNull(0)?.toIntOrNull() ?: defaultHour
      val min = parts.getOrNull(1)?.toIntOrNull() ?: defaultMinute
      Pair(hour.coerceIn(0, 23), min.coerceIn(0, 59))
    } catch (e: Exception) {
      Pair(defaultHour, defaultMinute)
    }
  }
}
