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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class BabyMomChecklistItem(
  val id: String,
  val isBaby: Boolean,
  val categoryEn: String,
  val categoryFr: String,
  val titleEn: String,
  val titleFr: String,
  val priorityEn: String,
  val priorityFr: String,
  val quantity: String,
  val noteEn: String,
  val noteFr: String
)

val DEFAULT_CHECKLIST_ITEMS = listOf(
  // Baby Essentials (Clothing & Gear)
  BabyMomChecklistItem(
    id = "b1",
    isBaby = true,
    categoryEn = "Clothing & Gear",
    categoryFr = "Vêtements & Équipement",
    titleEn = "Two-way zip footies (0–3M)",
    titleFr = "Pyjamas zippés double sens (0-3M)",
    priorityEn = "Must-Have",
    priorityFr = "Indispensable",
    quantity = "4-6",
    noteEn = "Essential for quick and easy midnight diaper changes without exposing baby to cold.",
    noteFr = "Les fermetures éclair à double sens permettent de changer bébé la nuit sans déshabiller le haut."
  ),
  BabyMomChecklistItem(
    id = "b2",
    isBaby = true,
    categoryEn = "Clothing & Gear",
    categoryFr = "Vêtements & Équipement",
    titleEn = "Short and long-sleeve kimono bodysuits",
    titleFr = "Bodys croisés kimono manches courtes & longues",
    priorityEn = "Must-Have",
    priorityFr = "Indispensable",
    quantity = "6-8",
    noteEn = "Kimono wrap style avoids having to pull over newborn's delicate head and fontanelle.",
    noteFr = "La coupe croisée kimono s'enfile sans forcer sur la tête fragile du nouveau-né."
  ),
  BabyMomChecklistItem(
    id = "b3",
    isBaby = true,
    categoryEn = "Clothing & Gear",
    categoryFr = "Vêtements & Équipement",
    titleEn = "Swaddles / sleep sacks",
    titleFr = "Gigoteuses (turbulettes) / langes d'emmaillotage",
    priorityEn = "Must-Have",
    priorityFr = "Indispensable",
    quantity = "3-4",
    noteEn = "Helps soothe startle (Moro) reflex and promotes safe, cozy infant sleep.",
    noteFr = "Calme le réflexe de Moro et assure un sommeil sécurisé selon les recommandations pédiatriques."
  ),
  BabyMomChecklistItem(
    id = "b4",
    isBaby = true,
    categoryEn = "Clothing & Gear",
    categoryFr = "Vêtements & Équipement",
    titleEn = "Infant car seat and base",
    titleFr = "Siège auto bébé (cosy) avec base Isofix",
    priorityEn = "Must-Have",
    priorityFr = "Indispensable",
    quantity = "1",
    noteEn = "Mandatory for safe hospital departure and automotive transit.",
    noteFr = "Obligatoire dès la sortie de maternité pour un transport sécurisé en voiture."
  ),
  BabyMomChecklistItem(
    id = "b5",
    isBaby = true,
    categoryEn = "Clothing & Gear",
    categoryFr = "Vêtements & Équipement",
    titleEn = "Bassinet / crib with fitted sheets",
    titleFr = "Berceau / lit cododo avec draps-housses respirants",
    priorityEn = "Must-Have",
    priorityFr = "Indispensable",
    quantity = "1",
    noteEn = "Safe sleep space meeting current AAP safety guidelines with firm mattress.",
    noteFr = "Espace de couchage sécurisé avec matelas ferme et drap ajusté."
  ),
  BabyMomChecklistItem(
    id = "b6",
    isBaby = true,
    categoryEn = "Clothing & Gear",
    categoryFr = "Vêtements & Équipement",
    titleEn = "Baby first-aid (rectal/infant thermometer, nasal aspirator, infant acetaminophen)",
    titleFr = "Premiers soins bébé (thermomètre rectal/infantile, mouche-bébé, paracétamol nourrisson)",
    priorityEn = "Must-Have",
    priorityFr = "Indispensable",
    quantity = "1 kit",
    noteEn = "Pediatric health toolkit for temperature monitoring and gentle congestion clearing.",
    noteFr = "Indispensable pour surveiller la fièvre et moucher bébé en douceur dès les premiers jours."
  ),

  // Mom & Postpartum Care
  BabyMomChecklistItem(
    id = "m1",
    isBaby = false,
    categoryEn = "Hospital Bag",
    categoryFr = "Valise de Maternité",
    titleEn = "Hospital bag essentials (loose clothing, long charging cables, toiletries, ID/insurance)",
    titleFr = "Valise de maternité (vêtements amples, câbles de charge longs 3m, trousse de toilette, papiers/mutuelle)",
    priorityEn = "Hospital Bag Essential",
    priorityFr = "Essentiel Valise",
    quantity = "1 bag",
    noteEn = "Pack by week 36 with warm socks, loose layers, 10ft phone cord, and admission files.",
    noteFr = "Préparez votre valise vers 36 SA avec vêtements confortables, trousse complète et chargeur long."
  ),
  BabyMomChecklistItem(
    id = "m2",
    isBaby = false,
    categoryEn = "Perineal Recovery",
    categoryFr = "Récupération Périnéale",
    titleEn = "Perineal recovery (peri bottle, witch hazel pads, ice packs, disposable briefs)",
    titleFr = "Récupération périnéale (flacon bidet péri-bouteille, compresses hamamélis, poches de froid, culottes jetables)",
    priorityEn = "Must-Have",
    priorityFr = "Indispensable",
    quantity = "1 kit",
    noteEn = "Crucial for soothing postpartum tenderness, swelling, and gentle cleansing with warm water.",
    noteFr = "Soulage la douleur, les gonflements et permet une hygiène douce après chaque miction."
  ),
  BabyMomChecklistItem(
    id = "m3",
    isBaby = false,
    categoryEn = "Nursing & Pumping Gear",
    categoryFr = "Allaitement & Tire-lait",
    titleEn = "Nursing & pumping gear (nursing bras, nipple balm, breast pads, nursing pillow)",
    titleFr = "Allaitement & tire-lait (soutiens-gorge d'allaitement, baume lanoline mamelons, coussinets, coussin de maternité)",
    priorityEn = "Must-Have",
    priorityFr = "Indispensable",
    quantity = "1 set",
    noteEn = "Protects delicate skin, prevents sore nipples, and provides ergonomic support for feedings.",
    noteFr = "Évite les crevasses, absorbe les fuites de lait et soulage le dos et les bras pendant les tétées."
  )
)

