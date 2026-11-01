package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IvfProtocolSettings(
  val protocolName: String = "Protocole Antagoniste (FIV/ICSI)",
  val dailyDosages: String = "Gonal-F 150 UI (19h00), Cetrotide 0.25mg (08h00), Progestérone PIO 50mg (20h00)",
  val cycleDay1Date: String = "2026-10-15",
  val cycleDay1Ms: Long = 1792022400000L, // Approx Oct 2026
  val morningReminderEnabled: Boolean = true,
  val morningReminderTime: String = "08:00",
  val eveningReminderEnabled: Boolean = true,
  val eveningReminderTime: String = "20:00",
  val clinicPhone: String = "+33 1 42 16 00 00",
  val coordinatorEmail: String = "coordination.pma@hopital.fr",
  val clinicName: String = "Centre PMA",
  val emergencyPhone: String = "+33 1 42 16 00 01"
)

class IvfSettingsManager(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  private val _settingsFlow = MutableStateFlow(loadSettings())
  val settingsFlow: StateFlow<IvfProtocolSettings> = _settingsFlow.asStateFlow()

  companion object {
    private const val PREFS_NAME = "ivf_protocol_shared_prefs"
    private const val KEY_PROTOCOL_NAME = "key_protocol_name"
    private const val KEY_DAILY_DOSAGES = "key_daily_dosages"
    private const val KEY_CYCLE_DAY_1_DATE = "key_cycle_day_1_date"
    private const val KEY_CYCLE_DAY_1_MS = "key_cycle_day_1_ms"
    private const val KEY_MORNING_REMINDER_ENABLED = "key_morning_reminder_enabled"
    private const val KEY_MORNING_REMINDER_TIME = "key_morning_reminder_time"
    private const val KEY_EVENING_REMINDER_ENABLED = "key_evening_reminder_enabled"
    private const val KEY_EVENING_REMINDER_TIME = "key_evening_reminder_time"
    private const val KEY_CLINIC_PHONE = "key_clinic_phone"
    private const val KEY_COORDINATOR_EMAIL = "key_coordinator_email"
    private const val KEY_CLINIC_NAME = "key_clinic_name"
    private const val KEY_EMERGENCY_PHONE = "key_emergency_phone"

    @Volatile
    private var INSTANCE: IvfSettingsManager? = null

    fun getInstance(context: Context): IvfSettingsManager {
      return INSTANCE ?: synchronized(this) {
        val instance = IvfSettingsManager(context.applicationContext)
        INSTANCE = instance
        instance
      }
    }
  }

  fun loadSettings(): IvfProtocolSettings {
    return IvfProtocolSettings(
      protocolName = prefs.getString(KEY_PROTOCOL_NAME, "Protocole Antagoniste (FIV/ICSI)") ?: "Protocole Antagoniste (FIV/ICSI)",
      dailyDosages = prefs.getString(KEY_DAILY_DOSAGES, "Gonal-F 150 UI (19h00), Cetrotide 0.25mg (08h00), Progestérone PIO 50mg (20h00)") ?: "Gonal-F 150 UI (19h00), Cetrotide 0.25mg (08h00), Progestérone PIO 50mg (20h00)",
      cycleDay1Date = prefs.getString(KEY_CYCLE_DAY_1_DATE, "2026-10-15") ?: "2026-10-15",
      cycleDay1Ms = prefs.getLong(KEY_CYCLE_DAY_1_MS, System.currentTimeMillis() - (14L * 86400000L)),
      morningReminderEnabled = prefs.getBoolean(KEY_MORNING_REMINDER_ENABLED, true),
      morningReminderTime = prefs.getString(KEY_MORNING_REMINDER_TIME, "08:00") ?: "08:00",
      eveningReminderEnabled = prefs.getBoolean(KEY_EVENING_REMINDER_ENABLED, true),
      eveningReminderTime = prefs.getString(KEY_EVENING_REMINDER_TIME, "20:00") ?: "20:00",
      clinicPhone = prefs.getString(KEY_CLINIC_PHONE, "+33 1 42 16 00 00") ?: "+33 1 42 16 00 00",
      coordinatorEmail = prefs.getString(KEY_COORDINATOR_EMAIL, "coordination.pma@hopital.fr") ?: "coordination.pma@hopital.fr",
      clinicName = prefs.getString(KEY_CLINIC_NAME, "Centre PMA")
        ?.replace("Centre de Fertilité & PMA Hôpital", "Centre PMA")
        ?.replace("Centre de Fertilité", "Centre PMA")
        ?.replace("Clinique de Fertilité & PMA", "Centre PMA")
        ?.replace("Clinique de Fertilité", "Centre PMA")
        ?.replace("Clinique PMA", "Centre PMA")
        ?.replace("Clinique", "Centre PMA") ?: "Centre PMA",
      emergencyPhone = prefs.getString(KEY_EMERGENCY_PHONE, "+33 1 42 16 00 01") ?: "+33 1 42 16 00 01"
    )
  }

  fun saveSettings(settings: IvfProtocolSettings) {
    prefs.edit().apply {
      putString(KEY_PROTOCOL_NAME, settings.protocolName)
      putString(KEY_DAILY_DOSAGES, settings.dailyDosages)
      putString(KEY_CYCLE_DAY_1_DATE, settings.cycleDay1Date)
      putLong(KEY_CYCLE_DAY_1_MS, settings.cycleDay1Ms)
      putBoolean(KEY_MORNING_REMINDER_ENABLED, settings.morningReminderEnabled)
      putString(KEY_MORNING_REMINDER_TIME, settings.morningReminderTime)
      putBoolean(KEY_EVENING_REMINDER_ENABLED, settings.eveningReminderEnabled)
      putString(KEY_EVENING_REMINDER_TIME, settings.eveningReminderTime)
      putString(KEY_CLINIC_PHONE, settings.clinicPhone)
      putString(KEY_COORDINATOR_EMAIL, settings.coordinatorEmail)
      putString(KEY_CLINIC_NAME, settings.clinicName)
      putString(KEY_EMERGENCY_PHONE, settings.emergencyPhone)
      apply()
    }
    _settingsFlow.value = settings
  }

  fun updateClinicContact(phone: String, email: String, name: String = "") {
    val current = _settingsFlow.value
    val updated = current.copy(
      clinicPhone = phone,
      coordinatorEmail = email,
      clinicName = if (name.isNotBlank()) name else current.clinicName
    )
    saveSettings(updated)
  }

  fun updateReminders(morning: Boolean, morningTime: String, evening: Boolean, eveningTime: String) {
    val current = _settingsFlow.value
    val updated = current.copy(
      morningReminderEnabled = morning,
      morningReminderTime = morningTime,
      eveningReminderEnabled = evening,
      eveningReminderTime = eveningTime
    )
    saveSettings(updated)
  }
}
