package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.viewmodel.PregnancyViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

fun exportCompletePdf(context: Context, viewModel: PregnancyViewModel, isEnglish: Boolean) {
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
    color = android.graphics.Color.rgb(74, 53, 37) // Sepia Primary
  }

  val headerText = if (isEnglish) "MAMAN & BEBE - COMPLETE MEDICAL DOSSIER" else "MAMAN & BEBE - DOSSIER MÉDICAL COMPLET"
  canvas.drawText(headerText, 40f, y, titlePaint)
  y += 28f

  val settings = viewModel.settings.value
  val lmp = settings?.lmpDateMs ?: System.currentTimeMillis()
  val (weeks, days) = viewModel.calculateCurrentGestationalWeek(lmp)
  val dueDate = viewModel.calculateDueDate(lmp)
  val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

  canvas.drawText(if (isEnglish) "Baby Name: ${settings?.babyName ?: "Baby"}" else "Nom du Bébé : ${settings?.babyName ?: "Bébé"}", 40f, y, paint)
  y += 16f
  canvas.drawText(if (isEnglish) "Current Term: $weeks Weeks + $days Days (Due: ${sdfDate.format(Date(dueDate))})" else "Terme actuel : $weeks Semaines + $days Jours (DPA : ${sdfDate.format(Date(dueDate))})", 40f, y, paint)
  y += 24f

  // Appointments
  canvas.drawText(if (isEnglish) "--- Medical Appointments ---" else "--- Consultations & Rendez-vous ---", 40f, y, titlePaint)
  y += 18f
  viewModel.appointments.value.take(8).forEach { appt ->
    if (y > 780f) return@forEach
    canvas.drawText("• ${appt.title} (${appt.scheduledDateStr} - ${appt.scheduledTimeStr})", 40f, y, paint)
    y += 15f
    canvas.drawText("  Location: ${appt.location} | Doctor: ${appt.doctorName}", 50f, y, paint)
    y += 18f
  }

  y += 10f
  // Injections / Medications
  canvas.drawText(if (isEnglish) "--- Medication & IVF Protocol ---" else "--- Traitements & Protocole PMA ---", 40f, y, titlePaint)
  y += 18f
  viewModel.injections.value.take(10).forEach { inj ->
    if (y > 780f) return@forEach
    canvas.drawText("• ${inj.medicationName} (${inj.dose}) - Site: ${inj.site}", 40f, y, paint)
    y += 18f
  }

  pdfDocument.finishPage(page)

  val file = File(context.cacheDir, "Dossier_Complet_Maman_Bebe.pdf")
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
  try {
    context.startActivity(Intent.createChooser(intent, if (isEnglish) "Export PDF Dossier" else "Exporter le Dossier PDF"))
  } catch (e: Exception) {
    Toast.makeText(context, if (isEnglish) "Export failed" else "Échec de l'export", Toast.LENGTH_SHORT).show()
  }
}
