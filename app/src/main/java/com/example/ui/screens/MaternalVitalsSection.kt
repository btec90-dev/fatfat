package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BloodGlucoseLogEntity
import com.example.data.BloodPressureLogEntity
import com.example.data.WeightLogEntity
import com.example.util.DiagnosticResult
import com.example.util.DiagnosticSeverity
import com.example.util.VitalSignDiagnostics
import com.example.viewmodel.PregnancyViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun MaternalVitalsSection(
  viewModel: PregnancyViewModel,
  isEnglish: Boolean = false,
  onNavigateToSettings: () -> Unit = {}
) {
  val context = LocalContext.current
  val ivfSettings by viewModel.ivfSettings.collectAsState()
  val bloodPressures by viewModel.bloodPressures.collectAsState()
  val bloodGlucoses by viewModel.bloodGlucoses.collectAsState()
  val weights by viewModel.weights.collectAsState()
  val settings by viewModel.settings.collectAsState()

  val lmp = settings?.lmpDateMs ?: (System.currentTimeMillis() - 60L * 24 * 3600 * 1000L)
  val (weeks, _) = viewModel.calculateCurrentGestationalWeek(lmp)
  val currentTrimester = when {
    weeks <= 13 -> 1
    weeks <= 26 -> 2
    else -> 3
  }

  // Dialog States
  var showAddBpDialog by remember { mutableStateOf(false) }
  var showAddGlucoseDialog by remember { mutableStateOf(false) }
  var showAddWeightDialog by remember { mutableStateOf(false) }

  // Most recent vital diagnostics
  val latestBp = bloodPressures.firstOrNull()
  val latestGlucose = bloodGlucoses.firstOrNull()
  val latestWeight = weights.firstOrNull()
  val previousWeight = if (weights.size > 1) weights[1] else null

  val bpDiag = latestBp?.let { VitalSignDiagnostics.evaluateBloodPressure(it.systolic, it.diastolic) }
  val glucoseDiag = latestGlucose?.let { VitalSignDiagnostics.evaluateBloodGlucose(it.glucoseMgDl, it.contextMeal) }
  val weightDiag = latestWeight?.let {
    val daysDiff = if (previousWeight != null) {
      TimeUnit.MILLISECONDS.toDays(it.dateMs - previousWeight.dateMs).coerceAtLeast(1)
    } else 0
    VitalSignDiagnostics.evaluateWeightGain(it.weightKg, previousWeight?.weightKg, daysDiff, currentTrimester)
  }

  // Check if any vital sign requires alerting the doctor
  val isAnyAlertActive = (bpDiag?.isPromptDoctorRequired == true) ||
                         (glucoseDiag?.isPromptDoctorRequired == true) ||
                         (weightDiag?.isPromptDoctorRequired == true)

  fun callClinic() {
    val phone = ivfSettings.clinicPhone.trim()
    if (phone.isNotBlank()) {
      val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
      context.startActivity(intent)
    } else {
      Toast.makeText(
        context,
        if (isEnglish) "Clinic phone number not set. Opening Settings." else "Numéro de clinique non configuré. Redirection vers les Réglages.",
        Toast.LENGTH_SHORT
      ).show()
      onNavigateToSettings()
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header Hero Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
              text = if (isEnglish) "Maternal Health & Vital Diagnostics" else "Santé Maternelle & Diagnostic des Constantes",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
          Text(
            text = if (isEnglish) {
              "Automated clinical thresholds aligned with ACOG and CDC maternal guidelines for Blood Pressure, Blood Glucose, and Weight Gain monitoring."
            } else {
              "Suivi clinique automatique calibré sur les recommandations ACOG et CDC pour la Tension Artérielle, la Glycémie et la prise de poids gestationnelle."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
          )
        }
      }
    }

    // AUTOMATIC ACOG/CDC ALERT BANNER (If any vital is out of range)
    if (isAnyAlertActive) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("maternal_alert_banner"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
          )
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
              )
              Text(
                text = if (isEnglish) "ACOG / CDC Alert: Out of Range Values" else "Alerte ACOG / CDC : Valeurs Hors Limites",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
            }

            if (bpDiag?.isPromptDoctorRequired == true) {
              Text(
                text = "• " + (if (isEnglish) bpDiag.feedbackEn else bpDiag.feedbackFr),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
            }
            if (glucoseDiag?.isPromptDoctorRequired == true) {
              Text(
                text = "• " + (if (isEnglish) glucoseDiag.feedbackEn else glucoseDiag.feedbackFr),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
            }
            if (weightDiag?.isPromptDoctorRequired == true) {
              Text(
                text = "• " + (if (isEnglish) weightDiag.feedbackEn else weightDiag.feedbackFr),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
            }

            Button(
              onClick = { callClinic() },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("alert_call_clinic_button")
            ) {
              Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(8.dp))
              Text(
                text = if (isEnglish) "Contact Clinic / Healthcare Provider" else "Contacter la Maternité / Clinique",
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // SECTION 1: BLOOD PRESSURE PANEL
    item {
      VitalSignCard(
        title = if (isEnglish) "Blood Pressure (BP)" else "Tension Artérielle (TA)",
        icon = Icons.Default.Favorite,
        latestValueText = latestBp?.let { "${it.systolic}/${it.diastolic} mmHg (${it.pulse} bpm)" } ?: if (isEnglish) "No readings" else "Aucune mesure",
        diagnostic = bpDiag,
        isEnglish = isEnglish,
        onAddClick = { showAddBpDialog = true },
        historyContent = {
          if (bloodPressures.isEmpty()) {
            Text(
              text = if (isEnglish) "Record your first blood pressure reading." else "Enregistrez votre première mesure de tension.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            bloodPressures.take(3).forEach { bp ->
              val itemDiag = VitalSignDiagnostics.evaluateBloodPressure(bp.systolic, bp.diastolic)
              Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "${bp.systolic}/${bp.diastolic} mmHg  •  ${bp.pulse} bpm",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault()).format(Date(bp.dateMs)) + if (bp.notes.isNotBlank()) " (${bp.notes})" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                  )
                }
                SeverityBadge(severity = itemDiag.severity, text = if (isEnglish) itemDiag.badgeTextEn else itemDiag.badgeTextFr)
              }
            }
          }
        }
      )
    }

    // SECTION 2: BLOOD GLUCOSE PANEL
    item {
      VitalSignCard(
        title = if (isEnglish) "Blood Glucose (Gestational Diabetes)" else "Glycémie (Diabète Gestationnel)",
        icon = Icons.Default.Bloodtype,
        latestValueText = latestGlucose?.let { "${it.glucoseMgDl.toInt()} mg/dL (${it.contextMeal})" } ?: if (isEnglish) "No readings" else "Aucune mesure",
        diagnostic = glucoseDiag,
        isEnglish = isEnglish,
        onAddClick = { showAddGlucoseDialog = true },
        historyContent = {
          if (bloodGlucoses.isEmpty()) {
            Text(
              text = if (isEnglish) "Record your fasting or post-meal glucose." else "Enregistrez votre glycémie à jeun ou post-prandiale.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            bloodGlucoses.take(3).forEach { bg ->
              val itemDiag = VitalSignDiagnostics.evaluateBloodGlucose(bg.glucoseMgDl, bg.contextMeal)
              Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "${bg.glucoseMgDl.toInt()} mg/dL  •  ${bg.contextMeal}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault()).format(Date(bg.dateMs)) + if (bg.notes.isNotBlank()) " (${bg.notes})" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                  )
                }
                SeverityBadge(severity = itemDiag.severity, text = if (isEnglish) itemDiag.badgeTextEn else itemDiag.badgeTextFr)
              }
            }
          }
        }
      )
    }

    // SECTION 3: WEIGHT & GESTATIONAL GAIN PANEL
    item {
      VitalSignCard(
        title = if (isEnglish) "Weight & Gain Diagnostics" else "Poids & Diagnostic Pondéral",
        icon = Icons.Default.MonitorWeight,
        latestValueText = latestWeight?.let { "${it.weightKg} kg" } ?: if (isEnglish) "No weight logged" else "Aucun poids enregistré",
        diagnostic = weightDiag,
        isEnglish = isEnglish,
        onAddClick = { showAddWeightDialog = true },
        historyContent = {
          if (weights.isEmpty()) {
            Text(
              text = if (isEnglish) "No weight logged yet." else "Aucun poids enregistré pour le moment.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            weights.take(3).forEach { w ->
              Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "${w.weightKg} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(w.dateMs)) + if (w.notes.isNotBlank()) " • ${w.notes}" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                  )
                }
              }
            }
          }
        }
      )
    }
  }

  // DIALOG 1: ADD BLOOD PRESSURE
  if (showAddBpDialog) {
    AddBloodPressureDialog(
      isEnglish = isEnglish,
      onDismiss = { showAddBpDialog = false },
      onSave = { sys, dia, pulse, notes ->
        viewModel.addBloodPressure(sys, dia, pulse, notes)
        showAddBpDialog = false
      }
    )
  }

  // DIALOG 2: ADD BLOOD GLUCOSE
  if (showAddGlucoseDialog) {
    AddBloodGlucoseDialog(
      isEnglish = isEnglish,
      onDismiss = { showAddGlucoseDialog = false },
      onSave = { valMgDl, contextMeal, notes ->
        viewModel.addBloodGlucose(valMgDl, contextMeal, notes)
        showAddGlucoseDialog = false
      }
    )
  }

  // DIALOG 3: ADD WEIGHT
  if (showAddWeightDialog) {
    AddWeightDiagnosticDialog(
      isEnglish = isEnglish,
      onDismiss = { showAddWeightDialog = false },
      onSave = { w, notes ->
        viewModel.addWeight(w, notes)
        showAddWeightDialog = false
      }
    )
  }
}

