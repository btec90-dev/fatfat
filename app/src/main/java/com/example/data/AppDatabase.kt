package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
  entities = [
    WeightLogEntity::class,
    KickSessionEntity::class,
    InjectionEntity::class,
    UltrasoundEntity::class,
    JournalEntity::class,
    PregnancySettingsEntity::class,
    MedicalAppointmentEntity::class,
    BloodPressureLogEntity::class,
    BloodGlucoseLogEntity::class,
    BabyActivityLogEntity::class
  ],
  version = 4,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun pregnancyDao(): PregnancyDao

  companion object {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "pregnancy_database"
        )
          .fallbackToDestructiveMigration()
          .addCallback(DatabaseCallback())
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          CoroutineScope(Dispatchers.IO).launch {
            populateInitialData(database.pregnancyDao())
          }
        }
      }

      suspend fun populateInitialData(dao: PregnancyDao) {
        val now = System.currentTimeMillis()
        val day = 24 * 60 * 60 * 1000L

        // Settings (DPA in ~200 days)
        dao.saveSettings(
          PregnancySettingsEntity(
            id = 1,
            lmpDateMs = now - (60L * day), // ~8 semaines d'aménorrhée
            conceptionDateMs = now - (46L * day),
            babyName = "Bébé",
            motherName = "Fatou"
          )
        )

        // Weight logs
        dao.insertWeight(WeightLogEntity(dateMs = now - 14L * day, weightKg = 62.4f, notes = "Premier trimestre stable"))
        dao.insertWeight(WeightLogEntity(dateMs = now - 7L * day, weightKg = 62.8f, notes = "Léger appétit retrouvé"))
        dao.insertWeight(WeightLogEntity(dateMs = now, weightKg = 63.2f, notes = "Semaine 9, tout va bien"))

        // Kick sessions
        dao.insertKick(KickSessionEntity(dateMs = now - 2L * day, durationSec = 300, kickCount = 12, notes = "Très actif après le goûter"))
        dao.insertKick(KickSessionEntity(dateMs = now, durationSec = 240, kickCount = 10, notes = "Petits coups doux le matin"))

        // Injections
        dao.insertInjection(InjectionEntity(dateMs = now - 1L * day, medicationName = "Progestérone (PIO)", dose = "50mg / 1ml", site = "Fesse gauche", notes = "Bien toléré", isTaken = true))
        dao.insertInjection(InjectionEntity(dateMs = now, medicationName = "Progestérone (PIO)", dose = "50mg / 1ml", site = "Fesse droite", notes = "Appliquer de la glace après", isTaken = false))

        // Ultrasounds
        dao.insertUltrasound(UltrasoundEntity(dateMs = now - 30L * day, trimester = "Précoce", title = "Échographie de viabilité (8 SA)", notes = "Activité cardiaque bien visible, l'embryon mesure 15mm."))

        // Journals
        dao.insertJournal(JournalEntity(dateMs = now, mood = "😊", symptoms = "Fatigue, Légères nausées matinales", notes = "Journée calme, promenade de 20 minutes au soleil."))

        // Editable Medical Appointments Seed (8 Key Milestones from official roadmap)
        val initialAppointments = listOf(
          MedicalAppointmentEntity(
            weeksRange = "Semaines 8 à 10",
            title = "Consultation OBGYN",
            location = "Cabinet du Gynécologue-Obstétricien",
            scheduledDateStr = "2026-12-28",
            scheduledTimeStr = "14:30",
            datePeriodDisplay = "Fin Déc. 2026 – Début Janv. 2027",
            description = "Transition officielle du centre de fertilité vers le suivi de grossesse standard. Bilan de sortie RMA, transmission du dossier médical complet, et arrêt progressif sous surveillance du soutien hormonal (progestérone / œstrogènes).",
            keyExams = "Échographie de transition\nExamen clinique complet\nArrêt progressif des hormones de soutien",
            doctorName = "Dr. Moreau (OBGYN)",
            notes = "Apporter les ordonnances RMA et résultats Bêta-hCG",
            isCompleted = false
          ),
          MedicalAppointmentEntity(
            weeksRange = "Semaines 11 à 13",
            title = "Échographie de Clarté Nucale (NT Scan) & Dépistage 1er Trimestre",
            location = "Centre d'imagerie obstétricale / Hôpital",
            scheduledDateStr = "2027-01-14",
            scheduledTimeStr = "10:00",
            datePeriodDisplay = "Janvier 2027",
            description = "Échographie morphologique précoce pour mesurer la nuque du fœtus, vérifier la présence des os nasaux et exclure les anomalies chromosomiques majeures, combinée à une prise de sang (marqueurs sériques).",
            keyExams = "Échographie NT (Clarté nucale)\nPrise de sang de dépistage prénatal\nÉvaluation du risque combiné",
            doctorName = "Dr. Lambert (Échographiste référent)",
            notes = "Venir vessie semi-pleine, dossier sérologie complet",
            isCompleted = false
          ),
          MedicalAppointmentEntity(
            weeksRange = "Semaines 16 à 18",
            title = "Visite de routine du 2ème Trimestre & Échographie de contrôle",
            location = "Cabinet OBGYN",
            scheduledDateStr = "2027-02-12",
            scheduledTimeStr = "15:00",
            datePeriodDisplay = "Février 2027",
            description = "Consultation mensuelle standard : écoute des bruits du cœur fœtal (Doppler), mesure de la hauteur utérine, contrôle de la tension artérielle et du poids. Discussion des premiers mouvements de bébé.",
            keyExams = "Doppler cardiaque fœtal\nMesure hauteur utérine\nContrôle tension & analyse d'urines",
            doctorName = "Dr. Moreau (OBGYN)",
            notes = "Vérifier tension artérielle au repos",
            isCompleted = false
          ),
          MedicalAppointmentEntity(
            weeksRange = "Semaines 20 à 22",
            title = "Grande Échographie Morphologique (Anatomy Scan)",
            location = "Centre d'imagerie obstétricale spécialisé",
            scheduledDateStr = "2027-03-05",
            scheduledTimeStr = "11:15",
            datePeriodDisplay = "Février – Mars 2027",
            description = "L'examen clé de la grossesse ! Examen minutieux de chaque organe du bébé (cerveau, cœur, reins, colonne, membres), vérification du placenta, de la quantité de liquide amniotique et de la longueur du col utérin.",
            keyExams = "Échographie morphologique détaillée (3D/4D possible)\nMesure du col utérin (longueur)\nVérification emplacement placenta",
            doctorName = "Dr. Lambert (Échographiste référent)",
            notes = "Durée prévue 45 minutes, co-parent bienvenu",
            isCompleted = false
          ),
          MedicalAppointmentEntity(
            weeksRange = "Semaines 24 à 28",
            title = "Test de Glycémie (HGPO) & Visite du 6ème mois",
            location = "Laboratoire d'analyses & Cabinet OBGYN",
            scheduledDateStr = "2027-04-02",
            scheduledTimeStr = "08:00",
            datePeriodDisplay = "Mars – Avril 2027",
            description = "Dépistage du diabète gestationnel (test d'hyperglycémie provoquée par voie orale avec glucose 50g ou 75g). Si Fatou est de rhesus négatif, injection de Rhogam préventive.",
            keyExams = "Test HGPO (Glycémie post-glucose)\nBilan sanguin NFS / Ferritine\nInjection de Rhogam (si nécessaire)",
            doctorName = "Laboratoire BioSanté / Dr. Moreau",
            notes = "Être strictement à jeun depuis minuit",
            isCompleted = false
          ),
          MedicalAppointmentEntity(
            weeksRange = "Semaines 30 à 32",
            title = "Échographie de Croissance du 3ème Trimestre",
            location = "Centre d'imagerie obstétricale",
            scheduledDateStr = "2027-04-28",
            scheduledTimeStr = "14:00",
            datePeriodDisplay = "Avril 2027",
            description = "Vérification de la courbe de croissance fœtale (poids estimé, diamètres bipariétaux), de la vascularisation utérine (Doppler ombilical) et de la position du bébé (tête en bas / siège).",
            keyExams = "Échographie de croissance fœtale\nDoppler ombilical et utérin\nEstimation du poids de naissance",
            doctorName = "Dr. Lambert",
            notes = "Estimation biométrique du percentile de poids",
            isCompleted = false
          ),
          MedicalAppointmentEntity(
            weeksRange = "Semaines 34 à 36",
            title = "Consultations bimensuelles & Dépistage GBS",
            location = "Cabinet OBGYN & Maternité",
            scheduledDateStr = "2027-05-18",
            scheduledTimeStr = "16:00",
            datePeriodDisplay = "Mai 2027",
            description = "Les visites deviennent bi-mensuelles. Prélèvement vaginal pour le dépistage du streptocoque du groupe B (GBS). Discussion approfondie du plan de naissance et des signes de début de travail.",
            keyExams = "Prélèvement vaginal GBS\nMonitoring fœtal (RCF) de routine\nÉlaboration du plan d'accouchement",
            doctorName = "Dr. Moreau",
            notes = "Apporter le projet de naissance rempli",
            isCompleted = false
          ),
          MedicalAppointmentEntity(
            weeksRange = "Semaines 37 à 40+",
            title = "Visites hebdomadaires & ARRIVÉE DE BÉBÉ 🎉",
            location = "Maternité / Hôpital d'accouchement",
            scheduledDateStr = "2027-06-08",
            scheduledTimeStr = "09:30",
            datePeriodDisplay = "Juin 2027",
            description = "Suivi hebdomadaire avec contrôles du col et monitoring (RCF). Attente sereine du déclenchement naturel du travail ou de la date terme. C'est le grand moment de l'accueil de votre bébé !",
            keyExams = "Examens du col hebdomadaires\nMonitorets de bien-être fœtal (NST)\nAccouchement et premiers câlins en peau à peau ♡",
            doctorName = "Équipe Maternité",
            notes = "Valise de maternité prête dans la voiture !",
            isCompleted = false
          )
        )
        dao.insertAppointments(initialAppointments)

        // Initial Vital Signs (Blood Pressure & Blood Glucose)
        dao.insertBloodPressure(BloodPressureLogEntity(dateMs = now - 2L * day, systolic = 116, diastolic = 74, pulse = 72, notes = "Au repos le matin"))
        dao.insertBloodPressure(BloodPressureLogEntity(dateMs = now, systolic = 118, diastolic = 76, pulse = 74, notes = "Tension optimale"))

        dao.insertBloodGlucose(BloodGlucoseLogEntity(dateMs = now - 1L * day, glucoseMgDl = 84.0f, contextMeal = "FASTING", notes = "À jeun au réveil"))
        dao.insertBloodGlucose(BloodGlucoseLogEntity(dateMs = now, glucoseMgDl = 112.0f, contextMeal = "POST_2H", notes = "2h après déjeuner équilibré"))
      }
    }
  }
}
