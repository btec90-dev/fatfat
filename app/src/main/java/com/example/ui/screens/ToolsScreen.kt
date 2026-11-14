package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.components.*
import com.example.util.NotificationHelper
import com.example.viewmodel.PregnancyViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
  viewModel: PregnancyViewModel,
  isEnglish: Boolean = false,
  onNavigateToSettings: () -> Unit = {}
) {
  var selectedSubTab by remember { mutableStateOf(0) }
  val subTabs = if (isEnglish) {
    listOf("Inside Bump 3D", "Baby Tracker", "Vitals & Diagnostics", "Medication Safety", "Kicks", "Weight", "Injections", "Scans", "Albums", "Calculator", "Calendar")
  } else {
    listOf("Dans le Ventre 3D", "Tracker Bébé", "Constantes & Santé", "Médicaments & Sécurité", "Coups", "Poids", "Injections", "Échos", "Albums", "Calculateur", "Calendrier")
  }

  val weights by viewModel.weights.collectAsState()
  val kicks by viewModel.kicks.collectAsState()
  val injections by viewModel.injections.collectAsState()
  val ultrasounds by viewModel.ultrasounds.collectAsState()
  val settings by viewModel.settings.collectAsState()

  // Dialog states
  var showAddWeightDialog by remember { mutableStateOf(false) }
  var showAddInjectionDialog by remember { mutableStateOf(false) }
  var showAddEchoDialog by remember { mutableStateOf(false) }
  var showCalcDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    ScrollableTabRow(
      selectedTabIndex = selectedSubTab,
      edgePadding = 16.dp,
      containerColor = MaterialTheme.colorScheme.surface
    ) {
      subTabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedSubTab == index,
          onClick = { selectedSubTab = index },
          text = { Text(title) },
          icon = {
            when (index) {
              0 -> Icon(Icons.Default.ViewInAr, contentDescription = null)
              1 -> Icon(Icons.Default.ChildCare, contentDescription = null)
              2 -> Icon(Icons.Default.HealthAndSafety, contentDescription = null)
              3 -> Icon(Icons.Default.Medication, contentDescription = null)
              4 -> Icon(Icons.Default.DirectionsWalk, contentDescription = null)
              5 -> Icon(Icons.Default.MonitorWeight, contentDescription = null)
              6 -> Icon(Icons.Default.Vaccines, contentDescription = null)
              7 -> Icon(Icons.Default.Image, contentDescription = null)
              8 -> Icon(Icons.Default.PhotoAlbum, contentDescription = null)
              9 -> Icon(Icons.Default.Calculate, contentDescription = null)
              10 -> Icon(Icons.Default.CalendarMonth, contentDescription = null)
              else -> Icon(Icons.Default.MoreHoriz, contentDescription = null)
            }
          }
        )
      }
    }

    Box(modifier = Modifier.fillMaxSize()) {
      when (selectedSubTab) {
        0 -> InsideBump3DScreen(viewModel = viewModel, isEnglish = isEnglish)
        1 -> BabyTrackerScreen(viewModel = viewModel, isEnglish = isEnglish)
        2 -> MaternalVitalsSection(viewModel = viewModel, isEnglish = isEnglish, onNavigateToSettings = onNavigateToSettings)
        3 -> MedicationSafetyScreen(isEnglish = isEnglish)
        4 -> KickCounterSection(kicks = kicks, viewModel = viewModel, isEnglish = isEnglish)
        5 -> WeightSection(weights = weights, onAddClick = { showAddWeightDialog = true }, viewModel = viewModel, isEnglish = isEnglish)
        6 -> InjectionSection(injections = injections, onAddClick = { showAddInjectionDialog = true }, viewModel = viewModel, isEnglish = isEnglish)
        7 -> UltrasoundSection(ultrasounds = ultrasounds, onAddClick = { showAddEchoDialog = true }, viewModel = viewModel, isEnglish = isEnglish)
        8 -> PhotoAlbumsSection(ultrasounds = ultrasounds, viewModel = viewModel, isEnglish = isEnglish)
        9 -> CalculatorSection(settings = settings, viewModel = viewModel, onOpenCalc = { showCalcDialog = true }, isEnglish = isEnglish)
        10 -> ProtocolCalendarSection(injections = injections, ultrasounds = ultrasounds, viewModel = viewModel, isEnglish = isEnglish)
      }
    }
  }

  // Add Weight Dialog
  if (showAddWeightDialog) {
    var weightInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showAddWeightDialog = false },
      title = { Text(if (isEnglish) "Log Weight" else "Ajouter une pesée") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = weightInput,
            onValueChange = { weightInput = it },
            label = { Text(if (isEnglish) "Weight (kg)" else "Poids (kg)") },
            singleLine = true
          )
          OutlinedTextField(
            value = noteInput,
            onValueChange = { noteInput = it },
            label = { Text(if (isEnglish) "Notes (e.g. morning fasting)" else "Notes (ex: matin à jeun)") }
          )
        }
      },
      confirmButton = {
        TextButton(onClick = {
          val w = weightInput.toFloatOrNull()
          if (w != null) {
            viewModel.addWeight(w, noteInput)
            showAddWeightDialog = false
          }
        }) { Text(if (isEnglish) "Save" else "Enregistrer") }
      },
      dismissButton = {
        TextButton(onClick = { showAddWeightDialog = false }) { Text(if (isEnglish) "Cancel" else "Annuler") }
      }
    )
  }

  // Add Injection Dialog
  if (showAddInjectionDialog) {
    var medName by remember { mutableStateOf("Progestérone (PIO)") }
    var dose by remember { mutableStateOf("50mg") }
    var site by remember { mutableStateOf(if (isEnglish) "Left Thigh / Buttock" else "Ventre / Fesse gauche") }
    var note by remember { mutableStateOf("") }
    var isTaken by remember { mutableStateOf(true) }

    AlertDialog(
      onDismissRequest = { showAddInjectionDialog = false },
      title = { Text(if (isEnglish) "Log Injection / Medication" else "Enregistrer une injection / médicament") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = medName, onValueChange = { medName = it }, label = { Text(if (isEnglish) "Medication name (e.g. Progesterone)" else "Nom (ex: Progestérone)") })
          OutlinedTextField(value = dose, onValueChange = { dose = it }, label = { Text(if (isEnglish) "Dose (e.g. 50mg / 1ml)" else "Dose (ex: 50mg)") })
          OutlinedTextField(value = site, onValueChange = { site = it }, label = { Text(if (isEnglish) "Injection site" else "Zone d'injection") })
          OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(if (isEnglish) "Notes / Side effects" else "Notes / Effets") })
        }
      },
      confirmButton = {
        TextButton(onClick = {
          viewModel.addInjection(medName, dose, site, note, isTaken)
          showAddInjectionDialog = false
        }) { Text(if (isEnglish) "Add" else "Ajouter") }
      },
      dismissButton = {
        TextButton(onClick = { showAddInjectionDialog = false }) { Text(if (isEnglish) "Cancel" else "Annuler") }
      }
    )
  }

  // Add Ultrasound Dialog
  if (showAddEchoDialog) {
    var trimester by remember { mutableStateOf("T1") }
    var title by remember { mutableStateOf(if (isEnglish) "Checkup Ultrasound Scan" else "Échographie de contrôle") }
    var note by remember { mutableStateOf(if (isEnglish) "Everything is normal, good nuchal translucency." else "Tout est normal, bonne clarté nucale.") }

    AlertDialog(
      onDismissRequest = { showAddEchoDialog = false },
      title = { Text(if (isEnglish) "Add Ultrasound Exam" else "Ajouter une échographie") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = trimester, onValueChange = { trimester = it }, label = { Text(if (isEnglish) "Trimester (T1, T2, T3, Early)" else "Trimestre (T1, T2, T3, Précoce)") })
          OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(if (isEnglish) "Title / Short description" else "Titre / Description courte") })
          OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(if (isEnglish) "Medical findings" else "Notes médicales") })
        }
      },
      confirmButton = {
        TextButton(onClick = {
          viewModel.addUltrasound(trimester, title, note, "")
          showAddEchoDialog = false
        }) { Text(if (isEnglish) "Add" else "Ajouter") }
      },
      dismissButton = {
        TextButton(onClick = { showAddEchoDialog = false }) { Text(if (isEnglish) "Cancel" else "Annuler") }
      }
    )
  }
}

