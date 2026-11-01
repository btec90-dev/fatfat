package com.example.data

import java.util.Locale

/**
 * Backend medical information assistant for the pregnancy tracking application.
 *
 * Implements strict safety rules:
 * 1. Strict safety tier categorization: "Safe", "Caution (Consult Doctor)", or "Unsafe/Avoid".
 * 2. Prioritizes pregnancy-specific complications (bleeding, preeclampsia, preterm labor, fetal developmental issues).
 * 3. References official FDA pregnancy categories (A, B, C, D, X) or modern PLLR guidelines.
 * 4. Includes empathetic, pregnancy-focused clinical summaries.
 * 5. Provides outputs in structured JSON format matching the schema.
 */
object PregnancyMedicationAssistant {

  /**
   * Primary entry point returning strictly valid JSON matching the user schema:
   * {
   *   "searchItem": "...",
   *   "pregnancySafetyTier": "...",
   *   "fdaCategory": "...",
   *   "clinicalSummary": "...",
   *   "trimesterSpecificNotes": { ... },
   *   "fetalRisks": [...],
   *   "pregnancySafeAlternatives": [...]
   * }
   *
   * Contains NO conversational filler and NO markdown formatting.
   */
  fun processSearchQueryAsJson(query: String, isEnglish: Boolean = true): String {
    val result = processSearchQuery(query, isEnglish)
    return result.toJsonString(indent = 2)
  }

  /**
   * Resolves a user search query into the structured MedicationSafetyResponse.
   */
  fun processSearchQuery(query: String, isEnglish: Boolean = true): MedicationSafetyResponse {
    val cleanQuery = query.trim().lowercase(Locale.ROOT)
    if (cleanQuery.isEmpty()) {
      return getFallbackEmptyResponse(isEnglish)
    }

    // 1. Direct match or synonym match from curated medical records
    val matched = curatedMedications.firstOrNull { med ->
      med.searchItem.lowercase(Locale.ROOT).contains(cleanQuery) ||
      (med.searchItemFr?.lowercase(Locale.ROOT)?.contains(cleanQuery) == true) ||
      med.commonBrands.any { brand -> brand.lowercase(Locale.ROOT).contains(cleanQuery) }
    }

    if (matched != null) {
      return matched.localized(isEnglish)
    }

    // 2. Intelligent clinical rule classifier for unlisted queries
    return classifyUnknownQuery(cleanQuery, query.trim(), isEnglish)
  }

  /**
   * Returns all curated medications for browsing and cataloging.
   */
  fun getAllCuratedMedications(isEnglish: Boolean = true): List<MedicationSafetyResponse> {
    return curatedMedications.map { it.localized(isEnglish) }
  }

  // --- Curated Database ---
  private val curatedMedications = listOf(
    // 1. Paracetamol / Acetaminophen
    MedicationSafetyResponse(
      searchItem = "Paracetamol (Acetaminophen)",
      searchItemFr = "Paracétamol (Doliprane, Efferalgan)",
      pregnancySafetyTier = "Safe",
      fdaCategory = "Category B / PLLR: Compatible",
      clinicalSummary = "Paracetamol is the primary, first-line recommended pain reliever and antipyretic throughout all stages of pregnancy. When taken at standard therapeutic dosages (max 3-4g/day), it has a well-established safety profile for both mother and developing baby.",
      clinicalSummaryFr = "Le paracétamol est l'antalgique et antipyrétique de premier choix recommandé à tous les stades de la grossesse aux doses thérapeutiques usuelles (max 3-4g/jour).",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Safe for acute headaches, mild pains, and fever reduction. Controlling high maternal fevers (>38.5°C) is medically critical to protect neural tube closure.",
        secondTrimester = "Preferred analgesic for musculoskeletal aches, back pain, and round ligament strain. Maintain standard intervals (at least 4-6 hours between doses).",
        thirdTrimester = "Safe choice up to delivery. Does not affect platelet aggregation, uterine contractility, or fetal circulation."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Sûr pour soulager les maux de tête et la fièvre. Traiter la fièvre maternelle élevée (>38,5°C) est essentiel pour le tube neural.",
        secondTrimester = "Analgésique de référence pour les douleurs ligamentaires ou lombaires. Espacer les prises d'au moins 4 à 6 heures.",
        thirdTrimester = "Sûr jusqu'au terme. Ne modifie ni la coagulation, ni les contractions utérines, ni la circulation fœtale."
      ),
      fetalRisks = listOf("None identified at recommended therapeutic doses"),
      fetalRisksFr = listOf("Aucun risque identifié aux doses thérapeutiques recommandées"),
      pregnancySafeAlternatives = listOf("Warm compresses", "Adequate hydration", "Prenatal rest"),
      pregnancySafeAlternativesFr = listOf("Compresses tièdes", "Hydratation optimale", "Repos"),
      categoryGroup = "Pain & Fever",
      commonBrands = listOf("Doliprane", "Tylenol", "Efferalgan", "Dafalgan", "Panadol")
    ),

