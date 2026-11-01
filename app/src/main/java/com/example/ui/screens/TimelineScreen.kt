package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.util.NotificationHelper
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.MedicalAppointmentEntity
import com.example.data.PregnancyData
import com.example.data.TimelineWeekInfo
import com.example.viewmodel.PregnancyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimelineScreen(
  viewModel: PregnancyViewModel,
  isEnglish: Boolean = false
) {
  val settings by viewModel.settings.collectAsState()
  val lmp = settings?.lmpDateMs ?: (System.currentTimeMillis() - 60L * 24 * 3600 * 1000L)
  val (currentWeeks, _) = viewModel.calculateCurrentGestationalWeek(lmp)

  var selectedSubTab by remember { mutableStateOf(0) }
  val tabs = if (isEnglish) {
    listOf("Consultations & Scans", "3D Fetal Development")
  } else {
    listOf("Consultations & Examens", "Développement Fœtal 3D")
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    TabRow(
      selectedTabIndex = selectedSubTab,
      containerColor = MaterialTheme.colorScheme.surface
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedSubTab == index,
          onClick = { selectedSubTab = index },
          text = { Text(title, fontWeight = FontWeight.Bold) },
          icon = {
            if (index == 0) {
              Icon(Icons.Default.CalendarMonth, contentDescription = null)
            } else {
              Icon(Icons.Default.Visibility, contentDescription = null)
            }
          }
        )
      }
    }

    if (selectedSubTab == 0) {
      EditableConsultationsSection(viewModel = viewModel, isEnglish = isEnglish, currentWeeks = currentWeeks)
    } else {
      WeekByWeekVisualDevelopmentSection(viewModel = viewModel, isEnglish = isEnglish, currentWeeks = currentWeeks)
    }
  }
}

@Composable
fun EditableConsultationsSection(
  viewModel: PregnancyViewModel,
  isEnglish: Boolean,
  currentWeeks: Int
) {
  val appointments by viewModel.appointments.collectAsState()
  var appointmentToEdit by remember { mutableStateOf<MedicalAppointmentEntity?>(null) }
  var showAddDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(10.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Header Info & Quick Add
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.EventNote,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
            Text(
              text = if (isEnglish) "OBGYN Schedule & Scans" else "Calendrier Médical & Examens",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }

          Text(
            text = if (isEnglish) {
              "All dates and times are fully editable in case of scheduling adjustments. Tap 'Edit Date' on any appointment to update."
            } else {
              "Toutes les dates et heures sont modifiables en cas de changement d'agenda. Cliquez sur 'Modifier la date' pour actualiser un rendez-vous."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
          )

          Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
              .fillMaxWidth()
              .height(42.dp)
              .testTag("add_appointment_button"),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (isEnglish) "Add Appointment / Exam" else "Ajouter une consultation / examen", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isEnglish) "Consultations Roadmap (${appointments.size} Milestones)" else "Parcours des Consultations (${appointments.size} Jalons)",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }

    items(appointments) { appointment ->
      EditableAppointmentCard(
        appointment = appointment,
        isEnglish = isEnglish,
        onEditClick = { appointmentToEdit = appointment },
        onToggleDone = { viewModel.toggleAppointmentCompleted(appointment) }
      )
    }
  }

  // Edit / Reschedule Dialog
  if (appointmentToEdit != null) {
    EditAppointmentDialog(
      appointment = appointmentToEdit!!,
      isEnglish = isEnglish,
      onDismiss = { appointmentToEdit = null },
      onSave = { updated ->
        viewModel.saveAppointment(updated)
        appointmentToEdit = null
      },
      onDelete = { toDelete ->
        viewModel.deleteAppointment(toDelete)
        appointmentToEdit = null
      }
    )
  }

  // Add Custom Appointment Dialog
  if (showAddDialog) {
    EditAppointmentDialog(
      appointment = MedicalAppointmentEntity(
        weeksRange = if (isEnglish) "Week $currentWeeks GW" else "Semaine $currentWeeks SA",
        title = "",
        location = "",
        scheduledDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
        scheduledTimeStr = "14:00",
        datePeriodDisplay = if (isEnglish) "Scheduled visit" else "Visite programmée",
        description = "",
        keyExams = "",
        doctorName = "",
        notes = "",
        isCompleted = false
      ),
      isEnglish = isEnglish,
      isNew = true,
      onDismiss = { showAddDialog = false },
      onSave = { newAppointment ->
        viewModel.saveAppointment(newAppointment)
        showAddDialog = false
      },
      onDelete = {}
    )
  }
}

