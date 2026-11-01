package com.example.ui.components

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustPregnancyDateDialog(
  initialLmpMs: Long,
  isEnglish: Boolean = false,
  onDismiss: () -> Unit,
  onSave: (newLmpMs: Long, newConceptionMs: Long) -> Unit
) {
  val context = LocalContext.current
  val sdfDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
  val sdfDisplay = remember { SimpleDateFormat("dd MMMM yyyy", if (isEnglish) Locale.ENGLISH else Locale.FRENCH) }

  // 0 = Par DDR (LMP), 1 = Par DPA (Due date), 2 = Par Échographie (SA + jours)
  var selectedTab by remember { mutableStateOf(0) }

  // Current working LMP in milliseconds
  var workingLmpMs by remember { mutableStateOf(initialLmpMs) }

  // Mode 2 local states (Weeks & Days)
  val initialDiffDays = remember(initialLmpMs) {
    maxOf(0L, (System.currentTimeMillis() - initialLmpMs) / (24 * 3600 * 1000L))
  }
  var echoWeeks by remember { mutableStateOf((initialDiffDays / 7).toInt().coerceIn(1, 42)) }
  var echoDays by remember { mutableStateOf((initialDiffDays % 7).toInt().coerceIn(0, 6)) }

  // Formatted date string for inputs
  var lmpDateStr by remember { mutableStateOf(sdfDate.format(Date(initialLmpMs))) }
  var dpaDateStr by remember { mutableStateOf(sdfDate.format(Date(initialLmpMs + 280L * 24 * 3600 * 1000L))) }

  // Calculate stats based on workingLmpMs
  val nowMs = System.currentTimeMillis()
  val diffMs = nowMs - workingLmpMs
  val totalDays = maxOf(0L, diffMs / (24 * 3600 * 1000L))
  val currentWeeks = (totalDays / 7).toInt()
  val currentExtraDays = (totalDays % 7).toInt()
  val computedDueDate = workingLmpMs + (280L * 24 * 3600 * 1000L)
  val remainingDays = maxOf(0L, TimeUnit.MILLISECONDS.toDays(computedDueDate - nowMs))

  fun showLmpDatePicker() {
    val cal = Calendar.getInstance().apply { timeInMillis = workingLmpMs }
    DatePickerDialog(
      context,
      { _, y, m, d ->
        val chosenCal = Calendar.getInstance().apply {
          set(y, m, d, 0, 0, 0)
          set(Calendar.MILLISECOND, 0)
        }
        workingLmpMs = chosenCal.timeInMillis
        lmpDateStr = sdfDate.format(chosenCal.time)
        dpaDateStr = sdfDate.format(Date(workingLmpMs + 280L * 24 * 3600 * 1000L))
        val daysDiff = maxOf(0L, (System.currentTimeMillis() - workingLmpMs) / (24 * 3600 * 1000L))
        echoWeeks = (daysDiff / 7).toInt().coerceIn(1, 42)
        echoDays = (daysDiff % 7).toInt().coerceIn(0, 6)
      },
      cal.get(Calendar.YEAR),
      cal.get(Calendar.MONTH),
      cal.get(Calendar.DAY_OF_MONTH)
    ).show()
  }

  fun showDpaDatePicker() {
    val cal = Calendar.getInstance().apply { timeInMillis = computedDueDate }
    DatePickerDialog(
      context,
      { _, y, m, d ->
        val chosenCal = Calendar.getInstance().apply {
          set(y, m, d, 0, 0, 0)
          set(Calendar.MILLISECOND, 0)
        }
        val targetDueDateMs = chosenCal.timeInMillis
        workingLmpMs = targetDueDateMs - (280L * 24 * 3600 * 1000L)
        lmpDateStr = sdfDate.format(Date(workingLmpMs))
        dpaDateStr = sdfDate.format(chosenCal.time)
        val daysDiff = maxOf(0L, (System.currentTimeMillis() - workingLmpMs) / (24 * 3600 * 1000L))
        echoWeeks = (daysDiff / 7).toInt().coerceIn(1, 42)
        echoDays = (daysDiff % 7).toInt().coerceIn(0, 6)
      },
      cal.get(Calendar.YEAR),
      cal.get(Calendar.MONTH),
      cal.get(Calendar.DAY_OF_MONTH)
    ).show()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp)
      .testTag("dialog_adjust_pregnancy_date"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.EditCalendar,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
        }
        Column {
          Text(
            text = if (isEnglish) "Adjust Pregnancy Date" else "Ajuster la date de grossesse",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (isEnglish) "Recalculate term, due date & calendar" else "Recalculer le terme, la DPA & le calendrier",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Method Selector Tabs
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(3.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          val tabs = if (isEnglish) {
            listOf("LMP Date", "Due Date", "Ultrasound")
          } else {
            listOf("DDR (Règles)", "DPA (Terme)", "Échographie")
          }
          tabs.forEachIndexed { index, label ->
            val isSelected = selectedTab == index
            Surface(
              onClick = { selectedTab = index },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(9.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
              contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
              )
            }
          }
        }

        // TAB 0: DDR (LMP)
        if (selectedTab == 0) {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text(
                text = if (isEnglish) "First day of last period (LMP)" else "Premier jour des dernières règles (DDR)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedTextField(
                  value = lmpDateStr,
                  onValueChange = {
                    lmpDateStr = it
                    try {
                      val parsed = sdfDate.parse(it)
                      if (parsed != null) {
                        workingLmpMs = parsed.time
                        dpaDateStr = sdfDate.format(Date(workingLmpMs + 280L * 24 * 3600 * 1000L))
                        val daysDiff = maxOf(0L, (System.currentTimeMillis() - workingLmpMs) / (24 * 3600 * 1000L))
                        echoWeeks = (daysDiff / 7).toInt().coerceIn(1, 42)
                        echoDays = (daysDiff % 7).toInt().coerceIn(0, 6)
                      }
                    } catch (_: Exception) {}
                  },
                  label = { Text("YYYY-MM-DD") },
                  modifier = Modifier.weight(1f),
                  singleLine = true
                )
                FilledTonalIconButton(
                  onClick = { showLmpDatePicker() },
                  modifier = Modifier.size(50.dp)
                ) {
                  Icon(Icons.Default.CalendarMonth, contentDescription = "Choisir la date")
                }
              }

              // Quick shortcuts
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                listOf(6, 8, 10, 12).forEach { w ->
                  FilterChip(
                    selected = currentWeeks == w,
                    onClick = {
                      val newMs = System.currentTimeMillis() - (w * 7L * 24 * 3600 * 1000L)
                      workingLmpMs = newMs
                      lmpDateStr = sdfDate.format(Date(newMs))
                      dpaDateStr = sdfDate.format(Date(newMs + 280L * 24 * 3600 * 1000L))
                      echoWeeks = w
                      echoDays = 0
                    },
                    label = { Text("$w SA", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f)
                  )
                }
              }
            }
          }
        }

        // TAB 1: DPA (Due Date)
        if (selectedTab == 1) {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text(
                text = if (isEnglish) "Estimated Due Date (EDD)" else "Date prévue d'accouchement (DPA)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedTextField(
                  value = dpaDateStr,
                  onValueChange = {
                    dpaDateStr = it
                    try {
                      val parsed = sdfDate.parse(it)
                      if (parsed != null) {
                        workingLmpMs = parsed.time - (280L * 24 * 3600 * 1000L)
                        lmpDateStr = sdfDate.format(Date(workingLmpMs))
                        val daysDiff = maxOf(0L, (System.currentTimeMillis() - workingLmpMs) / (24 * 3600 * 1000L))
                        echoWeeks = (daysDiff / 7).toInt().coerceIn(1, 42)
                        echoDays = (daysDiff % 7).toInt().coerceIn(0, 6)
                      }
                    } catch (_: Exception) {}
                  },
                  label = { Text("YYYY-MM-DD") },
                  modifier = Modifier.weight(1f),
                  singleLine = true
                )
                FilledTonalIconButton(
                  onClick = { showDpaDatePicker() },
                  modifier = Modifier.size(50.dp)
                ) {
                  Icon(Icons.Default.CalendarMonth, contentDescription = "Choisir la DPA")
                }
              }
              Text(
                text = if (isEnglish) "LMP will be automatically aligned (DPA - 280 days)."
                else "La date des dernières règles est déduite automatiquement (DPA - 280 jours).",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // TAB 2: Échographie (Ultrasound Dating)
        if (selectedTab == 2) {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text(
                text = if (isEnglish) "Exact gestational age today (dating scan)" else "Âge gestationnel précis aujourd'hui (échographie)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
              )

              // Weeks stepper
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (isEnglish) "Weeks of Amenorrhea (GW / SA):" else "Semaines d'aménorrhée (SA) :",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Medium
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  FilledTonalIconButton(
                    onClick = {
                      if (echoWeeks > 1) {
                        echoWeeks--
                        val totalD = echoWeeks * 7L + echoDays
                        workingLmpMs = System.currentTimeMillis() - (totalD * 24 * 3600 * 1000L)
                        lmpDateStr = sdfDate.format(Date(workingLmpMs))
                        dpaDateStr = sdfDate.format(Date(workingLmpMs + 280L * 24 * 3600 * 1000L))
                      }
                    },
                    modifier = Modifier.size(36.dp)
                  ) {
                    Icon(Icons.Default.Remove, contentDescription = "-1 SA")
                  }
                  Text(
                    text = "$echoWeeks SA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.Center
                  )
                  FilledTonalIconButton(
                    onClick = {
                      if (echoWeeks < 42) {
                        echoWeeks++
                        val totalD = echoWeeks * 7L + echoDays
                        workingLmpMs = System.currentTimeMillis() - (totalD * 24 * 3600 * 1000L)
                        lmpDateStr = sdfDate.format(Date(workingLmpMs))
                        dpaDateStr = sdfDate.format(Date(workingLmpMs + 280L * 24 * 3600 * 1000L))
                      }
                    },
                    modifier = Modifier.size(36.dp)
                  ) {
                    Icon(Icons.Default.Add, contentDescription = "+1 SA")
                  }
                }
              }

              // Days stepper
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (isEnglish) "Additional days:" else "Jours supplémentaires :",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Medium
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  FilledTonalIconButton(
                    onClick = {
                      if (echoDays > 0) {
                        echoDays--
                        val totalD = echoWeeks * 7L + echoDays
                        workingLmpMs = System.currentTimeMillis() - (totalD * 24 * 3600 * 1000L)
                        lmpDateStr = sdfDate.format(Date(workingLmpMs))
                        dpaDateStr = sdfDate.format(Date(workingLmpMs + 280L * 24 * 3600 * 1000L))
                      }
                    },
                    modifier = Modifier.size(36.dp)
                  ) {
                    Icon(Icons.Default.Remove, contentDescription = "-1 j")
                  }
                  Text(
                    text = "+$echoDays j",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.Center
                  )
                  FilledTonalIconButton(
                    onClick = {
                      if (echoDays < 6) {
                        echoDays++
                        val totalD = echoWeeks * 7L + echoDays
                        workingLmpMs = System.currentTimeMillis() - (totalD * 24 * 3600 * 1000L)
                        lmpDateStr = sdfDate.format(Date(workingLmpMs))
                        dpaDateStr = sdfDate.format(Date(workingLmpMs + 280L * 24 * 3600 * 1000L))
                      }
                    },
                    modifier = Modifier.size(36.dp)
                  ) {
                    Icon(Icons.Default.Add, contentDescription = "+1 j")
                  }
                }
              }
            }
          }
        }

        // LIVE RECALCULATION PREVIEW BOX
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = if (isEnglish) "Preview of Adjusted Results" else "Aperçu des résultats ajustés",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }

            HorizontalDivider(
              modifier = Modifier.padding(vertical = 4.dp),
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (isEnglish) "New Term:" else "Nouveau terme :",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
              )
              Text(
                text = if (isEnglish) "$currentWeeks GW + $currentExtraDays d" else "$currentWeeks SA et $currentExtraDays j",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (isEnglish) "Due Date (EDD):" else "Date d'accouchement (DPA) :",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
              )
              Text(
                text = "${sdfDisplay.format(Date(computedDueDate))} ($remainingDays j)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (isEnglish) "Aligned LMP Date:" else "DDR correspondante :",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
              )
              Text(
                text = sdfDate.format(Date(workingLmpMs)),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val conceptionMs = workingLmpMs + (14L * 24 * 3600 * 1000L)
          onSave(workingLmpMs, conceptionMs)
          Toast.makeText(
            context,
            if (isEnglish) "Pregnancy date successfully updated!" else "Date de grossesse mise à jour avec succès !",
            Toast.LENGTH_SHORT
          ).show()
          onDismiss()
        },
        modifier = Modifier.testTag("btn_save_adjusted_pregnancy_date"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(if (isEnglish) "Apply & Save" else "Appliquer & Enregistrer")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(if (isEnglish) "Cancel" else "Annuler")
      }
    }
  )
}
