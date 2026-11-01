package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.InjectionEntity
import com.example.data.PregnancyData
import com.example.data.UltrasoundEntity
import com.example.ui.components.*
import com.example.viewmodel.PregnancyViewModel
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import com.example.util.VitalSignDiagnostics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  viewModel: PregnancyViewModel,
  isEnglish: Boolean = false,
  onToggleLanguage: () -> Unit = {},
  onNavigateToTab: (Int) -> Unit,
  onNavigateToSettings: () -> Unit = {}
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsState()
  val ivfSettings by viewModel.ivfSettings.collectAsState()
  val weights by viewModel.weights.collectAsState()
  val kicks by viewModel.kicks.collectAsState()
  val injections by viewModel.injections.collectAsState()
  val ultrasounds by viewModel.ultrasounds.collectAsState()

  val lmp = settings?.lmpDateMs ?: (System.currentTimeMillis() - 60L * 24 * 3600 * 1000L)
  val (weeks, days) = viewModel.calculateCurrentGestationalWeek(lmp)
  val dueDate = viewModel.calculateDueDate(lmp)
  val remainingDays = maxOf(0L, TimeUnit.MILLISECONDS.toDays(dueDate - System.currentTimeMillis()))
  val babyName = settings?.babyName ?: if (isEnglish) "Baby" else "Bébé"

  val latestWeight = weights.firstOrNull()?.weightKg ?: 62.0f
  val nextInjection = injections.firstOrNull { !it.isTaken }

  // Quick entry states
  var quickWeightInput by remember { mutableStateOf("") }
  var quickSymptomInput by remember { mutableStateOf("") }
  var showQuickSuccess by remember { mutableStateOf(false) }

  // Auto-dismiss the quick success badge gently after 2.8 seconds
  LaunchedEffect(showQuickSuccess) {
    if (showQuickSuccess) {
      delay(2800)
      showQuickSuccess = false
    }
  }

  // Ultrasound Album & photo attach states
  var showAdjustPregnancyDateDialog by remember { mutableStateOf(false) }
  var showAddEditEchoDialog by remember { mutableStateOf(false) }
  var echoToEdit by remember { mutableStateOf<UltrasoundEntity?>(null) }
  var echoForLightbox by remember { mutableStateOf<UltrasoundEntity?>(null) }
  var echoToDelete by remember { mutableStateOf<UltrasoundEntity?>(null) }
  var showInsideBumpDialog by remember { mutableStateOf(false) }
  var showBabyTrackerDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(10.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Header Banner Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showAdjustPregnancyDateDialog = true },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.primary
              )
              Text(
                text = if (isEnglish) "Gestational Term & Due Date" else "Suivi du Terme & DPA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
              )
            }
            FilledTonalButton(
              onClick = { showAdjustPregnancyDateDialog = true },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .height(28.dp)
                .testTag("btn_adjust_pregnancy_date")
            ) {
              Icon(
                imageVector = Icons.Default.EditCalendar,
                contentDescription = null,
                modifier = Modifier.size(13.dp)
              )
              Spacer(Modifier.width(4.dp))
              Text(
                text = if (isEnglish) "Adjust" else "Ajuster",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1.1f)) {
              Text(
                text = if (isEnglish) "Current Term" else "Terme actuel",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
              )
              Text(
                text = if (isEnglish) "$weeks GW + $days d" else "$weeks SA et $days j",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1
              )
            }
            Column(
              horizontalAlignment = Alignment.End,
              modifier = Modifier.weight(1.3f)
            ) {
              Text(
                text = if (isEnglish) "Due Date" else "Accouchement prévu",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
              )
              Text(
                text = if (isEnglish) "${formatShortDate(dueDate, isEnglish)} ($remainingDays d)" else "${formatShortDate(dueDate, isEnglish)} ($remainingDays j)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1
              )
            }
          }

          val progress = (weeks.toFloat() / 40f).coerceIn(0f, 1f)
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LinearProgressIndicator(
              progress = progress,
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
            Text(
              text = if (isEnglish) "Week $weeks of 40 (${(progress * 100).toInt()}%)" else "Semaine $weeks sur 40 (${(progress * 100).toInt()}%)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
          }
        }
      }
    }

    // Interactive Protocol Calendar directly on Main Screen (MOVED TO TOP)
    item {
      MainProtocolCalendarCard(
        injections = injections,
        ultrasounds = ultrasounds,
        journals = viewModel.journals.collectAsState().value,
        viewModel = viewModel,
        onNavigateToTab = onNavigateToTab
      )
    }

    // QUICK TAP : BABY & MOM CHECKLIST LAUNCHER (DIRECT 1-CLICK ACCESS)
    item {
      val (readyCount, totalCount) = remember { getChecklistStats(context) }
      val checklistProgress = if (totalCount > 0) readyCount.toFloat() / totalCount.toFloat() else 0f

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToTab(4) } // Direct 1-click access to Baby & Mom Checklist
          .testTag("home_quick_tap_checklist"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Checklist,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
              Column {
                Text(
                  text = if (isEnglish) "🍼 Quick Tap: Baby & Mom Checklist" else "🍼 Accès Rapide : Trousseau Bébé & Maman",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                  text = if (isEnglish) "$readyCount of $totalCount items checked off" else "$readyCount sur $totalCount articles prêts",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
              }
            }
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }

          // Progress bar
          LinearProgressIndicator(
            progress = { checklistProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
          )

          FilledTonalButton(
            onClick = { onNavigateToTab(4) },
            modifier = Modifier
              .fillMaxWidth()
              .height(38.dp)
              .testTag("home_open_checklist_button"),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
              contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
          ) {
            Text(
              text = if (isEnglish) "Open Baby & Mom Checklist →" else "Ouvrir le Trousseau & Préparatifs →",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // INSIDE YOUR BUMP : 3D INTERACTIVE VISUALIZATION
    item {
      val currentFetalData = remember(weeks) { getFetalDataForWeek(weeks) }
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showInsideBumpDialog = true }
          .testTag("home_card_inside_bump_3d"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1810)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                  )
                }
              }
              Column {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = if (isEnglish) "Inside your Bump 3D" else "Dans votre Ventre en 3D",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                  ) {
                    Text(
                      text = "3D",
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      fontSize = 10.sp
                    )
                  }
                }
                Text(
                  text = if (isEnglish) {
                    "Week $weeks: ${currentFetalData.fruitEmoji} ${currentFetalData.fruitEn} (${currentFetalData.lengthCm} cm)"
                  } else {
                    "Semaine $weeks : ${currentFetalData.fruitEmoji} ${currentFetalData.fruitFr} (${currentFetalData.lengthCm} cm)"
                  },
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFFFFCCBC)
                )
              }
            }
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = Color(0xFFFFCCBC)
            )
          }

          FilledTonalButton(
            onClick = { showInsideBumpDialog = true },
            modifier = Modifier.fillMaxWidth().height(38.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = Color.White
            )
          ) {
            Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
              text = if (isEnglish) "Explore 3D Model & Interactive Facts →" else "Explorer le Fœtus en 3D & Faits Clés →",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // BABY TRACKER (ONCE BABY ARRIVES)
    item {
      val babyActivitiesList by viewModel.babyActivities.collectAsState()
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showBabyTrackerDialog = true }
          .testTag("home_card_baby_tracker"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EAE0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFD7CCC8))
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Surface(
                shape = CircleShape,
                color = Color(0xFF5D4037),
                modifier = Modifier.size(44.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.ChildCare,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                  )
                }
              }
              Column {
                Text(
                  text = if (isEnglish) "Baby Tracker (Once Baby Arrives)" else "Suivi Quotidien de Bébé (Nouveau-né)",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF3E2723)
                )
                Text(
                  text = if (isEnglish) "Breastfeeding, bottles, pumping & diaper changes" else "Tétées, biberons, tire-lait & couches",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF5D4037).copy(alpha = 0.85f)
                )
              }
            }
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = Color(0xFF5D4037)
            )
          }

          // Quick Summary Pills
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.White.copy(alpha = 0.9f),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = "🤱 Tétée",
                modifier = Modifier.padding(vertical = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723)
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.White.copy(alpha = 0.9f),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = "🍼 Biberon",
                modifier = Modifier.padding(vertical = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723)
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.White.copy(alpha = 0.9f),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = "🥛 Tire-lait",
                modifier = Modifier.padding(vertical = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723)
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.White.copy(alpha = 0.9f),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = "🧷 Couche",
                modifier = Modifier.padding(vertical = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723)
              )
            }
          }

          FilledTonalButton(
            onClick = { showBabyTrackerDialog = true },
            modifier = Modifier.fillMaxWidth().height(38.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = Color(0xFF5D4037),
              contentColor = Color.White
            )
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
              text = if (isEnglish) "Log Activity / Open Baby Tracker →" else "Noter une activité / Ouvrir le Tracker →",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // 1-CLICK MEDICAL CALENDAR & CONSULTATIONS LAUNCHER
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToTab(1) } // Direct 1-click access to Timeline & Consultations Calendar
          .testTag("one_click_calendar_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(40.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
              Column {
                Text(
                  text = if (isEnglish) "📅 Medical Calendar & Consultations" else "📅 Calendrier Médical & Consultations",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                  text = if (isEnglish) "Appointments, scans & milestone dates" else "Rendez-vous, échographies & examens",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                )
              }
            }
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
          }

          FilledTonalButton(
            onClick = { onNavigateToTab(1) },
            modifier = Modifier
              .fillMaxWidth()
              .height(38.dp)
              .testTag("home_add_appointment_button"),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
              contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
              text = if (isEnglish) "Add Appointment / Scan" else "Ajouter une consultation / examen",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // MATERNAL VITALS & ACOG DIAGNOSTICS DIRECT ACCESS CARD
    item {
      val bloodPressures by viewModel.bloodPressures.collectAsState()
      val bloodGlucoses by viewModel.bloodGlucoses.collectAsState()
      val latestBp = bloodPressures.firstOrNull()
      val latestGlucose = bloodGlucoses.firstOrNull()
      val bpDiag = latestBp?.let { VitalSignDiagnostics.evaluateBloodPressure(it.systolic, it.diastolic) }
      val isAlert = bpDiag?.isPromptDoctorRequired == true

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToTab(2) } // Direct access to Tools & Vitals tab
          .testTag("maternal_vitals_shortcut_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isAlert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            Surface(
              shape = CircleShape,
              color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier.size(44.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.HealthAndSafety,
                  contentDescription = null,
                  tint = if (isAlert) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
            Column {
              Text(
                text = if (isEnglish) "🩺 Maternal Vitals & ACOG Diagnostics" else "🩺 Constantes Maternelles & Diagnostic ACOG",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isAlert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (latestBp != null) {
                  "TA : ${latestBp.systolic}/${latestBp.diastolic} mmHg" + (if (latestGlucose != null) " • Glycémie : ${latestGlucose.glucoseMgDl.toInt()} mg/dL" else "")
                } else {
                  if (isEnglish) "Daily tracking: Blood pressure, glucose, weight" else "Suivi quotidien : Tension, glycémie, poids"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isAlert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = if (isAlert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }


    // Échographies & Album Clichés visible directly on Main Screen
    item {
      HomeUltrasoundAlbumSection(
        ultrasounds = ultrasounds,
        isEnglish = isEnglish,
        onAddClick = {
          echoToEdit = null
          showAddEditEchoDialog = true
        },
        onEditClick = { echo ->
          echoToEdit = echo
          showAddEditEchoDialog = true
        },
        onDeleteClick = { echo ->
          echoToDelete = echo
        },
        onViewPhoto = { echo ->
          echoForLightbox = echo
        },
        onQuickCameraSaved = { photoPath ->
          viewModel.saveUltrasound(
            id = 0L,
            dateMs = System.currentTimeMillis(),
            trimester = "T1",
            title = if (isEnglish) "Ultrasound Photo" else "Cliché Échographique",
            notes = if (isEnglish) "Captured with camera and saved to album." else "Photo capturée avec l'appareil photo.",
            imagePath = photoPath
          )
        }
      )
    }

    // Immediate Injection Reminder Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Medication,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(36.dp)
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Prochain rappel d'injection",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
              text = if (nextInjection != null) "${nextInjection.medicationName} (${nextInjection.dose}) - Zone : ${nextInjection.site}" else "Aucune injection en attente",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
          }
        }
      }
    }

    // MEDICATION & HEALTH SAFETY DIRECT ACCESS CARD
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToTab(3) }
          .testTag("home_medication_safety_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F5EC)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8B492))
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = CircleShape,
            color = Color(0xFF3B2314),
            modifier = Modifier.size(42.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.HealthAndSafety,
                contentDescription = null,
                tint = Color(0xFFF7EEDD),
                modifier = Modifier.size(24.dp)
              )
            }
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isEnglish) "💊 Medication & Health Safety" else "💊 Médicaments & Sécurité Grossesse",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF3B2314)
            )
            Text(
              text = if (isEnglish)
                "Check safety tiers: Safe, Caution, or Unsafe. Review FDA categories, fetal risks & safe alternatives."
              else
                "Vérifiez la sécurité des molécules (Doliprane, Advil, Spasfon...). Risques par trimestre et alternatives.",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF5A4633),
              lineHeight = 17.sp
            )
          }
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF3B2314)
          )
        }
      }
    }



    // Upcoming Timeline Milestone
    item {
      val nextMilestone = PregnancyData.getTimelineData().firstOrNull { it.weekNumber >= weeks }
      if (nextMilestone != null) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(imageVector = Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Text(
                text = "Prochaine Étape (Semaine ${nextMilestone.weekNumber} SA)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
            }
            Text(
              text = nextMilestone.medicalMilestones,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Quick Navigation Hub
    item {
      Text(
        text = if (isEnglish) "Application Modules" else "Modules de l'Application",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
      )
    }

    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ModuleShortcutRow(
          title = if (isEnglish) "1. Guide & Meal Ideas" else "1. Guide & Idées de Repas",
          subtitle = if (isEnglish) "Safe foods, meals by trimester & warning signs" else "Aliments autorisés, repas par trimestre et signaux d'alerte",
          icon = Icons.Default.MenuBook,
          onClick = { onNavigateToTab(3) }
        )
        ModuleShortcutRow(
          title = if (isEnglish) "2. Reverse Timeline & Milestones" else "2. Chronologie Inversée & Étapes",
          subtitle = if (isEnglish) "From present to birth, week by week" else "Du présent vers l'accouchement et jalons",
          icon = Icons.Default.Timeline,
          onClick = { onNavigateToTab(1) }
        )
        ModuleShortcutRow(
          title = if (isEnglish) "3. Tracking & Tools (Kicks, Weight, Injections)" else "3. Suivi & Outils (Coups, Poids, Injections)",
          subtitle = if (isEnglish) "Kick counter, weight logs, protocol & calculator" else "Compteur de coups, historique des pesées et protocole",
          icon = Icons.Default.MonitorWeight,
          onClick = { onNavigateToTab(2) }
        )
        ModuleShortcutRow(
          title = if (isEnglish) "4. Birth Plan & Personal Diary" else "4. Projet de Naissance & Carnet Intime",
          subtitle = if (isEnglish) "PDF export for healthcare professionals & notes" else "Export PDF pour les professionnels de santé",
          icon = Icons.Default.Favorite,
          onClick = { onNavigateToTab(4) }
        )
        ModuleShortcutRow(
          title = if (isEnglish) "5. IVF Protocol, Reminders & Settings" else "5. Protocole PMA, Rappels & Réglages",
          subtitle = if (isEnglish) "Medications, dosages, push alerts & clinic phone" else "Traitements, dosages quotidiens, rappels push & téléphone clinique",
          icon = Icons.Default.Settings,
          onClick = onNavigateToSettings
        )
      }
    }

    // DEDICATED IVF PROTOCOL & CLINIC CONTACT CARD (AT THE BOTTOM OF THE MAIN SCREEN)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("home_clinic_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.LocalHospital,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(24.dp)
                )
              }
              Column {
                val clinicTitle = ivfSettings.clinicName
                  .replace("Centre de Fertilité & PMA Hôpital", "Centre PMA")
                  .replace("Clinique de Fertilité & PMA", "Centre PMA")
                  .replace("Clinique PMA", "Centre PMA")
                  .replace("Clinique", "Centre PMA")
                  .ifBlank { if (isEnglish) "IVF Center" else "Centre PMA" }
                Text(
                  text = clinicTitle,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = if (isEnglish) "Cycle Day 1: ${ivfSettings.cycleDay1Date}" else "Cycle J1 : ${ivfSettings.cycleDay1Date}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          // Active Protocol & Daily Dosages Summary Block
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  Icons.Default.Medication,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = ivfSettings.protocolName,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
              Text(
                text = ivfSettings.dailyDosages,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              if (ivfSettings.morningReminderEnabled || ivfSettings.eveningReminderEnabled) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                  modifier = Modifier.padding(top = 2.dp)
                ) {
                  Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.secondary
                  )
                  val reminderSummary = buildString {
                    if (ivfSettings.morningReminderEnabled) append(if (isEnglish) "Morning ${ivfSettings.morningReminderTime}" else "Matin ${ivfSettings.morningReminderTime}")
                    if (ivfSettings.morningReminderEnabled && ivfSettings.eveningReminderEnabled) append(" • ")
                    if (ivfSettings.eveningReminderEnabled) append(if (isEnglish) "Evening ${ivfSettings.eveningReminderTime}" else "Soir ${ivfSettings.eveningReminderTime}")
                  }
                  Text(
                    text = if (isEnglish) "Reminders: $reminderSummary" else "Rappels : $reminderSummary",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                  )
                }
              }
            }
          }

          // DYNAMIC 'CALL' AND 'EMAIL' BUTTONS (COMPACT)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = {
                val phone = ivfSettings.clinicPhone.trim()
                if (phone.isNotBlank()) {
                  val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                  context.startActivity(dialIntent)
                } else {
                  Toast.makeText(
                    context,
                    if (isEnglish) "Please configure the phone number in Settings"
                    else "Veuillez renseigner le numéro dans les Réglages",
                    Toast.LENGTH_SHORT
                  ).show()
                  onNavigateToSettings()
                }
              },
              modifier = Modifier
                .weight(1.1f)
                .height(36.dp)
                .testTag("call_clinic_button"),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
              Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(Modifier.width(4.dp))
              Text(
                text = if (isEnglish) "Call" else "Appeler",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
              )
            }

            OutlinedButton(
              onClick = {
                val email = ivfSettings.coordinatorEmail.trim()
                if (email.isNotBlank()) {
                  val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
                    putExtra(Intent.EXTRA_SUBJECT, "Question Protocole PMA")
                  }
                  context.startActivity(emailIntent)
                } else {
                  onNavigateToSettings()
                }
              },
              modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .testTag("email_coordinator_button"),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(Modifier.width(4.dp))
              Text(
                text = if (isEnglish) "Email" else "Email",
                style = MaterialTheme.typography.labelMedium
              )
            }
          }
        }
      }
    }
  }

  // Adjust Pregnancy Date Dialog
  if (showAdjustPregnancyDateDialog) {
    AdjustPregnancyDateDialog(
      initialLmpMs = lmp,
      isEnglish = isEnglish,
      onDismiss = { showAdjustPregnancyDateDialog = false },
      onSave = { newLmpMs, newConceptionMs ->
        viewModel.updateSettings(
          lmpMs = newLmpMs,
          conceptionMs = newConceptionMs,
          babyName = settings?.babyName ?: "Bébé"
        )
      }
    )
  }

  // Ultrasound Album Dialogs
  if (showAddEditEchoDialog) {
    AddEditUltrasoundDialog(
      echoToEdit = echoToEdit,
      isEnglish = isEnglish,
      onDismiss = {
        showAddEditEchoDialog = false
        echoToEdit = null
      },
      onSave = { id, dateMs, trimester, title, notes, photoPath ->
        viewModel.saveUltrasound(id, dateMs, trimester, title, notes, photoPath)
        showAddEditEchoDialog = false
        echoToEdit = null
      }
    )
  }

  if (echoForLightbox != null) {
    UltrasoundLightboxDialog(
      echo = echoForLightbox!!,
      isEnglish = isEnglish,
      onDismiss = { echoForLightbox = null },
      onEdit = {
        val target = echoForLightbox
        echoForLightbox = null
        echoToEdit = target
        showAddEditEchoDialog = true
      },
      onDelete = {
        val target = echoForLightbox
        echoForLightbox = null
        echoToDelete = target
      }
    )
  }

  if (echoToDelete != null) {
    DeleteUltrasoundConfirmationDialog(
      echo = echoToDelete!!,
      isEnglish = isEnglish,
      onDismiss = { echoToDelete = null },
      onConfirm = {
        viewModel.deleteUltrasound(echoToDelete!!)
        echoToDelete = null
      }
    )
  }

  if (showInsideBumpDialog) {
    androidx.compose.ui.window.Dialog(
      onDismissRequest = { showInsideBumpDialog = false },
      properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Scaffold(
        topBar = {
          TopAppBar(
            title = {
              Text(
                text = if (isEnglish) "Inside your Bump 3D" else "Dans votre Ventre en 3D",
                fontWeight = FontWeight.Bold
              )
            },
            navigationIcon = {
              IconButton(onClick = { showInsideBumpDialog = false }) {
                Icon(Icons.Default.Close, contentDescription = "Close")
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
          )
        }
      ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
          InsideBump3DScreen(viewModel = viewModel, isEnglish = isEnglish)
        }
      }
    }
  }

  if (showBabyTrackerDialog) {
    androidx.compose.ui.window.Dialog(
      onDismissRequest = { showBabyTrackerDialog = false },
      properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Scaffold(
        topBar = {
          TopAppBar(
            title = {
              Text(
                text = if (isEnglish) "Baby Daily Tracker" else "Suivi Quotidien de Bébé",
                fontWeight = FontWeight.Bold
              )
            },
            navigationIcon = {
              IconButton(onClick = { showBabyTrackerDialog = false }) {
                Icon(Icons.Default.Close, contentDescription = "Close")
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
          )
        }
      ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
          BabyTrackerScreen(viewModel = viewModel, isEnglish = isEnglish)
        }
      }
    }
  }
}

@Composable
fun MainProtocolCalendarCard(
  injections: List<InjectionEntity>,
  ultrasounds: List<UltrasoundEntity>,
  journals: List<com.example.data.JournalEntity> = emptyList(),
  viewModel: PregnancyViewModel? = null,
  onNavigateToTab: (Int) -> Unit
) {
  var currentMonthOffset by remember { mutableStateOf(0) }
  var selectedDayMs by remember {
    val cal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    mutableStateOf(cal.timeInMillis)
  }

  var showAddGynDialog by remember { mutableStateOf(false) }
  var showAddMemoDialog by remember { mutableStateOf(false) }
  var showAddJournalDialog by remember { mutableStateOf(false) }

  var gynDoctor by remember { mutableStateOf("Dr. Moreau (Gynécologue)") }
  var gynNotes by remember { mutableStateOf("") }
  var memoTitle by remember { mutableStateOf("") }
  var memoContent by remember { mutableStateOf("") }
  var journalMood by remember { mutableStateOf("😊") }
  var journalNotes by remember { mutableStateOf("") }

  val todayCal = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
  }
  val todayMs = todayCal.timeInMillis

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
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
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
          )
          Column {
            Text(
              text = "Calendrier du Protocole",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "Visites GYN, mémos, injections & journal",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Month Navigator with animated month title transition
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = { currentMonthOffset-- }) {
          Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = "Mois précédent",
            tint = MaterialTheme.colorScheme.primary
          )
        }
        AnimatedContent(
          targetState = currentMonthOffset,
          transitionSpec = {
            if (targetState > initialState) {
              (slideInHorizontally(animationSpec = tween(240)) { width -> width / 2 } + fadeIn(animationSpec = tween(240)))
                .togetherWith(slideOutHorizontally(animationSpec = tween(200)) { width -> -width / 2 } + fadeOut(animationSpec = tween(200)))
            } else {
              (slideInHorizontally(animationSpec = tween(240)) { width -> -width / 2 } + fadeIn(animationSpec = tween(240)))
                .togetherWith(slideOutHorizontally(animationSpec = tween(200)) { width -> width / 2 } + fadeOut(animationSpec = tween(200)))
            }
          },
          label = "CalendarMonthTitleTransition"
        ) { targetOffset ->
          val cal = Calendar.getInstance().apply { add(Calendar.MONTH, targetOffset) }
          val monthName = SimpleDateFormat("MMMM yyyy", Locale.FRENCH).format(cal.time)
          Text(
            text = monthName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString() },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
        IconButton(onClick = { currentMonthOffset++ }) {
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Mois suivant",
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }

      // Legend
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
          Text(text = "Visite GYN", style = MaterialTheme.typography.labelSmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
          Text(text = "Injections", style = MaterialTheme.typography.labelSmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
          Text(text = "Mémos/Notes", style = MaterialTheme.typography.labelSmall)
        }
      }

      // Day Names
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { day ->
          Text(
            text = day,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      // Animated Month Days Grid with directional sliding and smooth fade
      AnimatedContent(
        targetState = currentMonthOffset,
        transitionSpec = {
          if (targetState > initialState) {
            (slideInHorizontally(animationSpec = tween(280)) { width -> width } + fadeIn(animationSpec = tween(280)))
              .togetherWith(slideOutHorizontally(animationSpec = tween(240)) { width -> -width } + fadeOut(animationSpec = tween(240)))
          } else {
            (slideInHorizontally(animationSpec = tween(280)) { width -> -width } + fadeIn(animationSpec = tween(280)))
              .togetherWith(slideOutHorizontally(animationSpec = tween(240)) { width -> width } + fadeOut(animationSpec = tween(240)))
          }
        },
        label = "CalendarDaysGridTransition"
      ) { targetOffset ->
        val cal = Calendar.getInstance().apply {
          add(Calendar.MONTH, targetOffset)
        }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val totalCells = ((firstDayOfWeek + maxDays + 6) / 7) * 7
        val cellList = (0 until totalCells).map { index ->
          val dayNum = index - firstDayOfWeek + 1
          if (dayNum in 1..maxDays) dayNum else null
        }

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clipToBounds(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          cellList.chunked(7).forEach { weekDays ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
              weekDays.forEach { dayNum ->
                if (dayNum != null) {
                  val dayCal = Calendar.getInstance().apply {
                    set(year, month, dayNum, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                  }
                  val dayStart = dayCal.timeInMillis
                  val dayEnd = dayStart + 24 * 60 * 60 * 1000L - 1L

                  val isSelected = selectedDayMs == dayStart
                  val isToday = dayStart == todayMs

                  val hasInjection = injections.any { it.dateMs in dayStart..dayEnd }
                  val hasEcho = ultrasounds.any { it.dateMs in dayStart..dayEnd }
                  val hasJournal = journals.any { it.dateMs in dayStart..dayEnd }

                  Box(
                    modifier = Modifier
                      .size(40.dp)
                      .clip(RoundedCornerShape(10.dp))
                      .background(
                        when {
                          isSelected -> MaterialTheme.colorScheme.primary
                          isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                          else -> MaterialTheme.colorScheme.surface
                        }
                      )
                      .clickable { selectedDayMs = dayStart },
                    contentAlignment = Alignment.Center
                  ) {
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center
                    ) {
                      Text(
                        text = dayNum.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                          isSelected -> MaterialTheme.colorScheme.onPrimary
                          isToday -> MaterialTheme.colorScheme.primary
                          else -> MaterialTheme.colorScheme.onSurface
                        }
                      )
                      Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (hasInjection) {
                          Box(
                            modifier = Modifier
                              .size(5.dp)
                              .clip(CircleShape)
                              .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary)
                          )
                        }
                        if (hasEcho) {
                          Box(
                            modifier = Modifier
                              .size(5.dp)
                              .clip(CircleShape)
                              .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                          )
                        }
                        if (hasJournal) {
                          Box(
                            modifier = Modifier
                              .size(5.dp)
                              .clip(CircleShape)
                              .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.tertiary)
                          )
                        }
                      }
                    }
                  }
                } else {
                  Spacer(modifier = Modifier.size(40.dp))
                }
              }
            }
          }
        }
      }

      Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // Events for selected day
      val targetDayMs = selectedDayMs
      val selectedDateText = SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH).format(Date(targetDayMs))
      
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = selectedDateText.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString() },
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = "☁️ Synchronisé",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary
        )
      }

      // Quick Add Buttons for selected day
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        AssistChip(
          onClick = { showAddGynDialog = true },
          label = { Text("+ GYN", style = MaterialTheme.typography.labelSmall) },
          leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(14.dp)) }
        )
        AssistChip(
          onClick = { showAddMemoDialog = true },
          label = { Text("+ Mémo", style = MaterialTheme.typography.labelSmall) },
          leadingIcon = { Icon(Icons.Default.NoteAlt, contentDescription = null, modifier = Modifier.size(14.dp)) }
        )
        AssistChip(
          onClick = { showAddJournalDialog = true },
          label = { Text("+ Journal", style = MaterialTheme.typography.labelSmall) },
          leadingIcon = { Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(14.dp)) }
        )
      }

      val dayStart = targetDayMs
      val dayEnd = dayStart + 24 * 60 * 60 * 1000L - 1L
      val dayInjections = injections.filter { it.dateMs in dayStart..dayEnd }
      val dayEchos = ultrasounds.filter { it.dateMs in dayStart..dayEnd }
      val dayJournals = journals.filter { it.dateMs in dayStart..dayEnd }

      if (dayInjections.isEmpty() && dayEchos.isEmpty() && dayJournals.isEmpty()) {
        Text(
          text = "Aucun événement prévu pour cette date. Utilisez les boutons pour planifier un RDV ou une note.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          dayEchos.forEach { echo ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(text = "🩺", style = MaterialTheme.typography.bodyMedium)
                Column {
                  Text(
                    text = echo.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                  Text(
                    text = echo.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                  )
                }
              }
            }
          }

          dayInjections.forEach { inj ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(text = "💉", style = MaterialTheme.typography.bodyMedium)
                Column {
                  Text(
                    text = "${inj.medicationName} (${inj.dose})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                  )
                  Text(
                    text = "Zone : ${inj.site} • ${if (inj.isTaken) "Effectuée" else "À faire"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                  )
                }
              }
            }
          }

          dayJournals.forEach { j ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(text = j.mood, style = MaterialTheme.typography.bodyMedium)
                Column {
                  Text(
                    text = j.symptoms,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                  )
                  Text(
                    text = j.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                  )
                }
              }
            }
          }
        }
      }

      OutlinedButton(
        onClick = { onNavigateToTab(2) },
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp)
          .testTag("manage_gyn_protocol_bottom_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("Gérer les Visites GYN & Protocole", fontWeight = FontWeight.SemiBold)
      }
    }
  }

  // Dialog Add GYN Visit
  if (showAddGynDialog && viewModel != null) {
    AlertDialog(
      onDismissRequest = { showAddGynDialog = false },
      title = { Text("Ajouter une Visite GYN") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = gynDoctor,
            onValueChange = { gynDoctor = it },
            label = { Text("Praticien") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = gynNotes,
            onValueChange = { gynNotes = it },
            label = { Text("Motif / Consignes / Heure") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(onClick = {
          if (gynDoctor.isNotBlank()) {
            viewModel.addUltrasound("Consultation", gynDoctor, gynNotes.ifBlank { "Visite GYN planifiée" }, "")
            gynNotes = ""
            showAddGynDialog = false
          }
        }) {
          Text("Enregistrer")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddGynDialog = false }) { Text("Annuler") }
      }
    )
  }

  // Dialog Add Memo
  if (showAddMemoDialog && viewModel != null) {
    AlertDialog(
      onDismissRequest = { showAddMemoDialog = false },
      title = { Text("Ajouter un Mémo / Note") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = memoTitle,
            onValueChange = { memoTitle = it },
            label = { Text("Titre du mémo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = memoContent,
            onValueChange = { memoContent = it },
            label = { Text("Détails / Questions médecin") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(onClick = {
          if (memoTitle.isNotBlank()) {
            viewModel.addJournal("📝", "[Mémo] $memoTitle", memoContent)
            memoTitle = ""
            memoContent = ""
            showAddMemoDialog = false
          }
        }) {
          Text("Enregistrer")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddMemoDialog = false }) { Text("Annuler") }
      }
    )
  }

  // Dialog Add Journal
  if (showAddJournalDialog && viewModel != null) {
    AlertDialog(
      onDismissRequest = { showAddJournalDialog = false },
      title = { Text("Journal Intime du Jour") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("😊", "🥰", "🥱", "🤢", "✨").forEach { mood ->
              FilterChip(
                selected = journalMood == mood,
                onClick = { journalMood = mood },
                label = { Text(mood) }
              )
            }
          }
          OutlinedTextField(
            value = journalNotes,
            onValueChange = { journalNotes = it },
            label = { Text("Ressentis pour bébé") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(onClick = {
          if (journalNotes.isNotBlank()) {
            viewModel.addJournal(journalMood, "Notes du calendrier", journalNotes)
            journalNotes = ""
            showAddJournalDialog = false
          }
        }) {
          Text("Enregistrer")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddJournalDialog = false }) { Text("Annuler") }
      }
    )
  }
}

@Composable
fun StatCard(
  title: String,
  value: String,
  icon: ImageVector,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    onClick = onClick,
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
      )
      Text(
        text = title,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@Composable
fun ModuleShortcutRow(
  title: String,
  subtitle: String,
  icon: ImageVector,
  onClick: () -> Unit
) {
  Card(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
