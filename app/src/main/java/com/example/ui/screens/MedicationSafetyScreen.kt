package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicationSafetyResponse
import com.example.data.PregnancyMedicationAssistant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationSafetyScreen(
  isEnglish: Boolean = false,
  onBack: (() -> Unit)? = null
) {
  val context = LocalContext.current
  var searchQuery by remember { mutableStateOf("Paracetamol") }
  var selectedCategory by remember { mutableStateOf("All") }
  var showRawJson by remember { mutableStateOf(false) }

  // Current safety evaluation result
  val currentResult by remember(searchQuery, isEnglish) {
    derivedStateOf {
      PregnancyMedicationAssistant.processSearchQuery(searchQuery, isEnglish)
    }
  }

  // All curated drugs for directory browsing
  val allMedications = remember(isEnglish) {
    PregnancyMedicationAssistant.getAllCuratedMedications(isEnglish)
  }

  val categories = remember(isEnglish) {
    if (isEnglish) {
      listOf("All", "Pain & Fever", "Antibiotics", "Gastrointestinal & Spasms", "Dermatology & Skin", "Habits & Lifestyle", "Vitamins & Supplements")
    } else {
      listOf("Tous", "Douleur & Fièvre", "Antibiotiques", "Digestif & Spasmes", "Dermatologie & Peau", "Habitudes & Vie", "Vitamines & Minéraux")
    }
  }

  val quickPicks = remember(isEnglish) {
    if (isEnglish) {
      listOf(
        "Paracetamol" to "Safe",
        "Ibuprofen" to "Unsafe/Avoid",
        "Spasfon" to "Safe",
        "Amoxicillin" to "Safe",
        "Roaccutane" to "Unsafe/Avoid",
        "Aspirin" to "Caution (Consult Doctor)",
        "Lovenox" to "Safe",
        "Caffeine" to "Caution (Consult Doctor)",
        "Alcohol" to "Unsafe/Avoid",
        "Folic Acid" to "Safe"
      )
    } else {
      listOf(
        "Doliprane" to "Safe",
        "Ibuprofène" to "Unsafe/Avoid",
        "Spasfon" to "Safe",
        "Amoxicilline" to "Safe",
        "Roaccutane" to "Unsafe/Avoid",
        "Kardégic" to "Caution (Consult Doctor)",
        "Lovenox" to "Safe",
        "Café" to "Caution (Consult Doctor)",
        "Alcool" to "Unsafe/Avoid",
        "Spéciafoldine" to "Safe"
      )
    }
  }

  // Filtered medication items calculated in Composable scope
  val filteredList = remember(selectedCategory, allMedications) {
    if (selectedCategory == "All" || selectedCategory == "Tous") {
      allMedications
    } else {
      allMedications.filter {
        val catLower = selectedCategory.lowercase()
        it.categoryGroup.lowercase().contains(catLower) ||
        (catLower.contains("douleur") && it.categoryGroup.contains("Pain")) ||
        (catLower.contains("antibio") && it.categoryGroup.contains("Antibiotic")) ||
        (catLower.contains("digestif") && it.categoryGroup.contains("Gastrointestinal")) ||
        (catLower.contains("peau") && it.categoryGroup.contains("Dermatology")) ||
        (catLower.contains("vitamine") && it.categoryGroup.contains("Vitamin")) ||
        (catLower.contains("habitude") && it.categoryGroup.contains("Habit"))
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Header
    Surface(
      color = Color(0xFFE2D3B8),
      tonalElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            if (onBack != null) {
              IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(
                  Icons.Default.ArrowBack,
                  contentDescription = if (isEnglish) "Back" else "Retour",
                  tint = Color(0xFF3B2314)
                )
              }
            }
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF3B2314), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Medication,
                contentDescription = null,
                tint = Color(0xFFF7EEDD),
                modifier = Modifier.size(20.dp)
              )
            }
            Column {
              Text(
                text = if (isEnglish) "Medication & Health Safety" else "Médicaments & Sécurité",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3B2314)
              )
              Text(
                text = if (isEnglish) "Backend Medical Assistant • Strict Safety Tiers" else "Assistant Médical • Niveaux de Sécurité Stricts",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6E4D32)
              )
            }
          }

          // JSON view toggle
          IconButton(
            onClick = { showRawJson = !showRawJson },
            modifier = Modifier.testTag("btn_toggle_json")
          ) {
            Icon(
              imageVector = if (showRawJson) Icons.Default.VisibilityOff else Icons.Default.Code,
              contentDescription = if (isEnglish) "Toggle JSON output" else "Afficher JSON",
              tint = Color(0xFF3B2314)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_medication_search"),
          placeholder = {
            Text(
              if (isEnglish) "Search drug, symptom, or habit (e.g., Doliprane, Advil, Retinol...)"
              else "Rechercher médicament, symptôme ou habitude..."
            )
          },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF3B2314))
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF3B2314))
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = Color(0xFF3B2314),
            unfocusedBorderColor = Color(0xFFC8B492)
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Picks Horizontal Scroll
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          quickPicks.forEach { (name, tier) ->
            val chipColor = when (tier) {
              "Safe" -> Color(0xFF2E7D32)
              "Caution (Consult Doctor)" -> Color(0xFFE65100)
              else -> Color(0xFFC62828)
            }
            Surface(
              onClick = { searchQuery = name },
              shape = RoundedCornerShape(12.dp),
              color = Color.White.copy(alpha = 0.9f),
              border = androidx.compose.foundation.BorderStroke(1.dp, chipColor.copy(alpha = 0.5f)),
              modifier = Modifier.testTag("chip_quick_med_$name")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .background(chipColor, CircleShape)
                )
                Text(
                  text = name,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF3B2314)
                )
              }
            }
          }
        }
      }
    }

    // Body content
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 14.dp),
      contentPadding = PaddingValues(vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Raw JSON Viewer (when toggled)
      if (showRawJson) {
        item {
          val rawJson = remember(currentResult) {
            currentResult.toJsonString(indent = 2)
          }
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(18.dp))
                  Text(
                    text = if (isEnglish) "Assistant Raw JSON Response" else "Réponse JSON Brute de l'Assistant",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFE0E0E0),
                    fontWeight = FontWeight.Bold
                  )
                }

                FilledTonalButton(
                  onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Medication JSON", rawJson))
                    Toast.makeText(context, if (isEnglish) "JSON copied to clipboard" else "JSON copié dans le presse-papier", Toast.LENGTH_SHORT).show()
                  },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.height(30.dp)
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(if (isEnglish) "Copy" else "Copier", fontSize = 11.sp)
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = rawJson,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color(0xFFA5D6A7),
                lineHeight = 16.sp
              )
            }
          }
        }
      }

      // 2. Main Result Card with Safety Badge & Details
      item {
        MedicationResultCard(
          result = currentResult,
          isEnglish = isEnglish,
          onAlternativeClick = { alt -> searchQuery = alt }
        )
      }

      // 3. Category Filter Chips for Directory Browsing
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = if (isEnglish) "Browse Curated Directory" else "Explorer le Répertoire Médical",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            categories.forEach { cat ->
              FilterChip(
                selected = selectedCategory == cat,
                onClick = { selectedCategory = cat },
                label = { Text(cat, fontSize = 12.sp) }
              )
            }
          }
        }
      }

      items(filteredList) { med ->
        MedicationListItemCard(
          med = med,
          isEnglish = isEnglish,
          onClick = { searchQuery = med.searchItem }
        )
      }

      // 4. Clinical Medical Disclaimer
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F5EC)),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8B492).copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = Color(0xFF6E4D32),
              modifier = Modifier.size(22.dp)
            )
            Column {
              Text(
                text = if (isEnglish) "Official Medical Disclaimer" else "Avertissement Médical Officiel",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3B2314)
              )
              Text(
                text = if (isEnglish)
                  "This tool adheres to strict international obstetric guidelines (FDA PLLR, ANSM, CRAT). Never self-medicate, alter doses, or discontinue chronic prescriptions without direct supervision from your obstetrician or certified midwife."
                else
                  "Ces informations respectent les données médicales validées (CRAT, ANSM, FDA). Ne pratiquez jamais d'automédication et ne modifiez aucun traitement sans l'accord préalable de votre sage-femme ou médecin traitant.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5A4633),
                lineHeight = 18.sp
              )
            }
          }
        }
      }
    }
  }
}

