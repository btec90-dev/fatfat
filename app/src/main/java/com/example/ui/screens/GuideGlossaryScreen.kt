package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.GlossaryItem
import com.example.data.GuideItem
import com.example.data.PregnancyData
import com.example.viewmodel.PregnancyViewModel

@Composable
fun GuideGlossaryScreen(
  viewModel: PregnancyViewModel? = null,
  isEnglish: Boolean = false
) {
  var selectedTab by remember { mutableStateOf(0) }
  val tabs = if (isEnglish) {
    listOf("Medications & Safety", "Guide & Precautions", "Medical Glossary", "Meal Ideas")
  } else {
    listOf("Médicaments & Sécurité", "Guide & Précautions", "Glossaire Médical", "Idées de Repas")
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      edgePadding = 16.dp
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
        0 -> MedicationSafetyScreen(isEnglish = isEnglish)
        1 -> GuideSection(isEnglish = isEnglish)
        2 -> GlossarySection(isEnglish = isEnglish)
        3 -> MealIdeasSection(viewModel = viewModel, isEnglish = isEnglish)
      }
    }
  }
}

@Composable
fun GuideSection(isEnglish: Boolean = false) {
  val guideList = PregnancyData.guideItems

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(10.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Column {
        Text(
          text = if (isEnglish) "Medical Guide & Precautions" else "Guide Médical & Précautions",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = if (isEnglish) "Lifestyle recommendations, nutrition, and urgent warning signs" else "Recommandations d'hygiène de vie, alimentation et signaux d'alerte",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    items(guideList) { item ->
      val isEmergency = item.category == "Urgences"
      val displayCategory = if (isEnglish) {
        when (item.category) {
          "Interdictions" -> "Prohibited Foods"
          "Alimentation" -> "Nutrition"
          "Hygiène de vie" -> "Lifestyle"
          "Urgences" -> "Medical Emergencies"
          else -> item.category
        }
      } else {
        item.category
      }

      val displayTitle = if (isEnglish) {
        when (item.title) {
          "Aliments à éviter absolument" -> "Foods to strictly avoid"
          "Fruits et légumes" -> "Fruits and raw vegetables"
          "Suppléments recommandés" -> "Recommended supplements"
          "Hydratation et boissons" -> "Hydration and beverages"
          "Signaux d'alerte (Urgences médicales)" -> "Red flags (Medical emergencies)"
          else -> item.title
        }
      } else {
        item.title
      }

      val displayDescription = if (isEnglish) {
        when (item.title) {
          "Aliments à éviter absolument" -> "Raw or undercooked meats (toxoplasmosis risk), raw fish and sushi, unpasteurized seafood, raw eggs or raw egg-based dishes (homemade mayonnaise), unpasteurized raw milk cheeses (listeria risk)."
          "Fruits et légumes" -> "Thoroughly wash all raw fruits and vegetables with diluted vinegar water. Avoid raw salads when dining out if thorough cleaning cannot be verified."
          "Suppléments recommandés" -> "Folic acid (vitamin B9) is vital in early pregnancy to prevent neural tube defects, alongside vitamin D, iodine, and iron according to your doctor's prescriptions."
          "Hydratation et boissons" -> "Drink at least 1.5 to 2 liters of water daily. Strictly avoid alcohol, limit caffeine intake to 1-2 cups maximum per day, and abstain from energy drinks."
          "Signaux d'alerte (Urgences médicales)" -> "Contact your obstetrician or hospital immediately if you experience: vaginal bleeding, regular painful uterine contractions before term, amniotic fluid leakage, severe persistent headaches with visual disturbances or sudden facial swelling, decreased fetal movements, or fever > 38°C."
          else -> item.description
        }
      } else {
        item.description
      }

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isEmergency) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
        )
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
              text = displayTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = if (isEmergency) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
            )
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (isEmergency) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer
            ) {
              Text(
                text = displayCategory,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (isEmergency) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }
          Text(
            text = displayDescription,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isEmergency) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
fun GlossarySection(isEnglish: Boolean = false) {
  var searchQuery by remember { mutableStateOf("") }
  val glossaryList = PregnancyData.glossaryItems
  val filteredList = glossaryList.filter {
    it.term.contains(searchQuery, ignoreCase = true) ||
      it.definition.contains(searchQuery, ignoreCase = true) ||
      it.category.contains(searchQuery, ignoreCase = true)
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = if (isEnglish) "Obstetric Medical Glossary" else "Glossaire Médical Obstétrical",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          modifier = Modifier.fillMaxWidth(),
          placeholder = { Text(if (isEnglish) "Search a term (e.g., colostrum, NT...)" else "Rechercher un terme (ex: colostrum, amnio...)") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )
      }
    }

    items(filteredList) { item ->
      val displayTerm = if (isEnglish) {
        when {
          item.term.startsWith("SA") -> "GA (Gestational Age)"
          item.term.startsWith("DPA") -> "EDD (Estimated Due Date)"
          item.term.startsWith("DPNI") -> "NIPT (Non-Invasive Prenatal Testing)"
          item.term.startsWith("Clarté") -> "Nuchal Translucency"
          item.term.startsWith("Progestérone") -> "Progesterone (PIO)"
          item.term.startsWith("Terme") -> "Full Term / Post-term"
          else -> item.term
        }
      } else {
        item.term
      }

      val displayCategory = if (isEnglish) {
        when (item.category) {
          "Général" -> "General"
          "Allaitement" -> "Breastfeeding"
          "Examens" -> "Examinations"
          "Physiologie" -> "Physiology"
          "Échographie" -> "Ultrasound"
          "Médication" -> "Medication"
          "Accouchement" -> "Delivery"
          else -> item.category
        }
      } else {
        item.category
      }

      val displayDefinition = if (isEnglish) {
        when {
          item.term.startsWith("SA") -> "Number of weeks elapsed since the first day of the last menstrual period (LMP). Standard medical obstetric measurement."
          item.term.startsWith("DPA") -> "Estimated birth date calculated at 40 completed weeks of pregnancy."
          item.term.startsWith("Colostrum") -> "First maternal milk produced towards the end of pregnancy and immediately after birth, loaded with protective antibodies."
          item.term.startsWith("Amniocentèse") -> "Amniotic fluid sampling performed through the abdomen for detailed fetal genetic and karyotype diagnosis."
          item.term.startsWith("Contractions de Braxton-Hicks") -> "Gentle uterine practice contractions that prepare the body for labor without dilating the cervix."
          item.term.startsWith("DPNI") -> "Maternal blood test analyzing cell-free fetal DNA to screen for Down syndrome and other trisomies."
          item.term.startsWith("Clarté") -> "Ultrasound measurement of fetal neck fluid thickness during 11-13 weeks scan for chromosomal assessment."
          item.term.startsWith("Streptocoque") -> "Vaginal and rectal swab screening in the 3rd trimester to prevent newborn bacterial exposure at delivery."
          item.term.startsWith("Progestérone") -> "Crucial luteal phase hormone support administered in IVF to ensure endometrial receptivity and support implantation."
          item.term.startsWith("Terme") -> "Pregnancy exceeding 41+ weeks requiring frequent non-stress testing and monitored induction."
          else -> item.definition
        }
      } else {
        item.definition
      }

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = displayTerm,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = displayCategory,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Text(
            text = displayDefinition,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}

@Composable
fun MealIdeasSection(viewModel: PregnancyViewModel?, isEnglish: Boolean = false) {
  val settings = viewModel?.settings?.collectAsState()?.value
  val lmp = settings?.lmpDateMs ?: (System.currentTimeMillis() - 60L * 24 * 3600 * 1000L)
  val currentWeeks = viewModel?.calculateCurrentGestationalWeek(lmp)?.first ?: 8

  val autoTrimester = when {
    currentWeeks <= 13 -> 1
    currentWeeks <= 26 -> 2
    else -> 3
  }

  var selectedTrimester by remember { mutableStateOf(autoTrimester) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = if (isEnglish) "Meal Ideas & Nutrition" else "Idées de Repas & Nutrition",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = if (isEnglish) "Balanced meal plans tailored to your gestational age ($currentWeeks GW - Trimester $autoTrimester)" else "Suggestions équilibrées adaptées à votre terme actuel ($currentWeeks SA - Trimestre $autoTrimester)",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val labels = if (isEnglish) {
            listOf(1 to "1st Trimester", 2 to "2nd Trimester", 3 to "3rd Trimester")
          } else {
            listOf(1 to "1er Trimestre", 2 to "2ème Trimestre", 3 to "3ème Trimestre")
          }
          labels.forEach { (num, label) ->
            FilterChip(
              selected = selectedTrimester == num,
              onClick = { selectedTrimester = num },
              label = {
                Text(
                  text = if (num == autoTrimester) "$label " + (if (isEnglish) "(Current)" else "(Actuel)") else label,
                  fontWeight = if (selectedTrimester == num) FontWeight.Bold else FontWeight.Normal
                )
              },
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
              text = if (isEnglish) "Nutritional Goals - Trimester $selectedTrimester" else "Objectifs Nutritionnels - Trimestre $selectedTrimester",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
          Text(
            text = if (isEnglish) {
              when (selectedTrimester) {
                1 -> "• Folic acid (B9) to ensure proper neural tube closure.\n• Bland, soothing foods to ease morning sickness.\n• Eat 4 to 5 small, frequent meals throughout the day."
                2 -> "• Iron & vitamin C to support rapid blood volume increase.\n• Calcium and vitamin D for sturdy fetal bone formation.\n• Omega-3 (DHA) for optimal baby brain and eye development."
                else -> "• High fiber & hydration to avoid digestive constipation.\n• Easy-to-digest recipes to soothe acid reflux.\n• Magnesium intake to prevent nighttime leg cramps."
              }
            } else {
              when (selectedTrimester) {
                1 -> "• Acide folique (B9) pour la fermeture du tube neural.\n• Aliments doux pour atténuer les nausées matinales.\n• Fractionner les repas en 4 à 5 petites prises légères."
                2 -> "• Fer et vitamine C pour soutenir l'augmentation du volume sanguin.\n• Calcium et vitamine D pour l'ossification fœtale.\n• Oméga-3 (DHA) pour le développement cérébral."
                else -> "• Fibres et hydratation optimale contre la constipation.\n• Aliments digestes pour soulager les remontées acides.\n• Magnésium pour réduire les crampes nocturnes."
              }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }
    }

    item {
      Text(
        text = if (isEnglish) "Daily Sample Menu" else "Menu type d'une journée",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    val mealCards = if (isEnglish) {
      when (selectedTrimester) {
        1 -> listOf(
          "Breakfast" to "Toasted whole grain sourdough, smashed avocado, thoroughly cooked hard-boiled egg and warm ginger herbal tea.",
          "Morning Snack" to "A handful of raw unsalted almonds and unsweetened applesauce.",
          "Lunch" to "Grilled organic chicken breast, quinoa with steamed zucchini and carrots with a drizzle of virgin cold-pressed oil.",
          "Afternoon Snack" to "Pasteurized organic Greek yogurt with chia seeds and freshly washed raspberries.",
          "Dinner" to "Creamy butternut squash soup, whole rye crispbread and pasteurized fromage blanc."
        )
        2 -> listOf(
          "Breakfast" to "Warm oatmeal bowl with calcium-fortified almond milk, milled flaxseeds and antioxidant-rich blueberries.",
          "Morning Snack" to "Fresh seasonal fruit (orange or kiwi rich in vitamin C) with walnuts.",
          "Lunch" to "Wild salmon fillet cooked thoroughly (omega-3), brown basmati rice and warm baby spinach with toasted sesame.",
          "Afternoon Snack" to "Whole wheat bread slice with pasteurized hard aged cheese (Comté or Cheddar) for calcium.",
          "Dinner" to "Hearty coral lentils stewed with carrots and mild tomatoes, lean turkey breast and pear-cinnamon puree."
        )
        else -> listOf(
          "Breakfast" to "Creamy rolled oats porridge with ripe banana slices, almond butter and chamomile infusion.",
          "Morning Snack" to "Two soaked prunes and a tall glass of magnesium-rich spring water.",
          "Lunch" to "Steamed cod fillet with mashed sweet potatoes and tender green beans.",
          "Afternoon Snack" to "Pasteurized cottage cheese with a hint of raw honey and sunflower seeds.",
          "Dinner" to "Light garden vegetable broth, poached egg thoroughly cooked, and homemade applesauce."
        )
      }
    } else {
      when (selectedTrimester) {
        1 -> listOf(
          "Petit-déjeuner" to "Tartines de pain complet au levain, avocat écrasé, œuf dur bien cuit et infusion au gingembre.",
          "Collation matin" to "Une poignée d'amandes non salées et une compote de pomme sans sucre ajouté.",
          "Déjeuner" to "Filet de poulet fermier grillé à point, quinoa aux courgettes et carottes vapeur avec un filet d'huile de colza.",
          "Goûter" to "Yaourt nature pasteurisé avec graines de chia et framboises bien lavées.",
          "Dîner" to "Velouté doux de potimarron, crackers au seigle et fromage blanc pasteurisé."
        )
        2 -> listOf(
          "Petit-déjeuner" to "Bol de flocons d'avoine tiède, lait d'amande enrichi en calcium, graines de lin moulues et myrtilles fraîches.",
          "Collation matin" to "Un fruit frais de saison (orange ou kiwi riche en vitamine C) et quelques noix de Grenoble.",
          "Déjeuner" to "Pavé de saumon cuit à cœur (oméga-3), riz complet et poêlée d'épinards frais au sésame.",
          "Goûter" to "Tranche de pain complet avec fromage à pâte cuite pasteurisé (Comté/Emmental) pour le calcium.",
          "Dîner" to "Lentilles corail mijotées aux carottes et tomates douces, blanc de dinde et compote poire-cannelle."
        )
        else -> listOf(
          "Petit-déjeuner" to "Porridge crémeux d'avoine avec rondelles de banane mûre, une cuillère de purée d'amandes et infusion douce.",
          "Collation matin" to "Deux pruneaux réhydratés et un verre d'eau minérale riche en magnésium.",
          "Déjeuner" to "Dos de cabillaud vapeur avec purée de patates douces maison et haricots verts extra-fins.",
          "Goûter" to "Fromage blanc pasteurisé avec une touche de miel et graines de tournesol.",
          "Dîner" to "Soupe légère de légumes variés du potager, œuf poché bien cuit et compote de pommes maison sans sucre."
        )
      }
    }

    items(mealCards) { (mealTitle, mealContent) ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = mealTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = mealContent,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}
