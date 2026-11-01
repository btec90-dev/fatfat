package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.data.BirthPlanQuestion
import com.example.data.JournalEntity
import com.example.data.PregnancyData
import com.example.ui.components.formatDate
import com.example.viewmodel.PregnancyViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectJournalScreen(
  viewModel: PregnancyViewModel,
  isEnglish: Boolean = false
) {
  var selectedTab by remember { mutableStateOf(0) }
  val tabs = if (isEnglish) {
    listOf("Baby & Mom Checklist", "Diary & Symptoms")
  } else {
    listOf("Trousseau Bébé & Maman", "Carnet & Symptômes")
  }
  val context = LocalContext.current
  val journals by viewModel.journals.collectAsState()
  val selectedAnswers = remember { mutableStateMapOf<String, String>() }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title, fontWeight = FontWeight.Bold) }
        )
      }
    }

    Box(modifier = Modifier.fillMaxSize()) {
      when (selectedTab) {
        0 -> BabyMomChecklistSection(
          isEnglish = isEnglish,
          onExportPdf = {
            exportPdf(context, selectedAnswers, journals, isEnglish)
          }
        )
        1 -> JournalSection(
          viewModel = viewModel,
          journals = journals,
          isEnglish = isEnglish,
          onExportPdf = {
            exportPdf(context, selectedAnswers, journals, isEnglish)
          }
        )
      }
    }
  }
}

fun exportPdf(
  context: Context,
  answers: Map<String, String>,
  journals: List<JournalEntity>,
  isEnglish: Boolean = false
) {
  val pdfDocument = PdfDocument()
  val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
  val page = pdfDocument.startPage(pageInfo)
  val canvas = page.canvas
  val paint = Paint().apply {
    textSize = 12f
    color = android.graphics.Color.BLACK
  }

  var y = 40f
  val titlePaint = Paint().apply {
    textSize = 16f
    isFakeBoldText = true
    color = android.graphics.Color.rgb(160, 82, 45) // Warm Primary
  }

  val headerText = if (isEnglish) "MEDICAL SUMMARY - BIRTH PLAN & JOURNAL" else "DOSSIER MEDICAL - PROJET DE NAISSANCE & CARNET"
  canvas.drawText(headerText, 40f, y, titlePaint)
  y += 30f

  val birthPlanHeader = if (isEnglish) "--- Birth Plan Preferences ---" else "--- Projet de Naissance ---"
  canvas.drawText(birthPlanHeader, 40f, y, titlePaint)
  y += 20f

  PregnancyData.birthPlanQuestions.forEach { q ->
    val ans = answers[q.id] ?: if (isEnglish) "Not answered" else "Non renseigné"
    if (y > 780f) return@forEach
    canvas.drawText("• ${q.theme}: ${q.question}", 40f, y, paint)
    y += 16f
    canvas.drawText("  Choice: $ans", 50f, y, paint)
    y += 22f
  }

  y += 10f
  val journalHeader = if (isEnglish) "--- Personal Diary & Recent Symptoms ---" else "--- Carnet Intime & Symptômes Récents ---"
  canvas.drawText(journalHeader, 40f, y, titlePaint)
  y += 20f

  journals.take(15).forEach { j ->
    if (y > 780f) return@forEach
    canvas.drawText("• Date: ${formatDate(j.dateMs)} | Mood: ${j.mood}", 40f, y, paint)
    y += 16f
    canvas.drawText("  Symptoms: ${j.symptoms} - Notes: ${j.notes}", 50f, y, paint)
    y += 22f
  }

  pdfDocument.finishPage(page)

  val file = File(context.cacheDir, "Dossier_Medical_Maman_Bebe.pdf")
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
  context.startActivity(Intent.createChooser(intent, if (isEnglish) "Share PDF Summary" else "Partager le Dossier PDF"))
}

/**
 * Exports ONLY personal notes from the Carnet Intime to a CSV spreadsheet file for local backup.
 */
