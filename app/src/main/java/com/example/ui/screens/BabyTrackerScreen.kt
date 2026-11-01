package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.BabyActivityLogEntity
import com.example.ui.components.formatDate
import com.example.viewmodel.PregnancyViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabyTrackerScreen(
  viewModel: PregnancyViewModel,
  isEnglish: Boolean = false
) {
  val context = LocalContext.current
  val activities by viewModel.babyActivities.collectAsState()

  // Selected quick logger tab: "BREASTFEEDING", "FORMULA", "PUMPING", "DIAPER"
  var activeLogger by remember { mutableStateOf("BREASTFEEDING") }

  // Breastfeeding stopwatch timer state
  var isNursingTimerRunning by remember { mutableStateOf(false) }
  var nursingSide by remember { mutableStateOf("LEFT") } // "LEFT", "RIGHT", "BOTH"
  var nursingElapsedSeconds by remember { mutableStateOf(0) }

  // Live timer tick
  LaunchedEffect(isNursingTimerRunning) {
    while (isNursingTimerRunning) {
      delay(1000)
      nursingElapsedSeconds++
    }
  }

  // Formula state
  var formulaAmountMl by remember { mutableStateOf("120") }
  var formulaType by remember { mutableStateOf("FORMULA") } // "FORMULA", "BREAST_MILK", "MIXED"
  var formulaNotes by remember { mutableStateOf("") }

  // Pumping state
  var pumpingAmountMl by remember { mutableStateOf("90") }
  var pumpingDurationMin by remember { mutableStateOf("15") }
  var pumpingSide by remember { mutableStateOf("BOTH") } // "LEFT", "RIGHT", "BOTH"
  var pumpingStorage by remember { mutableStateOf("FRIDGE") } // "BOTTLE", "FRIDGE", "FREEZER"

  // Diaper state
  var diaperType by remember { mutableStateOf("WET") } // "WET", "DIRTY", "BOTH", "DRY"
  var diaperColor by remember { mutableStateOf("Yellow") }
  var diaperCreamApplied by remember { mutableStateOf(false) }
  var diaperNotes by remember { mutableStateOf("") }

  // Filter for history list
  var historyFilter by remember { mutableStateOf("ALL") } // "ALL", "FEEDING", "PUMPING", "DIAPER"

  // Calculate Today's Stats
  val todayStartMs = remember {
    val cal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    cal.timeInMillis
  }

  val todayActivities = activities.filter { it.dateMs >= todayStartMs }
  val nursingToday = todayActivities.filter { it.activityType == "BREASTFEEDING" }
  val formulaToday = todayActivities.filter { it.activityType == "FORMULA" }
  val pumpingToday = todayActivities.filter { it.activityType == "PUMPING" }
  val diapersToday = todayActivities.filter { it.activityType == "DIAPER" }

  val totalFormulaMlToday = formulaToday.sumOf { it.amountMl.toDouble() }.toInt()
  val totalPumpingMlToday = pumpingToday.sumOf { it.amountMl.toDouble() }.toInt()
  val totalNursingMinToday = nursingToday.sumOf { it.durationMinutes }

  // Last feeding info
  val lastFeed = activities.firstOrNull { it.activityType == "BREASTFEEDING" || it.activityType == "FORMULA" }
  val lastFeedTimeDiff = lastFeed?.let {
    val diffMs = System.currentTimeMillis() - it.dateMs
    val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
    val mins = TimeUnit.MILLISECONDS.toMinutes(diffMs) % 60
    if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
  }

  val lastNursingActivity = activities.firstOrNull { it.activityType == "BREASTFEEDING" }
  val nextSuggestedSide = when (lastNursingActivity?.subType) {
    "LEFT" -> if (isEnglish) "Right Breast" else "Sein Droit"
    "RIGHT" -> if (isEnglish) "Left Breast" else "Sein Gauche"
    else -> if (isEnglish) "Left or Right" else "Au choix"
  }

  val filteredHistory = activities.filter {
    when (historyFilter) {
      "FEEDING" -> it.activityType == "BREASTFEEDING" || it.activityType == "FORMULA"
      "PUMPING" -> it.activityType == "PUMPING"
      "DIAPER" -> it.activityType == "DIAPER"
      else -> true
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("baby_tracker_screen"),
    contentPadding = PaddingValues(14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Hero Header
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.ChildCare,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                  )
                }
              }
              Column {
                Text(
                  text = if (isEnglish) "Baby Daily Activity Tracker" else "Suivi Quotidien de Bébé",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                  text = if (isEnglish) "Once baby arrives: feedings, pumping & diapers" else "Dès la naissance : tétées, biberons, tire-lait & couches",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
              }
            }

            IconButton(
              onClick = { exportBabyTrackerPdf(context, activities, isEnglish) },
              modifier = Modifier.testTag("btn_export_baby_tracker_pdf")
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Export PDF",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }

          // Last feed & Next side hint badge
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (lastFeedTimeDiff != null) {
                  if (isEnglish) "⏱️ Last fed: $lastFeedTimeDiff ago" else "⏱️ Dernière tétée : il y a $lastFeedTimeDiff"
                } else {
                  if (isEnglish) "⏱️ No feed recorded today yet" else "⏱️ Aucune tétée enregistrée aujourd'hui"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (isEnglish) "👉 Next side: $nextSuggestedSide" else "👉 Prochain sein : $nextSuggestedSide",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }
    }

    // Daily Stats Grid (Today's Summary)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Nursing Card
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7))
        ) {
          Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🤱", fontSize = 20.sp)
            Text(
              text = "${nursingToday.size}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFD84315)
            )
            Text(
              text = if (isEnglish) "Tétées (${totalNursingMinToday}m)" else "Tétées (${totalNursingMinToday}m)",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF5D4037)
            )
          }
        }

        // Bottles Card
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
        ) {
          Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🍼", fontSize = 20.sp)
            Text(
              text = "${totalFormulaMlToday} ml",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF2E7D32)
            )
            Text(
              text = if (isEnglish) "Bottles (${formulaToday.size})" else "Biberons (${formulaToday.size})",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF1B5E20)
            )
          }
        }

        // Pumping Card
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
          Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🥛", fontSize = 20.sp)
            Text(
              text = "${totalPumpingMlToday} ml",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1565C0)
            )
            Text(
              text = if (isEnglish) "Pumped (${pumpingToday.size})" else "Exprimé (${pumpingToday.size})",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF0D47A1)
            )
          }
        }

        // Diaper Card
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
        ) {
          Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🧷", fontSize = 20.sp)
            Text(
              text = "${diapersToday.size}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFF57F17)
            )
            Text(
              text = if (isEnglish) "Diapers" else "Couches",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF5D4037)
            )
          }
        }
      }
    }

    // Logger Mode Switcher Pills
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        FilterChip(
          selected = activeLogger == "BREASTFEEDING",
          onClick = { activeLogger = "BREASTFEEDING" },
          label = { Text(if (isEnglish) "🤱 Breast" else "🤱 Tétée") },
          modifier = Modifier.weight(1f).testTag("chip_logger_breast")
        )
        FilterChip(
          selected = activeLogger == "FORMULA",
          onClick = { activeLogger = "FORMULA" },
          label = { Text(if (isEnglish) "🍼 Bottle" else "🍼 Biberon") },
          modifier = Modifier.weight(1f).testTag("chip_logger_bottle")
        )
        FilterChip(
          selected = activeLogger == "PUMPING",
          onClick = { activeLogger = "PUMPING" },
          label = { Text(if (isEnglish) "🥛 Pump" else "🥛 Tire-lait") },
          modifier = Modifier.weight(1f).testTag("chip_logger_pump")
        )
        FilterChip(
          selected = activeLogger == "DIAPER",
          onClick = { activeLogger = "DIAPER" },
          label = { Text(if (isEnglish) "🧷 Diaper" else "🧷 Couche") },
          modifier = Modifier.weight(1f).testTag("chip_logger_diaper")
        )
      }
    }

    // Interactive Logger Card according to chosen mode
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          when (activeLogger) {
            "BREASTFEEDING" -> {
              Text(
                text = if (isEnglish) "🤱 Breastfeeding Session" else "🤱 Séance d'Allaitement Maternel",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )

              // Side selector
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = { nursingSide = "LEFT" },
                  modifier = Modifier.weight(1f),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (nursingSide == "LEFT") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                  )
                ) {
                  Text(if (isEnglish) "Left Breast" else "Sein Gauche")
                }
                OutlinedButton(
                  onClick = { nursingSide = "RIGHT" },
                  modifier = Modifier.weight(1f),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (nursingSide == "RIGHT") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                  )
                ) {
                  Text(if (isEnglish) "Right Breast" else "Sein Droit")
                }
                OutlinedButton(
                  onClick = { nursingSide = "BOTH" },
                  modifier = Modifier.weight(1f),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (nursingSide == "BOTH") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                  )
                ) {
                  Text(if (isEnglish) "Both Sides" else "Les Deux")
                }
              }

              // Live Timer Stopwatch Display
              val timerMin = nursingElapsedSeconds / 60
              val timerSec = nursingElapsedSeconds % 60
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
              ) {
                Column(
                  modifier = Modifier.padding(16.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Text(
                    text = String.format("%02d:%02d", timerMin, timerSec),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )

                  Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                      onClick = { isNursingTimerRunning = !isNursingTimerRunning },
                      colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNursingTimerRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                      )
                    ) {
                      Icon(
                        imageVector = if (isNursingTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null
                      )
                      Spacer(Modifier.width(4.dp))
                      Text(if (isNursingTimerRunning) (if (isEnglish) "Pause" else "Pause") else (if (isEnglish) "Start Timer" else "Démarrer"))
                    }

                    OutlinedButton(
                      onClick = {
                        isNursingTimerRunning = false
                        nursingElapsedSeconds = 0
                      }
                    ) {
                      Text(if (isEnglish) "Reset" else "Réinitialiser")
                    }
                  }
                }
              }

              Button(
                onClick = {
                  val durationMin = maxOf(1, nursingElapsedSeconds / 60)
                  viewModel.addBabyActivity(
                    activityType = "BREASTFEEDING",
                    durationMinutes = durationMin,
                    subType = nursingSide,
                    notes = if (isEnglish) "Breastfeeding ($nursingSide) for $durationMin min" else "Tétée ($nursingSide) pendant $durationMin min"
                  )
                  isNursingTimerRunning = false
                  nursingElapsedSeconds = 0
                  Toast.makeText(context, if (isEnglish) "Nursing session saved!" else "Tétée enregistrée !", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (isEnglish) "Save Breastfeeding Session" else "Enregistrer la Tétée")
              }
            }

            "FORMULA" -> {
              Text(
                text = if (isEnglish) "🍼 Bottle / Formula Feeding" else "🍼 Biberon / Lait Infantile",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )

              // Quick Amount Chips
              Text(if (isEnglish) "Amount in ml:" else "Quantité en ml :", style = MaterialTheme.typography.labelSmall)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                listOf("60", "90", "120", "150", "180").forEach { ml ->
                  FilterChip(
                    selected = formulaAmountMl == ml,
                    onClick = { formulaAmountMl = ml },
                    label = { Text("$ml ml") }
                  )
                }
              }

              OutlinedTextField(
                value = formulaAmountMl,
                onValueChange = { formulaAmountMl = it },
                label = { Text(if (isEnglish) "Custom Volume (ml)" else "Volume personnalisé (ml)") },
                modifier = Modifier.fillMaxWidth()
              )

              // Milk Type
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = { formulaType = "FORMULA" },
                  modifier = Modifier.weight(1f),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (formulaType == "FORMULA") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                  )
                ) {
                  Text(if (isEnglish) "Formula" else "Lait Infantile", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                  onClick = { formulaType = "BREAST_MILK" },
                  modifier = Modifier.weight(1f),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (formulaType == "BREAST_MILK") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                  )
                ) {
                  Text(if (isEnglish) "Breast Milk" else "Lait Exprimé", style = MaterialTheme.typography.labelSmall)
                }
              }

              Button(
                onClick = {
                  val amount = formulaAmountMl.toFloatOrNull() ?: 120f
                  viewModel.addBabyActivity(
                    activityType = "FORMULA",
                    amountMl = amount,
                    subType = formulaType,
                    notes = if (isEnglish) "$formulaType: ${amount.toInt()} ml" else "$formulaType : ${amount.toInt()} ml"
                  )
                  Toast.makeText(context, if (isEnglish) "Bottle feed saved!" else "Biberon enregistré !", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (isEnglish) "Save Bottle Feeding" else "Enregistrer le Biberon")
              }
            }

            "PUMPING" -> {
              Text(
                text = if (isEnglish) "🥛 Breast Pumping Session" else "🥛 Séance d'Expression / Tire-Lait",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )

              // Volume Chips
              Text(if (isEnglish) "Pumped volume (ml):" else "Volume exprimé (ml) :", style = MaterialTheme.typography.labelSmall)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                listOf("40", "60", "90", "120", "150").forEach { ml ->
                  FilterChip(
                    selected = pumpingAmountMl == ml,
                    onClick = { pumpingAmountMl = ml },
                    label = { Text("$ml ml") }
                  )
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedTextField(
                  value = pumpingAmountMl,
                  onValueChange = { pumpingAmountMl = it },
                  label = { Text(if (isEnglish) "Volume (ml)" else "Volume (ml)") },
                  modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                  value = pumpingDurationMin,
                  onValueChange = { pumpingDurationMin = it },
                  label = { Text(if (isEnglish) "Minutes" else "Minutes") },
                  modifier = Modifier.weight(1f)
                )
              }

              Button(
                onClick = {
                  val amount = pumpingAmountMl.toFloatOrNull() ?: 90f
                  val dur = pumpingDurationMin.toIntOrNull() ?: 15
                  viewModel.addBabyActivity(
                    activityType = "PUMPING",
                    amountMl = amount,
                    durationMinutes = dur,
                    subType = pumpingStorage,
                    notes = if (isEnglish) "Pumped ${amount.toInt()} ml ($pumpingStorage)" else "Tire-lait ${amount.toInt()} ml ($pumpingStorage)"
                  )
                  Toast.makeText(context, if (isEnglish) "Pumping session saved!" else "Tire-lait enregistré !", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (isEnglish) "Save Pumping Record" else "Enregistrer le Tire-Lait")
              }
            }

            "DIAPER" -> {
              Text(
                text = if (isEnglish) "🧷 Diaper Change" else "🧷 Change de Couche",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )

              // Diaper Type Buttons
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                FilterChip(
                  selected = diaperType == "WET",
                  onClick = { diaperType = "WET" },
                  label = { Text(if (isEnglish) "💧 Wet" else "💧 Pipi") },
                  modifier = Modifier.weight(1f)
                )
                FilterChip(
                  selected = diaperType == "DIRTY",
                  onClick = { diaperType = "DIRTY" },
                  label = { Text(if (isEnglish) "💩 Dirty" else "💩 Selle") },
                  modifier = Modifier.weight(1f)
                )
                FilterChip(
                  selected = diaperType == "BOTH",
                  onClick = { diaperType = "BOTH" },
                  label = { Text(if (isEnglish) "💧💩 Both" else "💧💩 Les deux") },
                  modifier = Modifier.weight(1f)
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Checkbox(
                  checked = diaperCreamApplied,
                  onCheckedChange = { diaperCreamApplied = it }
                )
                Text(
                  text = if (isEnglish) "Barrier cream / balm applied" else "Crème pour le change appliquée",
                  style = MaterialTheme.typography.bodySmall
                )
              }

              Button(
                onClick = {
                  val creamNote = if (diaperCreamApplied) (if (isEnglish) " (Cream applied)" else " (Crème appliquée)") else ""
                  viewModel.addBabyActivity(
                    activityType = "DIAPER",
                    subType = diaperType,
                    notes = if (isEnglish) "Diaper: $diaperType$creamNote" else "Couche : $diaperType$creamNote"
                  )
                  Toast.makeText(context, if (isEnglish) "Diaper change saved!" else "Change enregistré !", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (isEnglish) "Save Diaper Change" else "Enregistrer le Change")
              }
            }
          }
        }
      }
    }

    // History Log Header & Filter
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isEnglish) "Today & Recent History" else "Historique & Journal Récent",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${filteredHistory.size} " + (if (isEnglish) "logs" else "entrées"),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Filter Chips for history
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        FilterChip(
          selected = historyFilter == "ALL",
          onClick = { historyFilter = "ALL" },
          label = { Text(if (isEnglish) "All" else "Tous") }
        )
        FilterChip(
          selected = historyFilter == "FEEDING",
          onClick = { historyFilter = "FEEDING" },
          label = { Text(if (isEnglish) "Feedings" else "Tétées & Biberons") }
        )
        FilterChip(
          selected = historyFilter == "PUMPING",
          onClick = { historyFilter = "PUMPING" },
          label = { Text(if (isEnglish) "Pumping" else "Tire-Lait") }
        )
        FilterChip(
          selected = historyFilter == "DIAPER",
          onClick = { historyFilter = "DIAPER" },
          label = { Text(if (isEnglish) "Diapers" else "Couches") }
        )
      }
    }

    // Activity Log Items
    if (filteredHistory.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
              text = if (isEnglish) "No baby activities recorded yet. Use the logger above to log baby's first session!" else "Aucune activité pour le moment. Enregistrez la première tétée ou change ci-dessus !",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(filteredHistory, key = { it.id }) { log ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = when (log.activityType) {
              "BREASTFEEDING" -> Color(0xFFFFF3E0)
              "FORMULA" -> Color(0xFFE8F5E9)
              "PUMPING" -> Color(0xFFE3F2FD)
              else -> Color(0xFFFFF8E1)
            }
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.size(38.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = when (log.activityType) {
                    "BREASTFEEDING" -> "🤱"
                    "FORMULA" -> "🍼"
                    "PUMPING" -> "🥛"
                    else -> "🧷"
                  },
                  fontSize = 18.sp
                )
              }
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = when (log.activityType) {
                  "BREASTFEEDING" -> (if (isEnglish) "Breastfeeding • " else "Tétée au sein • ") + log.subType
                  "FORMULA" -> (if (isEnglish) "Bottle Feeding • " else "Biberon • ") + "${log.amountMl.toInt()} ml"
                  "PUMPING" -> (if (isEnglish) "Pumping • " else "Tire-Lait • ") + "${log.amountMl.toInt()} ml"
                  else -> (if (isEnglish) "Diaper Change • " else "Couche • ") + log.subType
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )

              if (log.notes.isNotEmpty()) {
                Text(
                  text = log.notes,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Text(
                text = formatDate(log.dateMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
              )
            }

            IconButton(
              onClick = { viewModel.deleteBabyActivity(log) },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }
  }
}

fun exportBabyTrackerPdf(
  context: Context,
  activities: List<BabyActivityLogEntity>,
  isEnglish: Boolean
) {
  val pdfDocument = PdfDocument()
  val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
  val page = pdfDocument.startPage(pageInfo)
  val canvas = page.canvas
  val paint = Paint().apply {
    textSize = 11f
    color = android.graphics.Color.BLACK
  }

  var y = 40f
  val titlePaint = Paint().apply {
    textSize = 15f
    isFakeBoldText = true
    color = android.graphics.Color.rgb(74, 53, 37)
  }

  val headerText = if (isEnglish) "MAMAN & BEBE - NEWBORN ACTIVITY LOG" else "MAMAN & BEBE - JOURNAL QUOTIDIEN DE BÉBÉ"
  canvas.drawText(headerText, 40f, y, titlePaint)
  y += 24f

  val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
  canvas.drawText(if (isEnglish) "Exported: $dateStr" else "Date d'exportation : $dateStr", 40f, y, paint)
  y += 20f

  val sdfTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
  activities.take(30).forEach { log ->
    if (y > 780f) return@forEach
    val time = sdfTime.format(Date(log.dateMs))
    val type = when (log.activityType) {
      "BREASTFEEDING" -> if (isEnglish) "Breastfeeding" else "Tétée"
      "FORMULA" -> if (isEnglish) "Bottle" else "Biberon"
      "PUMPING" -> if (isEnglish) "Pumping" else "Tire-Lait"
      else -> if (isEnglish) "Diaper" else "Couche"
    }
    val details = if (log.amountMl > 0) "${log.amountMl.toInt()} ml" else if (log.durationMinutes > 0) "${log.durationMinutes} min" else log.subType
    canvas.drawText("• [$time] $type - $details (${log.notes})", 40f, y, paint)
    y += 18f
  }

  pdfDocument.finishPage(page)

  val file = File(context.cacheDir, "Journal_Activites_Bebe.pdf")
  try {
    pdfDocument.writeTo(FileOutputStream(file))
  } catch (e: Exception) {
    e.printStackTrace()
  }
  pdfDocument.close()

  val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
  val intent = Intent(Intent.ACTION_SEND).apply {
    type = "application/pdf"
    putExtra(Intent.EXTRA_STREAM, uri)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
  }
  context.startActivity(Intent.createChooser(intent, if (isEnglish) "Share Baby Tracker PDF" else "Partager le Journal de Bébé"))
}
