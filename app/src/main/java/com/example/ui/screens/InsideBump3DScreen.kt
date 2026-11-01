package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.PregnancyViewModel
import kotlin.math.*

data class WeeklyFetalData(
  val week: Int,
  val fruitEn: String,
  val fruitFr: String,
  val fruitEmoji: String,
  val lengthCm: Float,
  val weightG: Float,
  val stageEn: String,
  val stageFr: String,
  val headlineEn: String,
  val headlineFr: String,
  val factsEn: List<String>,
  val factsFr: List<String>
)

val WEEKLY_FETAL_DATABASE = listOf(
  WeeklyFetalData(
    week = 4,
    fruitEn = "Poppy Seed",
    fruitFr = "Graine de Pavot",
    fruitEmoji = "🌱",
    lengthCm = 0.1f,
    weightG = 0.1f,
    stageEn = "Blastocyst Implantation",
    stageFr = "Implantation du Blastocyste",
    headlineEn = "The tiny cluster of cells officially implants into the uterine lining.",
    headlineFr = "Le minuscule amas cellulaire s'implante dans la paroi utérine.",
    factsEn = listOf(
      "The blastocyst splits into two parts: the embryo and the placenta.",
      "The amniotic cavity and yolk sac form to nourish your baby.",
      "Home pregnancy tests can begin detecting hCG hormone."
    ),
    factsFr = listOf(
      "Le blastocyste se sépare en deux : le futur embryon et le placenta.",
      "La cavité amniotique et la vésicule vitelline se forment pour nourrir l'embryon.",
      "Les tests urinaires commencent à détecter l'hormone de grossesse hCG."
    )
  ),
  WeeklyFetalData(
    week = 8,
    fruitEn = "Raspberry",
    fruitFr = "Framboise",
    fruitEmoji = "🍇",
    lengthCm = 1.6f,
    weightG = 1.0f,
    stageEn = "Embryonic Organogenesis",
    stageFr = "Organogenèse Embryonnaire",
    headlineEn = "Tiny hands, feet, and eyelids are beginning to take distinct shape.",
    headlineFr = "Les mains, pieds et paupières commencent à se dessiner distinctement.",
    factsEn = listOf(
      "Baby's heart beats at an astonishing 150 to 170 beats per minute.",
      "Webbed fingers and toes are starting to separate into distinct digits.",
      "Spontaneous twitching and gentle floating movements begin."
    ),
    factsFr = listOf(
      "Le cœur bat à un rythme impressionnant de 150 à 170 battements par minute.",
      "Les doigts et orteils palmés commencent à se séparer.",
      "Les premiers soubresauts et mouvements spontanés débutent dans la poche."
    )
  ),
  WeeklyFetalData(
    week = 12,
    fruitEn = "Plum / Lime",
    fruitFr = "Prune / Citron Vert",
    fruitEmoji = "🍋",
    lengthCm = 5.4f,
    weightG = 14.0f,
    stageEn = "End of First Trimester",
    stageFr = "Fin du Premier Trimestre",
    headlineEn = "All vital organs, bones, and tiny fingernails are fully formed.",
    headlineFr = "Tous les organes vitaux, os et ongles minuscules sont formés.",
    factsEn = listOf(
      "Baby can now open and close tiny fingers and curl toes.",
      "Kidneys begin producing urine which circulates into amniotic fluid.",
      "The delicate vocal cords are developing inside the larynx."
    ),
    factsFr = listOf(
      "Bébé peut ouvrir et refermer ses petits doigts et plier ses orteils.",
      "Les reins commencent à produire de l'urine qui rejoint le liquide amniotique.",
      "Les cordes vocales se forment délicatement dans le larynx."
    )
  ),
  WeeklyFetalData(
    week = 16,
    fruitEn = "Avocado",
    fruitFr = "Avocat",
    fruitEmoji = "🥑",
    lengthCm = 11.6f,
    weightG = 100.0f,
    stageEn = "Second Trimester Glow",
    stageFr = "Épanouissement du 2ème Trimestre",
    headlineEn = "Baby is making facial expressions and may even suck their thumb.",
    headlineFr = "Bébé fait des expressions faciales et peut déjà sucer son pouce.",
    factsEn = listOf(
      "Baby's eyes can perceive light filtering through your abdomen.",
      "Fine soft hair called lanugo covers the body for warmth and protection.",
      "Circulatory system now pumps approximately 25 liters of blood per day."
    ),
    factsFr = listOf(
      "Les yeux perçoivent la lumière qui filtre à travers la paroi abdominale.",
      "Un duvet fin et soyeux (le lanugo) protège la peau fragile.",
      "Le système circulatoire pompe déjà près de 25 litres de sang par jour."
    )
  ),
  WeeklyFetalData(
    week = 20,
    fruitEn = "Banana (Halfway!)",
    fruitFr = "Banane (Mi-parcours !)",
    fruitEmoji = "🍌",
    lengthCm = 25.6f,
    weightG = 300.0f,
    stageEn = "Halfway Milestone",
    stageFr = "Grand Cap du Mi-Parcours",
    headlineEn = "Baby can clearly hear your voice, heartbeat, and digestive sounds.",
    headlineFr = "Bébé entend distinctement votre voix, vos battements de cœur et bruits digestifs.",
    factsEn = listOf(
      "The second trimester anatomy ultrasound (Morphology scan) is done now.",
      "Vernix caseosa (creamy protective coating) shields baby's delicate skin.",
      "You may feel fluttering 'quickening' kicks and somersaults daily."
    ),
    factsFr = listOf(
      "C'est la période de l'échographie morphologique du 2ème trimestre.",
      "Le vernix caseosa (enduit protecteur crémeux) recouvre la peau.",
      "Vous ressentez clairement les premiers coups d'ailes de papillon et galipettes."
    )
  ),
  WeeklyFetalData(
    week = 24,
    fruitEn = "Ear of Corn",
    fruitFr = "Épi de Maïs",
    fruitEmoji = "🌽",
    lengthCm = 30.0f,
    weightG = 600.0f,
    stageEn = "Viability Milestone",
    stageFr = "Cap de Viabilité Fœtale",
    headlineEn = "Taste buds have formed; baby tastes the foods you eat in amniotic fluid.",
    headlineFr = "Les papilles gustatives goûtent les saveurs des aliments dans le liquide.",
    factsEn = listOf(
      "Fetal lungs are producing surfactant to keep air sacs from collapsing.",
      "Baby has developed defined sleep and wake cycles.",
      "Reactions to loud sounds: baby may jump or startle inside the womb."
    ),
    factsFr = listOf(
      "Les poumons commencent à sécréter le surfactant indispensable à la respiration.",
      "Des cycles réguliers d'éveil et de sommeil s'installent.",
      "Bébé réagit aux bruits forts extérieurs par des sursauts protecteurs."
    )
  ),
  WeeklyFetalData(
    week = 28,
    fruitEn = "Eggplant",
    fruitFr = "Aubergine",
    fruitEmoji = "🍆",
    lengthCm = 37.6f,
    weightG = 1000.0f,
    stageEn = "Welcome to Third Trimester",
    stageFr = "Entrée dans le Troisième Trimestre",
    headlineEn = "Baby can open their eyes, blink, and dreams during active REM sleep.",
    headlineFr = "Bébé ouvre les yeux, cligne des paupières et rêve pendant son sommeil paradoxal.",
    factsEn = listOf(
      "Baby weighs around 1 kg (2.2 lbs) and is growing rapidly.",
      "The brain develops billions of new neurons and deep grooves (sulci).",
      "Baby hiccups can be felt as rhythmic, gentle rhythmic pulses."
    ),
    factsFr = listOf(
      "Bébé franchit le cap symbolique de 1 kg et grandit à toute allure.",
      "Le cortex cérébral se plisse de sillons et développe des milliards de neurones.",
      "Vous pouvez ressentir les petits hoquets rythmés et rassurants de bébé."
    )
  ),
  WeeklyFetalData(
    week = 32,
    fruitEn = "Butternut Squash",
    fruitFr = "Courge Butternut",
    fruitEmoji = "🎃",
    lengthCm = 42.4f,
    weightG = 1700.0f,
    stageEn = "Practicing Breathing",
    stageFr = "Entraînement Respiratoire",
    headlineEn = "Practicing breathing movements with diaphragm to prepare for birth.",
    headlineFr = "Entraînement des mouvements respiratoires avec le diaphragme pour le jour J.",
    factsEn = listOf(
      "All five senses are now working: vision, hearing, taste, touch, and smell.",
      "Bones are fully developed, though the skull remains soft and pliable.",
      "Fat deposits smooth out wrinkles and give baby adorable plump cheeks."
    ),
    factsFr = listOf(
      "Les 5 sens sont fonctionnels : vision, ouïe, goût, toucher et odorat.",
      "Le squelette est ossifié mais les os du crâne restent souples pour l'accouchement.",
      "La graisse sous-cutanée s'accumule et donne de jolies joues potelées."
    )
  ),
  WeeklyFetalData(
    week = 36,
    fruitEn = "Papaya / Honeydew",
    fruitFr = "Papaye / Melon",
    fruitEmoji = "🍈",
    lengthCm = 47.4f,
    weightG = 2600.0f,
    stageEn = "Head Down (Cephalic)",
    stageFr = "Descente et Engagement",
    headlineEn = "Most babies settle head-down into the pelvis preparing for birth.",
    headlineFr = "La majorité des bébés se positionnent tête en bas, prêts pour la naissance.",
    factsEn = listOf(
      "Digestion and liver are ready to process mother's milk or formula.",
      "Immune system absorbs maternal antibodies for post-birth protection.",
      "Space is tight; kicks turn into powerful rolls, stretches, and nudges."
    ),
    factsFr = listOf(
      "Le système digestif est prêt à accueillir le premier lait maternel ou formule.",
      "Bébé reçoit vos précieux anticorps pour son immunité néonatale.",
      "Moins de place pour tourner : les coups se transforment en étirements puissants."
    )
  ),
  WeeklyFetalData(
    week = 40,
    fruitEn = "Watermelon (Full Term!)",
    fruitFr = "Pastèque (À Terme !)",
    fruitEmoji = "🍉",
    lengthCm = 51.2f,
    weightG = 3400.0f,
    stageEn = "Full Term & Ready to Meet You",
    stageFr = "À Terme & Prêt à Vous Rencontrer",
    headlineEn = "Your baby is fully grown, healthy, and ready for their grand arrival!",
    headlineFr = "Votre bébé est parfaitement développé et prêt pour sa grande arrivée !",
    factsEn = listOf(
      "Lungs and surfactant levels are mature for that first big breath.",
      "Chest, arms, and legs have chubby rolls ready for newborn hugs.",
      "Any day now: your labor journey is right around the corner!"
    ),
    factsFr = listOf(
      "Les poumons sont totalement matures pour son tout premier cri.",
      "De beaux plis de peau potelés et un doux duvet pour vos premiers câlins.",
      "C'est une question de jours ou d'heures : votre rencontre magique approche !"
    )
  )
)