fun exportJournalNotesCsv(context: Context, journals: List<JournalEntity>, isEnglish: Boolean) {
  val fileName = if (isEnglish) "Carnet_Intime_Notes_Backup.csv" else "Carnet_Intime_Notes.csv"
  val file = File(context.cacheDir, fileName)

  val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
  val header = if (isEnglish) {
    "ID,Date,Mood,Symptoms,Personal Notes\n"
  } else {
    "ID,Date,Humeur,Symptômes,Notes Personnelles\n"
  }

  val sb = StringBuilder()
  // UTF-8 BOM for Microsoft Excel / Sheets compatibility
  sb.append("\uFEFF")
  sb.append(header)

  journals.forEach { j ->
    val dateStr = sdf.format(Date(j.dateMs))
    val moodEscaped = escapeCsv(j.mood)
    val symptomsEscaped = escapeCsv(j.symptoms)
    val notesEscaped = escapeCsv(j.notes)
    sb.append("${j.id},$dateStr,$moodEscaped,$symptomsEscaped,$notesEscaped\n")
  }

  try {
    FileOutputStream(file).use { fos ->
      fos.write(sb.toString().toByteArray(Charsets.UTF_8))
    }
    shareFile(
      context = context,
      file = file,
      mimeType = "text/csv",
      chooserTitle = if (isEnglish) "Export Personal Notes (CSV Backup)" else "Exporter le Carnet Intime (Sauvegarde CSV)"
    )
  } catch (e: Exception) {
    e.printStackTrace()
    Toast.makeText(context, if (isEnglish) "Export error: ${e.message}" else "Erreur d'exportation : ${e.message}", Toast.LENGTH_SHORT).show()
  }
}

/**
 * Exports ONLY personal notes from the Carnet Intime to a clean, human-readable structured text file.
 */
fun exportJournalNotesTxt(context: Context, journals: List<JournalEntity>, isEnglish: Boolean) {
  val fileName = if (isEnglish) "Carnet_Intime_Notes_Backup.txt" else "Carnet_Intime_Notes.txt"
  val file = File(context.cacheDir, fileName)

  val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
  val exportDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

  val sb = StringBuilder()
  if (isEnglish) {
    sb.append("============================================================\n")
    sb.append("CARNET INTIME & PERSONAL NOTES - LOCAL BACKUP\n")
    sb.append("Export Date: $exportDate\n")
    sb.append("Total Recorded Entries: ${journals.size}\n")
    sb.append("============================================================\n\n")

    journals.forEachIndexed { index, j ->
      sb.append("[Entry #${index + 1}]\n")
      sb.append("  • Date: ${sdf.format(Date(j.dateMs))}\n")
      sb.append("  • Mood: ${j.mood}\n")
      if (j.symptoms.isNotEmpty()) {
        sb.append("  • Symptoms: ${j.symptoms}\n")
      }
      sb.append("  • Personal Notes:\n")
      sb.append("    ${j.notes.replace("\n", "\n    ")}\n")
      sb.append("------------------------------------------------------------\n\n")
    }
  } else {
    sb.append("============================================================\n")
    sb.append("CARNET INTIME & NOTES PERSONNELLES - SAUVEGARDE LOCALE\n")
    sb.append("Date d'exportation : $exportDate\n")
    sb.append("Nombre total d'entrées : ${journals.size}\n")
    sb.append("============================================================\n\n")

    journals.forEachIndexed { index, j ->
      sb.append("[Entrée #${index + 1}]\n")
      sb.append("  • Date : ${sdf.format(Date(j.dateMs))}\n")
      sb.append("  • Humeur : ${j.mood}\n")
      if (j.symptoms.isNotEmpty()) {
        sb.append("  • Symptômes : ${j.symptoms}\n")
      }
      sb.append("  • Notes personnelles :\n")
      sb.append("    ${j.notes.replace("\n", "\n    ")}\n")
      sb.append("------------------------------------------------------------\n\n")
    }
  }

  try {
    FileOutputStream(file).use { fos ->
      fos.write(sb.toString().toByteArray(Charsets.UTF_8))
    }
    shareFile(
      context = context,
      file = file,
      mimeType = "text/plain",
      chooserTitle = if (isEnglish) "Export Personal Notes (Text Backup)" else "Exporter le Carnet Intime (Sauvegarde Texte)"
    )
  } catch (e: Exception) {
    e.printStackTrace()
    Toast.makeText(context, if (isEnglish) "Export error: ${e.message}" else "Erreur d'exportation : ${e.message}", Toast.LENGTH_SHORT).show()
  }
}