@Composable
fun VitalSignCard(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  latestValueText: String,
  diagnostic: DiagnosticResult?,
  isEnglish: Boolean,
  onAddClick: () -> Unit,
  historyContent: @Composable () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
          Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        FilledTonalButton(
          onClick = onAddClick,
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text(if (isEnglish) "Log" else "Ajouter", fontWeight = FontWeight.Bold)
        }
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (isEnglish) "Latest Reading" else "Dernière Mesure",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.outline
            )
            Text(
              text = latestValueText,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          if (diagnostic != null) {
            SeverityBadge(
              severity = diagnostic.severity,
              text = if (isEnglish) diagnostic.badgeTextEn else diagnostic.badgeTextFr
            )
          }
        }
      }

      if (diagnostic != null) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = when (diagnostic.severity) {
            DiagnosticSeverity.NORMAL -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            DiagnosticSeverity.ELEVATED -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            DiagnosticSeverity.WARNING, DiagnosticSeverity.CRITICAL_ALERT -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = if (isEnglish) diagnostic.statusTitleEn else diagnostic.statusTitleFr,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (diagnostic.severity == DiagnosticSeverity.WARNING || diagnostic.severity == DiagnosticSeverity.CRITICAL_ALERT) {
                MaterialTheme.colorScheme.error
              } else {
                MaterialTheme.colorScheme.primary
              }
            )
            Text(
              text = if (isEnglish) diagnostic.feedbackEn else diagnostic.feedbackFr,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      historyContent()
    }
  }
}