@Composable
fun EditableAppointmentCard(
  appointment: MedicalAppointmentEntity,
  isEnglish: Boolean,
  onEditClick: () -> Unit,
  onToggleDone: () -> Unit
) {
  val isCompleted = appointment.isCompleted

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 1.dp else 2.dp)
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
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isCompleted) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primaryContainer
        ) {
          Text(
            text = appointment.weeksRange,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }

        // Editable Date & Time Tag
        Surface(
          onClick = onEditClick,
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
          modifier = Modifier.testTag("edit_date_badge_${appointment.id}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
            Text(
              text = "${appointment.scheduledDateStr} • ${appointment.scheduledTimeStr}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Icon(Icons.Default.Edit, contentDescription = "Edit date", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = appointment.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
          textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
          modifier = Modifier.weight(1f)
        )

        IconButton(
          onClick = onToggleDone,
          modifier = Modifier.size(32.dp).testTag("toggle_done_btn_${appointment.id}")
        ) {
          Icon(
            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = "Toggle completed",
            tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
        Text(
          text = appointment.location + if (appointment.doctorName.isNotBlank()) " (${appointment.doctorName})" else "",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.secondary
        )
      }

      if (appointment.description.isNotBlank()) {
        Text(
          text = appointment.description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (appointment.notes.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "📝 " + appointment.notes,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(8.dp)
          )
        }
      }

      if (appointment.keyExams.isNotBlank()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = if (isEnglish) "Key exams & procedures:" else "Examens et actes clés :",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          appointment.keyExams.split("\n", ",").filter { it.isNotBlank() }.forEach { exam ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.padding(start = 4.dp)
            ) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(5.dp)
              ) {}
              Text(
                text = exam.trim(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }

      // Quick Action Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(
          onClick = onEditClick,
          modifier = Modifier.testTag("edit_appointment_btn_${appointment.id}")
        ) {
          Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text(if (isEnglish) "Edit Date & Details" else "Modifier la date & détails")
        }
      }
    }
  }
}

