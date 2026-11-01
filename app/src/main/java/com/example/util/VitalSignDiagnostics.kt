package com.example.util

enum class DiagnosticSeverity {
  NORMAL,
  ELEVATED,
  WARNING,
  CRITICAL_ALERT
}

data class DiagnosticResult(
  val severity: DiagnosticSeverity,
  val statusTitleFr: String,
  val statusTitleEn: String,
  val badgeTextFr: String,
  val badgeTextEn: String,
  val feedbackFr: String,
  val feedbackEn: String,
  val isPromptDoctorRequired: Boolean = false,
  val guidelineReference: String = "Recommandations ACOG / CDC"
)

object VitalSignDiagnostics {

  /**
   * Evaluates Blood Pressure based on ACOG (American College of Obstetricians and Gynecologists)
   * guidelines for pregnancy and hypertensive disorders:
   * - Normal: < 120 / < 80 mmHg
   * - Elevated: 120-129 / < 80 mmHg
   * - Stage 1 HTN: 130-139 / 80-89 mmHg
   * - Stage 2 Gestational HTN: 140-159 / 90-109 mmHg
   * - Severe Hypertensive Emergency / Preeclampsia alert: >= 160 or >= 110 mmHg
   */
  fun evaluateBloodPressure(systolic: Int, diastolic: Int): DiagnosticResult {
    return when {
      systolic >= 160 || diastolic >= 110 -> {
        DiagnosticResult(
          severity = DiagnosticSeverity.CRITICAL_ALERT,
          statusTitleFr = "🚨 ALERTE CRITIQUE : Hypertension Sévère (ACOG)",
          statusTitleEn = "🚨 CRITICAL ALERT: Severe Hypertension (ACOG)",
          badgeTextFr = "Urgence Maternité",
          badgeTextEn = "Immediate Care",
          feedbackFr = "Tension critique (≥ 160/110 mmHg). Risque élevé de pré-éclampsie sévère. Contactez immédiatement votre maternité, gynécologue ou faites le 15.",
          feedbackEn = "Critical blood pressure (≥ 160/110 mmHg). High risk of severe preeclampsia. Contact your maternity hospital, OBGYN or emergency services immediately.",
          isPromptDoctorRequired = true,
          guidelineReference = "ACOG Practice Bulletin #222 (Gestational HTN & Preeclampsia)"
        )
      }
      systolic >= 140 || diastolic >= 90 -> {
        DiagnosticResult(
          severity = DiagnosticSeverity.WARNING,
          statusTitleFr = "⚠️ Hypertension Gestationnelle (Stade 2)",
          statusTitleEn = "⚠️ Gestational Hypertension (Stage 2)",
          badgeTextFr = "Seuil Pathologique",
          badgeTextEn = "Out of Range",
          feedbackFr = "Valeur hors des limites recommandées (≥ 140/90 mmHg). Reposez-vous 15 min et reprenez la mesure. Si la valeur persiste, contactez votre médecin ou sage-femme.",
          feedbackEn = "Value outside target limits (≥ 140/90 mmHg). Rest for 15 min and re-measure. If persistent, contact your obstetric provider promptly.",
          isPromptDoctorRequired = true,
          guidelineReference = "ACOG Guidelines"
        )
      }
      systolic in 130..139 || diastolic in 80..89 -> {
        DiagnosticResult(
          severity = DiagnosticSeverity.WARNING,
          statusTitleFr = "Tension Limite - Stade 1 (À surveiller)",
          statusTitleEn = "Borderline - Stage 1 (Monitor closely)",
          badgeTextFr = "Surveillance",
          badgeTextEn = "Monitor",
          feedbackFr = "Tension légèrement augmentée (130-139 / 80-89 mmHg). Surveillez l'apparition d'autres symptômes (maux de tête, œdèmes, mouches devant les yeux).",
          feedbackEn = "Mildly elevated (130-139 / 80-89 mmHg). Note any concurrent symptoms such as persistent headaches, visual changes, or sudden swelling.",
          isPromptDoctorRequired = false,
          guidelineReference = "ACOG / CDC Maternal Health"
        )
      }
      systolic in 120..129 && diastolic < 80 -> {
        DiagnosticResult(
          severity = DiagnosticSeverity.ELEVATED,
          statusTitleFr = "Pression Artérielle Élevée",
          statusTitleEn = "Elevated Blood Pressure",
          badgeTextFr = "Modérée",
          badgeTextEn = "Elevated",
          feedbackFr = "Pression systolique légèrement au-dessus de la normale (120-129 mmHg). Pensez à bien vous hydrater et vous reposer.",
          feedbackEn = "Systolic BP slightly above normal baseline. Stay hydrated and rest on your left lateral side.",
          isPromptDoctorRequired = false,
          guidelineReference = "ACOG / CDC Maternal Health"
        )
      }
      else -> {
        DiagnosticResult(
          severity = DiagnosticSeverity.NORMAL,
          statusTitleFr = "Tension Artérielle Optimale",
          statusTitleEn = "Optimal Blood Pressure",
          badgeTextFr = "Normal",
          badgeTextEn = "Normal",
          feedbackFr = "Excellente tension artérielle (< 120/80 mmHg). Circulation et perfusion placentaire optimales.",
          feedbackEn = "Excellent blood pressure (< 120/80 mmHg). Optimal maternal and placental circulation.",
          isPromptDoctorRequired = false,
          guidelineReference = "ACOG Guidelines"
        )
      }
    }
  }