@Composable
fun SeverityBadge(severity: DiagnosticSeverity, text: String) {
  val (bgColor, textColor) = when (severity) {
    DiagnosticSeverity.NORMAL -> Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
    DiagnosticSeverity.ELEVATED -> Pair(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
    DiagnosticSeverity.WARNING -> Pair(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
    DiagnosticSeverity.CRITICAL_ALERT -> Pair(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError)
  }

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = bgColor
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = textColor,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
    )
  }
}

@Composable
fun AddBloodPressureDialog(
  isEnglish: Boolean,
  onDismiss: () -> Unit,
  onSave: (Int, Int, Int, String) -> Unit
) {
  var systolicStr by remember { mutableStateOf("118") }
  var diastolicStr by remember { mutableStateOf("76") }
  var pulseStr by remember { mutableStateOf("72") }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text = if (isEnglish) "Log Blood Pressure" else "Mesure de la Tension Artérielle", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = systolicStr,
            onValueChange = { systolicStr = it },
            label = { Text(if (isEnglish) "Systolic (mmHg)" else "Systolique (SYS)") },
            modifier = Modifier.weight(1f).testTag("input_sys"),
            singleLine = true
          )
          OutlinedTextField(
            value = diastolicStr,
            onValueChange = { diastolicStr = it },
            label = { Text(if (isEnglish) "Diastolic (mmHg)" else "Diastolique (DIA)") },
            modifier = Modifier.weight(1f).testTag("input_dia"),
            singleLine = true
          )
        }
        OutlinedTextField(
          value = pulseStr,
          onValueChange = { pulseStr = it },
          label = { Text(if (isEnglish) "Pulse (bpm)" else "Pouls (bpm)") },
          modifier = Modifier.fillMaxWidth().testTag("input_pulse"),
          singleLine = true
        )
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(if (isEnglish) "Context / Notes (ex: resting)" else "Contexte (ex: au repos le matin)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val sys = systolicStr.toIntOrNull() ?: 120
          val dia = diastolicStr.toIntOrNull() ?: 80
          val pulse = pulseStr.toIntOrNull() ?: 70
          onSave(sys, dia, pulse, notes)
        },
        modifier = Modifier.testTag("save_bp_button")
      ) {
        Text(if (isEnglish) "Save" else "Enregistrer")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (isEnglish) "Cancel" else "Annuler")
      }
    }
  )
}