@Composable
fun KickCounterSection(
  kicks: List<KickSessionEntity>,
  viewModel: PregnancyViewModel,
  isEnglish: Boolean = false
) {
  var isRunning by remember { mutableStateOf(false) }
  var secondsElapsed by remember { mutableStateOf(0) }
  var count by remember { mutableStateOf(0) }

  LaunchedEffect(isRunning) {
    while (isRunning) {
      delay(1000L)
      secondsElapsed++
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Text(
            text = if (isEnglish) "Fetal Movement & Kick Counter" else "Compteur de Mouvements Fœtaux",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )

          // Timer display
          val mins = secondsElapsed / 60
          val secs = secondsElapsed % 60
          Text(
            text = String.format("%02d:%02d", mins, secs),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          Text(
            text = if (isEnglish) "$count kick${if (count > 1) "s" else ""} logged" else "$count coups enregistrés",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Button(
              onClick = { count++ },
              enabled = isRunning,
              modifier = Modifier.weight(1f).height(56.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = null)
              Spacer(Modifier.width(8.dp))
              Text(if (isEnglish) "Kick!" else "Coup !")
            }

            if (!isRunning) {
              Button(
                onClick = {
                  isRunning = true
                  secondsElapsed = 0
                  count = 0
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
              ) {
                Text(if (isEnglish) "Start" else "Démarrer")
              }
            } else {
              Button(
                onClick = {
                  isRunning = false
                  if (count > 0 || secondsElapsed > 10) {
                    viewModel.addKickSession(secondsElapsed, count, if (isEnglish) "Kick counting session" else "Session de comptage")
                  }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
              ) {
                Text(if (isEnglish) "Finish & Save" else "Terminer & Sauver")
              }
            }
          }
        }
      }
    }

    item {
      Text(
        text = if (isEnglish) "Session history" else "Historique des sessions",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    items(kicks) { session ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (isEnglish) "${session.kickCount} kicks in ${session.durationSec / 60} min" else "${session.kickCount} coups en ${session.durationSec / 60} min",
              fontWeight = FontWeight.Bold
            )
            Text(text = formatDate(session.dateMs), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          IconButton(onClick = { viewModel.deleteKick(session) }) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
          }
        }
      }
    }
  }
}

