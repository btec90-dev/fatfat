package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.IvfProtocolSettings
import com.example.ui.components.AdjustPregnancyDateDialog
import com.example.ui.components.formatShortDate
import com.example.viewmodel.PregnancyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  viewModel: PregnancyViewModel,
  isEnglish: Boolean = false,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val currentSettings by viewModel.ivfSettings.collectAsState()
  val settings by viewModel.settings.collectAsState()

  val lmp = settings?.lmpDateMs ?: (System.currentTimeMillis() - 60L * 24 * 3600 * 1000L)
  val (weeks, days) = viewModel.calculateCurrentGestationalWeek(lmp)
  val dueDate = viewModel.calculateDueDate(lmp)
  var showAdjustPregnancyDateDialog by remember { mutableStateOf(false) }

  // Form State (initialized from SharedPreferences)
  var protocolName by remember(currentSettings) { mutableStateOf(currentSettings.protocolName) }
  var dailyDosages by remember(currentSettings) { mutableStateOf(currentSettings.dailyDosages) }
  var cycleDay1Date by remember(currentSettings) { mutableStateOf(currentSettings.cycleDay1Date) }
  var morningReminderEnabled by remember(currentSettings) { mutableStateOf(currentSettings.morningReminderEnabled) }
  var morningReminderTime by remember(currentSettings) { mutableStateOf(currentSettings.morningReminderTime) }
  var eveningReminderEnabled by remember(currentSettings) { mutableStateOf(currentSettings.eveningReminderEnabled) }
  var eveningReminderTime by remember(currentSettings) { mutableStateOf(currentSettings.eveningReminderTime) }
  var clinicPhone by remember(currentSettings) { mutableStateOf(currentSettings.clinicPhone) }
  var coordinatorEmail by remember(currentSettings) { mutableStateOf(currentSettings.coordinatorEmail) }
  var clinicName by remember(currentSettings) { mutableStateOf(currentSettings.clinicName) }

  var showSavedDialog by remember { mutableStateOf(false) }

  // Notification runtime permission launcher (Android 13+)
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (!isGranted) {
      Toast.makeText(
        context,
        if (isEnglish) "Notifications are disabled. Enable them in system settings to receive reminders."
        else "Les notifications sont désactivées. Activez-les dans les réglages système pour recevoir les rappels.",
        Toast.LENGTH_LONG
      ).show()
    }
  }

  // Handle hardware / gesture back
  BackHandler {
    onBack()
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .testTag("settings_screen"),
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = if (isEnglish) "IVF Protocol & Reminders" else "Protocole PMA & Réglages",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isEnglish) "Medications, Reminders & Clinic Contact" else "Traitements, Rappels quotidiens & Clinique",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("settings_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = if (isEnglish) "Back to dashboard" else "Retour au tableau de bord",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Color(0xFFE2D3B8),
          titleContentColor = Color(0xFF3B2314),
          navigationIconContentColor = Color(0xFF3B2314)
        )
      )
    },
    bottomBar = {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedButton(
            onClick = onBack,
            modifier = Modifier
              .weight(1f)
              .height(50.dp)
          ) {
            Text(if (isEnglish) "Cancel" else "Annuler")
          }
          Button(
            onClick = {
              val updated = currentSettings.copy(
                protocolName = protocolName.trim(),
                dailyDosages = dailyDosages.trim(),
                cycleDay1Date = cycleDay1Date.trim(),
                morningReminderEnabled = morningReminderEnabled,
                morningReminderTime = morningReminderTime.trim(),
                eveningReminderEnabled = eveningReminderEnabled,
                eveningReminderTime = eveningReminderTime.trim(),
                clinicPhone = clinicPhone.trim(),
                coordinatorEmail = coordinatorEmail.trim(),
                clinicName = clinicName.trim()
              )
              viewModel.saveIvfSettings(updated)
              showSavedDialog = true
            },
            modifier = Modifier
              .weight(1.5f)
              .height(50.dp)
              .testTag("save_settings_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (isEnglish) "Save Changes" else "Enregistrer", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      // INFO BANNER
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isEnglish) "Personalized Protocol Storage" else "Stockage Local du Protocole PMA",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = if (isEnglish)
                  "Your medications, doses, cycle dates and emergency clinic contacts are securely saved on your device."
                else
                  "Vos médicaments, dosages, date du Cycle J1 et contacts d'urgence sont conservés en toute sécurité sur votre appareil.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
              )
            }
          }
        }
      }

      // SECTION: DATATION & TERME DE GROSSESSE
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
          border = CardDefaults.outlinedCardBorder()
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CalendarMonth,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = if (isEnglish) "Pregnancy Dating & Term" else "Datation & Terme de Grossesse",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
              }
              Button(
                onClick = { showAdjustPregnancyDateDialog = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_settings_adjust_date")
              ) {
                Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isEnglish) "Adjust" else "Ajuster")
              }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = if (isEnglish) "Current Term" else "Terme actuel",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = if (isEnglish) "$weeks GW + $days d" else "$weeks SA et $days j",
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = if (isEnglish) "Estimated Due Date (EDD)" else "Accouchement prévu (DPA)",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = formatShortDate(dueDate, isEnglish),
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }

      // SECTION 1: PROTOCOLE MÉDICAL & DOSAGES
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Medication,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Text(
                text = if (isEnglish) "1. IVF Medication Protocol & Dosages" else "1. Protocole Médicamenteux PMA & Dosages",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            // Quick Protocol presets
            Text(
              text = if (isEnglish) "Protocol Templates / Quick Select:" else "Modèles de protocoles rapides :",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              val presets = if (isEnglish) {
                listOf("Antagonist", "Long Agonist", "Frozen Transfer", "IUI")
              } else {
                listOf("Antagoniste", "Agoniste Long", "TEC (Transfert)", "IAC")
              }
              presets.forEach { preset ->
                Surface(
                  onClick = {
                    protocolName = when (preset) {
                      "Antagonist", "Antagoniste" -> "Protocole Antagoniste Court (FIV/ICSI)"
                      "Long Agonist", "Agoniste Long" -> "Protocole Agoniste Long (Décapeptyl + Stim)"
                      "Frozen Transfer", "TEC (Transfert)" -> "Protocole Transfert Embryon Congelé (Oestrogènes + Progestérone)"
                      else -> "Protocole Insémination Intra-Utérine (IAC)"
                    }
                  },
                  shape = RoundedCornerShape(10.dp),
                  color = if (protocolName.contains(preset.take(4), ignoreCase = true))
                    MaterialTheme.colorScheme.primaryContainer
                  else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (protocolName.contains(preset.take(4), ignoreCase = true))
                      MaterialTheme.colorScheme.primary
                    else
                      MaterialTheme.colorScheme.outlineVariant
                  )
                ) {
                  Text(
                    text = preset,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                  )
                }
              }
            }

            // Protocol Name Field
            OutlinedTextField(
              value = protocolName,
              onValueChange = { protocolName = it },
              label = { Text(if (isEnglish) "Protocol Name" else "Nom du Protocole") },
              placeholder = { Text("Ex: Protocole Antagoniste Gonal-F + Cetrotide") },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("input_protocol_name"),
              singleLine = true,
              leadingIcon = {
                Icon(Icons.Default.Biotech, contentDescription = null)
              }
            )

            // Daily Dosages Field (multiline)
            OutlinedTextField(
              value = dailyDosages,
              onValueChange = { dailyDosages = it },
              label = { Text(if (isEnglish) "Daily Dosages & Schedule" else "Dosages Quotidiens & Horaires") },
              placeholder = { Text("Ex: Gonal-F 150 UI (19h00), Cetrotide 0.25mg (08h00), PIO 50mg (20h00)") },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("input_daily_dosages"),
              minLines = 3,
              maxLines = 5,
              supportingText = {
                Text(
                  if (isEnglish) "Specify medication names, units (UI, mg) and scheduled injection hours."
                  else "Précisez le nom des molécules, la dose (UI, mg) et l'heure habituelle de l'injection."
                )
              }
            )

            // Cycle Day 1 Start Date Field
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedTextField(
                value = cycleDay1Date,
                onValueChange = { cycleDay1Date = it },
                label = { Text(if (isEnglish) "Cycle Day 1 (LMP Start)" else "Jour 1 du Cycle (J1 / Début règles)") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier
                  .weight(1f)
                  .testTag("input_cycle_day_1"),
                singleLine = true,
                leadingIcon = {
                  Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
              )
              Button(
                onClick = {
                  val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                  cycleDay1Date = sdf.format(Date())
                },
                modifier = Modifier.padding(top = 8.dp)
              ) {
                Text(if (isEnglish) "Today" else "Aujourd'hui")
              }
            }
          }
        }
      }

      // SECTION 2: RAPPELS PUSH QUOTIDIENS (MATIN & SOIR)
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Text(
                text = if (isEnglish) "2. Daily Medication Push Reminders" else "2. Rappels Quotidiens & Notifications Push",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            // Morning Reminder Row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.WbSunny,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.tertiary
                )
                Column {
                  Text(
                    text = if (isEnglish) "Morning Medication Reminder" else "Rappel Médicaments du Matin",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = if (isEnglish) "Scheduled at $morningReminderTime" else "Programmé à $morningReminderTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Switch(
                checked = morningReminderEnabled,
                onCheckedChange = { isChecked ->
                  morningReminderEnabled = isChecked
                  if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  }
                },
                modifier = Modifier.testTag("switch_morning_reminder")
              )
            }

            if (morningReminderEnabled) {
              OutlinedTextField(
                value = morningReminderTime,
                onValueChange = { morningReminderTime = it },
                label = { Text(if (isEnglish) "Morning Time (HH:MM)" else "Heure du Matin (HH:MM)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                  Icon(Icons.Default.AccessTime, contentDescription = null)
                }
              )
            }

            // Evening Reminder Row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.NightsStay,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary
                )
                Column {
                  Text(
                    text = if (isEnglish) "Evening Injection Reminder" else "Rappel Injections du Soir",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = if (isEnglish) "Scheduled at $eveningReminderTime (PIO / Stim)" else "Programmé à $eveningReminderTime (PIO / Stim)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Switch(
                checked = eveningReminderEnabled,
                onCheckedChange = { isChecked ->
                  eveningReminderEnabled = isChecked
                  if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  }
                },
                modifier = Modifier.testTag("switch_evening_reminder")
              )
            }

            if (eveningReminderEnabled) {
              OutlinedTextField(
                value = eveningReminderTime,
                onValueChange = { eveningReminderTime = it },
                label = { Text(if (isEnglish) "Evening Time (HH:MM)" else "Heure du Soir (HH:MM)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                  Icon(Icons.Default.AccessTime, contentDescription = null)
                }
              )
            }

            // Immediate Test Notification Buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = {
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  }
                  viewModel.testMedicationNotification(isMorning = true)
                  Toast.makeText(
                    context,
                    if (isEnglish) "Morning test reminder sent! 🔔" else "Rappel test matin envoyé ! 🔔",
                    Toast.LENGTH_SHORT
                  ).show()
                },
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (isEnglish) "Test Morning" else "Tester Matin")
              }

              OutlinedButton(
                onClick = {
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  }
                  viewModel.testMedicationNotification(isMorning = false)
                  Toast.makeText(
                    context,
                    if (isEnglish) "Evening test reminder sent! 🔔" else "Rappel test soir envoyé ! 🔔",
                    Toast.LENGTH_SHORT
                  ).show()
                },
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (isEnglish) "Test Evening" else "Tester Soir")
              }
            }
          }
        }
      }

      // SECTION 3: CONTACT CLINIQUE PMA & COORDINATRICE
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Text(
                text = if (isEnglish) "3. Primary IVF Clinic & Coordinator" else "3. Clinique PMA & Coordinatrice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            Text(
              text = if (isEnglish)
                "Saving these details will instantly update the 'Call Clinic' and contact shortcuts on your main dashboard."
              else
                "L'enregistrement de ces coordonnées met à jour immédiatement le bouton 'Appeler la Clinique' sur votre tableau de bord principal.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Clinic Name Field
            OutlinedTextField(
              value = clinicName,
              onValueChange = { clinicName = it },
              label = { Text(if (isEnglish) "Clinic or Hospital Name" else "Nom de la Clinique ou Centre PMA") },
              placeholder = { Text("Ex: Centre PMA Hôpital") },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("input_clinic_name"),
              singleLine = true,
              leadingIcon = {
                Icon(Icons.Default.Business, contentDescription = null)
              }
            )

            // Clinic Phone Number Field
            OutlinedTextField(
              value = clinicPhone,
              onValueChange = { clinicPhone = it },
              label = { Text(if (isEnglish) "Primary Clinic Phone Number" else "Numéro de Téléphone Clinique PMA") },
              placeholder = { Text("Ex: +33 1 42 16 00 00") },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("input_clinic_phone"),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              },
              trailingIcon = {
                if (clinicPhone.isNotBlank()) {
                  IconButton(
                    onClick = {
                      val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${clinicPhone.trim()}"))
                      context.startActivity(dialIntent)
                    }
                  ) {
                    Icon(
                      Icons.Default.Call,
                      contentDescription = if (isEnglish) "Test Call" else "Tester l'appel",
                      tint = MaterialTheme.colorScheme.primary
                    )
                  }
                }
              }
            )

            // Coordinator Email Field
            OutlinedTextField(
              value = coordinatorEmail,
              onValueChange = { coordinatorEmail = it },
              label = { Text(if (isEnglish) "Coordinator Email" else "Email de la Coordinatrice / Sage-femme") },
              placeholder = { Text("Ex: coordination.pma@clinique.org") },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("input_coordinator_email"),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
              leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
              },
              trailingIcon = {
                if (coordinatorEmail.isNotBlank()) {
                  IconButton(
                    onClick = {
                      val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${coordinatorEmail.trim()}")).apply {
                        putExtra(Intent.EXTRA_SUBJECT, "Question Protocole PMA")
                      }
                      context.startActivity(emailIntent)
                    }
                  ) {
                    Icon(
                      Icons.Default.Send,
                      contentDescription = if (isEnglish) "Test Email" else "Tester l'e-mail",
                      tint = MaterialTheme.colorScheme.secondary
                    )
                  }
                }
              }
            )

            // Quick Dial & Contact Action Buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  if (clinicPhone.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${clinicPhone.trim()}"))
                    context.startActivity(intent)
                  } else {
                    Toast.makeText(context, if (isEnglish) "Please enter a phone number" else "Veuillez saisir un numéro", Toast.LENGTH_SHORT).show()
                  }
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("test_call_clinic_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
              ) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isEnglish) "Test Call" else "Tester l'Appel")
              }

              Button(
                onClick = {
                  if (coordinatorEmail.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${coordinatorEmail.trim()}")).apply {
                      putExtra(Intent.EXTRA_SUBJECT, "Question Protocole PMA")
                    }
                    context.startActivity(intent)
                  } else {
                    Toast.makeText(context, if (isEnglish) "Please enter an email" else "Veuillez saisir un email", Toast.LENGTH_SHORT).show()
                  }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
              ) {
                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isEnglish) "Test Email" else "Tester l'E-mail")
              }
            }
          }
        }
      }
    }
  }

  // Saved Confirmation Dialog
  if (showSavedDialog) {
    AlertDialog(
      onDismissRequest = {
        showSavedDialog = false
        onBack()
      },
      icon = {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
      },
      title = {
        Text(if (isEnglish) "Settings Saved Successfully" else "Réglages Enregistrés !")
      },
      text = {
        Text(
          if (isEnglish)
            "Your IVF medication protocol, daily dosages, cycle date, reminder toggles, and clinic phone number have been updated in local storage. The home screen 'Call Clinic' button is now synchronized."
          else
            "Votre protocole, les dosages quotidiens, la date J1, les rappels push et le numéro de la clinique ont été sauvegardés. Le bouton 'Appeler la Clinique' de l'accueil est synchronisé."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showSavedDialog = false
            onBack()
          }
        ) {
          Text(if (isEnglish) "Back to Dashboard" else "Retour à l'Accueil")
        }
      }
    )
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
}
