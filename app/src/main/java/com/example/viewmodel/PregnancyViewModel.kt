package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class PregnancyViewModel(application: Application) : AndroidViewModel(application) {
  private val dao = AppDatabase.getDatabase(application).pregnancyDao()

  val settings: StateFlow<PregnancySettingsEntity?> = dao.getSettings()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val weights: StateFlow<List<WeightLogEntity>> = dao.getAllWeights()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val kicks: StateFlow<List<KickSessionEntity>> = dao.getAllKicks()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val injections: StateFlow<List<InjectionEntity>> = dao.getAllInjections()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val ultrasounds: StateFlow<List<UltrasoundEntity>> = dao.getAllUltrasounds()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val journals: StateFlow<List<JournalEntity>> = dao.getAllJournals()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val appointments: StateFlow<List<MedicalAppointmentEntity>> = dao.getAllAppointments()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val bloodPressures: StateFlow<List<BloodPressureLogEntity>> = dao.getAllBloodPressures()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val bloodGlucoses: StateFlow<List<BloodGlucoseLogEntity>> = dao.getAllBloodGlucoses()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val babyActivities: StateFlow<List<BabyActivityLogEntity>> = dao.getAllBabyActivities()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val ivfSettingsManager = IvfSettingsManager.getInstance(application)
  val ivfSettings: StateFlow<IvfProtocolSettings> = ivfSettingsManager.settingsFlow

  fun saveIvfSettings(settings: IvfProtocolSettings) {
    ivfSettingsManager.saveSettings(settings)
    com.example.util.NotificationHelper.scheduleDailyMedicationReminders(
      context = getApplication(),
      morningEnabled = settings.morningReminderEnabled,
      morningTime = settings.morningReminderTime,
      eveningEnabled = settings.eveningReminderEnabled,
      eveningTime = settings.eveningReminderTime,
      protocolSummary = settings.dailyDosages
    )
  }

  fun updateClinicContact(phone: String, email: String, name: String = "") {
    ivfSettingsManager.updateClinicContact(phone, email, name)
  }

  fun toggleMorningReminder(enabled: Boolean) {
    val current = ivfSettings.value
    val updated = current.copy(morningReminderEnabled = enabled)
    saveIvfSettings(updated)
  }

  fun toggleEveningReminder(enabled: Boolean) {
    val current = ivfSettings.value
    val updated = current.copy(eveningReminderEnabled = enabled)
    saveIvfSettings(updated)
  }

  fun testMedicationNotification(isMorning: Boolean) {
    val settings = ivfSettings.value
    val title = if (isMorning) "⏰ Test Rappel PMA : Matin (${settings.morningReminderTime})" else "🌙 Test Rappel PMA : Soir (${settings.eveningReminderTime})"
    val message = "Protocole : ${settings.protocolName}\nDosages : ${settings.dailyDosages}"
    com.example.util.NotificationHelper.sendNotification(getApplication(), title, message, if (isMorning) 101 else 102)
  }

  fun updateSettings(lmpMs: Long, conceptionMs: Long, babyName: String) {
    viewModelScope.launch {
      dao.saveSettings(PregnancySettingsEntity(id = 1, lmpDateMs = lmpMs, conceptionDateMs = conceptionMs, babyName = babyName))
    }
  }

  fun addWeight(weightKg: Float, notes: String) {
    viewModelScope.launch {
      dao.insertWeight(WeightLogEntity(dateMs = System.currentTimeMillis(), weightKg = weightKg, notes = notes))
    }
  }

  fun deleteWeight(weight: WeightLogEntity) {
    viewModelScope.launch { dao.deleteWeight(weight) }
  }

  fun addKickSession(durationSec: Int, kickCount: Int, notes: String) {
    viewModelScope.launch {
      dao.insertKick(KickSessionEntity(dateMs = System.currentTimeMillis(), durationSec = durationSec, kickCount = kickCount, notes = notes))
    }
  }

  fun deleteKick(kick: KickSessionEntity) {
    viewModelScope.launch { dao.deleteKick(kick) }
  }

  fun addInjection(medicationName: String, dose: String, site: String, notes: String, isTaken: Boolean) {
    viewModelScope.launch {
      dao.insertInjection(InjectionEntity(dateMs = System.currentTimeMillis(), medicationName = medicationName, dose = dose, site = site, notes = notes, isTaken = isTaken))
    }
  }

  fun deleteInjection(injection: InjectionEntity) {
    viewModelScope.launch { dao.deleteInjection(injection) }
  }

  fun addUltrasound(trimester: String, title: String, notes: String, imagePath: String) {
    viewModelScope.launch {
      dao.insertUltrasound(UltrasoundEntity(dateMs = System.currentTimeMillis(), trimester = trimester, title = title, notes = notes, imagePath = imagePath))
    }
  }

  fun updateUltrasound(ultrasound: UltrasoundEntity) {
    viewModelScope.launch {
      dao.insertUltrasound(ultrasound)
    }
  }

  fun saveUltrasound(
    id: Long = 0L,
    dateMs: Long = System.currentTimeMillis(),
    trimester: String,
    title: String,
    notes: String,
    imagePath: String
  ) {
    viewModelScope.launch {
      dao.insertUltrasound(
        UltrasoundEntity(
          id = id,
          dateMs = dateMs,
          trimester = trimester,
          title = title,
          notes = notes,
          imagePath = imagePath
        )
      )
    }
  }

  fun deleteUltrasound(ultrasound: UltrasoundEntity) {
    viewModelScope.launch { dao.deleteUltrasound(ultrasound) }
  }

  fun addJournal(mood: String, symptoms: String, notes: String) {
    viewModelScope.launch {
      dao.insertJournal(JournalEntity(dateMs = System.currentTimeMillis(), mood = mood, symptoms = symptoms, notes = notes))
    }
  }

  fun deleteJournal(journal: JournalEntity) {
    viewModelScope.launch { dao.deleteJournal(journal) }
  }

  // Medical Appointments Management (Editable Calendar)
  fun saveAppointment(appointment: MedicalAppointmentEntity) {
    viewModelScope.launch {
      if (appointment.id == 0L) {
        dao.insertAppointment(appointment)
      } else {
        dao.updateAppointment(appointment)
      }
    }
  }

  fun updateAppointmentDateAndTime(appointment: MedicalAppointmentEntity, newDateStr: String, newTimeStr: String, newNotes: String) {
    viewModelScope.launch {
      dao.updateAppointment(
        appointment.copy(
          scheduledDateStr = newDateStr,
          scheduledTimeStr = newTimeStr,
          notes = newNotes
        )
      )
    }
  }

  fun toggleAppointmentCompleted(appointment: MedicalAppointmentEntity) {
    viewModelScope.launch {
      dao.updateAppointment(appointment.copy(isCompleted = !appointment.isCompleted))
    }
  }

  fun deleteAppointment(appointment: MedicalAppointmentEntity) {
    viewModelScope.launch { dao.deleteAppointment(appointment) }
  }

  // Vital Signs Tracking (Blood Pressure & Blood Glucose)
  fun addBloodPressure(systolic: Int, diastolic: Int, pulse: Int, notes: String = "") {
    viewModelScope.launch {
      dao.insertBloodPressure(
        BloodPressureLogEntity(
          dateMs = System.currentTimeMillis(),
          systolic = systolic,
          diastolic = diastolic,
          pulse = pulse,
          notes = notes
        )
      )
    }
  }

  fun deleteBloodPressure(bp: BloodPressureLogEntity) {
    viewModelScope.launch { dao.deleteBloodPressure(bp) }
  }

  fun addBloodGlucose(glucoseMgDl: Float, contextMeal: String, notes: String = "") {
    viewModelScope.launch {
      dao.insertBloodGlucose(
        BloodGlucoseLogEntity(
          dateMs = System.currentTimeMillis(),
          glucoseMgDl = glucoseMgDl,
          contextMeal = contextMeal,
          notes = notes
        )
      )
    }
  }

  fun deleteBloodGlucose(bg: BloodGlucoseLogEntity) {
    viewModelScope.launch { dao.deleteBloodGlucose(bg) }
  }

  fun addBabyActivity(
    activityType: String,
    durationMinutes: Int = 0,
    subType: String = "",
    amountMl: Float = 0f,
    notes: String = "",
    dateMs: Long = System.currentTimeMillis()
  ) {
    viewModelScope.launch {
      dao.insertBabyActivity(
        BabyActivityLogEntity(
          dateMs = dateMs,
          activityType = activityType,
          durationMinutes = durationMinutes,
          subType = subType,
          amountMl = amountMl,
          notes = notes
        )
      )
    }
  }

  fun deleteBabyActivity(activity: BabyActivityLogEntity) {
    viewModelScope.launch { dao.deleteBabyActivity(activity) }
  }

  // Helper computations
  fun calculateCurrentGestationalWeek(lmpMs: Long?): Pair<Int, Int> {
    if (lmpMs == null) return Pair(1, 0)
    val diffMs = System.currentTimeMillis() - lmpMs
    val days = (diffMs / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(0)
    val weeks = days / 7
    val extraDays = days % 7
    return Pair(weeks.coerceIn(1, 42), extraDays)
  }

  fun calculateDueDate(lmpMs: Long?): Long {
    if (lmpMs == null) return System.currentTimeMillis() + (200L * 24 * 60 * 60 * 1000L)
    // Naegele's rule approximation: LMP + 280 days (40 weeks)
    return lmpMs + (280L * 24 * 60 * 60 * 1000L)
  }
}