  /**
   * Evaluates Blood Glucose according to ACOG & ADA Gestational Diabetes standards:
   * Context:
   * - Fasting (À jeun): Target <= 95 mg/dL
   * - 1h Post-Prandial: Target <= 140 mg/dL
   * - 2h Post-Prandial: Target <= 120 mg/dL
   * - Random: Target <= 140 mg/dL
   * - Hypoglycemia: < 60 mg/dL
   */
  fun evaluateBloodGlucose(valueMgDl: Float, context: String): DiagnosticResult {
    if (valueMgDl < 60f) {
      return DiagnosticResult(
        severity = DiagnosticSeverity.CRITICAL_ALERT,
        statusTitleFr = "🚨 Hypoglycémie (< 60 mg/dL)",
        statusTitleEn = "🚨 Hypoglycemia (< 60 mg/dL)",
        badgeTextFr = "Hypoglycémie",
        badgeTextEn = "Low Glucose",
        feedbackFr = "Taux de sucre bas. Consommez immédiatement 15g de sucre rapide (jus de fruit, demi-verre d'eau sucrée) et remesurez après 15 min.",
        feedbackEn = "Low blood sugar level. Ingest 15g of fast-acting carbs (fruit juice) immediately and recheck in 15 minutes.",
        isPromptDoctorRequired = true,
        guidelineReference = "ACOG / ADA Gestational Care"
      )
    }

    val (threshold, labelFr, labelEn) = when (context.uppercase()) {
      "FASTING", "A_JEUN" -> Triple(95f, "À jeun (Seuil : ≤ 95 mg/dL)", "Fasting (Target: ≤ 95 mg/dL)")
      "POST_1H", "1H_POST_MEAL" -> Triple(140f, "1h Post-Repas (Seuil : ≤ 140 mg/dL)", "1h Post-Meal (Target: ≤ 140 mg/dL)")
      "POST_2H", "2H_POST_MEAL" -> Triple(120f, "2h Post-Repas (Seuil : ≤ 120 mg/dL)", "2h Post-Meal (Target: ≤ 120 mg/dL)")
      else -> Triple(140f, "Aléatoire (Seuil : ≤ 140 mg/dL)", "Random (Target: ≤ 140 mg/dL)")
    }

    return if (valueMgDl > threshold) {
      val isSeverelyElevated = valueMgDl > threshold + 40f
      DiagnosticResult(
        severity = if (isSeverelyElevated) DiagnosticSeverity.CRITICAL_ALERT else DiagnosticSeverity.WARNING,
        statusTitleFr = if (isSeverelyElevated) "🚨 Hyperglycémie Marquée" else "⚠️ Glycémie Supérieure à l'Objectif",
        statusTitleEn = if (isSeverelyElevated) "🚨 Marked Hyperglycemia" else "⚠️ Glucose Above Target",
        badgeTextFr = "Hors Cibles",
        badgeTextEn = "Above Target",
        feedbackFr = "$labelFr : Valeur enregistrée (${valueMgDl.toInt()} mg/dL) au-delà des objectifs ACOG. Notez ce repas et parlez-en à votre diabétologue ou sage-femme.",
        feedbackEn = "$labelEn: Recorded value (${valueMgDl.toInt()} mg/dL) exceeds ACOG targets. Log your meal and notify your care provider.",
        isPromptDoctorRequired = true,
        guidelineReference = "ACOG Practice Bulletin #190 (Gestational Diabetes Mellitus)"
      )
    } else {
      DiagnosticResult(
        severity = DiagnosticSeverity.NORMAL,
        statusTitleFr = "Glycémie dans les Objectifs",
        statusTitleEn = "Glucose On Target",
        badgeTextFr = "Dans la Cible",
        badgeTextEn = "In Target",
        feedbackFr = "$labelFr : Parfait (${valueMgDl.toInt()} mg/dL). Le métabolisme glucidique est bien équilibré pour vous et bébé.",
        feedbackEn = "$labelEn: Perfect (${valueMgDl.toInt()} mg/dL). Glucose metabolism is balanced for mother and baby.",
        isPromptDoctorRequired = false,
        guidelineReference = "ACOG / ADA Guidelines"
      )
    }
  }