@Composable
fun WeightSection(
  weights: List<WeightLogEntity>,
  onAddClick: () -> Unit,
  viewModel: PregnancyViewModel,
  isEnglish: Boolean = false
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (isEnglish) "Maternal Weight Tracker" else "Suivi du Poids Maternel",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (isEnglish) "Weight gain progression and history" else "Courbe d'évolution et prise de poids totale",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Button(onClick = onAddClick) {
          Icon(Icons.Default.Add, contentDescription = null)
          Spacer(Modifier.width(4.dp))
          Text(if (isEnglish) "Log Weight" else "Pesée")
        }
      }
    }

    items(weights) { w ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(text = "${w.weightKg} kg", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            if (w.notes.isNotEmpty()) {
              Text(text = w.notes, style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = formatDate(w.dateMs), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          IconButton(onClick = { viewModel.deleteWeight(w) }) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
          }
        }
      }
    }
  }
}

@Composable
fun InjectionSection(
  injections: List<InjectionEntity>,
  onAddClick: () -> Unit,
  viewModel: PregnancyViewModel,
  isEnglish: Boolean = false
) {
  val context = LocalContext.current
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      NotificationHelper.sendNotification(
        context,
        if (isEnglish) "Injection Reminder" else "Rappel d'Injection",
        if (isEnglish) "Reminder active: Progesterone (PIO) injection scheduled on time." else "Rappel activé : Injection de Progestérone (PIO) prévue à l'heure."
      )
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isEnglish) "IVF Protocol & Injections (Progesterone / PIO)" else "Injections & PMA (Progestérone / PIO)",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (isEnglish) "Reminders, dosages and injection site rotation" else "Rappels, doses et rotation des zones",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(if (isEnglish) "Add Injection" else "Injection")
          }
        }
        OutlinedButton(
          onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
              NotificationHelper.sendNotification(
                context,
                if (isEnglish) "Injection Reminder" else "Rappel d'Injection",
                if (isEnglish) "Reminder active: Progesterone (PIO) injection scheduled." else "Rappel activé : Injection de Progestérone (PIO) prévue."
              )
            }
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.Notifications, contentDescription = null)
          Spacer(Modifier.width(8.dp))
          Text(if (isEnglish) "Enable / Test Push Notification Reminder" else "Activer / Tester le Rappel de Notification")
        }
      }
    }

    items(injections) { inj ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (inj.isTaken) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "${inj.medicationName} (${inj.dose})", fontWeight = FontWeight.Bold)
            Text(text = if (isEnglish) "Site: ${inj.site}" else "Zone : ${inj.site}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            if (inj.notes.isNotEmpty()) {
              Text(text = inj.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = formatDate(inj.dateMs), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          IconButton(onClick = { viewModel.deleteInjection(inj) }) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
          }
        }
      }
    }
  }
}