fun getChecklistStats(context: Context): Pair<Int, Int> {
  val prefs = context.getSharedPreferences("maman_bebe_checklist", Context.MODE_PRIVATE)
  val checkedSet = prefs.getStringSet("checked_ids", emptySet()) ?: emptySet()
  return Pair(checkedSet.size, DEFAULT_CHECKLIST_ITEMS.size)
}

@Composable
fun BabyMomChecklistSection(
  isEnglish: Boolean = false,
  onExportPdf: () -> Unit
) {
  val context = LocalContext.current
  val prefs = remember { context.getSharedPreferences("maman_bebe_checklist", Context.MODE_PRIVATE) }
  
  var checkedIds by remember {
    val saved = prefs.getStringSet("checked_ids", emptySet()) ?: emptySet()
    mutableStateOf<Set<String>>(saved.toSet())
  }

  var selectedFilter by remember { mutableStateOf("all") } // "all", "baby", "mom"
  var expandedNoteIds by remember { mutableStateOf<Set<String>>(emptySet()) }
  var showAddItemDialog by remember { mutableStateOf(false) }

  // Custom items added by user
  var customItems by remember {
    mutableStateOf<List<BabyMomChecklistItem>>(emptyList())
  }

  val allItems: List<BabyMomChecklistItem> = remember(customItems) {
    DEFAULT_CHECKLIST_ITEMS + customItems
  }

  val filteredItems: List<BabyMomChecklistItem> = remember(allItems, selectedFilter) {
    when (selectedFilter) {
      "baby" -> allItems.filter { it.isBaby }
      "mom" -> allItems.filter { !it.isBaby }
      else -> allItems
    }
  }

  val totalCount = allItems.size
  val checkedCount = allItems.count { item -> checkedIds.contains(item.id) }
  val progress = if (totalCount > 0) checkedCount.toFloat() / totalCount.toFloat() else 0f

  fun toggleItem(id: String) {
    val newSet = if (checkedIds.contains(id)) {
      checkedIds - id
    } else {
      checkedIds + id
    }
    checkedIds = newSet
    prefs.edit().putStringSet("checked_ids", newSet).apply()
  }

  fun toggleNote(id: String) {
    expandedNoteIds = if (expandedNoteIds.contains(id)) {
      expandedNoteIds - id
    } else {
      expandedNoteIds + id
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("baby_mom_checklist_section"),
    contentPadding = PaddingValues(14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header Banner
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isEnglish) "🍼 Newborn Baby & Mom Checklist" else "🍼 Trousseau Bébé & Maman",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = if (isEnglish) "Complete preparation: Nursery, hospital bag & recovery" else "Préparation complète : Trousseau, valise & post-partum",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
              )
            }
            IconButton(
              onClick = { exportChecklistPdf(context, allItems, checkedIds, isEnglish) },
              modifier = Modifier.testTag("btn_export_checklist_pdf")
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = if (isEnglish) "Share PDF" else "Partager PDF",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }

          // Progress bar
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (isEnglish) "Progress: $checkedCount / $totalCount ready" else "Progression : $checkedCount sur $totalCount prêts",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
            LinearProgressIndicator(
              progress = { progress },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
          }
        }
      }
    }

    // Filter pills
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = selectedFilter == "all",
          onClick = { selectedFilter = "all" },
          label = { Text(if (isEnglish) "All (${totalCount})" else "Tous (${totalCount})") },
          modifier = Modifier.testTag("chip_filter_all")
        )
        FilterChip(
          selected = selectedFilter == "baby",
          onClick = { selectedFilter = "baby" },
          label = { Text(if (isEnglish) "🍼 Baby Essentials" else "🍼 Trousseau Bébé") },
          modifier = Modifier.testTag("chip_filter_baby")
        )
        FilterChip(
          selected = selectedFilter == "mom",
          onClick = { selectedFilter = "mom" },
          label = { Text(if (isEnglish) "💖 Mom & Recovery" else "💖 Maman & Soins") },
          modifier = Modifier.testTag("chip_filter_mom")
        )
      }
    }

    // Add item button banner
    item {
      OutlinedButton(
        onClick = { showAddItemDialog = true },
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
          .testTag("btn_add_custom_checklist_item"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(
          text = if (isEnglish) "Add Custom Item to Checklist" else "Ajouter un article personnalisé",
          fontWeight = FontWeight.Bold
        )
      }
    }

    // Checklist Items
    items(filteredItems, key = { it.id }) { item ->
      val isChecked = checkedIds.contains(item.id)
      val isExpanded = expandedNoteIds.contains(item.id)
      val title = if (isEnglish) item.titleEn else item.titleFr
      val category = if (isEnglish) item.categoryEn else item.categoryFr
      val priority = if (isEnglish) item.priorityEn else item.priorityFr
      val note = if (isEnglish) item.noteEn else item.noteFr

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { toggleItem(item.id) }
          .testTag("checklist_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isChecked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
          1.dp,
          if (isChecked) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
        )
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Checkbox(
              checked = isChecked,
              onCheckedChange = { toggleItem(item.id) },
              modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
              )

              Spacer(Modifier.height(4.dp))

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (item.isBaby) Color(0xFFF3E9DD) else Color(0xFFF7E1DE)
                ) {
                  Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isBaby) Color(0xFF5D4037) else Color(0xFF880E4F)
                  )
                }

                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                  Text(
                    text = priority,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                }

                Text(
                  text = if (isEnglish) "Qty: ${item.quantity}" else "Qté : ${item.quantity}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            if (note.isNotEmpty()) {
              IconButton(
                onClick = { toggleNote(item.id) },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.Lightbulb,
                  contentDescription = "Tips",
                  tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }

          AnimatedVisibility(visible = isExpanded && note.isNotEmpty()) {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
              ) {
                Text("💡", style = MaterialTheme.typography.bodyMedium)
                Column {
                  Text(
                    text = if (isEnglish) "Practical Advice & Tip:" else "Conseil Pratique & Astuce :",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                  )
                  Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Dialog for adding custom item
  if (showAddItemDialog) {
    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("") }
    var newQuantity by remember { mutableStateOf("1") }
    var newNote by remember { mutableStateOf("") }
    var isNewBabyModule by remember { mutableStateOf(true) }

    AlertDialog(
      onDismissRequest = { showAddItemDialog = false },
      title = {
        Text(
          text = if (isEnglish) "Add Custom Item" else "Ajouter un article",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = isNewBabyModule,
              onClick = { isNewBabyModule = true },
              label = { Text(if (isEnglish) "🍼 Baby" else "🍼 Bébé") },
              modifier = Modifier.weight(1f)
            )
            FilterChip(
              selected = !isNewBabyModule,
              onClick = { isNewBabyModule = false },
              label = { Text(if (isEnglish) "💖 Mom" else "💖 Maman") },
              modifier = Modifier.weight(1f)
            )
          }

          OutlinedTextField(
            value = newTitle,
            onValueChange = { newTitle = it },
            label = { Text(if (isEnglish) "Item Name *" else "Nom de l'article *") },
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = newCategory,
            onValueChange = { newCategory = it },
            label = { Text(if (isEnglish) "Subcategory (e.g. Nursery, Care)" else "Sous-catégorie (ex: Chambre, Soins)") },
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = newQuantity,
            onValueChange = { newQuantity = it },
            label = { Text(if (isEnglish) "Quantity" else "Quantité") },
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = newNote,
            onValueChange = { newNote = it },
            label = { Text(if (isEnglish) "Practical tip / Advice" else "Conseil / Note pratique") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newTitle.isNotBlank()) {
              val custom = BabyMomChecklistItem(
                id = "custom_${System.currentTimeMillis()}",
                isBaby = isNewBabyModule,
                categoryEn = if (newCategory.isNotBlank()) newCategory else (if (isNewBabyModule) "Baby Gear" else "Mom Care"),
                categoryFr = if (newCategory.isNotBlank()) newCategory else (if (isNewBabyModule) "Équipement Bébé" else "Soins Maman"),
                titleEn = newTitle,
                titleFr = newTitle,
                priorityEn = "Must-Have",
                priorityFr = "Indispensable",
                quantity = if (newQuantity.isNotBlank()) newQuantity else "1",
                noteEn = newNote,
                noteFr = newNote
              )
              customItems = customItems + custom
              showAddItemDialog = false
            }
          }
        ) {
          Text(if (isEnglish) "Add" else "Ajouter")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddItemDialog = false }) {
          Text(if (isEnglish) "Cancel" else "Annuler")
        }
      }
    )
  }
}