    // 2. Ibuprofen (Advil, Nurofen, Motrin)
    MedicationSafetyResponse(
      searchItem = "Ibuprofen (Advil, Motrin)",
      searchItemFr = "Ibuprofène (Advil, Nurofen)",
      pregnancySafetyTier = "Unsafe/Avoid",
      fdaCategory = "Category D (3rd Trimester) / Category C (1st/2nd Trimester) / FDA Warning",
      clinicalSummary = "Ibuprofen is a non-steroidal anti-inflammatory drug (NSAID). It is strictly contraindicated from the 20th week of gestation onward because it causes fetal renal impairment leading to oligohydramnios (low amniotic fluid) and premature closure of the fetal ductus arteriosus, which can lead to neonatal heart failure or pulmonary hypertension.",
      clinicalSummaryFr = "L'ibuprofène (AINS) est formellement contre-indiqué dès la 20e semaine de grossesse (et totalement interdit au 3e trimestre) car il entraîne la fermeture prématurée du canal artériel fœtal et une insuffisance rénale fœtale avec oligoamnios.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Avoid if possible. Some epidemiological studies suggest a slightly elevated risk of early miscarriage or cardiac septal defects.",
        secondTrimester = "Strictly contraindicated from 20 weeks onward due to risk of oligohydramnios (reduction of protective amniotic fluid volume) and fetal renal dysfunction.",
        thirdTrimester = "Absolute contraindication (even a single dose). High risk of premature closure of the ductus arteriosus, persistent pulmonary hypertension, and uterine inertia."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "À éviter. Risque potentiel accru de fausse couche précoce et d'anomalies de cloisonnement cardiaque.",
        secondTrimester = "Contre-indiqué formellement dès la 20e semaine d'aménorrhée : risque majeur d'oligoamnios et de toxicité rénale fœtale.",
        thirdTrimester = "Interdiction absolue (même en prise unique). Risque de fermeture prématurée du canal artériel et d'hypertension artérielle pulmonaire néonatale."
      ),
      fetalRisks = listOf(
        "Premature closure of fetal ductus arteriosus",
        "Oligohydramnios (critical loss of amniotic fluid)",
        "Fetal renal impairment or failure",
        "Persistent pulmonary hypertension of the newborn (PPHN)",
        "Increased bleeding risk at delivery"
      ),
      fetalRisksFr = listOf(
        "Fermeture prématurée du canal artériel fœtal",
        "Oligoamnios (chute du liquide amniotique)",
        "Atteinte rénale fœtale",
        "Hypertension artérielle pulmonaire néonatale",
        "Risque hémorragique à l'accouchement"
      ),
      pregnancySafeAlternatives = listOf("Paracetamol (Acetaminophen)", "Cold/Warm compress", "Physical therapy"),
      pregnancySafeAlternativesFr = listOf("Paracétamol (Doliprane)", "Poches chaud/froid", "Kinésithérapie douce"),
      categoryGroup = "Pain & Fever",
      commonBrands = listOf("Advil", "Nurofen", "Motrin", "Antarene", "Spedifen")
    ),

    // 3. Phloroglucinol (Spasfon)
    MedicationSafetyResponse(
      searchItem = "Phloroglucinol (Spasfon)",
      searchItemFr = "Phloroglucinol (Spasfon)",
      pregnancySafetyTier = "Safe",
      fdaCategory = "PLLR: Compatible / High Safety Profile",
      clinicalSummary = "Phloroglucinol is a musculotropic antispasmodic routinely prescribed in European obstetrics to relieve benign uterine tightenings, Braxton Hicks contractions, and gastrointestinal cramping. Extensive clinical and post-marketing surveillance demonstrates no teratogenic or fetotoxic effects.",
      clinicalSummaryFr = "Le phloroglucinol est un antispasmodique largement prescrit pendant la grossesse pour calmer les contractions utérines bénignes et les spasmes digestifs sans effet nocif fœtal.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Safe for uterine cramping during embryo implantation and pelvic discomfort.",
        secondTrimester = "Widely used for ligament pain and physiological uterine tightenings. Always notify your practitioner if contractions become rhythmic.",
        thirdTrimester = "Safe up to term to differentiate Braxton Hicks false labor from true labor contractions."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Sûr pour les tiraillements pelviens et les douleurs utérines d'implantation.",
        secondTrimester = "Très utilisé contre les tiraillements ligamentaires et les contractions d'effort. Consulter si contractions rythmées.",
        thirdTrimester = "Sûr jusqu'au terme pour apaiser les fausses alertes (contractions d'entraînement de Braxton Hicks)."
      ),
      fetalRisks = listOf("None identified in clinical studies"),
      fetalRisksFr = listOf("Aucun risque identifié dans les études cliniques"),
      pregnancySafeAlternatives = listOf("Left lateral rest", "Warm relaxing bath (<37.5°C)", "Magnesium supplementation"),
      pregnancySafeAlternativesFr = listOf("Repos en décubitus latéral gauche", "Bain tiède (<37,5°C)", "Cure de magnésium"),
      categoryGroup = "Gastrointestinal & Spasms",
      commonBrands = listOf("Spasfon", "Spasfon Lyoc")
    ),

    // 4. Aspirin / Acetylsalicylic Acid
    MedicationSafetyResponse(
      searchItem = "Aspirin (High dose vs Low dose)",
      searchItemFr = "Aspirine (Aspégic, Kardégic)",
      pregnancySafetyTier = "Caution (Consult Doctor)",
      fdaCategory = "Category D (High dose) / PLLR: Low-dose (75-160mg) clinically recommended",
      clinicalSummary = "A critical distinction exists: full-dose anti-inflammatory aspirin (500-1000mg) acts as an NSAID and is contraindicated, particularly after 20 weeks. Conversely, low-dose aspirin (75-160mg daily, e.g., Kardégic) is standard obstetric protocol prescribed to high-risk mothers to prevent preeclampsia and placental insufficiency.",
      clinicalSummaryFr = "Distinguer absolument la dose : l'aspirine à forte dose (>500mg) est un AINS contre-indiqué. En revanche, l'aspirine à faible dose (75 à 160mg/jour) est prescrite par l'obstétricien pour prévenir la prééclampsie.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Low-dose aspirin is initiated before 16 weeks in women at risk of preeclampsia. High doses must be avoided.",
        secondTrimester = "Continuous daily low-dose therapy promotes healthy placental vasodilation. High anti-inflammatory doses cause oligohydramnios.",
        thirdTrimester = "Low-dose is typically discontinued around week 36 to prevent delivery bleeding complications. High doses strictly prohibited."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "L'aspirine à faible dose est débutée avant 16 SA en prévention de la prééclampsie. Éviter les fortes doses.",
        secondTrimester = "Poursuite du traitement à faible dose sous surveillance médicale. Fortes doses interdites.",
        thirdTrimester = "Arrêt de la faible dose généralement planifié vers 36 SA pour le risque hémorragique. Fortes doses formellement proscrites."
      ),
      fetalRisks = listOf(
        "High dose: Ductus arteriosus premature closure",
        "High dose: Oligohydramnios",
        "Maternal & neonatal hemorrhage at delivery",
        "Low dose (prescribed): Negligible fetal risk when monitored"
      ),
      fetalRisksFr = listOf(
        "Forte dose : Fermeture prématurée du canal artériel",
        "Forte dose : Oligoamnios",
        "Risque d'hémorragie néonatale à forte dose",
        "Faible dose (sur ordonnance) : Bénéfice protecteur majeur contre la prééclampsie"
      ),
      pregnancySafeAlternatives = listOf("Paracetamol for pain/fever", "Low-dose aspirin protocol under OB supervision only"),
      pregnancySafeAlternativesFr = listOf("Paracétamol pour la douleur/fièvre", "Aspirine faible dose uniquement sur prescription obstétricale"),
      categoryGroup = "Pain & Fever",
      commonBrands = listOf("Aspégic", "Kardégic", "Bayer Aspirin")
    ),

    // 5. Amoxicillin & Augmentin
    MedicationSafetyResponse(
      searchItem = "Amoxicillin & Augmentin",
      searchItemFr = "Amoxicilline (Clamoxyl, Augmentin)",
      pregnancySafetyTier = "Safe",
      fdaCategory = "Category B / PLLR: Compatible",
      clinicalSummary = "Amoxicillin (with or without clavulanic acid) is the first-line antibiotic of choice during pregnancy. It safely treats maternal bacterial infections (urinary tract infections, respiratory infections, dental infections, and streptococcus B) without harming the developing fetus.",
      clinicalSummaryFr = "L'amoxicilline (avec ou sans acide clavulanique) est l'antibiotique de référence pendant la grossesse pour soigner les infections urinaires, dentaires ou ORL sans danger pour le fœtus.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Extensively studied and safe. Untreated bacterial infections pose far greater risks to pregnancy than amoxicillin.",
        secondTrimester = "Safe and effective for pyelonephritis, bronchitis, and sinusitis.",
        thirdTrimester = "Safe up to and during delivery. Intravenous amoxicillin is standard intrapartum prophylaxis for Group B Strep (GBS)."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Sûr et bien toléré. Les infections non traitées sont bien plus dangereuses pour l'embryon.",
        secondTrimester = "Traitement de choix des pyélonéphrites et infections bronchiques.",
        thirdTrimester = "Sûr jusqu'à l'accouchement. Utilisé en prophylaxie du Streptocoque B."
      ),
      fetalRisks = listOf("No increased risk of fetal malformations or congenital anomalies"),
      fetalRisksFr = listOf("Aucun surcroît d'anomalies congénitales fœtales"),
      pregnancySafeAlternatives = listOf("Cefalexin", "Ceftriaxone", "Erythromycin"),
      pregnancySafeAlternativesFr = listOf("Céfixime", "Céfalexine", "Érythromycine"),
      categoryGroup = "Antibiotics",
      commonBrands = listOf("Clamoxyl", "Augmentin", "Amoxil")
    ),

    // 6. Isotretinoin (Roaccutane, Accutane)
    MedicationSafetyResponse(
      searchItem = "Isotretinoin (Accutane, Roaccutane)",
      searchItemFr = "Isotrétinoïne (Roaccutane, Curacné)",
      pregnancySafetyTier = "Unsafe/Avoid",
      fdaCategory = "Category X / Black Box Severe Teratogen",
      clinicalSummary = "Isotretinoin is a potent human teratogen. Exposure during pregnancy carries an extremely high risk (over 30-35%) of severe congenital malformations, central nervous system damage, craniofacial abnormalities, heart defects, and spontaneous abortion. It is strictly contraindicated.",
      clinicalSummaryFr = "L'isotrétinoïne est l'un des tératogènes humains les plus puissants. L'exposition entraîne un risque supérieur à 35% de malformations fœtales majeures (cerveau, cœur, visage) et d'avortement spontané. Contre-indication absolue.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Extremely dangerous. Causes major organogenesis defects including microcephaly, hydrocephalus, and absent ears.",
        secondTrimester = "Strictly contraindicated. Causes persistent brain development abnormalities and fetal demise.",
        thirdTrimester = "Strictly contraindicated. Severe systemic and neurological toxicity."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Danger critique. Malformations majeures du tube neural, cardiopathies et anomalies cranio-faciales.",
        secondTrimester = "Contre-indication absolue. Altération définitive du développement cérébral.",
        thirdTrimester = "Interdiction absolue."
      ),
      fetalRisks = listOf(
        "Craniofacial malformations (microtia, absent ear canals)",
        "Congenital heart defects (transposition of great vessels, aortic arch anomalies)",
        "Central nervous system defects (microcephaly, hydrocephalus)",
        "Spontaneous abortion and fetal death",
        "Severe cognitive impairment"
      ),
      fetalRisksFr = listOf(
        "Malformations cranio-faciales (anomalies des oreilles, du palais)",
        "Cardiopathies congénitales majeures",
        "Anomalies du système nerveux central (microcéphalie, hydrocéphalie)",
        "Avortement spontané ou mort fœtale",
        "Retard cognitif sévère"
      ),
      pregnancySafeAlternatives = listOf("Topical Azelaic Acid", "Gentle Salicylic Acid (<2%)", "Topical Erythromycin"),
      pregnancySafeAlternativesFr = listOf("Acide azélaïque topique", "Zinc", "Érythromycine locale"),
      categoryGroup = "Dermatology & Skin",
      commonBrands = listOf("Roaccutane", "Curacné", "Accutane", "Procuta")
    ),

    // 7. Retinol & Topical Retinoids
    MedicationSafetyResponse(
      searchItem = "Retinol & Topical Retinoids (Tretinoin, Retin-A)",
      searchItemFr = "Rétinol & Vitamine A Acide (Trétinoïne)",
      pregnancySafetyTier = "Unsafe/Avoid",
      fdaCategory = "Category C/X / PLLR: Avoid",
      clinicalSummary = "Although systemic absorption through skin is minimal, health authorities (FDA, EMA, ANSM) strictly advise against using topical retinoids during pregnancy due to the recognized teratogenic potential of vitamin A derivatives.",
      clinicalSummaryFr = "Même si le passage sanguin par la peau est faible, les autorités sanitaires recommandent d'arrêter tout rétinoïde topique pendant la grossesse par mesure de précaution envers l'embryopathie aux rétinoïdes.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Halt immediately. Avoid during embryonic organ formation.",
        secondTrimester = "Avoid. Switch to gentle hydrating and brightening botanicals.",
        thirdTrimester = "Avoid throughout remainder of pregnancy and breastfeeding."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Arrêt immédiat conseillé dès le projet ou le début de grossesse.",
        secondTrimester = "À proscrire. Remplacer par des soins doux hydratants.",
        thirdTrimester = "À éviter jusqu'à la fin de l'allaitement."
      ),
      fetalRisks = listOf("Theoretical retinoid embryopathy risk", "Avoidable systemic exposure"),
      fetalRisksFr = listOf("Risque théorique d'embryopathie aux rétinoïdes"),
      pregnancySafeAlternatives = listOf("Bakuchiol", "Hyaluronic Acid", "Vitamin C (L-Ascorbic Acid)", "Niacinamide"),
      pregnancySafeAlternativesFr = listOf("Bakuchiol", "Acide hyaluronique", "Vitamine C", "Niacinamide"),
      categoryGroup = "Dermatology & Skin",
      commonBrands = listOf("Retin-A", "Differin", "Effederm", "A-313")
    ),

    // 8. Lisinopril & ACE Inhibitors / ARBs
    MedicationSafetyResponse(
      searchItem = "Lisinopril & ACE Inhibitors / ARBs",
      searchItemFr = "Lisinopril & Inhibiteurs de l'IEC (Ramipril, Losartan)",
      pregnancySafetyTier = "Unsafe/Avoid",
      fdaCategory = "Category D / Black Box Teratogen",
      clinicalSummary = "ACE inhibitors and angiotensin II receptor blockers interfere with the renin-angiotensin system crucial for fetal kidney formation and amniotic fluid production. They cause fetal renal dysgenesis, anuria, severe oligohydramnios, pulmonary hypoplasia, and skull bone hypoplasia.",
      clinicalSummaryFr = "Les IEC et sartans bloquent le système rénine-angiotensine indispensable aux reins fœtaux, provoquant oligoamnios sévère, hypoplasie pulmonaire, anomalies de la voûte crânienne et mort fœtale.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Elevated risk of cardiovascular and neurological congenital malformations.",
        secondTrimester = "Severe fetal renal damage, oligohydramnios, limb contractures, and skull hypoplasia.",
        thirdTrimester = "Persistent fetal hypotension, renal failure, pulmonary hypoplasia, and perinatal death."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Risque accru de malformations cardiovasculaires et crâniennes.",
        secondTrimester = "Toxicité rénale fœtale majeure, anamnios et hypoplasie pulmonaire mortelle.",
        thirdTrimester = "Insuffisance rénale néonatale irréversible et décès in utero."
      ),
      fetalRisks = listOf(
        "Fetal renal failure and anuria",
        "Severe oligohydramnios (Potter sequence)",
        "Pulmonary hypoplasia (incomplete lung development)",
        "Cranial ossification defects",
        "Intrauterine growth restriction and fetal death"
      ),
      fetalRisksFr = listOf(
        "Insuffisance rénale fœtale irréversible",
        "Oligoamnios sévère (séquence de Potter)",
        "Hypoplasie pulmonaire",
        "Défaut d'ossification de la voûte crânienne",
        "Mort fœtale in utero"
      ),
      pregnancySafeAlternatives = listOf("Labetalol", "Methyldopa (Aldomet)", "Nifedipine extended-release"),
      pregnancySafeAlternativesFr = listOf("Labétalol (Trandate)", "Méthyldopa (Aldomet)", "Nifédipine LP"),
      categoryGroup = "Cardiovascular & Blood Pressure",
      commonBrands = listOf("Zestril", "Prinivil", "Triatec", "Coversyl", "Cozaar")
    ),

    // 9. Lovenox / Enoxaparin (Low Molecular Weight Heparin)
    MedicationSafetyResponse(
      searchItem = "Lovenox (Enoxaparin / LMWH)",
      searchItemFr = "Lovenox (Énoxaparine / HBPM)",
      pregnancySafetyTier = "Safe",
      fdaCategory = "Category B / First-Line Anticoagulant",
      clinicalSummary = "Low Molecular Weight Heparin (LMWH) molecules are large and polar; they do NOT cross the placenta, ensuring zero fetal anticoagulant effect. It is the gold standard anticoagulant in pregnancy for deep vein thrombosis (DVT) prevention, pulmonary embolism, antiphospholipid syndrome, and recurrent pregnancy loss.",
      clinicalSummaryFr = "Les héparines de bas poids moléculaire (HBPM) ne traversent pas le placenta. Elles constituent le traitement anticoagulant de référence pour prévenir les phlébites, embolies et pertes fœtales récurrentes.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Essential in women with thrombophilia or recurrent miscarriage history to support embryonic implantation and placentation.",
        secondTrimester = "Safely maintained. Requires periodic monitoring of anti-Xa levels if indicated.",
        thirdTrimester = "Continued until delivery planning. Discontinued 12-24 hours prior to scheduled induction or epidural analgesia to prevent hematoma."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Traitement clé en cas de thrombophilie pour protéger la nidation et la vascularisation placentaire.",
        secondTrimester = "Sûr et efficace. Ne passe pas la barrière fœto-placentaire.",
        thirdTrimester = "Planifier l'arrêt 12 à 24h avant l'accouchement ou la pose de péridurale selon protocole médical."
      ),
      fetalRisks = listOf("No direct fetal toxicity (does not cross the placenta)"),
      fetalRisksFr = listOf("Aucune toxicité fœtale directe (ne franchit pas le placenta)"),
      pregnancySafeAlternatives = listOf("Unfractionated Heparin", "Graduated compression stockings (Class 2)"),
      pregnancySafeAlternativesFr = listOf("Héparine standard", "Bas de contention veineuse classe 2"),
      categoryGroup = "Anticoagulants & Injections",
      commonBrands = listOf("Lovenox", "Clexane", "Fragmin", "Innohep")
    ),

    // 10. Progesterone (Utrogestan, Prometrium)
    MedicationSafetyResponse(
      searchItem = "Progesterone (Utrogestan, Endometrin)",
      searchItemFr = "Progestérone (Utrogestan, Progestan)",
      pregnancySafetyTier = "Safe",
      fdaCategory = "Category B / Essential Endocrine Support",
      clinicalSummary = "Micronized bioidentical progesterone is chemically identical to natural maternal luteal hormone. It is standard medical practice for luteal support during assisted reproduction (IVF), early pregnancy bleeding, threatened miscarriage, and preventing preterm labor in women with shortened cervix.",
      clinicalSummaryFr = "La progestérone micronisée naturelle est identique à l'hormone de grossesse. Elle est prescrite pour soutenir la phase lutéale, en cas de saignements précoces ou pour prévenir la prématurité.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Widely prescribed to stabilize endometrium and maintain early pregnancy until the placenta takes over hormone synthesis (week 10-12).",
        secondTrimester = "Used vaginally in women with cervical shortening on ultrasound (<25mm) to decrease preterm birth risk.",
        thirdTrimester = "Usually discontinued between 34 and 36 weeks as cervical remodeling prepares for normal labor."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Prescrite pour sécuriser la nidation et soutenir le corps jaune jusqu'au relais placentaire.",
        secondTrimester = "Utilisée par voie vaginale en cas de col court (<25mm) pour prévenir l'accouchement prématuré.",
        thirdTrimester = "Généralement arrêtée vers 34-36 SA."
      ),
      fetalRisks = listOf("None identified with natural bioidentical progesterone"),
      fetalRisksFr = listOf("Aucun risque identifié avec la progestérone naturelle bio-identique"),
      pregnancySafeAlternatives = listOf("Obstetric rest", "Cervical cerclage (when indicated by OB)"),
      pregnancySafeAlternativesFr = listOf("Repos gynécologique", "Cerclage du col (sur indication obstétricale)"),
      categoryGroup = "Hormones & Fertility",
      commonBrands = listOf("Utrogestan", "Progestan", "Endometrin", "Crinone")
    ),

    // 11. Gaviscon & Antacids
    MedicationSafetyResponse(
      searchItem = "Gaviscon & Calcium Antacids",
      searchItemFr = "Gaviscon & Anti-acides (Rennie, Maalox)",
      pregnancySafetyTier = "Safe",
      fdaCategory = "Category B / Local Action",
      clinicalSummary = "Sodium alginate and calcium carbonate form a viscous protective barrier floating on top of gastric contents, preventing acidic reflux into the esophagus. Because they act locally and have minimal systemic absorption, they are safe first-line remedies for pregnancy heartburn.",
      clinicalSummaryFr = "L'alginate de sodium et le carbonate de calcium forment un gel protecteur qui surnage dans l'estomac pour stopper les remontées acides, sans passage dans le sang maternel ou fœtal.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Safe for morning sickness and early reflux.",
        secondTrimester = "Very useful as progesterone relaxes the lower esophageal sphincter.",
        thirdTrimester = "Highly beneficial as the enlarging uterus compresses the stomach upwards."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Sûr pour calmer les nausées acides matinales.",
        secondTrimester = "Efficace contre le reflux lié au relâchement du cardia sous l'effet de la progestérone.",
        thirdTrimester = "Indispensable pour soulager la compression gastrique par l'utérus en fin de grossesse."
      ),
      fetalRisks = listOf("None (local physical action without systemic absorption)"),
      fetalRisksFr = listOf("Aucun (action mécanique locale sans absorption générale)"),
      pregnancySafeAlternatives = listOf("Elevating the head of the bed", "Fractionated meals", "Avoiding lying down after eating"),
      pregnancySafeAlternativesFr = listOf("Surélever la tête du lit", "Fractionner les repas", "Éviter la position allongée après le repas"),
      categoryGroup = "Gastrointestinal & Spasms",
      commonBrands = listOf("Gaviscon", "Rennie", "Maalox", "Tums")
    ),

    // 12. Omeprazole & PPIs (Mopral, Inexium)
    MedicationSafetyResponse(
      searchItem = "Omeprazole (Prilosec, Mopral)",
      searchItemFr = "Oméprazole (Mopral, Inexium, Ésoméprazole)",
      pregnancySafetyTier = "Safe",
      fdaCategory = "Category C / PLLR: Compatible",
      clinicalSummary = "Large prospective human pregnancy registries including tens of thousands of exposed pregnancies demonstrate that omeprazole does not increase the baseline risk of congenital malformations. Indicated when antacids fail for erosive reflux or severe gastritis.",
      clinicalSummaryFr = "L'oméprazole et les IPP bénéficient d'un recul clinique majeur sur des dizaines de milliers de grossesses confirmant l'absence de risque malformatif. Recommandé en cas de reflux résistant au Gaviscon.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Safe if indicated for intractable nausea/vomiting associated with acid reflux.",
        secondTrimester = "Preferred proton pump inhibitor for severe pregnancy reflux.",
        thirdTrimester = "Safe up to term for persistent pyrosis and esophagitis."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Sûr si nécessaire en cas de vomissements acides incoercibles.",
        secondTrimester = "IPP de référence pour le pyrosis sévère de la femme enceinte.",
        thirdTrimester = "Sûr jusqu'au terme."
      ),
      fetalRisks = listOf("No evidence of increased congenital malformations"),
      fetalRisksFr = listOf("Aucun surcroît de malformations congénitales"),
      pregnancySafeAlternatives = listOf("Gaviscon", "Famotidine", "Dietary adjustments"),
      pregnancySafeAlternativesFr = listOf("Gaviscon", "Alimentation non épicée", "Fractionnement des repas"),
      categoryGroup = "Gastrointestinal & Spasms",
      commonBrands = listOf("Mopral", "Inexium", "Prilosec", "Losec")
    ),

    // 13. Cetirizine & Loratadine (Antihistamines)
    MedicationSafetyResponse(
      searchItem = "Cetirizine & Loratadine (Zyrtec, Claritin)",
      searchItemFr = "Cétirizine & Loratadine (Zyrtec, Clarityne)",
      pregnancySafetyTier = "Safe",
      fdaCategory = "Category B / Second-Generation Antihistamines",
      clinicalSummary = "Second-generation non-sedating antihistamines are considered safe during pregnancy. Large epidemiological studies have not revealed any adverse impact on pregnancy outcomes or fetal development when managing seasonal allergies, rhinitis, or urticaria.",
      clinicalSummaryFr = "Les antihistaminiques de deuxième génération (cétirizine, loratadine) sont les antiallergiques de référence pendant la grossesse, sans surcroît de risque fœtal.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Safe for allergic rhinitis and pruritus during early pregnancy.",
        secondTrimester = "Well-tolerated; minimizes maternal sleep disruption from allergies.",
        thirdTrimester = "Safe up to delivery. Non-sedating compared to older first-generation agents."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Sûr pour la rhinite allergique saisonnière et l'urticaire.",
        secondTrimester = "Bien toléré, améliore le sommeil maternel perturbé par les allergies.",
        thirdTrimester = "Sûr jusqu'à terme sans sédation excessive."
      ),
      fetalRisks = listOf("None identified in prospective registry studies"),
      fetalRisksFr = listOf("Aucun risque identifié dans les registres de suivi"),
      pregnancySafeAlternatives = listOf("Saline nasal irrigation", "HEPA air filtration", "Avoiding allergen triggers"),
      pregnancySafeAlternativesFr = listOf("Lavage nasal au sérum physiologique", "Aération du domicile", "Éviction des allergènes"),
      categoryGroup = "Allergies & Respiratory",
      commonBrands = listOf("Zyrtec", "Clarityne", "Claritin", "Aerius")
    ),

    // 14. Pseudoephedrine & Oral Decongestants (Sudafed, Actifed)
    MedicationSafetyResponse(
      searchItem = "Pseudoephedrine (Sudafed, Actifed)",
      searchItemFr = "Pseudoéphédrine (Actifed, Dolirhume, Humex)",
      pregnancySafetyTier = "Unsafe/Avoid",
      fdaCategory = "Category C / Vasoconstrictor Risk",
      clinicalSummary = "Pseudoephedrine is a sympathomimetic vasoconstrictor. In the first trimester, maternal use has been epidemiologically associated with gastroschisis (fetal abdominal wall defect) and hemi-facial microsomia due to vascular disruption. It can also induce maternal hypertension and diminish placental blood perfusion.",
      clinicalSummaryFr = "La pseudoéphédrine est un puissant vasoconstricteur. Au 1er trimestre, elle est suspectée d'augmenter le risque de laparoschisis (défaut de paroi abdominale fœtale) et peut réduire le flux utéro-placentaire.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Avoid. Associated with vascular disruption defects (gastroschisis).",
        secondTrimester = "Avoid. Risk of maternal hypertension and impaired uteroplacental blood flow.",
        thirdTrimester = "Avoid. Can cause maternal tachycardia, elevated blood pressure, and fetal tachycardia."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "À proscrire absolument (suspicion de laparoschisis fœtal par spasme vasculaire).",
        secondTrimester = "À éviter : risque d'élévation tensionnelle et de baisse de perfusion utérine.",
        thirdTrimester = "À éviter : tachycardie fœtale et maternelle."
      ),
      fetalRisks = listOf(
        "Gastroschisis (fetal abdominal wall fissure)",
        "Compromised uteroplacental blood flow",
        "Fetal tachycardia"
      ),
      fetalRisksFr = listOf(
        "Laparoschisis fœtal (défaut de fermeture de la paroi abdominale)",
        "Baisse du débit sanguin utéro-placentaire",
        "Tachycardie fœtale"
      ),
      pregnancySafeAlternatives = listOf("Saline nasal sprays", "Steam inhalation with pure water", "Paracetamol for headache"),
      pregnancySafeAlternativesFr = listOf("Sérum physiologique ou eau de mer en spray", "Inhalations de vapeur d'eau pure", "Paracétamol"),
      categoryGroup = "Allergies & Respiratory",
      commonBrands = listOf("Sudafed", "Actifed", "Dolirhume", "Humex Rhume")
    ),

    // 15. Hot Tubs, Saunas & Extreme Maternal Heat
    MedicationSafetyResponse(
      searchItem = "Hot Tubs & Saunas (Hyperthermia)",
      searchItemFr = "Bains bouillonnants chauds, Jacuzzis & Saunas",
      pregnancySafetyTier = "Unsafe/Avoid",
      fdaCategory = "N/A (Health Habit / Thermal Hazard)",
      clinicalSummary = "Maternal core body temperatures exceeding 39°C (102.2°F), especially in hot tubs (>38°C) or saunas for longer than 10-15 minutes, double the risk of neural tube defects (anencephaly, spina bifida) in early pregnancy, and can trigger uterine hyperperfusion compromise or syncope.",
      clinicalSummaryFr = "Une température corporelle maternelle dépassant 39°C (bains très chauds, saunas >10 min) au 1er trimestre double le risque d'anomalies de fermeture du tube neural (spina bifida) et favorise les malaises.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Strictly avoid hot tubs and prolonged saunas. Core hyperthermia disrupts embryonic neural fold closure.",
        secondTrimester = "Caution. Hot water causes maternal vasodilatation, leading to maternal hypotension and dizziness.",
        thirdTrimester = "Avoid excessive heat. Maternal hypotension reduces placental perfusion; increases risk of maternal falls."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "À proscrire formellement : risque majeur d'anomalies de fermeture du tube neural fœtal.",
        secondTrimester = "Déconseillé : risque de chute de tension maternelle brutale et malaise.",
        thirdTrimester = "À éviter : hypoperfusion placentaire par vasodilatation périphérique et risque de chute."
      ),
      fetalRisks = listOf(
        "Neural tube defects (spina bifida, anencephaly)",
        "Early miscarriage / embryonic arrest",
        "Reduced placental perfusion during maternal syncope"
      ),
      fetalRisksFr = listOf(
        "Anomalies de fermeture du tube neural (spina bifida)",
        "Fausse couche précoce par hyperthermie embryonnaire",
        "Hypoxie fœtale transitoire lors des malaises maternels"
      ),
      pregnancySafeAlternatives = listOf("Warm bath under 37°C / 98.6°F", "Foot soaks with Epsom salts", "Prenatal massage"),
      pregnancySafeAlternativesFr = listOf("Bain tiède à température corporelle (<37°C)", "Bain de pieds délassant", "Massage prénatal"),
      categoryGroup = "Habits & Lifestyle",
      commonBrands = listOf("Jacuzzi", "Sauna", "Hammam")
    ),

    // 16. Caffeine & Coffee
    MedicationSafetyResponse(
      searchItem = "Caffeine & Coffee (>200mg/day)",
      searchItemFr = "Caféine & Café (Consommation >200mg/jour)",
      pregnancySafetyTier = "Caution (Consult Doctor)",
      fdaCategory = "N/A (Dietary Substance)",
      clinicalSummary = "Caffeine freely crosses the placenta, and the fetal liver lacks the cytochrome P450 enzymes needed to metabolize it. High intake (>200mg/day, roughly two standard cups of brewed coffee) has been correlated with reduced birth weight and fetal vasoconstriction. Moderate consumption under 200mg/day is considered acceptable.",
      clinicalSummaryFr = "La caféine traverse librement le placenta et le fœtus l'élimine très lentement. Une consommation supérieure à 200 mg/jour (environ 2 tasses) est liée à un risque de petit poids de naissance. Modérer à 1-2 tasses max.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Limit intake to ≤200mg/day. Heavy intake (>300mg) has been associated with increased miscarriage risk.",
        secondTrimester = "Moderate intake. Excessive caffeine can exacerbate maternal heartburn and nocturnal insomnia.",
        thirdTrimester = "Fetal clearance of caffeine slows significantly; high maternal intake can cause fetal tachycardia and maternal palpitations."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Limiter impérativement sous 200 mg/jour pour éviter tout surcroît de fausse couche.",
        secondTrimester = "La caféine aggrave le reflux gastrique et les troubles du sommeil.",
        thirdTrimester = "L'élimination fœtale est très lente à terme : risque d'agitation et de tachycardie fœtale."
      ),
      fetalRisks = listOf(
        "Low birth weight / Intrauterine growth restriction (with >200mg/day)",
        "Fetal vasoconstriction and tachycardia",
        "Delayed fetal neurobehavioral settling"
      ),
      fetalRisksFr = listOf(
        "Retard de croissance intra-utérin / Petit poids de naissance",
        "Vasoconstriction placentaire et tachycardie fœtale",
        "Perturbation du sommeil fœtal"
      ),
      pregnancySafeAlternatives = listOf("Decaffeinated coffee", "Rooibos herbal tea", "Warm spiced milk", "Fruit infusions"),
      pregnancySafeAlternativesFr = listOf("Café décaféiné à l'eau", "Rooibos (sans théine)", "Infusions de verveine ou tilleul"),
      categoryGroup = "Habits & Lifestyle",
      commonBrands = listOf("Espresso", "Cold Brew", "Energy Drinks", "Cola")
    ),

    // 17. Alcohol (Ethanol)
    MedicationSafetyResponse(
      searchItem = "Alcohol (Wine, Beer, Spirits)",
      searchItemFr = "Alcool (Vin, Bière, Spiritueux)",
      pregnancySafetyTier = "Unsafe/Avoid",
      fdaCategory = "Category X / Zero Tolerance Rule",
      clinicalSummary = "There is no known safe amount or timing for alcohol consumption during pregnancy. Ethanol easily traverses the placental membrane into fetal blood and amniotic fluid. It directly impairs neurogenesis, leading to Fetal Alcohol Spectrum Disorders (FASD), microcephaly, characteristic facial dysmorphisms, and irreversible cognitive impairments.",
      clinicalSummaryFr = "Zéro alcool pendant la grossesse. L'alcool passe directement dans le liquide amniotique et détruit les neurones fœtaux en développement, entraînant le syndrome d'alcoolisation fœtale (SAF) avec retard mental irréversible.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Absolute avoidance. Highly vulnerable window for gross brain and organogenesis defects.",
        secondTrimester = "Absolute avoidance. Cellular migration and neurodevelopment in the fetal brain are compromised.",
        thirdTrimester = "Absolute avoidance. Brain growth spurt continues; risk of preterm delivery and neonatal withdrawal."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Zéro alcool absolu. Phase critique d'organogenèse et de formation du cerveau embryonnaire.",
        secondTrimester = "Zéro alcool. Altération majeure de la migration cellulaire neuronale.",
        thirdTrimester = "Zéro alcool. Période de croissance synaptique intense et risque d'accouchement prématuré."
      ),
      fetalRisks = listOf(
        "Fetal Alcohol Syndrome (FAS)",
        "Microcephaly and permanent intellectual disability",
        "Distinctive facial dysmorphism (smooth philtrum, thin vermilion border)",
        "Intrauterine growth restriction",
        "Spontaneous abortion and stillbirth"
      ),
      fetalRisksFr = listOf(
        "Syndrome d'Alcoolisation Fœtale (SAF)",
        "Microcéphalie et handicap intellectuel irréversible",
        "Dysmorphie cranio-faciale caractéristique",
        "Retard de croissance intra-utérin sévère",
        "Mort fœtale in utero"
      ),
      pregnancySafeAlternatives = listOf("Zero-proof mocktails", "Sparkling water with fresh mint and citrus", "Chilled herbal teas"),
      pregnancySafeAlternativesFr = listOf("Mocktails sans alcool", "Eau pétillante avec rondelle de citron et menthe", "Jus de fruits frais"),
      categoryGroup = "Habits & Lifestyle",
      commonBrands = listOf("Vin", "Bière", "Champagne", "Cocktails")
    ),

    // 18. Folic Acid / Vitamin B9
    MedicationSafetyResponse(
      searchItem = "Folic Acid (Vitamin B9)",
      searchItemFr = "Acide Folique (Vitamine B9 / Spéciafoldine)",
      pregnancySafetyTier = "Safe",
      fdaCategory = "Category A / Mandatory Prophylaxis",
      clinicalSummary = "Folic acid is essential for DNA synthesis and maternal erythrocyte expansion. Periconceptional supplementation (400 mcg daily, or 5 mg daily for women with diabetes or prior affected pregnancy) reduces the incidence of neural tube defects (anencephaly, spina bifida) by up to 70-80%.",
      clinicalSummaryFr = "L'acide folique est le nutriment protecteur indispensable. Sa prise préconceptionnelle et au 1er trimestre réduit de plus de 70% le risque d'anomalies de fermeture du tube neural (spina bifida).",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Critical window: mandatory from pre-conception through week 12 of pregnancy to guarantee complete neural tube closure.",
        secondTrimester = "Continued via prenatal multivitamins to support rapid maternal blood volume expansion and fetal cell growth.",
        thirdTrimester = "Supports maternal red blood cell mass and reduces maternal megaloblastic anemia."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Période clé absolue : à prendre dès le désir d'enfant et jusqu'à 12 SA pour sceller le tube neural.",
        secondTrimester = "Poursuivi dans les multivitamines prénatales pour la multiplication cellulaire fœtale.",
        thirdTrimester = "Soutient la synthèse des globules rouges et prévient l'anémie mégaloblastique."
      ),
      fetalRisks = listOf("Deficiency causes severe neural tube defects (spina bifida, anencephaly)"),
      fetalRisksFr = listOf("La carence entraîne de graves anomalies du tube neural (spina bifida, anencéphalie)"),
      pregnancySafeAlternatives = listOf("Dietary folates (spinach, lentils, broccoli, avocado, citrus)"),
      pregnancySafeAlternativesFr = listOf("Légumes verts à feuilles, lentilles, pois chiches, agrumes"),
      categoryGroup = "Vitamins & Supplements",
      commonBrands = listOf("Spéciafoldine", "Maternov", "Femibion", "Gestarelle")
    ),

    // 19. Sertraline (Zoloft)
    MedicationSafetyResponse(
      searchItem = "Sertraline (Zoloft)",
      searchItemFr = "Sertraline (Zoloft)",
      pregnancySafetyTier = "Caution (Consult Doctor)",
      fdaCategory = "Category C / PLLR: Favorable Benefit-Risk SSRI",
      clinicalSummary = "Sertraline is considered the selective serotonin reuptake inhibitor (SSRI) of choice when maternal depression, anxiety, or OCD necessitates medical therapy. Untreated maternal depression poses real risks of poor nutrition, preeclampsia, and preterm birth. Mild, transient Poor Neonatal Adaptation (PNA) may occur post-delivery.",
      clinicalSummaryFr = "La sertraline est l'antidépresseur ISRS de référence si une dépression ou anxiété maternelle sévère nécessite un traitement. Ne jamais interrompre brutalement sans avis médical.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Lowest rates of cardiac congenital malformations among SSRIs in extensive cohorts. Consult psychiatrist/OB before making dose adjustments.",
        secondTrimester = "Promotes maternal emotional stability and adherence to prenatal care.",
        thirdTrimester = "Near-term use is associated with self-limiting neonatal behavioral syndrome (jitteriness, mild respiratory tachypnea) resolving within 48-72 hours."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Meilleur profil malformatif parmi les ISRS. Ne pas stopper brutalement.",
        secondTrimester = "Maintien de l'équilibre psychique maternel essentiel à la grossesse.",
        thirdTrimester = "Surveillance néonatale recommandée (syndrome d'adaptation néonatal transitoire bénin pendant 48-72h)."
      ),
      fetalRisks = listOf(
        "Transient neonatal behavioral syndrome (jitteriness, transient feeding difficulty)",
        "Rare persistent pulmonary hypertension of newborn (PPHN) baseline increase (~0.2%)",
        "Untreated maternal depression poses equal or greater fetal hazards"
      ),
      fetalRisksFr = listOf(
        "Syndrome d'adaptation néonatale transitoire (trémulations, irritabilité passagère)",
        "Risque très rare d'hypertension pulmonaire persistante (<0,2%)",
        "La dépression maternelle non traitée présente des risques équivalents"
      ),
      pregnancySafeAlternatives = listOf("Cognitive Behavioral Therapy (CBT)", "Perinatal psychotherapy", "Mindfulness-based stress reduction"),
      pregnancySafeAlternativesFr = listOf("Thérapie Cognitive et Comportementale (TCC)", "Soutien psychologique périnatal", "Méditation de pleine conscience"),
      categoryGroup = "Mental Health",
      commonBrands = listOf("Zoloft", "Sertraline")
    ),

    // 20. Valproic Acid / Depakine
    MedicationSafetyResponse(
      searchItem = "Valproic Acid (Depakote, Depakine)",
      searchItemFr = "Acide Valproïque (Dépakine, Micropakine)",
      pregnancySafetyTier = "Unsafe/Avoid",
      fdaCategory = "Category X / Major Human Teratogen",
      clinicalSummary = "Valproic acid is one of the most hazardous medications in pregnancy. First-trimester exposure causes neural tube defects in up to 1-2% of exposed fetuses (10 to 20 times the population risk), major facial clefts, heart defects, and induces permanent intellectual disability and autism spectrum disorder in 30-40% of children.",
      clinicalSummaryFr = "La Dépakine est formellement contre-indiquée pendant la grossesse en raison d'un taux de malformations congénitales de plus de 10% (spina bifida, cœur) et de 30 à 40% de troubles neuro-développementaux majeurs (autisme, baisse de QI).",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Severe risk of neural tube defects (spina bifida), cleft palate, and cardiovascular anomalies.",
        secondTrimester = "Persistent impairment of fetal neurodevelopment and cortical architecture.",
        thirdTrimester = "Severe neonatal hypoglycemia, withdrawal, and coagulopathy."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Risque extrême de spina bifida, de cardiopathies et de fentes faciales.",
        secondTrimester = "Altération irréversible de l'architecture cérébrale fœtale.",
        thirdTrimester = "Troubles de la coagulation et sevrage néonatal sévère."
      ),
      fetalRisks = listOf(
        "Spina bifida and severe neural tube defects (10-20x increased risk)",
        "Autism spectrum disorders and intellectual deficit (up to 40%)",
        "Congenital heart malformations",
        "Cleft lip and palate",
        "Fetal valproate syndrome facial dysmorphism"
      ),
      fetalRisksFr = listOf(
        "Spina bifida et anomalies de fermeture du tube neural",
        "Troubles du spectre de l'autisme et baisse de QI (jusqu'à 40%)",
        "Malformations cardiaques et fentes palatines",
        "Syndrome fœtal de l'acide valproïque"
      ),
      pregnancySafeAlternatives = listOf("Lamotrigine (Lamictal)", "Levetiracetam (Keppra) - both under strict neurologist guidance"),
      pregnancySafeAlternativesFr = listOf("Lamotrigine (Lamictal)", "Lévétiracétam (Keppra) sous contrôle strict du neurologue"),
      categoryGroup = "Mental Health",
      commonBrands = listOf("Dépakine", "Depakote", "Micropakine")
    ),

    // 21. Doxylamine & Pyridoxine (Diclegis, Cariban)
    MedicationSafetyResponse(
      searchItem = "Doxylamine + Vitamin B6 (Diclegis, Cariban)",
      searchItemFr = "Doxylamine + Vitamine B6 (Cariban, Donormyl)",
      pregnancySafetyTier = "Safe",
      fdaCategory = "Category A / First-Line Nausea Treatment",
      clinicalSummary = "The combination of doxylamine (an H1 antihistamine) and pyridoxine (vitamin B6) is the only FDA Category A antiemetic indicated explicitly for nausea and vomiting of pregnancy (NVP). Decades of rigorous safety trials demonstrate complete safety for embryonic and fetal development.",
      clinicalSummaryFr = "L'association de doxylamine et de vitamine B6 est le traitement de premier choix validé par l'ANSM et la FDA pour les nausées et vomissements de grossesse, avec le plus haut niveau de sécurité (Catégorie A).",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Gold standard treatment for morning sickness and hyperemesis gravidarum support.",
        secondTrimester = "Safe to continue if nausea persists into the mid-trimester.",
        thirdTrimester = "Safe if nocturnal reflux or nausea continues. May cause mild drowsiness."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Traitement de référence des nausées matinales du premier trimestre.",
        secondTrimester = "Poursuite sans danger si les nausées persistent.",
        thirdTrimester = "Sûr si les nausées ou insomnies persistent en fin de grossesse."
      ),
      fetalRisks = listOf("None identified (Category A highest safety classification)"),
      fetalRisksFr = listOf("Aucun risque identifié (Sécurité maximale de Catégorie A)"),
      pregnancySafeAlternatives = listOf("Ginger root capsules or infusion", "Acupressure wristbands (P6)", "Small frequent dry meals"),
      pregnancySafeAlternativesFr = listOf("Gélules ou infusions de gingembre", "Bracelets d'acupression P6", "Petits repas fractionnés"),
      categoryGroup = "Gastrointestinal & Spasms",
      commonBrands = listOf("Cariban", "Diclegis", "Donormyl")
    ),

    // 22. Metformin (Glucophage)
    MedicationSafetyResponse(
      searchItem = "Metformin (Glucophage)",
      searchItemFr = "Metformine (Glucophage, Stagid)",
      pregnancySafetyTier = "Safe",
      fdaCategory = "Category B / Endorsed for Gestational Diabetes & PCOS",
      clinicalSummary = "Metformin crosses the placenta but does not cause congenital malformations or adverse glycemic episodes in the fetus. It is widely used for polycystic ovary syndrome (PCOS) and gestational diabetes when dietary modifications alone fail to achieve target blood glucose levels.",
      clinicalSummaryFr = "La metformine traverse le placenta sans effet tératogène. Elle est utilisée pour le syndrome des ovaires polykystiques (SOPK) et le diabète gestationnel en complément des mesures hygiéno-diététiques.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Safe to maintain in patients with PCOS to prevent early pregnancy loss.",
        secondTrimester = "Controls fasting and postprandial glucose surges, reducing macrosomia risk.",
        thirdTrimester = "Maintains glycemic stability; insulin remains the gold standard if targets are not met."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Peut être poursuivie en cas de SOPK.",
        secondTrimester = "Aide à contrôler les glycémies et prévient la macrosomie fœtale.",
        thirdTrimester = "Surveillance étroite de la croissance fœtale."
      ),
      fetalRisks = listOf("No increased risk of structural birth defects"),
      fetalRisksFr = listOf("Aucun surcroît d'anomalies morphologiques fœtales"),
      pregnancySafeAlternatives = listOf("Subcutaneous Insulin (Gold standard)", "Nutritional carbohydrate control"),
      pregnancySafeAlternativesFr = listOf("Insuline sous-cutanée (traitement de référence)", "Régime diabétique adapté"),
      categoryGroup = "Endocrine & Diabetes",
      commonBrands = listOf("Glucophage", "Stagid")
    ),

    // 23. Vitamin D3 (Cholecalciferol)
    MedicationSafetyResponse(
      searchItem = "Vitamin D3 (Cholecalciferol)",
      searchItemFr = "Vitamine D3 (Cholécalciférol, Uvedose)",
      pregnancySafetyTier = "Safe",
      fdaCategory = "Category A / Standard Supplementation",
      clinicalSummary = "Vitamin D is essential for maternal calcium absorption, optimal fetal skeleton mineralization, and healthy neonatal dental enamel. Adequate maternal vitamin D levels are linked to lower rates of preeclampsia, gestational diabetes, and low birth weight.",
      clinicalSummaryFr = "La vitamine D est indispensable à l'absorption du calcium et à la minéralisation du squelette du fœtus. Une dose de charge (ex: Uvedose 100 000 UI) est classiquement prescrite au début du 3e trimestre.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Promotes early placental implantation and maternal immune balance.",
        secondTrimester = "Supports embryonic and early fetal osteogenesis.",
        thirdTrimester = "Crucial period for fetal calcium accretion in bones. Single high-dose ampoule (100,000 IU) is routine at 6-7 months."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Participe au bon équilibre immunitaire de l'implantation.",
        secondTrimester = "Soutient la minéralisation osseuse du fœtus.",
        thirdTrimester = "Période clé de fixation du calcium fœtal : ampoule unique de 100 000 UI recommandée au 7e mois."
      ),
      fetalRisks = listOf("Deficiency causes neonatal hypocalcemia and impaired bone mineralization"),
      fetalRisksFr = listOf("La carence favorise l'hypocalcémie néonatale et le rachitisme"),
      pregnancySafeAlternatives = listOf("Safe outdoor sun exposure (15 min/day)", "Enriched dairy products"),
      pregnancySafeAlternativesFr = listOf("Exposition modérée au soleil", "Produits laitiers enrichis"),
      categoryGroup = "Vitamins & Supplements",
      commonBrands = listOf("Uvedose", "ZymaD", "D-Cure")
    ),

    // 24. Doxycycline & Tetracyclines
    MedicationSafetyResponse(
      searchItem = "Doxycycline & Tetracyclines",
      searchItemFr = "Doxycycline & Tétracyclines (Vibramycine, Tolexine)",
      pregnancySafetyTier = "Unsafe/Avoid",
      fdaCategory = "Category D / Calcium-Binding Antibiotic",
      clinicalSummary = "Tetracyclines readily cross the placenta and bind to calcium in developing fetal bones and deciduous teeth. Use from the 16th week of gestation onward causes permanent yellow-brown tooth discoloration, enamel hypoplasia, and temporary inhibition of fetal fibular bone growth.",
      clinicalSummaryFr = "Les cyclines se fixent sur le calcium des os et des dents de lait du fœtus à partir de la 16e SA, provoquant une coloration jaune-brunâtre définitive des dents et un retard passager de croissance osseuse.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Avoid. Safer alternatives should always be chosen.",
        secondTrimester = "Strictly contraindicated from 16 weeks onward due to irreversible dental discoloration.",
        thirdTrimester = "Strictly contraindicated. Permanent tooth staining and temporary bone growth arrest."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "À éviter. Privilégier des antibiotiques sans affinité calcique.",
        secondTrimester = "Contre-indiqué formellement dès 16 SA : coloration irréversible des dents de lait fœtales.",
        thirdTrimester = "Interdiction absolue."
      ),
      fetalRisks = listOf(
        "Permanent yellow-brown discoloration of deciduous teeth",
        "Dental enamel hypoplasia",
        "Transient inhibition of fetal bone growth"
      ),
      fetalRisksFr = listOf(
        "Coloration jaune-brunâtre indélébile des dents de lait fœtales",
        "Hypoplasie de l'émail dentaire",
        "Ralentissement transitoire de la croissance osseuse"
      ),
      pregnancySafeAlternatives = listOf("Amoxicillin", "Azithromycin", "Cefalexin"),
      pregnancySafeAlternativesFr = listOf("Amoxicilline", "Azithromycine", "Céfalexine"),
      categoryGroup = "Antibiotics",
      commonBrands = listOf("Doxycycline", "Tolexine", "Vibramycine")
    ),

    // 25. Ciprofloxacin & Fluoroquinolones
    MedicationSafetyResponse(
      searchItem = "Ciprofloxacin & Fluoroquinolones (Cipro, Oflocet)",
      searchItemFr = "Ciprofloxacine & Fluoroquinolones (Ciflox, Oflocet)",
      pregnancySafetyTier = "Unsafe/Avoid",
      fdaCategory = "Category C / Cartilage Toxicity",
      clinicalSummary = "Fluoroquinolones exhibit high affinity for developing connective tissue and articular cartilage in juvenile animal models, producing arthropathies and erosions. They are avoided during pregnancy whenever beta-lactam or macrolide antibiotics are effective.",
      clinicalSummaryFr = "Les fluoroquinolones présentent une affinité toxique pour les cartilages de croissance dans les études animales. Elles sont évitées chez la femme enceinte au profit des pénicillines et céphalosporines.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Avoid. Switch to beta-lactams (Amoxicillin, Ceftriaxone).",
        secondTrimester = "Avoid. Potential risk to developing fetal articular cartilage.",
        thirdTrimester = "Avoid. Reserve strictly for resistant, life-threatening maternal infections where no alternative exists."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "À éviter. Remplacer par des bêta-lactamines.",
        secondTrimester = "À éviter en raison du tropisme pour les cartilages articulaires fœtaux.",
        thirdTrimester = "À éviter sauf urgence infectieuse vitale sans aucune alternative."
      ),
      fetalRisks = listOf("Potential fetal cartilage and joint development lesions"),
      fetalRisksFr = listOf("Risque potentiel d'atteinte du cartilage articulaire fœtal"),
      pregnancySafeAlternatives = listOf("Amoxicillin", "Cefixime", "Ceftriaxone", "Nitrofurantoin (except near delivery)"),
      pregnancySafeAlternativesFr = listOf("Amoxicilline", "Céfixime", "Ceftriaxone"),
      categoryGroup = "Antibiotics",
      commonBrands = listOf("Ciflox", "Cipro", "Oflocet", "Tavanic")
    ),

    // 26. Nicotine & Smoking
    MedicationSafetyResponse(
      searchItem = "Nicotine & Tobacco (Smoking, Vaping)",
      searchItemFr = "Nicotine & Tabac (Cigarette, Vapoteuse)",
      pregnancySafetyTier = "Unsafe/Avoid",
      fdaCategory = "Category D/X (Toxicant)",
      clinicalSummary = "Inhaled nicotine and carbon monoxide cause acute vasoconstriction of uterine and umbilical arteries, causing chronic fetal hypoxia. Smoking significantly elevates rates of placental abruption, placenta previa, intrauterine growth restriction (IUGR), stillbirth, and sudden infant death syndrome (SIDS).",
      clinicalSummaryFr = "Le tabagisme et la nicotine provoquent une vasoconstriction des vaisseaux placentaires et une hypoxie fœtale chronique, multipliant les risques de retard de croissance fœtal, d'hématome rétro-placentaire et de mort subite du nourrisson.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Stopping smoking immediately restores embryonic oxygenation and halves miscarriage risk.",
        secondTrimester = "Reduces risk of placental abruption and premature rupture of membranes.",
        thirdTrimester = "Significantly improves birth weight and neonatal lung function."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "L'arrêt précoce restaure l'oxygénation de l'embryon et réduit le risque de fausse couche.",
        secondTrimester = "Diminue le risque d'hématome rétro-placentaire et de rupture prématurée des poches des eaux.",
        thirdTrimester = "Chaque jour sans tabac fait gagner des grammes précieux au bébé."
      ),
      fetalRisks = listOf(
        "Intrauterine growth restriction (IUGR) & low birth weight",
        "Placental abruption and placenta previa",
        "Preterm birth and premature rupture of membranes",
        "Sudden Infant Death Syndrome (SIDS)",
        "Childhood respiratory asthma"
      ),
      fetalRisksFr = listOf(
        "Retard de croissance intra-utérin (RCIU) et petit poids de naissance",
        "Hématome rétro-placentaire et placenta praevia",
        "Accouchement prématuré",
        "Mort inattendue du nourrisson (MIN)",
        "Asthme infantile"
      ),
      pregnancySafeAlternatives = listOf("Medical smoking cessation counseling", "Behavioral therapy", "Doctor-supervised nicotine replacement patches/gums if needed"),
      pregnancySafeAlternativesFr = listOf("Consultation de tabacologie prénatale", "Accompagnement psychologique", "Substituts nicotiniques sous avis médical si besoin"),
      categoryGroup = "Habits & Lifestyle",
      commonBrands = listOf("Cigarettes", "Vape", "Puff", "Chicha")
    ),

    // 27. Iron / Ferrous Sulfate
    MedicationSafetyResponse(
      searchItem = "Iron (Ferrous Sulfate, Tardyferon)",
      searchItemFr = "Fer (Tardyféron, Fumafer)",
      pregnancySafetyTier = "Safe",
      fdaCategory = "Category A / Essential Mineral",
      clinicalSummary = "Maternal plasma volume expands by nearly 50% during pregnancy, creating high physiological iron demands for fetal development and placental growth. Iron supplementation prevents maternal iron deficiency anemia, fatigue, and reduces risks of low birth weight and preterm delivery.",
      clinicalSummaryFr = "Le volume sanguin maternel augmente de 50% pendant la grossesse. Une supplémentation en fer (ex: Tardyféron) est indispensable en cas d'anémie ou de ferritine basse pour prévenir la fatigue et la prématurité.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Routine screening of ferritin; iron is taken with orange juice for enhanced absorption.",
        secondTrimester = "Standard period for supplementation as fetal red blood cell production peaks.",
        thirdTrimester = "Ensures sufficient fetal iron stores for the first 6 months of neonatal life."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Bilan initial de la ferritine. Prendre avec un jus d'orange riche en vitamine C.",
        secondTrimester = "Période habituelle de supplémentation en cas de baisse du taux d'hémoglobine.",
        thirdTrimester = "Constitue les réserves en fer du bébé pour ses six premiers mois de vie."
      ),
      fetalRisks = listOf("Maternal deficiency causes fetal growth restriction and preterm birth"),
      fetalRisksFr = listOf("La carence maternelle favorise la prématurité et le retard de croissance"),
      pregnancySafeAlternatives = listOf("Dietary iron (lentils, eggs, dark poultry, spinach)"),
      pregnancySafeAlternativesFr = listOf("Alimentation riche en fer (lentilles, boudin noir bien cuit, œufs, épinards)"),
      categoryGroup = "Vitamins & Supplements",
      commonBrands = listOf("Tardyféron", "Fumafer", "Ferrostrane")
    ),

    // 28. Tramadol & Codeine (Opioids)
    MedicationSafetyResponse(
      searchItem = "Tramadol & Codeine (Opioids)",
      searchItemFr = "Tramadol & Codéine (Topalgic, Dafalgan Codéiné)",
      pregnancySafetyTier = "Caution (Consult Doctor)",
      fdaCategory = "Category C/D / Opioid Caution",
      clinicalSummary = "Opioid analgesics should be reserved strictly for severe maternal pain when paracetamol is ineffective, for the shortest possible duration under close medical supervision. Prolonged use near delivery can lead to neonatal opioid withdrawal syndrome (NOWS) and maternal/neonatal respiratory depression.",
      clinicalSummaryFr = "Les antalgiques opioïdes (palier 2) ne doivent être pris que sur prescription médicale formelle pour une douleur aiguë résistante au paracétamol. L'usage prolongé expose au syndrome de sevrage néonatal.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Use with caution. Only if benefits outweigh risks for severe intractable pain.",
        secondTrimester = "Short-term monitored use acceptable when paracetamol fails.",
        thirdTrimester = "High caution. Prolonged exposure causes neonatal opioid withdrawal syndrome (NOWS) requiring NICU monitoring."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Prudence. Uniquement sur avis médical pour douleur réfractaire.",
        secondTrimester = "Usage court et surveillé si le paracétamol seul échoue.",
        thirdTrimester = "Prudence accrue : l'usage répété au 3e trimestre entraîne un syndrome de sevrage chez le nouveau-né."
      ),
      fetalRisks = listOf(
        "Neonatal Opioid Withdrawal Syndrome (NOWS)",
        "Transient neonatal respiratory depression near term",
        "Neonatal lethargy and feeding difficulties"
      ),
      fetalRisksFr = listOf(
        "Syndrome de sevrage néonatal aux opioïdes",
        "Dépression respiratoire néonatale si prise proche de l'accouchement",
        "Hypotonie et somnolence néonatale"
      ),
      pregnancySafeAlternatives = listOf("Paracetamol at full therapeutic dose", "Regional local analgesia", "Acupuncture and physiotherapy"),
      pregnancySafeAlternativesFr = listOf("Paracétamol à dose optimale (1g x 3/jour)", "Kinésithérapie et ostéopathie prénatale douce"),
      categoryGroup = "Pain & Fever",
      commonBrands = listOf("Tramadol", "Topalgic", "Ixprim", "Dafalgan Codéiné", "Codoliprane")
    )
  )

  /**
   * Fallback rule-based classifier that handles unlisted user queries
   * adhering strictly to the 4 critical safety rules.
   */
  private fun classifyUnknownQuery(cleanQuery: String, originalQuery: String, isEnglish: Boolean): MedicationSafetyResponse {
    // 1. Check for NSAID / COX-2 inhibitors -> Unsafe/Avoid
    if (cleanQuery.contains("nsaid") || cleanQuery.contains("anti-inflamm") || cleanQuery.contains("naproxen") ||
        cleanQuery.contains("ketoprofen") || cleanQuery.contains("diclofenac") || cleanQuery.contains("celecoxib") ||
        cleanQuery.contains("voltar") || cleanQuery.contains("profénid") || cleanQuery.contains("flurbiprofen")) {
      return MedicationSafetyResponse(
        searchItem = originalQuery,
        pregnancySafetyTier = "Unsafe/Avoid",
        fdaCategory = "Category D / Black Box NSAID Warning",
        clinicalSummary = "NSAIDs are contraindicated during pregnancy, particularly from week 20 onward. They block prostaglandin synthesis required for fetal renal blood flow (causing oligohydramnios) and cause premature closure of the fetal ductus arteriosus.",
        clinicalSummaryFr = "Les anti-inflammatoires non stéroïdiens (AINS) sont contre-indiqués pendant la grossesse et formellement interdits dès la 20e semaine car ils provoquent la fermeture prématurée du canal artériel fœtal et une atteinte rénale fœtale avec oligoamnios.",
        trimesterSpecificNotes = TrimesterNotes(
          firstTrimester = "Avoid. Potential elevated risk of early pregnancy loss.",
          secondTrimester = "Contraindicated from 20 weeks due to fetal renal toxicity and loss of amniotic fluid.",
          thirdTrimester = "Strict contraindication. Even a single dose can cause premature ductus arteriosus constriction and neonatal pulmonary hypertension."
        ),
        trimesterSpecificNotesFr = TrimesterNotes(
          firstTrimester = "À éviter. Risque de fausse couche précoce.",
          secondTrimester = "Contre-indiqué dès la 20e SA (atteinte rénale fœtale et oligoamnios).",
          thirdTrimester = "Interdiction absolue (fermeture prématurée du canal artériel et hypertension pulmonaire néonatale)."
        ),
        fetalRisks = listOf(
          "Premature closure of the ductus arteriosus",
          "Oligohydramnios (reduced amniotic fluid)",
          "Fetal renal impairment",
          "Persistent pulmonary hypertension of the newborn (PPHN)"
        ),
        fetalRisksFr = listOf(
          "Fermeture prématurée du canal artériel fœtal",
          "Oligoamnios (chute du liquide amniotique)",
          "Insuffisance rénale fœtale",
          "Hypertension artérielle pulmonaire du nouveau-né"
        ),
        pregnancySafeAlternatives = listOf("Paracetamol (Acetaminophen)", "Warm compress", "Gentle massage"),
        pregnancySafeAlternativesFr = listOf("Paracétamol (Doliprane)", "Poches thermiques tièdes", "Massages doux"),
        categoryGroup = "Pain & Fever"
      ).localized(isEnglish)
    }

    // 2. Check for Retinoids / Vitamin A excess -> Unsafe/Avoid
    if (cleanQuery.contains("retin") || cleanQuery.contains("tretinoin") || cleanQuery.contains("adapalene") ||
        cleanQuery.contains("tazarotene")) {
      return MedicationSafetyResponse(
        searchItem = originalQuery,
        pregnancySafetyTier = "Unsafe/Avoid",
        fdaCategory = "Category X / Teratogen Warning",
        clinicalSummary = "Retinoids and potent vitamin A derivatives carry recognized teratogenic risks, disrupting embryonic neural crest migration and craniofacial/cardiac morphogenesis. Systemic or topical retinoid formulations must be avoided throughout pregnancy.",
        clinicalSummaryFr = "Les rétinoïdes et dérivés de vitamine A sont tératogènes et altèrent le développement du tube neural, du crâne et du cœur fœtal. Ils sont formellement à proscrire pendant toute la grossesse.",
        trimesterSpecificNotes = TrimesterNotes(
          firstTrimester = "Strictly contraindicated due to major organogenesis malformation risks.",
          secondTrimester = "Contraindicated. Risk of persistent central nervous system disruption.",
          thirdTrimester = "Contraindicated up to birth and breastfeeding."
        ),
        trimesterSpecificNotesFr = TrimesterNotes(
          firstTrimester = "Contre-indication formelle (risque élevé de malformations d'organogenèse).",
          secondTrimester = "Contre-indiqué pour préserver le développement cérébral.",
          thirdTrimester = "À éviter jusqu'à la fin de la grossesse et de l'allaitement."
        ),
        fetalRisks = listOf(
          "Craniofacial malformations",
          "Cardiac septal defects",
          "Central nervous system structural anomalies",
          "Spontaneous abortion"
        ),
        fetalRisksFr = listOf(
          "Malformations cranio-faciales",
          "Cardiopathies congénitales",
          "Anomalies du système nerveux central",
          "Avortement spontané"
        ),
        pregnancySafeAlternatives = listOf("Azelaic Acid", "Bakuchiol", "Hyaluronic Acid", "Niacinamide"),
        pregnancySafeAlternativesFr = listOf("Acide azélaïque", "Bakuchiol", "Acide hyaluronique", "Niacinamide"),
        categoryGroup = "Dermatology & Skin"
      ).localized(isEnglish)
    }

    // 3. Check for ACE inhibitors / ARBs (-pril, -sartan) -> Unsafe/Avoid
    if (cleanQuery.endsWith("pril") || cleanQuery.endsWith("sartan") || cleanQuery.contains("enalapril") || cleanQuery.contains("ramipril")) {
      return MedicationSafetyResponse(
        searchItem = originalQuery,
        pregnancySafetyTier = "Unsafe/Avoid",
        fdaCategory = "Category D / Black Box Fetotoxicity",
        clinicalSummary = "Antihypertensives blocking the renin-angiotensin cascade cause profound fetal renal failure, anuria, severe oligohydramnios, skull hypoplasia, and perinatal death.",
        clinicalSummaryFr = "Ces antihypertenseurs bloquent le système rénine-angiotensine fœtal, provoquant anamnios, hypoplasie pulmonaire, anomalies crâniennes et décès in utero.",
        trimesterSpecificNotes = TrimesterNotes(
          firstTrimester = "Avoid. Risk of cardiovascular and cranial malformations.",
          secondTrimester = "Strictly contraindicated due to fetal renal failure and oligohydramnios.",
          thirdTrimester = "Strictly contraindicated due to pulmonary hypoplasia and fetal death."
        ),
        trimesterSpecificNotesFr = TrimesterNotes(
          firstTrimester = "À éviter (anomalies cardiovasculaires).",
          secondTrimester = "Contre-indiqué formellement (oligoamnios et atteinte rénale).",
          thirdTrimester = "Interdiction absolue (décès in utero, hypoplasie pulmonaire)."
        ),
        fetalRisks = listOf(
          "Fetal renal failure and anuria",
          "Severe oligohydramnios (Potter's sequence)",
          "Pulmonary hypoplasia",
          "Neonatal death"
        ),
        fetalRisksFr = listOf(
          "Insuffisance rénale fœtale",
          "Oligoamnios sévère",
          "Hypoplasie pulmonaire",
          "Mort fœtale in utero"
        ),
        pregnancySafeAlternatives = listOf("Labetalol", "Methyldopa", "Nifedipine extended-release"),
        pregnancySafeAlternativesFr = listOf("Labétalol (Trandate)", "Méthyldopa (Aldomet)", "Nifédipine LP"),
        categoryGroup = "Cardiovascular & Blood Pressure"
      ).localized(isEnglish)
    }

    // 4. Default responsible caution response for any unspecified drug/topic
    return MedicationSafetyResponse(
      searchItem = originalQuery,
      pregnancySafetyTier = "Caution (Consult Doctor)",
      fdaCategory = "PLLR: Individualized Medical Assessment Required",
      clinicalSummary = "Safety data for \"$originalQuery\" requires individual clinical evaluation by your obstetrician, gynecologist, or certified midwife. Never initiate, adjust, or discontinue medication during pregnancy without direct medical guidance.",
      clinicalSummaryFr = "Les données de sécurité pour \"$originalQuery\" nécessitent une évaluation médicale personnalisée par votre gynécologue, obstétricien ou sage-femme. Ne commencez ni n'arrêtez aucun traitement sans avis médical.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = "Exercise heightened caution during embryonic organ formation (weeks 3 through 10). Always review treatment safety with your care team.",
        secondTrimester = "Assess dosage, fetal growth parameters, and maternal indications under physician supervision.",
        thirdTrimester = "Evaluate near-term effects, timing regarding labor, and neonatal transition."
      ),
      trimesterSpecificNotesFr = TrimesterNotes(
        firstTrimester = "Prudence maximale pendant la période d'organogenèse (semaines 3 à 10). Consulter votre médecin avant toute prise.",
        secondTrimester = "Évaluer les bénéfices et la posologie sous surveillance obstétricale.",
        thirdTrimester = "Vérifier la compatibilité avec l'accouchement et l'accueil du nouveau-né."
      ),
      fetalRisks = listOf(
        "Unknown specific risks without individualized medical review",
        "Potential unintended placental transfer or pharmacologic effect"
      ),
      fetalRisksFr = listOf(
        "Risques spécifiques indéterminés sans avis médical personnalisé",
        "Passage transplacentaire possible nécessitant confirmation"
      ),
      pregnancySafeAlternatives = listOf(
        "Consult your OB/GYN or midwife",
        "Paracetamol (if searching for pain relief)",
        "Non-pharmacologic lifestyle measures"
      ),
      pregnancySafeAlternativesFr = listOf(
        "Consulter votre sage-femme ou obstétricien",
        "Paracétamol (si recherche d'antalgique)",
        "Mesures hygiéno-diététiques adaptées"
      ),
      categoryGroup = "General Inquiry"
    ).localized(isEnglish)
  }

  private fun getFallbackEmptyResponse(isEnglish: Boolean): MedicationSafetyResponse {
    return MedicationSafetyResponse(
      searchItem = if (isEnglish) "Medication Search" else "Recherche Médicament",
      pregnancySafetyTier = "Caution (Consult Doctor)",
      fdaCategory = "N/A",
      clinicalSummary = if (isEnglish) "Please enter the name of a medication, active ingredient, symptom, or health habit to check pregnancy safety."
                        else "Veuillez saisir le nom d'un médicament, d'une molécule, d'un symptôme ou d'une habitude pour vérifier sa sécurité.",
      trimesterSpecificNotes = TrimesterNotes(
        firstTrimester = if (isEnglish) "Enter a query to view 1st trimester risks." else "Entrez une recherche pour voir les risques au 1er trimestre.",
        secondTrimester = if (isEnglish) "Enter a query to view 2nd trimester risks." else "Entrez une recherche pour voir les risques au 2e trimestre.",
        thirdTrimester = if (isEnglish) "Enter a query to view 3rd trimester risks." else "Entrez une recherche pour voir les risques au 3e trimestre."
      ),
      fetalRisks = listOf(if (isEnglish) "No search query provided" else "Aucune recherche saisie"),
      pregnancySafeAlternatives = listOf(
        if (isEnglish) "Paracetamol" else "Paracétamol",
        if (isEnglish) "Folic Acid" else "Acide folique"
      ),
      categoryGroup = "General"
    )
  }
}
