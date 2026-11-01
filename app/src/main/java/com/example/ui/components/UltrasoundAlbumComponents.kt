package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.UltrasoundEntity
import java.io.File

fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
  val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
  val file = File(photosDir, "echo_${System.currentTimeMillis()}.jpg")
  file.outputStream().use { out ->
    bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
  }
  return file.absolutePath
}

// Sample high-quality ultrasound image presets for quick testing and realistic previews
val SAMPLE_ULTRASOUND_PRESETS = listOf(
  Pair("8 SA (Viabilité)", "https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?auto=format&fit=crop&w=800&q=80"),
  Pair("12 SA (Clarté Nucale)", "https://images.unsplash.com/photo-1579684385127-1ef15d508118?auto=format&fit=crop&w=800&q=80"),
  Pair("22 SA (Morpho 3D)", "https://images.unsplash.com/photo-1516549655169-df83a0774514?auto=format&fit=crop&w=800&q=80"),
  Pair("32 SA (Croissance)", "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?auto=format&fit=crop&w=800&q=80")
)

@Composable
fun HomeUltrasoundAlbumSection(
  ultrasounds: List<UltrasoundEntity>,
  isEnglish: Boolean = false,
  onAddClick: () -> Unit,
  onEditClick: (UltrasoundEntity) -> Unit,
  onDeleteClick: (UltrasoundEntity) -> Unit,
  onViewPhoto: (UltrasoundEntity) -> Unit,
  onQuickCameraSaved: ((String) -> Unit)? = null
) {
  val context = LocalContext.current
  val directCameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    if (bitmap != null) {
      val savedPath = saveBitmapToInternalStorage(context, bitmap)
      onQuickCameraSaved?.invoke(savedPath)
      Toast.makeText(
        context,
        if (isEnglish) "Photo saved to gallery! 📸" else "Photo enregistrée dans la galerie ! 📸",
        Toast.LENGTH_SHORT
      ).show()
    }
  }

  val directCameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      directCameraLauncher.launch(null)
    } else {
      Toast.makeText(
        context,
        if (isEnglish) "Camera permission is needed to take pictures" else "Permission appareil photo requise pour capturer",
        Toast.LENGTH_SHORT
      ).show()
    }
  }

  fun triggerCamera() {
    directCameraLauncher.launch(null)
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Header
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
          Text(
            text = if (isEnglish) "Ultrasounds & Baby Album" else "Échographies & Album Clichés",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = if (isEnglish) {
              "${ultrasounds.size} scan(s) & photo attachments"
            } else {
              "${ultrasounds.size} cliché(s) & photos archivés"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Action Buttons: 'Ajouter cliché' stacked in the bottom of 'Prendre photo'
        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          FilledTonalButton(
            onClick = { triggerCamera() },
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PhotoCamera,
              contentDescription = if (isEnglish) "Take Picture" else "Prendre photo",
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isEnglish) "Take Photo" else "Prendre photo",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold
            )
          }

          Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AddPhotoAlternate,
              contentDescription = null,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isEnglish) "Add Scan" else "Ajouter cliché",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // Content: Empty State or Horizontal Album Row
      if (ultrasounds.isEmpty()) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PhotoCamera,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(36.dp)
            )
            Text(
              text = if (isEnglish) "No ultrasound photo attached yet" else "Aucune photo d'échographie enregistrée",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isEnglish) {
                "Take a picture with your camera or add a scan from your gallery"
              } else {
                "Prenez directement une photo avec votre appareil photo ou choisissez depuis la galerie"
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Button(
                onClick = { triggerCamera() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.8f)
              ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isEnglish) "Take Photo" else "Prendre photo")
              }
              OutlinedButton(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.8f)
              ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isEnglish) "Add Scan" else "Ajouter cliché")
              }
            }
          }
        }
      } else {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
          items(ultrasounds) { echo ->
            UltrasoundCardItem(
              echo = echo,
              isEnglish = isEnglish,
              onViewPhoto = { onViewPhoto(echo) },
              onEditClick = { onEditClick(echo) },
              onDeleteClick = { onDeleteClick(echo) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun UltrasoundCardItem(
  echo: UltrasoundEntity,
  isEnglish: Boolean,
  onViewPhoto: () -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit
) {
  val hasPhoto = echo.imagePath.isNotBlank()

  Card(
    modifier = Modifier
      .width(230.dp)
      .clip(RoundedCornerShape(18.dp)),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Column {
      // Photo Container
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
          .background(Color(0xFF18181B))
          .clickable { onViewPhoto() }
      ) {
        if (hasPhoto) {
          AsyncImage(
            model = echo.imagePath,
            contentDescription = echo.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.MedicalServices,
              contentDescription = null,
              tint = Color(0xFFF472B6),
              modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isEnglish) "Ultrasound Scan" else "Cliché Échographique",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.8f)
            )
          }
        }

        // Top Trimester Badge
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(8.dp)
        ) {
          Text(
            text = echo.trimester,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        // Tap to expand indicator
        Surface(
          shape = CircleShape,
          color = Color.Black.copy(alpha = 0.6f),
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ZoomIn,
            contentDescription = "Agrandir",
            tint = Color.White,
            modifier = Modifier
              .padding(4.dp)
              .size(16.dp)
          )
        }
      }

      // Card Information
      Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text(
          text = echo.title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Text(
          text = formatShortDate(echo.dateMs, isEnglish),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (echo.notes.isNotBlank()) {
          Text(
            text = echo.notes,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
          )
        }

        Divider(modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Action Buttons Row: View, Edit, Delete
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(
            onClick = onViewPhoto,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
          ) {
            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isEnglish) "View" else "Voir", style = MaterialTheme.typography.labelSmall)
          }

          Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(
              onClick = onEditClick,
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = if (isEnglish) "Edit" else "Modifier",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
            }
            IconButton(
              onClick = onDeleteClick,
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = if (isEnglish) "Delete" else "Supprimer",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun AddEditUltrasoundDialog(
  echoToEdit: UltrasoundEntity?,
  isEnglish: Boolean,
  onDismiss: () -> Unit,
  onSave: (id: Long, dateMs: Long, trimester: String, title: String, notes: String, photoPath: String) -> Unit
) {
  var title by remember { mutableStateOf(echoToEdit?.title ?: if (isEnglish) "Ultrasound Scan" else "Échographie T1") }
  var trimester by remember { mutableStateOf(echoToEdit?.trimester ?: "T1") }
  var notes by remember { mutableStateOf(echoToEdit?.notes ?: "") }
  var photoPath by remember { mutableStateOf(echoToEdit?.imagePath ?: "") }

  val context = LocalContext.current

  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    if (bitmap != null) {
      photoPath = saveBitmapToInternalStorage(context, bitmap)
      Toast.makeText(
        context,
        if (isEnglish) "Photo captured! 📸" else "Photo capturée ! 📸",
        Toast.LENGTH_SHORT
      ).show()
    }
  }

  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      cameraLauncher.launch(null)
    } else {
      Toast.makeText(
        context,
        if (isEnglish) "Camera permission is required" else "Permission appareil photo requise",
        Toast.LENGTH_SHORT
      ).show()
    }
  }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      photoPath = uri.toString()
    }
  }

  val trimesterOptions = listOf(
    Pair("Précoce", if (isEnglish) "Early (6-8w)" else "Précoce (6-8 SA)"),
    Pair("T1", if (isEnglish) "1st Trim (11-13w)" else "T1 (11-13 SA)"),
    Pair("T2", if (isEnglish) "2nd Trim (22w)" else "T2 (22 SA)"),
    Pair("T3", if (isEnglish) "3rd Trim (32w)" else "T3 (32 SA)"),
    Pair("Contrôle", if (isEnglish) "Follow-up" else "Contrôle")
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (echoToEdit != null) {
          if (isEnglish) "Edit Ultrasound & Photo" else "Modifier l'Échographie"
        } else {
          if (isEnglish) "Add Ultrasound & Photo" else "Ajouter une Échographie"
        },
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(if (isEnglish) "Exam Title" else "Titre de l'examen") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Trimester selector chips
        Text(
          text = if (isEnglish) "Trimester / Stage" else "Trimestre / Étape",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          trimesterOptions.take(3).forEach { (code, label) ->
            FilterChip(
              selected = trimester == code,
              onClick = { trimester = code },
              label = { Text(label, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          trimesterOptions.drop(3).forEach { (code, label) ->
            FilterChip(
              selected = trimester == code,
              onClick = { trimester = code },
              label = { Text(label, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }

        // Photo Attachment Section
        Text(
          text = if (isEnglish) "Photo Attachment / Scan" else "Photo du cliché / Pièce jointe",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold
        )

        if (photoPath.isNotBlank()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.Black)
          ) {
            AsyncImage(
              model = photoPath,
              contentDescription = "Photo jointe",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
            IconButton(
              onClick = { photoPath = "" },
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(28.dp)
                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Supprimer photo", tint = Color.White, modifier = Modifier.size(16.dp))
            }
          }
        }

        // Camera and Gallery action buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              cameraLauncher.launch(null)
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isEnglish) "Camera" else "Photo")
          }

          OutlinedButton(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isEnglish) "Gallery" else "Galerie")
          }
        }

        // Presets selector
        Text(
          text = if (isEnglish) "Or pick a realistic medical preset:" else "Ou choisir un modèle de cliché :",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          SAMPLE_ULTRASOUND_PRESETS.take(2).forEach { (presetName, presetUrl) ->
            AssistChip(
              onClick = { photoPath = presetUrl },
              label = { Text(presetName, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          SAMPLE_ULTRASOUND_PRESETS.drop(2).forEach { (presetName, presetUrl) ->
            AssistChip(
              onClick = { photoPath = presetUrl },
              label = { Text(presetName, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }

        // Notes and biometrics
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(if (isEnglish) "Notes, CRL, BPD, Heartbeat" else "Compte-rendu, LCC, BIP, Battements") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 3
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            onSave(
              echoToEdit?.id ?: 0L,
              echoToEdit?.dateMs ?: System.currentTimeMillis(),
              trimester,
              title,
              notes,
              photoPath
            )
          }
        }
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
fun UltrasoundLightboxDialog(
  echo: UltrasoundEntity,
  isEnglish: Boolean,
  onDismiss: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight(),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // High-res Image
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(Color.Black)
        ) {
          if (echo.imagePath.isNotBlank()) {
            AsyncImage(
              model = echo.imagePath,
              contentDescription = echo.title,
              contentScale = ContentScale.Fit,
              modifier = Modifier.fillMaxSize()
            )
          } else {
            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = null,
                tint = Color(0xFFF472B6),
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (isEnglish) "No photo attached" else "Aucun cliché photo joint",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(8.dp)
              .size(36.dp)
              .background(Color.Black.copy(alpha = 0.6f), CircleShape)
          ) {
            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
          }
        }

        // Details
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = echo.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.weight(1f)
            )
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer
            ) {
              Text(
                text = echo.trimester,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Text(
            text = formatShortDate(echo.dateMs, isEnglish),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          if (echo.notes.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = echo.notes,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp)
              )
            }
          }

          Divider(modifier = Modifier.padding(vertical = 4.dp))

          // Action Buttons: Edit, Delete, Close
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = onEdit,
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isEnglish) "Edit" else "Modifier")
            }

            OutlinedButton(
              onClick = onDelete,
              colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isEnglish) "Delete" else "Supprimer")
            }
          }
        }
      }
    }
  }
}

@Composable
fun DeleteUltrasoundConfirmationDialog(
  echo: UltrasoundEntity,
  isEnglish: Boolean,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
    title = {
      Text(if (isEnglish) "Delete Ultrasound?" else "Supprimer l'échographie ?")
    },
    text = {
      Text(
        if (isEnglish) {
          "Are you sure you want to permanently delete \"${echo.title}\" and its photo attachment?"
        } else {
          "Êtes-vous sûre de vouloir supprimer définitivement \"${echo.title}\" et sa photo attachée ?"
        }
      )
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
      ) {
        Text(if (isEnglish) "Delete" else "Supprimer")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (isEnglish) "Cancel" else "Annuler")
      }
    }
  )
}