fun exportChecklistPdf(
  context: Context,
  items: List<BabyMomChecklistItem>,
  checkedIds: Set<String>,
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

  val headerText = if (isEnglish) "NEWBORN BABY & MOM CHECKLIST" else "TROUSSEAU BÉBÉ & SOINS MAMAN"
  canvas.drawText(headerText, 40f, y, titlePaint)
  y += 24f

  val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
  canvas.drawText(if (isEnglish) "Exported: $dateStr" else "Date d'exportation : $dateStr", 40f, y, paint)
  y += 20f

  val babyItems = items.filter { it.isBaby }
  canvas.drawText(if (isEnglish) "--- Baby Essentials (Clothing & Gear) ---" else "--- Trousseau Bébé (Vêtements & Équipement) ---", 40f, y, titlePaint)
  y += 18f

  babyItems.forEach { item ->
    if (y > 780f) return@forEach
    val checkStatus = if (checkedIds.contains(item.id)) "[X]" else "[ ]"
    val title = if (isEnglish) item.titleEn else item.titleFr
    canvas.drawText("$checkStatus $title (Qty: ${item.quantity})", 40f, y, paint)
    y += 16f
  }

  y += 12f
  val momItems = items.filter { !it.isBaby }
  canvas.drawText(if (isEnglish) "--- Mom & Postpartum Care ---" else "--- Maman & Soins Post-partum ---", 40f, y, titlePaint)
  y += 18f

  momItems.forEach { item ->
    if (y > 780f) return@forEach
    val checkStatus = if (checkedIds.contains(item.id)) "[X]" else "[ ]"
    val title = if (isEnglish) item.titleEn else item.titleFr
    canvas.drawText("$checkStatus $title (Qty: ${item.quantity})", 40f, y, paint)
    y += 16f
  }

  pdfDocument.finishPage(page)

  val file = File(context.cacheDir, "Trousseau_Bebe_Maman.pdf")
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
  context.startActivity(Intent.createChooser(intent, if (isEnglish) "Share Checklist PDF" else "Partager le Trousseau PDF"))
}