fun getFetalDataForWeek(week: Int): WeeklyFetalData {
  val clamped = week.coerceIn(4, 40)
  return WEEKLY_FETAL_DATABASE.minByOrNull { abs(it.week - clamped) } ?: WEEKLY_FETAL_DATABASE.first()
}

@Composable
fun InsideBump3DScreen(
  viewModel: PregnancyViewModel,
  isEnglish: Boolean = false
) {
  val settings by viewModel.settings.collectAsState()
  val lmp = settings?.lmpDateMs ?: (System.currentTimeMillis() - 140L * 24 * 3600 * 1000L)
  val (currentWeek, _) = viewModel.calculateCurrentGestationalWeek(lmp)

  var selectedWeek by remember { mutableStateOf(currentWeek.coerceIn(4, 40)) }
  val fetalData = remember(selectedWeek) { getFetalDataForWeek(selectedWeek) }

  // 3D Orbit Angles (Yaw and Pitch)
  var yawAngle by remember { mutableStateOf(35f) }
  var pitchAngle by remember { mutableStateOf(15f) }
  var zoomScale by remember { mutableStateOf(1.0f) }
  var isAutoOrbit by remember { mutableStateOf(true) }
  var selectedHotspot by remember { mutableStateOf<String?>(null) }

  // Auto-Orbit Infinite Animation
  val infiniteTransition = rememberInfiniteTransition(label = "3D Orbit")
  val autoOrbitAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 24000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "Auto Orbit Angle"
  )

  // Pulsing Heartbeat Animation (150 BPM -> ~400ms cycle)
  val heartPulse by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "Heart Pulse"
  )

  val effectiveYaw = if (isAutoOrbit) (yawAngle + autoOrbitAngle) % 360f else yawAngle

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("inside_bump_3d_screen"),
    contentPadding = PaddingValues(14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header Title Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
              Column {
                Text(
                  text = if (isEnglish) "Inside your Bump 3D" else "Dans votre Ventre en 3D",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                  text = if (isEnglish) "Interactive week-by-week 3D fetal explorer" else "Visualisation 3D interactive semaine par semaine",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
              Text(
                text = if (isEnglish) "Week $selectedWeek" else "Semaine $selectedWeek",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }
    }

    // 3D Interactive Viewport
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .height(350.dp)
          .testTag("canvas_3d_viewport"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E140F)),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          // Touch canvas with 3D projection
          Canvas(
            modifier = Modifier
              .fillMaxSize()
              .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                  change.consume()
                  isAutoOrbit = false
                  yawAngle = (yawAngle + dragAmount.x * 0.6f) % 360f
                  pitchAngle = (pitchAngle - dragAmount.y * 0.4f).coerceIn(-65f, 65f)
                }
              }
          ) {
            draw3DFetus(
              yaw = effectiveYaw,
              pitch = pitchAngle,
              scale = zoomScale,
              week = selectedWeek,
              heartPulse = heartPulse
            )
          }

          // Controls Overlay (Top Right: Auto Orbit toggle, Reset)
          Row(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilledTonalIconButton(
              onClick = { isAutoOrbit = !isAutoOrbit },
              modifier = Modifier.size(36.dp),
              colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (isAutoOrbit) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.6f),
                contentColor = Color.White
              )
            ) {
              Icon(
                imageVector = if (isAutoOrbit) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Auto Orbit",
                modifier = Modifier.size(18.dp)
              )
            }

            FilledTonalIconButton(
              onClick = {
                yawAngle = 35f
                pitchAngle = 15f
                zoomScale = 1.0f
                isAutoOrbit = true
              },
              modifier = Modifier.size(36.dp),
              colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.6f),
                contentColor = Color.White
              )
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset View",
                modifier = Modifier.size(18.dp)
              )
            }
          }

          // Heartbeat live rate badge (Bottom Left)
          Surface(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(12.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.Black.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, Color(0xFFE57373).copy(alpha = 0.6f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text("❤️", fontSize = (14 * heartPulse).sp)
              Text(
                text = if (isEnglish) "Fetal Heart: ~150 BPM" else "Cœur Fœtal : ~150 BPM",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFFCDD2),
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Gesture hint (Bottom Right)
          Surface(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(12.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.Black.copy(alpha = 0.5f)
          ) {
            Text(
              text = if (isEnglish) "👆 Drag to 3D rotate" else "👆 Glissez pour tourner à 360°",
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.8f)
            )
          }
        }
      }
    }

    // Anatomy Hotspot Chips
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = if (isEnglish) "✨ Tap an anatomical feature to explore:" else "✨ Touchez un organe pour découvrir ses secrets :",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = selectedHotspot == "brain",
            onClick = { selectedHotspot = if (selectedHotspot == "brain") null else "brain" },
            label = { Text(if (isEnglish) "🧠 Brain" else "🧠 Cerveau") },
            modifier = Modifier.weight(1f)
          )
          FilterChip(
            selected = selectedHotspot == "heart",
            onClick = { selectedHotspot = if (selectedHotspot == "heart") null else "heart" },
            label = { Text(if (isEnglish) "❤️ Heart" else "❤️ Cœur") },
            modifier = Modifier.weight(1f)
          )
          FilterChip(
            selected = selectedHotspot == "senses",
            onClick = { selectedHotspot = if (selectedHotspot == "senses") null else "senses" },
            label = { Text(if (isEnglish) "👂 Hearing" else "👂 Audition") },
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // Hotspot Popover Fact
    if (selectedHotspot != null) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
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
                text = when (selectedHotspot) {
                  "brain" -> if (isEnglish) "🧠 Brain & Neurological Development" else "🧠 Cerveau & Développement Neurologique"
                  "heart" -> if (isEnglish) "❤️ Cardiovascular System & Beat" else "❤️ Système Cardiovasculaire & Pulsation"
                  else -> if (isEnglish) "👂 Hearing & Voice Recognition" else "👂 Audition & Reconnaissance Vocale"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
              )
              IconButton(
                onClick = { selectedHotspot = null },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close",
                  tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
              }
            }

            Text(
              text = when (selectedHotspot) {
                "brain" -> if (isEnglish) {
                  "At Week $selectedWeek, baby's brain forms over 250,000 new neurons every minute! Complex synapses regulate breathing rhythms, swallowing, and sleep states."
                } else {
                  "À la semaine $selectedWeek, le cerveau de bébé produit plus de 250 000 nouveaux neurones chaque minute ! Les synapses contrôlent la succion, la déglutition et le sommeil."
                }
                "heart" -> if (isEnglish) {
                  "The tiny fetal heart pumps around 300 times more oxygen-rich blood per body ounce than an adult! The foramen ovale and ductus arteriosus naturally shunt blood away from non-functioning lungs."
                } else {
                  "Le cœur fœtal pompe avec une force remarquable pour irriguer tous les organes en développement. Le foramen ovale assure une circulation sanguine unique adaptée à la vie utérine."
                }
                else -> if (isEnglish) {
                  "Baby hears low-frequency sounds: mother's soothing voice, deep laughter, and father's voice through amniotic vibrations. Reading or singing to your bump creates lasting bonding memories."
                } else {
                  "Bébé perçoit les fréquences graves : votre voix maternelle, vos rires et la voix du papa à travers le liquide amniotique. Lui parler ou lui chanter des berceuses crée déjà un lien d'attachement précieux."
                }
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onTertiaryContainer
            )
          }
        }
      }
    }

    // Week Selector Slider
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
            Text(
              text = if (isEnglish) "Choose Pregnancy Week" else "Sélectionner la Semaine",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (isEnglish) "Week $selectedWeek / 40" else "Semaine $selectedWeek / 40",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }

          Slider(
            value = selectedWeek.toFloat(),
            onValueChange = { selectedWeek = it.roundToInt() },
            valueRange = 4f..40f,
            steps = 35,
            modifier = Modifier.testTag("slider_gestational_week")
          )

          // Trimester Quick Jump Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            FilledTonalButton(
              onClick = { selectedWeek = 12 },
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
            ) {
              Text(if (isEnglish) "T1 (W12)" else "T1 (12SA)", style = MaterialTheme.typography.labelSmall)
            }
            FilledTonalButton(
              onClick = { selectedWeek = 20 },
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
            ) {
              Text(if (isEnglish) "T2 (W20)" else "T2 (20SA)", style = MaterialTheme.typography.labelSmall)
            }
            FilledTonalButton(
              onClick = { selectedWeek = 28 },
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
            ) {
              Text(if (isEnglish) "T3 (W28)" else "T3 (28SA)", style = MaterialTheme.typography.labelSmall)
            }
            FilledTonalButton(
              onClick = { selectedWeek = 40 },
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
            ) {
              Text(if (isEnglish) "Birth (W40)" else "Terme (40SA)", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }
    }

    // Fruit Size Comparison & Dimensions
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = Color(0xFFFFF3E0),
              modifier = Modifier.size(56.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(text = fetalData.fruitEmoji, fontSize = 30.sp)
              }
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isEnglish) "Baby is the size of a:" else "Bébé a la taille de :",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = if (isEnglish) fetalData.fruitEn else fetalData.fruitFr,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = if (isEnglish) fetalData.stageEn else fetalData.stageFr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
              )
            }
          }

          Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = if (isEnglish) "Estimated Length" else "Longueur estimée",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${fetalData.lengthCm} cm",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${(fetalData.lengthCm / 2.54f).roundToInt(1)} in",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Box(
              modifier = Modifier
                .width(1.dp)
                .height(36.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = if (isEnglish) "Estimated Weight" else "Poids estimé",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = if (fetalData.weightG >= 1000f) "${(fetalData.weightG / 1000f).roundToInt(2)} kg" else "${fetalData.weightG.toInt()} g",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (fetalData.weightG >= 453.6f) "${(fetalData.weightG / 453.6f).roundToInt(1)} lbs" else "${(fetalData.weightG / 28.35f).roundToInt(1)} oz",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    // Weekly Facts & Milestones
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = if (isEnglish) "Week $selectedWeek Growth Milestones" else "Développement Clé en Semaine $selectedWeek",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = if (isEnglish) fetalData.headlineEn else fetalData.headlineFr,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
          )

          val facts = if (isEnglish) fetalData.factsEn else fetalData.factsFr
          facts.forEach { fact ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.Top
            ) {
              Text("•", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
              Text(
                text = fact,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }
}

private fun Float.roundToInt(decimals: Int): String {
  var multiplier = 1.0
  repeat(decimals) { multiplier *= 10 }
  return ((this * multiplier).roundToInt() / multiplier).toString()
}

/**
 * Custom 3D Fetus mathematical renderer on Compose Canvas with perspective, depth-shading,
 * and anatomical morphing based on gestational week.
 */
fun DrawScope.draw3DFetus(
  yaw: Float,
  pitch: Float,
  scale: Float,
  week: Int,
  heartPulse: Float
) {
  val cx = size.width / 2f
  val cy = size.height / 2f
  val baseRadius = min(size.width, size.height) * 0.42f * scale

  // 1. Amniotic Sac Background Glow
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        Color(0xFF5D2E17).copy(alpha = 0.55f),
        Color(0xFF32140A).copy(alpha = 0.35f),
        Color(0xFF140803).copy(alpha = 0.05f)
      ),
      center = Offset(cx, cy),
      radius = baseRadius * 1.15f
    ),
    center = Offset(cx, cy),
    radius = baseRadius * 1.15f
  )

  // Amniotic Sac Membrane Outline with specular highlight
  drawCircle(
    color = Color(0xFFFFCCBC).copy(alpha = 0.35f),
    center = Offset(cx, cy),
    radius = baseRadius,
    style = Stroke(width = 2.5f)
  )

  // Floating micro-bubbles in amniotic fluid
  val bubbleAngles = listOf(0.4f, 1.2f, 2.1f, 3.5f, 4.8f, 5.7f)
  bubbleAngles.forEachIndexed { i, angle ->
    val bx = cx + cos(angle + (yaw * 0.01f)) * (baseRadius * (0.6f + (i % 3) * 0.12f))
    val by = cy + sin(angle + (pitch * 0.01f)) * (baseRadius * (0.6f + (i % 3) * 0.12f))
    drawCircle(
      color = Color.White.copy(alpha = 0.25f),
      center = Offset(bx, by),
      radius = 3f + (i % 3)
    )
  }

  // 3D coordinate rotation math:
  val radYaw = (yaw * Math.PI / 180.0).toFloat()
  val radPitch = (pitch * Math.PI / 180.0).toFloat()
  val d = 600f // camera distance for perspective

  fun project3D(x: Float, y: Float, z: Float): Triple<Float, Float, Float> {
    // Yaw rotation around Y axis
    val x1 = x * cos(radYaw) + z * sin(radYaw)
    val z1 = -x * sin(radYaw) + z * cos(radYaw)
    // Pitch rotation around X axis
    val y2 = y * cos(radPitch) - z1 * sin(radPitch)
    val z2 = y * sin(radPitch) + z1 * cos(radPitch)

    // Perspective projection
    val p = d / (d + z2)
    val screenX = cx + x1 * p * scale
    val screenY = cy + y2 * p * scale
    return Triple(screenX, screenY, z2)
  }

  // Dynamic fetal morphology sizing:
  val growthFactor = ((week - 4).toFloat() / 36f).coerceIn(0.2f, 1.0f)
  val headSize = (26f + growthFactor * 32f)
  val bodyLength = (40f + growthFactor * 60f)

  // 3D Nodes for Fetal Anatomy
  val headNode = project3D(0f, -bodyLength * 0.45f, 0f)
  val neckNode = project3D(0f, -bodyLength * 0.15f, 5f)
  val chestNode = project3D(10f * sin(radYaw), bodyLength * 0.1f, 10f)
  val pelvisNode = project3D(-5f, bodyLength * 0.5f, -10f)

  // Limbs
  val leftHandNode = project3D(-15f, -5f, 25f)
  val rightHandNode = project3D(15f, -10f, 20f)
  val leftFootNode = project3D(-12f, bodyLength * 0.65f, 15f)
  val rightFootNode = project3D(18f, bodyLength * 0.62f, 10f)

  // Umbilical cord coiling
  val cordStart = project3D(0f, bodyLength * 0.2f, 15f)
  val cordMid = project3D(baseRadius * 0.4f * cos(radYaw), bodyLength * 0.1f, -baseRadius * 0.3f)
  val cordEnd = project3D(baseRadius * 0.8f, -baseRadius * 0.4f, -40f)

  // Draw Umbilical Cord (spiral/helical feel)
  val cordPath = Path().apply {
    moveTo(cordStart.first, cordStart.second)
    quadraticBezierTo(cordMid.first, cordMid.second, cordEnd.first, cordEnd.second)
  }
  drawPath(
    path = cordPath,
    color = Color(0xFF80DEEA).copy(alpha = 0.55f),
    style = Stroke(width = 5f, cap = StrokeCap.Round)
  )

  // Depth sorting: Draw parts from furthest to nearest
  val parts = listOf(
    Pair("pelvis", pelvisNode),
    Pair("body", chestNode),
    Pair("limbs", rightHandNode),
    Pair("head", headNode),
    Pair("heart", chestNode)
  ).sortedBy { it.second.third }

  parts.forEach { (part, node) ->
    when (part) {
      "head" -> {
        // Head volumetric sphere with soft radial shading
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFFFD54F),
              Color(0xFFFFB74D),
              Color(0xFFE65100).copy(alpha = 0.9f)
            ),
            center = Offset(node.first - headSize * 0.25f, node.second - headSize * 0.25f),
            radius = headSize
          ),
          center = Offset(node.first, node.second),
          radius = headSize
        )

        // Facial profile & tiny eye
        if (week >= 10) {
          val eyeOffset = project3D(12f, -bodyLength * 0.45f - 4f, 15f)
          drawCircle(
            color = Color(0xFF3E2723),
            center = Offset(eyeOffset.first, eyeOffset.second),
            radius = (headSize * 0.12f).coerceAtLeast(2.5f)
          )
        }
      }

      "body" -> {
        // Spine & Torso curved volume
        val bodyRadius = headSize * 0.85f
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFFFB74D),
              Color(0xFFF57C00),
              Color(0xFFBF360C)
            ),
            center = Offset(node.first - 10f, node.second - 10f),
            radius = bodyRadius
          ),
          center = Offset(node.first, node.second),
          radius = bodyRadius
        )
      }

      "pelvis" -> {
        drawCircle(
          color = Color(0xFFF57C00).copy(alpha = 0.85f),
          center = Offset(node.first, node.second),
          radius = headSize * 0.65f
        )
        // Legs & Feet
        drawLine(
          color = Color(0xFFFFB74D),
          start = Offset(node.first, node.second),
          end = Offset(leftFootNode.first, leftFootNode.second),
          strokeWidth = 10f,
          cap = StrokeCap.Round
        )
        drawLine(
          color = Color(0xFFFFB74D),
          start = Offset(node.first, node.second),
          end = Offset(rightFootNode.first, rightFootNode.second),
          strokeWidth = 10f,
          cap = StrokeCap.Round
        )
      }

      "limbs" -> {
        // Arms reaching forward in comfortable fetal tuck
        drawLine(
          color = Color(0xFFFFB74D),
          start = Offset(chestNode.first, chestNode.second),
          end = Offset(leftHandNode.first, leftHandNode.second),
          strokeWidth = 8f,
          cap = StrokeCap.Round
        )
        drawLine(
          color = Color(0xFFFFB74D),
          start = Offset(chestNode.first, chestNode.second),
          end = Offset(rightHandNode.first, rightHandNode.second),
          strokeWidth = 8f,
          cap = StrokeCap.Round
        )
      }

      "heart" -> {
        // Pulsing Fetal Heart with animated expanding glow
        val heartR = (8f * heartPulse).coerceAtLeast(4f)
        drawCircle(
          color = Color(0xFFFF1744).copy(alpha = 0.4f * heartPulse),
          center = Offset(node.first + 4f, node.second - 4f),
          radius = heartR * 2.2f
        )
        drawCircle(
          color = Color(0xFFFF5252),
          center = Offset(node.first + 4f, node.second - 4f),
          radius = heartR
        )
      }
    }
  }
}