@Composable
fun AddBloodGlucoseDialog(
  isEnglish: Boolean,
  onDismiss: () -> Unit,
  onSave: (Float, String, String) -> Unit
) {
  var glucoseStr by remember { mutableStateOf("88") }
  var contextMeal by remember { mutableStateOf("FASTING") }
  var notes by remember { mutableStateOf("") }

  val contextOptions = if (isEnglish) {
    listOf("FASTING" to "Fasting", "POST_1H" to "1h Post-Meal", "POST_2H" to "2h Post-Meal", "RANDOM" to "Random")
  } else {
    listOf("FASTING" to "À jeun (Matin)", "POST_1H" to "1h Après Repas", "POST_2H" to "2h Après Repas", "RANDOM" to "Aléatoire")
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Bloodtype, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text = if (isEnglish) "Log Blood Glucose" else "Mesure de la Glycémie", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = glucoseStr,
          onValueChange = { glucoseStr = it },
          label = { Text(if (isEnglish) "Glucose (mg/dL)" else "Glycémie (mg/dL)") },
          modifier = Modifier.fillMaxWidth().testTag("input_glucose"),
          singleLine = true
        )

        Text(
          text = if (isEnglish) "Measurement Context (ACOG Target):" else "Contexte de mesure (Cibles ACOG) :",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          contextOptions.forEach { (key, label) ->
            Surface(
              onClick = { contextMeal = key },
              shape = RoundedCornerShape(8.dp),
              color = if (contextMeal == key) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                RadioButton(selected = contextMeal == key, onClick = { contextMeal = key })
                Text(text = label, style = MaterialTheme.typography.bodyMedium)
              }
            }
          }
        }

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(if (isEnglish) "Meal notes (ex: whole grain bread)" else "Notes (ex: pain complet, salade)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val v = glucoseStr.toFloatOrNull() ?: 90f
          onSave(v, contextMeal, notes)
        },
        modifier = Modifier.testTag("save_glucose_button")
      ) {
        Text(if (isEnglish) "Save" else "Enregistrer")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (isEnglish) "Cancel" else "Annuler")
      }
    }
  )
}

@Composable
fun AddWeightDiagnosticDialog(
  isEnglish: Boolean,
  onDismiss: () -> Unit,
  onSave: (Float, String) -> Unit
) {
  var weightStr by remember { mutableStateOf("63.0") }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text = if (isEnglish) "Log Weight" else "Enregistrer le Poids", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = weightStr,
          onValueChange = { weightStr = it },
          label = { Text(if (isEnglish) "Weight (kg)" else "Poids (kg)") },
          modifier = Modifier.fillMaxWidth().testTag("input_weight_diag"),
          singleLine = true
        )
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(if (isEnglish) "Notes" else "Notes (ex: pesée à jeun)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val w = weightStr.toFloatOrNull() ?: 60f
          onSave(w, notes)
        },
        modifier = Modifier.testTag("save_weight_button")
      ) {
        Text(if (isEnglish) "Save" else "Enregistrer")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (isEnglish) "Cancel" else "Annuler")
      }
    }
  )
}