/**
 * High-polish Card displaying the evaluated result matching the user schema.
 */
@Composable
fun MedicationResultCard(
  result: MedicationSafetyResponse,
  isEnglish: Boolean,
  onAlternativeClick: (String) -> Unit
) {
  val (badgeBg, badgeBorder, badgeTextColor, badgeIcon, localizedTierName) = when (result.pregnancySafetyTier) {
    "Safe" -> Tuple5(
      Color(0xFFE8F5E9),
      Color(0xFF2E7D32),
      Color(0xFF1B5E20),
      Icons.Default.CheckCircle,
      if (isEnglish) "Safe in Pregnancy" else "Sûr & Compatible"
    )
    "Caution (Consult Doctor)" -> Tuple5(
      Color(0xFFFFF8E1),
      Color(0xFFF57F17),
      Color(0xFFB78103),
      Icons.Default.Warning,
      if (isEnglish) "Caution (Consult Doctor)" else "Prudence (Avis Médical Requis)"
    )
    else -> Tuple5(
      Color(0xFFFFEBEE),
      Color(0xFFC62828),
      Color(0xFFB71C1C),
      Icons.Default.Dangerous,
      if (isEnglish) "Unsafe / Avoid in Pregnancy" else "Dangereux / À Éviter Absolument"
    )
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Top Tier Banner
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = badgeBg,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, badgeBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(badgeIcon, contentDescription = null, tint = badgeTextColor, modifier = Modifier.size(26.dp))
          Column {
            Text(
              text = localizedTierName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = badgeTextColor
            )
            Text(
              text = "Tier: ${result.pregnancySafetyTier} • ${result.fdaCategory}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              color = badgeTextColor.copy(alpha = 0.9f)
            )
          }
        }
      }

      // Title & FDA Category badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = result.searchItem,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          if (result.commonBrands.isNotEmpty()) {
            Text(
              text = (if (isEnglish) "Brands: " else "Marques : ") + result.commonBrands.joinToString(", "),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.padding(start = 8.dp)
        ) {
          Text(
            text = result.fdaCategory.take(18) + if (result.fdaCategory.length > 18) "..." else "",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      // Clinical Summary
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFFF9F5EC), RoundedCornerShape(12.dp))
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color(0xFF6E4D32), modifier = Modifier.size(16.dp))
          Text(
            text = if (isEnglish) "Clinical Summary" else "Résumé Clinique",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3B2314)
          )
        }
        Text(
          text = result.clinicalSummary,
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFF3B2314),
          lineHeight = 20.sp
        )
      }

      // Trimester Specific Notes (T1, T2, T3)
      Text(
        text = if (isEnglish) "Trimester-Specific Risks & Notes" else "Risques par Trimestre de Grossesse",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TrimesterCard(
          trimesterLabel = if (isEnglish) "1st Trimester (0-13 Weeks)" else "1er Trimestre (0-13 SA)",
          subLabel = if (isEnglish) "Organogenesis & Implantation" else "Organogenèse & Implantation",
          notes = result.trimesterSpecificNotes.firstTrimester,
          tier = result.pregnancySafetyTier
        )

        TrimesterCard(
          trimesterLabel = if (isEnglish) "2nd Trimester (14-27 Weeks)" else "2e Trimestre (14-27 SA)",
          subLabel = if (isEnglish) "Fetal Growth & Renal Perfusion" else "Croissance & Fonction Rénale",
          notes = result.trimesterSpecificNotes.secondTrimester,
          tier = result.pregnancySafetyTier
        )

        TrimesterCard(
          trimesterLabel = if (isEnglish) "3rd Trimester (28-40+ Weeks)" else "3e Trimestre (28-40+ SA)",
          subLabel = if (isEnglish) "Ductus Arteriosus & Perinatal" else "Canal Artériel & Périnatal",
          notes = result.trimesterSpecificNotes.thirdTrimester,
          tier = result.pregnancySafetyTier
        )
      }

      // Known Fetal Risks
      if (result.fetalRisks.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.ChildCare, contentDescription = null, tint = Color(0xFFB71C1C), modifier = Modifier.size(16.dp))
            Text(
              text = if (isEnglish) "Known Fetal Risks / Complications" else "Risques Fœtaux & Complications Connues",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = if (result.pregnancySafetyTier == "Unsafe/Avoid") Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurface
            )
          }

          result.fetalRisks.forEach { risk ->
            Row(
              verticalAlignment = Alignment.Top,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            ) {
              Box(
                modifier = Modifier
                  .padding(top = 6.dp)
                  .size(6.dp)
                  .background(
                    if (result.pregnancySafetyTier == "Unsafe/Avoid") Color(0xFFC62828)
                    else if (result.pregnancySafetyTier == "Safe") Color(0xFF2E7D32)
                    else Color(0xFFE65100),
                    CircleShape
                  )
              )
              Text(
                text = risk,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
              )
            }
          }
        }
      }

      // Pregnancy-Safe Alternatives
      if (result.pregnancySafeAlternatives.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
            Text(
              text = if (isEnglish) "Pregnancy-Safe Alternatives" else "Alternatives Sûres Pendant la Grossesse",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF2E7D32)
            )
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            result.pregnancySafeAlternatives.forEach { alt ->
              AssistChip(
                onClick = { onAlternativeClick(alt) },
                label = { Text(alt, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                leadingIcon = {
                  Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF2E7D32))
                },
                colors = AssistChipDefaults.assistChipColors(
                  containerColor = Color(0xFFE8F5E9),
                  labelColor = Color(0xFF1B5E20)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784))
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun TrimesterCard(
  trimesterLabel: String,
  subLabel: String,
  notes: String,
  tier: String
) {
  val accentColor = when (tier) {
    "Safe" -> Color(0xFF2E7D32)
    "Caution (Consult Doctor)" -> Color(0xFFF57F17)
    else -> Color(0xFFC62828)
  }

  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .width(4.dp)
          .height(38.dp)
          .background(accentColor, RoundedCornerShape(2.dp))
      )
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = trimesterLabel,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "• $subLabel",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = notes,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 18.sp
        )
      }
    }
  }
}