private fun escapeCsv(value: String): String {
  val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
  val escaped = value.replace("\"", "\"\"")
  return if (needsQuotes) "\"$escaped\"" else escaped
}

private fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
  val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
  val intent = Intent(Intent.ACTION_SEND).apply {
    type = mimeType
    putExtra(Intent.EXTRA_STREAM, uri)
    putExtra(Intent.EXTRA_SUBJECT, file.name)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
  }
  context.startActivity(Intent.createChooser(intent, chooserTitle))
}

@Composable
fun BirthPlanSection(
  selectedAnswers: MutableMap<String, String>,
  isEnglish: Boolean = false,
  onExportPdf: () -> Unit
) {
  val questions = PregnancyData.birthPlanQuestions

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(10.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isEnglish) "Birth Plan" else "Projet de Naissance",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = if (isEnglish) "Your personalized wishes for the labor & delivery team" else "Vos souhaits pour l'équipe médicale",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Button(onClick = onExportPdf) {
          Icon(Icons.Default.Share, contentDescription = null)
          Spacer(Modifier.width(4.dp))
          Text(if (isEnglish) "Export PDF" else "PDF")
        }
      }
    }

    items(questions) { q ->
      val displayTheme = if (isEnglish) {
        when (q.theme) {
          "Accueil" -> "Environment"
          "Travail" -> "Labor"
          "Péridurale" -> "Pain Relief"
          "Accouchement" -> "Delivery"
          "Nouveau-né" -> "Newborn First Moments"
          else -> q.theme
        }
      } else {
        q.theme
      }

      val displayQuestion = if (isEnglish) {
        when (q.id) {
          "q1" -> "Would you like dimmed lighting and your own soothing playlist during labor?"
          "q2" -> "Which non-pharmacological pain relief methods do you prefer?"
          "q3" -> "What is your preference regarding an epidural?"
          "q4" -> "How do you envision the pushing stage?"
          "q5" -> "What are your special wishes for baby's very first moments?"
          else -> q.question
        }
      } else {
        q.question
      }

      val displayOptions = if (isEnglish) {
        when (q.id) {
          "q1" -> listOf("Yes, dimmed soothing atmosphere and personal playlist", "Standard delivery room illumination", "No specific preference")
          "q2" -> listOf("Birthing ball, walking, and warm showers", "Lower back massage and counter-pressure", "Full freedom of movement following natural instinct")
          "q3" -> listOf("Request epidural as early as possible", "Low-dose walking epidural to maintain mobility", "Aim to manage naturally without epidural as long as possible")
          "q4" -> listOf("Coached pushing directed by medical team", "Spontaneous physiological pushing with contractions", "Use of a delivery mirror to witness birth")
          "q5" -> listOf("Immediate and prolonged skin-to-skin contact", "Umbilical cord cut by partner after pulsation stops", "First bath and weighing postponed after initial nursing")
          else -> q.options
        }
      } else {
        q.options
      }

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primaryContainer
          ) {
            Text(
              text = displayTheme,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = displayQuestion,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )

          displayOptions.forEach { option ->
            val isSelected = selectedAnswers[q.id] == option
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = isSelected,
                onClick = { selectedAnswers[q.id] = option }
              )
              Spacer(Modifier.width(8.dp))
              Text(
                text = option,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun JournalSection(
  viewModel: PregnancyViewModel,
  journals: List<JournalEntity>,
  isEnglish: Boolean = false,
  onExportPdf: () -> Unit
) {
  var showAddDialog by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf(false) }
  val context = LocalContext.current

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
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isEnglish) "Diary & Symptoms" else "Carnet Intime & Symptômes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = if (isEnglish) "Mood, physical feelings, and notes" else "Humeur, maux et notes personnelles",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(onClick = onExportPdf) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("PDF")
          }
          Button(onClick = { showAddDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(if (isEnglish) "Entry" else "Entrée")
          }
        }
      }
    }

    // Dedicated Local Export Card specifically for the Carnet Intime notes
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.FileDownload,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSecondary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Column {
              Text(
                text = if (isEnglish) "Export Personal Notes" else "Sauvegarde locale des notes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
              )
              Text(
                text = if (isEnglish) "Export only personal notes to CSV or Text" else "Exporter uniquement les notes au format CSV ou Texte",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
              )
            }
          }
          FilledTonalButton(
            onClick = {
              if (journals.isEmpty()) {
                Toast.makeText(
                  context,
                  if (isEnglish) "No notes to export yet." else "Aucune note à exporter pour le moment.",
                  Toast.LENGTH_SHORT
                ).show()
              } else {
                showExportDialog = true
              }
            }
          ) {
            Text(if (isEnglish) "Export" else "Exporter")
          }
        }
      }
    }

    if (journals.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp)
        ) {
          Text(
            text = if (isEnglish) "No diary entries yet. Tap 'Entry' to log your feelings!" else "Aucune entrée pour l'instant. Appuyez sur 'Entrée' pour noter votre journée !",
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      items(journals) { j ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = (if (isEnglish) "Mood: " else "Humeur : ") + j.mood,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              IconButton(onClick = { viewModel.deleteJournal(j) }) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
              }
            }
            if (j.symptoms.isNotEmpty()) {
              Text(
                text = (if (isEnglish) "Symptoms: " else "Symptômes : ") + j.symptoms,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
              )
            }
            Text(text = j.notes, style = MaterialTheme.typography.bodyMedium)
            Text(
              text = formatDate(j.dateMs),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }

  // Dialog for choosing between CSV and Structured Text export
  if (showExportDialog) {
    AlertDialog(
      onDismissRequest = { showExportDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Text(if (isEnglish) "Export Personal Notes" else "Exporter le Carnet Intime")
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = if (isEnglish) {
              "Select a local backup format for your personal notes (${journals.size} entries):"
            } else {
              "Choisissez le format de sauvegarde locale pour vos notes personnelles (${journals.size} entrées) :"
            },
            style = MaterialTheme.typography.bodyMedium
          )

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                showExportDialog = false
                exportJournalNotesCsv(context, journals, isEnglish)
              },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Icon(Icons.Default.TableChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Column {
                Text(
                  text = if (isEnglish) "CSV Spreadsheet (.csv)" else "Format Tableur CSV (.csv)",
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.titleSmall
                )
                Text(
                  text = if (isEnglish) "Structured columns (Date, Mood, Symptoms, Notes) for Excel & Sheets" else "Colonnes structurées (Date, Humeur, Symptômes, Notes) pour Excel & tableurs",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                showExportDialog = false
                exportJournalNotesTxt(context, journals, isEnglish)
              },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
              Column {
                Text(
                  text = if (isEnglish) "Structured Text Document (.txt)" else "Document Texte Structuré (.txt)",
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.titleSmall
                )
                Text(
                  text = if (isEnglish) "Readable chronological offline log for personal reading & backup" else "Journal chronologique lisible pour archivage personnel ou lecture hors-ligne",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showExportDialog = false }) {
          Text(if (isEnglish) "Cancel" else "Annuler")
        }
      }
    )
  }

  if (showAddDialog) {
    var mood by remember { mutableStateOf("😊") }
    var symptoms by remember { mutableStateOf(if (isEnglish) "Fatigue, mild nausea" else "Fatigue, Nausées") }
    var notes by remember { mutableStateOf(if (isEnglish) "Peaceful day with gentle kicks." else "Journée paisible.") }

    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text(if (isEnglish) "New Diary Entry" else "Nouvelle entrée au carnet") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = mood,
            onValueChange = { mood = it },
            label = { Text(if (isEnglish) "Mood emoji (e.g. 😊, 🥱, 😌)" else "Humeur (ex: 😊, 🥱, 😌)") }
          )
          OutlinedTextField(
            value = symptoms,
            onValueChange = { symptoms = it },
            label = { Text(if (isEnglish) "Physical symptoms" else "Symptômes physiques") }
          )
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(if (isEnglish) "Personal notes" else "Notes personnelles") }
          )
        }
      },
      confirmButton = {
        TextButton(onClick = {
          viewModel.addJournal(mood, symptoms, notes)
          showAddDialog = false
        }) { Text(if (isEnglish) "Save" else "Enregistrer") }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) { Text(if (isEnglish) "Cancel" else "Annuler") }
      }
    )
  }
}
