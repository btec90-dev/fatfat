package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.UltrasoundEntity
import com.example.ui.components.*
import com.example.viewmodel.PregnancyViewModel

@Composable
fun PhotoAlbumsSection(
  ultrasounds: List<UltrasoundEntity>,
  viewModel: PregnancyViewModel,
  isEnglish: Boolean = false
) {
  val context = LocalContext.current
  var selectedAlbumId by remember { mutableStateOf("all") }
  var showAddDialog by remember { mutableStateOf(false) }
  var echoToEdit by remember { mutableStateOf<UltrasoundEntity?>(null) }
  var echoForLightbox by remember { mutableStateOf<UltrasoundEntity?>(null) }
  var echoToDelete by remember { mutableStateOf<UltrasoundEntity?>(null) }

  val albumCategories = listOf(
    Pair("all", if (isEnglish) "All Albums" else "Tous les Albums"),
    Pair("T1", if (isEnglish) "1st Trimester (T1)" else "1er Trimestre (T1)"),
    Pair("T2", if (isEnglish) "2nd Trimester (T2)" else "2ème Trimestre (T2)"),
    Pair("T3", if (isEnglish) "3rd Trimester (T3)" else "3ème Trimestre (T3)"),
    Pair("Précoce", if (isEnglish) "Early & IVF" else "Précoce & PMA"),
    Pair("Souvenirs", if (isEnglish) "Bump & Memories" else "Ventre & Souvenirs"),
    Pair("Contrôle", if (isEnglish) "Follow-up Checks" else "Examens Contrôles")
  )

  val filteredList = remember(ultrasounds, selectedAlbumId) {
    if (selectedAlbumId == "all") {
      ultrasounds
    } else {
      ultrasounds.filter {
        it.trimester.equals(selectedAlbumId, ignoreCase = true) ||
          it.title.contains(selectedAlbumId, ignoreCase = true) ||
          it.notes.contains(selectedAlbumId, ignoreCase = true)
      }
    }
  }

  val directCameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    if (bitmap != null) {
      val savedPath = saveBitmapToInternalStorage(context, bitmap)
      val albumTag = if (selectedAlbumId != "all") selectedAlbumId else "T1"
      viewModel.saveUltrasound(
        id = 0L,
        dateMs = System.currentTimeMillis(),
        trimester = albumTag,
        title = if (isEnglish) "Album Photo (${albumTag})" else "Photo Album (${albumTag})",
        notes = if (isEnglish) "Captured with camera." else "Capturée avec l'appareil photo.",
        imagePath = savedPath
      )
      Toast.makeText(
        context,
        if (isEnglish) "Photo added to $albumTag album! 📸" else "Photo ajoutée à l'album $albumTag ! 📸",
        Toast.LENGTH_SHORT
      ).show()
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("photo_albums_section"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        )
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
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isEnglish) "Photo Gallery & Albums" else "Galerie Photos & Albums",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = if (isEnglish) {
                  "${ultrasounds.size} photo(s) organized across albums"
                } else {
                  "${ultrasounds.size} photo(s) classée(s) par albums"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              FilledTonalIconButton(
                onClick = { directCameraLauncher.launch(null) },
                modifier = Modifier.size(40.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.PhotoCamera,
                  contentDescription = if (isEnglish) "Take Photo" else "Prendre photo"
                )
              }

              Button(
                onClick = {
                  echoToEdit = null
                  showAddDialog = true
                },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (isEnglish) "Add Photo" else "Ajouter")
              }
            }
          }

          // Album filter chips
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            albumCategories.forEach { (catId, catLabel) ->
              val count = if (catId == "all") {
                ultrasounds.size
              } else {
                ultrasounds.count { it.trimester.equals(catId, ignoreCase = true) }
              }
              val isSelected = selectedAlbumId == catId

              FilterChip(
                selected = isSelected,
                onClick = { selectedAlbumId = catId },
                label = {
                  Text(
                    text = "$catLabel ($count)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                },
                leadingIcon = if (isSelected) {
                  { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                } else null
              )
            }
          }
        }
      }
    }

    // Content: Photos List
    if (filteredList.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PhotoLibrary,
              contentDescription = null,
              modifier = Modifier.size(48.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Text(
              text = if (isEnglish) "No photos in this album yet" else "Aucune photo dans cet album pour le moment",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (isEnglish) {
                "Take a photo with your camera or add a scan to organize it into this album"
              } else {
                "Prenez une photo ou ajoutez un cliché pour l'enregistrer dans cet album"
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.padding(top = 8.dp)
            ) {
              OutlinedButton(
                onClick = { directCameraLauncher.launch(null) },
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isEnglish) "Take Photo" else "Prendre photo")
              }
              Button(
                onClick = {
                  echoToEdit = null
                  showAddDialog = true
                },
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isEnglish) "New Entry" else "Nouvelle photo")
              }
            }
          }
        }
      }
    } else {
      items(filteredList) { photo ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = CardDefaults.outlinedCardBorder()
        ) {
          Column {
            // Photo Preview container
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFF1E1E1E))
                .clickable { echoForLightbox = photo }
            ) {
              if (photo.imagePath.isNotBlank()) {
                AsyncImage(
                  model = photo.imagePath,
                  contentDescription = photo.title,
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
                    imageVector = Icons.Default.PhotoAlbum,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                  )
                  Spacer(Modifier.height(6.dp))
                  Text(
                    text = if (isEnglish) "No image preview" else "Pas d'aperçu image",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f)
                  )
                }
              }

              // Album Badge
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                modifier = Modifier
                  .align(Alignment.TopStart)
                  .padding(10.dp)
              ) {
                Text(
                  text = photo.trimester,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }

              // Tap to enlarge badge
              Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                  .align(Alignment.BottomEnd)
                  .padding(10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ZoomIn,
                  contentDescription = if (isEnglish) "Zoom" else "Agrandir",
                  tint = Color.White,
                  modifier = Modifier
                    .padding(6.dp)
                    .size(18.dp)
                )
              }
            }

            // Description info
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = photo.title,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f)
                )
                Text(
                  text = formatShortDate(photo.dateMs, isEnglish),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              if (photo.notes.isNotBlank()) {
                Text(
                  text = photo.notes,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
              }

              Divider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
              )

              // Action buttons
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                TextButton(
                  onClick = { echoForLightbox = photo },
                  contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                  Spacer(Modifier.width(4.dp))
                  Text(if (isEnglish) "View" else "Voir", style = MaterialTheme.typography.labelSmall)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  IconButton(
                    onClick = {
                      echoToEdit = photo
                      showAddDialog = true
                    },
                    modifier = Modifier.size(34.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = if (isEnglish) "Edit" else "Modifier",
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(17.dp)
                    )
                  }

                  IconButton(
                    onClick = { echoToDelete = photo },
                    modifier = Modifier.size(34.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.DeleteOutline,
                      contentDescription = if (isEnglish) "Delete" else "Supprimer",
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(17.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // DIALOGS
  if (showAddDialog) {
    AddEditUltrasoundDialog(
      echoToEdit = echoToEdit,
      isEnglish = isEnglish,
      onDismiss = {
        showAddDialog = false
        echoToEdit = null
      },
      onSave = { id, dateMs, trimester, title, notes, photoPath ->
        viewModel.saveUltrasound(
          id = id,
          dateMs = dateMs,
          trimester = trimester,
          title = title,
          notes = notes,
          imagePath = photoPath
        )
        showAddDialog = false
        echoToEdit = null
        Toast.makeText(
          context,
          if (isEnglish) "Saved to album!" else "Enregistré dans l'album !",
          Toast.LENGTH_SHORT
        ).show()
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
        showAddDialog = true
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
        Toast.makeText(
          context,
          if (isEnglish) "Deleted from album" else "Supprimé de l'album",
          Toast.LENGTH_SHORT
        ).show()
      }
    )
  }
}