@Composable
fun MedicationListItemCard(
  med: MedicationSafetyResponse,
  isEnglish: Boolean,
  onClick: () -> Unit
) {
  val (tierColor, tierIcon) = when (med.pregnancySafetyTier) {
    "Safe" -> Color(0xFF2E7D32) to Icons.Default.CheckCircle
    "Caution (Consult Doctor)" -> Color(0xFFF57F17) to Icons.Default.Warning
    else -> Color(0xFFC62828) to Icons.Default.Dangerous
  }

  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .background(tierColor.copy(alpha = 0.12f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(tierIcon, contentDescription = null, tint = tierColor, modifier = Modifier.size(18.dp))
        }

        Column {
          Text(
            text = med.searchItem,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${med.categoryGroup} • ${med.fdaCategory}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = tierColor.copy(alpha = 0.1f)
      ) {
        Text(
          text = when (med.pregnancySafetyTier) {
            "Safe" -> if (isEnglish) "Safe" else "Sûr"
            "Caution (Consult Doctor)" -> if (isEnglish) "Caution" else "Prudence"
            else -> if (isEnglish) "Avoid" else "À Éviter"
          },
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = tierColor,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }
  }
}

private data class Tuple5<A, B, C, D, E>(
  val a: A, val b: B, val c: C, val d: D, val e: E
)
