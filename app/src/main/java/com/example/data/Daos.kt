package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PregnancyDao {
  // Weight
  @Query("SELECT * FROM weight_logs ORDER BY dateMs DESC")
  fun getAllWeights(): Flow<List<WeightLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWeight(weight: WeightLogEntity)

  @Delete
  suspend fun deleteWeight(weight: WeightLogEntity)

  // Kicks
  @Query("SELECT * FROM kick_sessions ORDER BY dateMs DESC")
  fun getAllKicks(): Flow<List<KickSessionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertKick(kick: KickSessionEntity)

  @Delete
  suspend fun deleteKick(kick: KickSessionEntity)

  // Injections
  @Query("SELECT * FROM injections ORDER BY dateMs DESC")
  fun getAllInjections(): Flow<List<InjectionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInjection(injection: InjectionEntity)

  @Delete
  suspend fun deleteInjection(injection: InjectionEntity)

  // Ultrasounds
  @Query("SELECT * FROM ultrasounds ORDER BY dateMs DESC")
  fun getAllUltrasounds(): Flow<List<UltrasoundEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUltrasound(ultrasound: UltrasoundEntity)

  @Delete
  suspend fun deleteUltrasound(ultrasound: UltrasoundEntity)

  // Journal
  @Query("SELECT * FROM journal_entries ORDER BY dateMs DESC")
  fun getAllJournals(): Flow<List<JournalEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertJournal(journal: JournalEntity)

  @Delete
  suspend fun deleteJournal(journal: JournalEntity)

  // Settings
  @Query("SELECT * FROM pregnancy_settings WHERE id = 1")
  fun getSettings(): Flow<PregnancySettingsEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveSettings(settings: PregnancySettingsEntity)

  // Medical Appointments / Consultations (Editable)
  @Query("SELECT * FROM medical_appointments ORDER BY id ASC")
  fun getAllAppointments(): Flow<List<MedicalAppointmentEntity>>

  @Query("SELECT COUNT(*) FROM medical_appointments")
  suspend fun getAppointmentCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAppointment(appointment: MedicalAppointmentEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAppointments(appointments: List<MedicalAppointmentEntity>)

  @androidx.room.Update
  suspend fun updateAppointment(appointment: MedicalAppointmentEntity)

  @Delete
  suspend fun deleteAppointment(appointment: MedicalAppointmentEntity)

  // Blood Pressure Logs
  @Query("SELECT * FROM blood_pressure_logs ORDER BY dateMs DESC")
  fun getAllBloodPressures(): Flow<List<BloodPressureLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBloodPressure(bp: BloodPressureLogEntity)

  @Delete
  suspend fun deleteBloodPressure(bp: BloodPressureLogEntity)

  // Blood Glucose Logs
  @Query("SELECT * FROM blood_glucose_logs ORDER BY dateMs DESC")
  fun getAllBloodGlucoses(): Flow<List<BloodGlucoseLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBloodGlucose(bg: BloodGlucoseLogEntity)

  @Delete
  suspend fun deleteBloodGlucose(bg: BloodGlucoseLogEntity)

  // Baby Activity Logs
  @Query("SELECT * FROM baby_activity_logs ORDER BY dateMs DESC")
  fun getAllBabyActivities(): Flow<List<BabyActivityLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBabyActivity(activity: BabyActivityLogEntity)

  @Delete
  suspend fun deleteBabyActivity(activity: BabyActivityLogEntity)
}