  /**
   * Evaluates Weight Progression:
   * Rapid weight gain (> 1.5 kg in < 7 days in 2nd/3rd trimester) can be a hallmark sign
   * of pathological fluid retention / preeclampsia.
   */
  fun evaluateWeightGain(currentWeight: Float, previousWeight: Float?, daysDiff: Long, trimester: Int): DiagnosticResult {
    if (previousWeight == null || daysDiff <= 0) {
      return DiagnosticResult(
        severity = DiagnosticSeverity.NORMAL,
        statusTitleFr = "Poids Enregistré",
        statusTitleEn = "Weight Logged",
        badgeTextFr = "Mesure Initiale",
        badgeTextEn = "Baseline",
        feedbackFr = "Poids actuel : $currentWeight kg. Continuez le suivi régulier chaque semaine au réveil.",
        feedbackEn = "Current weight: $currentWeight kg. Continue logging weekly morning weights.",
        isPromptDoctorRequired = false
      )
    }

    val diffKg = currentWeight - previousWeight
    // Excessive rapid gain check: > 1.5 kg in less than 8 days
    if (trimester >= 2 && diffKg >= 1.5f && daysDiff <= 8) {
      return DiagnosticResult(
        severity = DiagnosticSeverity.WARNING,
        statusTitleFr = "⚠️ Prise de Poids Rapide (+${String.format("%.1f", diffKg)} kg en ${daysDiff}j)",
        statusTitleEn = "⚠️ Rapid Weight Gain (+${String.format("%.1f", diffKg)} kg in ${daysDiff}d)",
        badgeTextFr = "Alerte Rétention",
        badgeTextEn = "Fluid Retention",
        feedbackFr = "Une prise de poids rapide (> 1,5 kg en une semaine) au 2e/3e trimestre peut signaler une rétention d'eau importante liée à la pré-éclampsie. Vérifiez votre tension et contactez votre médecin si vous avez des œdèmes ou maux de tête.",
        feedbackEn = "Rapid weight gain (> 1.5 kg in a single week) in T2/T3 may reflect acute fluid retention associated with preeclampsia. Check your blood pressure and notify your provider if swelling occurs.",
        isPromptDoctorRequired = true,
        guidelineReference = "CDC / ACOG Maternal Weight Guidelines"
      )
    }

    return DiagnosticResult(
      severity = DiagnosticSeverity.NORMAL,
      statusTitleFr = "Évolution Pondérale Régulière",
      statusTitleEn = "Normal Weight Trajectory",
      badgeTextFr = "Progression Saine",
      badgeTextEn = "Healthy Gain",
      feedbackFr = "Variation de ${if (diffKg >= 0) "+" else ""}${String.format("%.1f", diffKg)} kg en $daysDiff jours. Conforme aux courbes de croissance saines recommandées.",
      feedbackEn = "Change of ${if (diffKg >= 0) "+" else ""}${String.format("%.1f", diffKg)} kg over $daysDiff days. Consistent with healthy prenatal weight curve.",
      isPromptDoctorRequired = false,
      guidelineReference = "IOM / ACOG Guidelines"
    )
  }
}