@Composable
fun UltrasoundSection(
  ultrasounds: List<UltrasoundEntity>,
  onAddClick: () -> Unit,
  viewModel: PregnancyViewModel,
  isEnglish: Boolean = false
) {
  val context = LocalContext.current
  var showAddEditDialog by remember { mutableStateOf(false) }
  var echoToEdit by remember { mutableStateOf<UltrasoundEntity?>(null) }
  var echoForLightbox by remember { mutableStateOf<UltrasoundEntity?>(null) }
  var echoToDelete by remember { mutableStateOf<UltrasoundEntity?>(null) }

  val directCameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: android.graphics.Bitmap? ->
    if (bitmap != null) {
      val savedPath = saveBitmapToInternalStorage(context, bitmap)
      viewModel.saveUltrasound(
        id = 0L,
        dateMs = System.currentTimeMillis(),
        trimester = "T1",
        title = if (isEnglish) "Ultrasound Scan" else "Cliché Échographique",
        notes = if (isEnglish) "Photo captured with camera." else "Photo capturée avec l'appareil photo.",
        imagePath = savedPath
      )
      android.widget.Toast.makeText(context, if (isEnglish) "Photo saved to album! 📸" else "Photo enregistrée dans l'album ! 📸", android.widget.Toast.LENGTH_SHORT).show()
    }
  }

  val directCameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      directCameraLauncher.launch(null)
    } else {
      android.widget.Toast.makeText(context, if (isEnglish) "Camera permission required" else "Permission appareil photo requise", android.widget.Toast.LENGTH_SHORT).show()
    }
  }

  fun triggerCamera() {
    try {
      directCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
    } catch (e: Exception) {
      try {
        directCameraLauncher.launch(null)
      } catch (ex: Exception) {
        android.widget.Toast.makeText(context, if (isEnglish) "Camera error" else "Erreur appareil photo", android.widget.Toast.LENGTH_SHORT).show()
      }
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
          Text(text = if (isEnglish) "Ultrasound Gallery & Baby Album" else "Galerie d'Échographies & Album", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text(text = if (isEnglish) "${ultrasounds.size} scan(s) with photos & doctor reports" else "${ultrasounds.size} cliché(s) avec photos & comptes-rendus", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          FilledTonalIconButton(
            onClick = { triggerCamera() },
            modifier = Modifier.size(42.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer,
              contentColor = MaterialTheme.colorScheme.primary
            )
          ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = if (isEnglish) "Take a photo" else "Prendre une photo")
          }
          Button(onClick = {
            echoToEdit = null
            showAddEditDialog = true
          }) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(if (isEnglish) "Scan" else "Écho")
          }
        }
      }
    }

    if (ultrasounds.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              echoToEdit = null
              showAddEditDialog = true
            },
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier
              .padding(24.dp)
              .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            Text("Aucune échographie enregistrée", fontWeight = FontWeight.Bold)
            Text("Touchez pour ajouter votre premier cliché photo ou examen", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    } else {
      items(ultrasounds) { echo ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Photo if available
            if (echo.imagePath.isNotBlank()) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(160.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(androidx.compose.ui.graphics.Color.Black)
                  .clickable { echoForLightbox = echo }
              ) {
                coil.compose.AsyncImage(
                  model = echo.imagePath,
                  contentDescription = echo.title,
                  contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
                Surface(
                  shape = CircleShape,
                  color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                  modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                ) {
                  Icon(
                    Icons.Default.ZoomIn,
                    contentDescription = "Agrandir",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier
                      .padding(4.dp)
                      .size(18.dp)
                  )
                }
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = echo.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer
              ) {
                Text(
                  text = echo.trimester,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }

            if (echo.notes.isNotBlank()) {
              Text(text = echo.notes, style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = formatDate(echo.dateMs), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (echo.imagePath.isNotBlank()) {
                TextButton(onClick = { echoForLightbox = echo }) {
                  Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("Agrandir")
                }
              } else {
                Spacer(Modifier.width(1.dp))
              }

              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = {
                  echoToEdit = echo
                  showAddEditDialog = true
                }) {
                  Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { echoToDelete = echo }) {
                  Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                }
              }
            }
          }
        }
      }
    }
  }

  if (showAddEditDialog) {
    AddEditUltrasoundDialog(
      echoToEdit = echoToEdit,
      isEnglish = isEnglish,
      onDismiss = {
        showAddEditDialog = false
        echoToEdit = null
      },
      onSave = { id, dateMs, trimester, title, notes, photoPath ->
        viewModel.saveUltrasound(id, dateMs, trimester, title, notes, photoPath)
        showAddEditDialog = false
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
        showAddEditDialog = true
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
}

@Composable
fun CalculatorSection(
  settings: PregnancySettingsEntity?,
  viewModel: PregnancyViewModel,
  onOpenCalc: () -> Unit,
  isEnglish: Boolean = false
) {
  val lmp = settings?.lmpDateMs ?: System.currentTimeMillis()
  val dueDate = viewModel.calculateDueDate(lmp)
  val (weeks, days) = viewModel.calculateCurrentGestationalWeek(lmp)

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = if (isEnglish) "Fetal Calculator & Due Date (EDD)" else "Calculateur Fœtal & Date Prévue (DPA)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Text(
            text = if (isEnglish) "Current gestational age: $weeks GW and $days days" else "Âge gestationnel actuel : $weeks SA et $days jours",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Text(
            text = if (isEnglish) "Estimated Due Date (EDD): ${formatDate(dueDate)}" else "Date Prévue d'Accouchement (DPA) : ${formatDate(dueDate)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }
    }
  }
}

@Composable
fun ProtocolCalendarSection(
  injections: List<InjectionEntity>,
  ultrasounds: List<UltrasoundEntity>,
  viewModel: PregnancyViewModel,
  isEnglish: Boolean = false
) {
  var currentMonthOffset by remember { mutableStateOf(0) }
  var selectedDayMs by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }

  var showAddOptionsDialog by remember { mutableStateOf(false) }
  var showAddAppointmentDialog by remember { mutableStateOf(false) }
  var showAddInjectionDialog by remember { mutableStateOf(false) }
  var showAddEchoDialog by remember { mutableStateOf(false) }
  var showAddWeightDialog by remember { mutableStateOf(false) }

  // Form states for dialogs
  var apptTitle by remember { mutableStateOf("") }
  var apptLocation by remember { mutableStateOf("") }
  var apptNotes by remember { mutableStateOf("") }

  var injMed by remember { mutableStateOf("") }
  var injDose by remember { mutableStateOf("") }
  var injSite by remember { mutableStateOf("") }

  var echoTitle by remember { mutableStateOf("") }
  var echoNotes by remember { mutableStateOf("") }

  var weightVal by remember { mutableStateOf("") }

  val targetDayMs = selectedDayMs ?: System.currentTimeMillis()
  val targetDateStr = formatShortDate(targetDayMs, isEnglish)

  val calendar = Calendar.getInstance()
  calendar.add(Calendar.MONTH, currentMonthOffset)
  val year = calendar.get(Calendar.YEAR)
  val month = calendar.get(Calendar.MONTH)
  val monthLocale = if (isEnglish) Locale.ENGLISH else Locale.FRENCH
  val monthName = SimpleDateFormat("MMMM yyyy", monthLocale).format(calendar.time)

  calendar.set(Calendar.DAY_OF_MONTH, 1)
  val firstDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
  val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // COUNTDOWN & NEXT STEPS BANNER RIGHT ON TOP OF CALENDAR
    item {
      val settings by viewModel.settings.collectAsState()
      val lmp = settings?.lmpDateMs ?: (System.currentTimeMillis() - 60L * 24 * 3600 * 1000L)
      val dueDate = viewModel.calculateDueDate(lmp)
      val nextInj = injections.firstOrNull { !it.isTaken }

      var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
      LaunchedEffect(Unit) {
        while (true) {
          delay(1000L)
          currentTime = System.currentTimeMillis()
        }
      }

      val diffMs = maxOf(0L, dueDate - currentTime)
      val days = TimeUnit.MILLISECONDS.toDays(diffMs)
      val hours = TimeUnit.MILLISECONDS.toHours(diffMs) % 24
      val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs) % 60
      val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMs) % 60

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
              Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Text(
                text = if (isEnglish) "Countdown to Due Date" else "Compte à rebours avant DPA",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
            }
          }

          // 4-Column Countdown Timer: DD : HH : MM : SS with labels underneath D, HR, MIN, S
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = String.format(Locale.getDefault(), "%02d", days), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              Text(text = "D", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(":", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = String.format(Locale.getDefault(), "%02d", hours), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              Text(text = "HR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(":", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = String.format(Locale.getDefault(), "%02d", minutes), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              Text(text = "MIN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(":", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = String.format(Locale.getDefault(), "%02d", seconds), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              Text(text = "S", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(Icons.Default.NextWeek, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
            Text(
              text = if (isEnglish) {
                "Next Step: " + (nextInj?.let { "Take ${it.medicationName} (${it.dose})" } ?: "All medication steps completed")
              } else {
                "Prochaine étape : " + (nextInj?.let { "Prendre ${it.medicationName} (${it.dose})" } ?: "Tous les traitements terminés")
              },
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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
            IconButton(onClick = { currentMonthOffset-- }) {
              Icon(Icons.Default.ChevronLeft, contentDescription = if (isEnglish) "Previous month" else "Mois précédent")
            }
            Text(
              text = monthName.replaceFirstChar { it.uppercase() },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            IconButton(onClick = { currentMonthOffset++ }) {
              Icon(Icons.Default.ChevronRight, contentDescription = if (isEnglish) "Next month" else "Mois suivant")
            }
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            val weekDays = if (isEnglish) listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") else listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
            weekDays.forEach { day ->
              Text(text = day, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
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
                  val dayCal = Calendar.getInstance().apply {
                    set(year, month, dayNum, 0, 0, 0)
                  }
                  val dayStart = dayCal.timeInMillis
                  val dayEnd = dayStart + 24 * 60 * 60 * 1000L

                  val hasInjection = injections.any { it.dateMs in dayStart..dayEnd }
                  val hasEcho = ultrasounds.any { it.dateMs in dayStart..dayEnd }

                  Box(
                    modifier = Modifier
                      .size(40.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (selectedDayMs == dayStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                      .clickable { 
                        selectedDayMs = dayStart
                        showAddOptionsDialog = true
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text(
                        text = dayNum.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedDayMs == dayStart) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                      )
                      Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (hasInjection) {
                          Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
                        }
                        if (hasEcho) {
                          Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
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
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (isEnglish) "Events for $targetDateStr" else "Événements du $targetDateStr",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (isEnglish) "Tap any date to add appointments & more" else "Touchez une date pour ajouter rdv & plus",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Button(
          onClick = { showAddOptionsDialog = true },
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("btn_calendar_add_item")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text(if (isEnglish) "Add Item" else "Ajouter")
        }
      }
    }

    val dayInjections = injections.filter {
      val diff = Math.abs(it.dateMs - targetDayMs)
      diff < 24 * 60 * 60 * 1000L
    }
    val dayEchos = ultrasounds.filter {
      val diff = Math.abs(it.dateMs - targetDayMs)
      diff < 24 * 60 * 60 * 1000L
    }

    if (dayInjections.isEmpty() && dayEchos.isEmpty()) {
      item {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
          Text(
            text = if (isEnglish) "No events scheduled for this day. Click 'Add' to schedule an appointment or treatment." else "Aucun événement prévu ce jour. Cliquez sur 'Ajouter' pour programmer.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      items(dayInjections) { inj ->
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "💉 ${inj.medicationName} (${inj.dose})", fontWeight = FontWeight.Bold)
            Text(text = if (isEnglish) "Site: ${inj.site}" else "Zone : ${inj.site}", style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
      items(dayEchos) { echo ->
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "🖥️ ${echo.title}", fontWeight = FontWeight.Bold)
            Text(text = echo.notes, style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
    }
  }

  // ADD OPTIONS POPUP DIALOG
  if (showAddOptionsDialog) {
    AlertDialog(
      onDismissRequest = { showAddOptionsDialog = false },
      title = { Text(if (isEnglish) "Add Event for $targetDateStr" else "Ajouter pour le $targetDateStr") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = if (isEnglish) "Choose what you need to add for this date:" else "Choisissez l'élément à ajouter pour cette date :",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Button(
            onClick = {
              showAddOptionsDialog = false
              showAddAppointmentDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
          ) {
            Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (isEnglish) "Medical Appointment" else "Rendez-vous Médical", fontWeight = FontWeight.Bold)
          }
          Button(
            onClick = {
              showAddOptionsDialog = false
              showAddInjectionDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
          ) {
            Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (isEnglish) "Medication / Injection" else "Injection / Traitement", fontWeight = FontWeight.Bold)
          }
          Button(
            onClick = {
              showAddOptionsDialog = false
              showAddEchoDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
          ) {
            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (isEnglish) "Ultrasound / Scan" else "Échographie / Examen", fontWeight = FontWeight.Bold)
          }
          Button(
            onClick = {
              showAddOptionsDialog = false
              showAddWeightDialog = true
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.MonitorWeight, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (isEnglish) "Weight / Vitals" else "Poids / Pesée", fontWeight = FontWeight.Bold)
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showAddOptionsDialog = false }) {
          Text(if (isEnglish) "Cancel" else "Annuler")
        }
      }
    )
  }

  // SUB-DIALOG: ADD APPOINTMENT
  if (showAddAppointmentDialog) {
    AlertDialog(
      onDismissRequest = { showAddAppointmentDialog = false },
      title = { Text(if (isEnglish) "Add Appointment ($targetDateStr)" else "Ajouter un Rendez-vous ($targetDateStr)") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = apptTitle,
            onValueChange = { apptTitle = it },
            label = { Text(if (isEnglish) "Appointment Title" else "Titre du Rendez-vous") },
            placeholder = { Text("Ex: Échographie T1 / Consultation") }
          )
          OutlinedTextField(
            value = apptLocation,
            onValueChange = { apptLocation = it },
            label = { Text(if (isEnglish) "Location / Clinic" else "Lieu / Clinique") },
            placeholder = { Text("Ex: Cabinet Gynécologue") }
          )
          OutlinedTextField(
            value = apptNotes,
            onValueChange = { apptNotes = it },
            label = { Text(if (isEnglish) "Notes / Instructions" else "Notes / Instructions") }
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (apptTitle.isNotBlank()) {
              val appt = MedicalAppointmentEntity(
                weeksRange = "Personnalisé",
                title = apptTitle.trim(),
                location = apptLocation.ifBlank { "Clinique" },
                scheduledDateStr = targetDateStr,
                scheduledTimeStr = "09:00",
                datePeriodDisplay = targetDateStr,
                description = "Ajouté via le calendrier",
                keyExams = "",
                doctorName = "",
                notes = apptNotes.trim()
              )
              viewModel.saveAppointment(appt)
              apptTitle = ""
              apptLocation = ""
              apptNotes = ""
              showAddAppointmentDialog = false
            }
          }
        ) {
          Text(if (isEnglish) "Save" else "Enregistrer")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddAppointmentDialog = false }) {
          Text(if (isEnglish) "Cancel" else "Annuler")
        }
      }
    )
  }

  // SUB-DIALOG: ADD INJECTION
  if (showAddInjectionDialog) {
    AlertDialog(
      onDismissRequest = { showAddInjectionDialog = false },
      title = { Text(if (isEnglish) "Add Medication ($targetDateStr)" else "Ajouter Traitement ($targetDateStr)") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = injMed,
            onValueChange = { injMed = it },
            label = { Text(if (isEnglish) "Medication Name" else "Nom du Médicament") },
            placeholder = { Text("Ex: Progestérone / Gonal-F") }
          )
          OutlinedTextField(
            value = injDose,
            onValueChange = { injDose = it },
            label = { Text(if (isEnglish) "Dose" else "Dosage") },
            placeholder = { Text("Ex: 100mg / 150UI") }
          )
          OutlinedTextField(
            value = injSite,
            onValueChange = { injSite = it },
            label = { Text(if (isEnglish) "Injection Site" else "Zone d'injection") },
            placeholder = { Text("Ex: Sous-cutanée / Intramusculaire") }
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (injMed.isNotBlank()) {
              viewModel.addInjection(
                medicationName = injMed.trim(),
                dose = injDose.ifBlank { "1 dose" },
                site = injSite.ifBlank { "Général" },
                notes = "Ajouté via le calendrier",
                isTaken = false
              )
              injMed = ""
              injDose = ""
              injSite = ""
              showAddInjectionDialog = false
            }
          }
        ) {
          Text(if (isEnglish) "Save" else "Enregistrer")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddInjectionDialog = false }) {
          Text(if (isEnglish) "Cancel" else "Annuler")
        }
      }
    )
  }

  // SUB-DIALOG: ADD ULTRASOUND
  if (showAddEchoDialog) {
    AlertDialog(
      onDismissRequest = { showAddEchoDialog = false },
      title = { Text(if (isEnglish) "Add Ultrasound ($targetDateStr)" else "Ajouter Échographie ($targetDateStr)") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = echoTitle,
            onValueChange = { echoTitle = it },
            label = { Text(if (isEnglish) "Scan Title" else "Titre de l'examen") },
            placeholder = { Text("Ex: Échographie de contrôle") }
          )
          OutlinedTextField(
            value = echoNotes,
            onValueChange = { echoNotes = it },
            label = { Text(if (isEnglish) "Notes / Observations" else "Notes / Observations") }
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (echoTitle.isNotBlank()) {
              viewModel.addUltrasound(
                trimester = "PMA / Suivi",
                title = echoTitle.trim(),
                notes = echoNotes.trim(),
                imagePath = ""
              )
              echoTitle = ""
              echoNotes = ""
              showAddEchoDialog = false
            }
          }
        ) {
          Text(if (isEnglish) "Save" else "Enregistrer")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddEchoDialog = false }) {
          Text(if (isEnglish) "Cancel" else "Annuler")
        }
      }
    )
  }

  // SUB-DIALOG: ADD WEIGHT
  if (showAddWeightDialog) {
    AlertDialog(
      onDismissRequest = { showAddWeightDialog = false },
      title = { Text(if (isEnglish) "Add Weight ($targetDateStr)" else "Ajouter Pesée ($targetDateStr)") },
      text = {
        OutlinedTextField(
          value = weightVal,
          onValueChange = { weightVal = it },
          label = { Text(if (isEnglish) "Weight (kg)" else "Poids (kg)") },
          placeholder = { Text("Ex: 65.5") },
          singleLine = true
        )
      },
      confirmButton = {
        Button(
          onClick = {
            val kg = weightVal.toFloatOrNull()
            if (kg != null) {
              viewModel.addWeight(kg, "Ajouté via le calendrier")
              weightVal = ""
              showAddWeightDialog = false
            }
          }
        ) {
          Text(if (isEnglish) "Save" else "Enregistrer")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddWeightDialog = false }) {
          Text(if (isEnglish) "Cancel" else "Annuler")
        }
      }
    )
  }
}

