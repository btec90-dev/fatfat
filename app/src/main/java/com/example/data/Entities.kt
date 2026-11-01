package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_logs")
data class WeightLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val dateMs: Long,
  val weightKg: Float,
  val notes: String = ""
)

@Entity(tableName = "kick_sessions")
data class KickSessionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val dateMs: Long,
  val durationSec: Int,
  val kickCount: Int,
  val notes: String = ""
)

@Entity(tableName = "injections")
data class InjectionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val dateMs: Long,
  val medicationName: String, // ex: Progestérone PIO, Lovenox
  val dose: String, // ex: 50mg / 1ml
  val site: String, // ex: Ventre gauche, Cuisse droite
  val notes: String = "",
  val isTaken: Boolean = true
)

@Entity(tableName = "ultrasounds")
data class UltrasoundEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val dateMs: Long,
  val trimester: String, // T1, T2, T3, Précoce
  val title: String,
  val notes: String,
  val imagePath: String = ""
)

@Entity(tableName = "journal_entries")
data class JournalEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val dateMs: Long,
  val mood: String, // 😊, 😌, 😐, 🥱, 😭
  val symptoms: String, // comma separated: Nausées, Fatigue, Tiraillements...
  val notes: String
)

@Entity(tableName = "pregnancy_settings")
data class PregnancySettingsEntity(
  @PrimaryKey val id: Int = 1,
  val lmpDateMs: Long, // Dernières règles (DDR)
  val conceptionDateMs: Long, // Date de conception / transfert
  val babyName: String = "Bébé",
  val motherName: String = "Fatou"
)

@Entity(tableName = "medical_appointments")
data class MedicalAppointmentEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val weeksRange: String, // ex: "Semaines 8 à 10"
  val title: String, // ex: "Consultation OBGYN"
  val location: String, // ex: "Cabinet du Gynécologue-Obstétricien"
  val scheduledDateStr: String, // ex: "2026-12-28" (modifiable)
  val scheduledTimeStr: String = "14:00", // ex: "14:00" (modifiable)
  val datePeriodDisplay: String, // ex: "Fin Déc. 2026 – Début Janv. 2027"
  val description: String,
  val keyExams: String, // Comma or newline separated
  val doctorName: String = "Dr. Moreau",
  val notes: String = "",
  val isCompleted: Boolean = false
)

@Entity(tableName = "blood_pressure_logs")
data class BloodPressureLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val dateMs: Long,
  val systolic: Int, // ex: 118 mmHg
  val diastolic: Int, // ex: 76 mmHg
  val pulse: Int = 72, // bpm
  val notes: String = ""
)

@Entity(tableName = "blood_glucose_logs")
data class BloodGlucoseLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val dateMs: Long,
  val glucoseMgDl: Float, // ex: 85.0 mg/dL
  val contextMeal: String, // "FASTING", "POST_1H", "POST_2H", "RANDOM"
  val notes: String = ""
)

@Entity(tableName = "baby_activity_logs")
data class BabyActivityLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val dateMs: Long,
  val activityType: String, // "BREASTFEEDING", "FORMULA", "PUMPING", "DIAPER"
  val durationMinutes: Int = 0, // for breastfeeding or pumping
  val subType: String = "", // e.g. "LEFT", "RIGHT", "BOTH" or diaper "WET", "DIRTY", "BOTH"
  val amountMl: Float = 0f, // for formula feeding or pumping
  val notes: String = ""
)