@Composable
fun EditAppointmentDialog(
  appointment: MedicalAppointmentEntity,
  isEnglish: Boolean,
  isNew: Boolean = false,
  onDismiss: () -> Unit,
  onSave: (MedicalAppointmentEntity) -> Unit,
  onDelete: (MedicalAppointmentEntity) -> Unit
) {
  var title by remember { mutableStateOf(appointment.title) }
  var weeksRange by remember { mutableStateOf(appointment.weeksRange) }
  var scheduledDate by remember { mutableStateOf(appointment.scheduledDateStr) }
  var scheduledTime by remember { mutableStateOf(appointment.scheduledTimeStr) }
  var location by remember { mutableStateOf(appointment.location) }
  var doctorName by remember { mutableStateOf(appointment.doctorName) }
  var notes by remember { mutableStateOf(appointment.notes) }
  var keyExams by remember { mutableStateOf(appointment.keyExams) }
  var isCompleted by remember { mutableStateOf(appointment.isCompleted) }

  val context = LocalContext.current

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.EditCalendar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
          text = if (isNew) {
            if (isEnglish) "Add Medical Visit" else "Nouveau Rendez-vous Médical"
          } else {
            if (isEnglish) "Edit Appointment Date & Info" else "Modifier le Rendez-vous & Date"
          },
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(if (isEnglish) "Consultation / Scan Title" else "Titre de la Consultation / Examen") },
          modifier = Modifier.fillMaxWidth().testTag("input_apt_title"),
          singleLine = true
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          var showCalendarPopup by remember { mutableStateOf(false) }

          OutlinedButton(
            onClick = { showCalendarPopup = true },
            modifier = Modifier.weight(1.2f).height(56.dp).testTag("btn_trigger_calendar_picker"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              contentColor = MaterialTheme.colorScheme.onSurface
            )
          ) {
            Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = scheduledDate, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
          }

          if (showCalendarPopup) {
            InteractiveCalendarPopup(
              initialDateMs = try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(scheduledDate)?.time ?: System.currentTimeMillis()
              } catch (e: Exception) {
                System.currentTimeMillis()
              },
              isEnglish = isEnglish,
              onDateSelected = { selectedMs ->
                scheduledDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedMs))
                showCalendarPopup = false
              },
              onDismiss = { showCalendarPopup = false }
            )
          }
          OutlinedTextField(
            value = scheduledTime,
            onValueChange = { scheduledTime = it },
            label = { Text(if (isEnglish) "Time" else "Heure") },
            modifier = Modifier.weight(0.8f).testTag("input_apt_time"),
            singleLine = true
          )
        }

        OutlinedTextField(
          value = weeksRange,
          onValueChange = { weeksRange = it },
          label = { Text(if (isEnglish) "Gestational Period (e.g. Weeks 11-13)" else "Période (ex: Semaines 11 à 13)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text(if (isEnglish) "Location / Clinic" else "Lieu / Cabinet / Hôpital") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = doctorName,
            onValueChange = { doctorName = it },
            label = { Text(if (isEnglish) "Doctor / Specialist" else "Médecin / Praticien") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        OutlinedTextField(
          value = keyExams,
          onValueChange = { keyExams = it },
          label = { Text(if (isEnglish) "Key exams (comma separated)" else "Examens & actes (séparés par virgule)") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(if (isEnglish) "Notes / Preparations" else "Notes / Instructions de préparation") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { isCompleted = !isCompleted }
        ) {
          Checkbox(
            checked = isCompleted,
            onCheckedChange = { isCompleted = it }
          )
          Spacer(Modifier.width(6.dp))
          Text(
            text = if (isEnglish) "Mark as completed" else "Marquer comme consultation effectuée",
            style = MaterialTheme.typography.bodyMedium
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            NotificationHelper.sendNotification(
              context = context,
              title = if (isEnglish) "Appointment Scheduled" else "Rendez-vous Programmé",
              message = "$title le $scheduledDate à $scheduledTime",
              notificationId = System.currentTimeMillis().toInt()
            )
            onSave(
              appointment.copy(
                title = title,
                weeksRange = weeksRange,
                scheduledDateStr = scheduledDate,
                scheduledTimeStr = scheduledTime,
                location = location,
                doctorName = doctorName,
                notes = notes,
                keyExams = keyExams,
                isCompleted = isCompleted
              )
            )
          }
        },
        modifier = Modifier.testTag("save_appointment_button")
      ) {
        Text(if (isEnglish) "Save Changes" else "Enregistrer")
      }
    },
    dismissButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (!isNew) {
          TextButton(
            onClick = { onDelete(appointment) },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
          ) {
            Text(if (isEnglish) "Delete" else "Supprimer")
          }
        }
        TextButton(onClick = onDismiss) {
          Text(if (isEnglish) "Cancel" else "Annuler")
        }
      }
    }
  )
}

@Composable
fun WeekByWeekVisualDevelopmentSection(
  viewModel: PregnancyViewModel,
  isEnglish: Boolean,
  currentWeeks: Int
) {
  val allMilestones = PregnancyData.getTimelineData()
  var selectedWeekNumber by remember { mutableStateOf(currentWeeks.coerceIn(1, 40)) }

  // Select corresponding 3D render asset based on gestational week
  val current3DImageRes = when {
    selectedWeekNumber <= 13 -> R.drawable.img_fetal_dev_t1
    selectedWeekNumber <= 26 -> R.drawable.img_fetal_dev_t2
    else -> R.drawable.img_fetal_dev_t3
  }

  val selectedInfo = allMilestones.minByOrNull { kotlin.math.abs(it.weekNumber - selectedWeekNumber) }
    ?: allMilestones.first()

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 3D Rendering Hero Display
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(210.dp)
              .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
          ) {
            Image(
              painter = painterResource(id = current3DImageRes),
              contentDescription = "3D Fetal Render",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
            // Floating 3D Badge
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
              modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Text(
                  text = if (isEnglish) "3D Fetal Rendering" else "Rendu Fœtal 3D Haute Résolution",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }

            // Current Week Pill
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp)
            ) {
              Text(
                text = if (isEnglish) "Week $selectedWeekNumber GW" else "Semaine $selectedWeekNumber SA",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }

          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Text(
              text = if (isEnglish) "Week-by-Week Visual Development" else "Développement Fœtal Semaine par Semaine",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = if (isEnglish) {
                "Explore anatomical growth, sensory progress, and fetal morphology through realistic 3D renderings."
              } else {
                "Visualisez l'anatomie fœtale, la motricité et l'éveil des sens à chaque étape de la grossesse grâce aux représentations 3D."
              },
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Interactive Week Selector
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = if (isEnglish) "Select Gestational Week" else "Sélectionnez la Semaine de Grossesse",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          items(allMilestones) { week ->
            val isSelected = week.weekNumber == selectedWeekNumber
            Surface(
              onClick = { selectedWeekNumber = week.weekNumber },
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
              shadowElevation = if (isSelected) 3.dp else 0.dp,
              modifier = Modifier.testTag("week_chip_${week.weekNumber}")
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "SA ${week.weekNumber}",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = week.trimester.take(3),
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                )
              }
            }
          }
        }
      }
    }

    // Detailed Milestone Breakdown Card for Selected Week
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
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
              Icon(Icons.Default.ChildCare, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Text(
                text = if (isEnglish) "Milestone Summary: Week ${selectedInfo.weekNumber}" else "Bilan Jalon : Semaine ${selectedInfo.weekNumber} SA",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer
            ) {
              Text(
                text = selectedInfo.trimester,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          // Size Comparison
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text("🌱", style = MaterialTheme.typography.titleLarge)
              Column {
                Text(
                  text = if (isEnglish) "Fetal Size & Weight:" else "Taille & Poids Fœtal :",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                  text = selectedInfo.fetalSize,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSecondaryContainer
                )
              }
            }
          }

          // Anatomical Development
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
              Text(
                text = if (isEnglish) "Anatomical & Organ Growth:" else "Développement Anatomique & Organes :",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Text(
              text = selectedInfo.fetalDevelopment,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

          // Maternal Adaptations
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Face, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
              Text(
                text = if (isEnglish) "Maternal Physiological Changes:" else "Changements chez la Future Maman :",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Text(
              text = selectedInfo.maternalChanges,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

          // Key Clinical Medical Milestone
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
              Text(
                text = if (isEnglish) "Recommended Medical Milestone:" else "Examen Médical Recommandé :",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Text(
              text = selectedInfo.medicalMilestones,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }
  }
}

@Composable
fun InteractiveCalendarPopup(
  initialDateMs: Long = System.currentTimeMillis(),
  isEnglish: Boolean = false,
  onDateSelected: (Long) -> Unit,
  onDismiss: () -> Unit
) {
  var currentMonthOffset by remember { mutableStateOf(0) }
  var tempSelectedMs by remember { mutableStateOf(initialDateMs) }

  val calendar = java.util.Calendar.getInstance().apply {
    timeInMillis = initialDateMs
    add(java.util.Calendar.MONTH, currentMonthOffset)
  }
  val year = calendar.get(java.util.Calendar.YEAR)
  val month = calendar.get(java.util.Calendar.MONTH)
  val monthLocale = if (isEnglish) java.util.Locale.ENGLISH else java.util.Locale.FRENCH
  val monthName = java.text.SimpleDateFormat("MMMM yyyy", monthLocale).format(calendar.time)

  calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
  val firstDayOfWeek = (calendar.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
  val maxDays = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = { currentMonthOffset-- }) {
          Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }
        Text(
          text = monthName.replaceFirstChar { it.uppercase() },
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = { currentMonthOffset++ }) {
          Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
          val weekDays = if (isEnglish) listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") else listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
          weekDays.forEach { day ->
            Text(text = day, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }

        val totalCells = ((firstDayOfWeek + maxDays + 6) / 7) * 7
        val cellList = (0 until totalCells).map { index ->
          val dayNum = index - firstDayOfWeek + 1
          if (dayNum in 1..maxDays) dayNum else null
        }

        cellList.chunked(7).forEach { weekDays ->
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            weekDays.forEach { dayNum ->
              if (dayNum != null) {
                val dayCal = java.util.Calendar.getInstance().apply {
                  set(year, month, dayNum, 0, 0, 0)
                }
                val dayStart = dayCal.timeInMillis
                val isSelected = java.util.Calendar.getInstance().apply { timeInMillis = tempSelectedMs }.let {
                  it.get(java.util.Calendar.YEAR) == year && it.get(java.util.Calendar.MONTH) == month && it.get(java.util.Calendar.DAY_OF_MONTH) == dayNum
                }

                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { tempSelectedMs = dayStart },
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = dayNum.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                  )
                }
              } else {
                Spacer(modifier = Modifier.size(38.dp))
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onDateSelected(tempSelectedMs) },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text(if (isEnglish) "Select" else "Sélectionner")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (isEnglish) "Cancel" else "Annuler")
      }
    },
    containerColor = MaterialTheme.colorScheme.surface
  )
}

