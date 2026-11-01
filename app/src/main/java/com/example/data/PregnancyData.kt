package com.example.data

data class GuideItem(
  val title: String,
  val category: String, // "Alimentation", "Interdictions", "Hygiène de vie", "Urgences"
  val description: String,
  val iconName: String
)

data class GlossaryItem(
  val term: String,
  val definition: String,
  val category: String
)

data class TimelineWeekInfo(
  val weekNumber: Int,
  val trimester: String,
  val fetalSize: String, // ex: "Taille d'une framboise (~1.5 cm)"
  val fetalDevelopment: String,
  val maternalChanges: String,
  val medicalMilestones: String
)

data class MedicalAppointmentMilestone(
  val weeksTextFr: String,
  val weeksTextEn: String,
  val titleFr: String,
  val titleEn: String,
  val locationFr: String,
  val locationEn: String,
  val datePeriodFr: String,
  val datePeriodEn: String,
  val descriptionFr: String,
  val descriptionEn: String,
  val keyExamsFr: List<String>,
  val keyExamsEn: List<String>
)

data class BirthPlanQuestion(
  val id: String,
  val theme: String, // "Accueil", "Travail", "Péridurale", "Accouchement", "Nouveau-né"
  val question: String,
  val options: List<String>
)

object PregnancyData {
  val guideItems = listOf(
    GuideItem(
      title = "Aliments à éviter absolument",
      category = "Interdictions",
      description = "Viandes crues ou peu cuites (toxoplasmose), poissons crus (sushis), fruits de mer non cuits, œufs crus ou produits à base d'œufs crus (mayonnaise maison), fromages au lait cru (risques de listériose).",
      iconName = "warning"
    ),
    GuideItem(
      title = "Fruits et légumes",
      category = "Alimentation",
      description = "Lavez soigneusement tous les fruits et légumes crus avec de l'eau vinaigrée pour éliminer les risques de toxoplasmose. Évitez de consommer des crudités hors de chez vous si le lavage n'est pas garanti.",
      iconName = "eco"
    ),
    GuideItem(
      title = "Suppléments recommandés",
      category = "Alimentation",
      description = "Acide folique (vitamine B9) indispensable en début de grossesse (et avant conception), vitamine D, iode et fer selon les analyses sanguines prescrites par votre médecin.",
      iconName = "medication"
    ),
    GuideItem(
      title = "Hydratation et boissons",
      category = "Hygiène de vie",
      description = "Buvez au moins 1,5 à 2 litres d'eau par jour. Évitez totalement l'alcool, limitez la caféine à 1-2 tasses par jour et bannissez les boissons énergisantes.",
      iconName = "water"
    ),
    GuideItem(
      title = "Signaux d'alerte (Urgences médicales)",
      category = "Urgences",
      description = "Consultez immédiatement ou rendez-vous à la maternité en cas de : pertes de sang vaginales, contractions utérines douloureuses et régulières avant terme, perte de liquide amniotique (fissure de la poche), maux de tête violents associés à des troubles visuels ou œdèmes soudains, diminution ou absence de mouvements fœtaux, fièvre > 38°C.",
      iconName = "emergency"
    )
  )

  val glossaryItems = listOf(
    GlossaryItem("SA (Semaines d'Aménorrhée)", "Nombre de semaines écoulées depuis le premier jour des dernières règles (DDR). C'est l'unité de mesure standard en obstétrique.", "Général"),
    GlossaryItem("DPA (Date Prévue d'Accouchement)", "Date théorique du terme de la grossesse, fixée à 41 semaines d'aménorrhée (soit 39 semaines de grossesse effective).", "Général"),
    GlossaryItem("Colostrum", "Premier lait maternel sécrété dès la fin de la grossesse et les premiers jours après l'accouchement, extrêmement riche en anticorps.", "Allaitement"),
    GlossaryItem("Amniocentèse", "Prélèvement de liquide amniotique par ponction à travers la paroi abdominale, pratiqué pour analyser le caryotype fœtal.", "Examens"),
    GlossaryItem("Contractions de Braxton-Hicks", "Contractions utérines de préparation, indolores, irrégulières et non rythmées, survenant souvent dès le deuxième trimestre.", "Physiologie"),
    GlossaryItem("DPNI (Dépistage Prénatal Non Invasif)", "Prise de sang maternelle analysant l'ADN fœtal circulant pour évaluer le risque de trisomie 21, 13 et 18 sans risque pour le fœtus.", "Examens"),
    GlossaryItem("Clarté Nucale", "Mesure de l'épaisseur de la nuque du fœtus lors de l'échographie du premier trimestre (11-13 SA), indicateur de dépistage chromosomique.", "Échographie"),
    GlossaryItem("Streptocoque B (SGB)", "Bactérie recherchée par prélèvement vaginal en fin de grossesse (35-37 SA) afin de prévenir une infection chez le nouveau-né lors de l'accouchement.", "Examens"),
    GlossaryItem("Progestérone (PIO)", "Hormone progestative administrée en soutien de phase lutéale ou en début de grossesse (notamment en PMA / TEC) pour favoriser la nidation.", "Médication"),
    GlossaryItem("Terme / Post-terme", "Grossesse dépassant 41 SA + 6 jours, nécessitant une surveillance rapprochée et un déclenchement programmé.", "Accouchement")
  )

  fun getTimelineData(): List<TimelineWeekInfo> {
    return listOf(
      TimelineWeekInfo(1, "1er Trimestre", "Cellule microscopique", "Fécondation et division cellulaire rapide dans la trompe de Fallope.", "Début de la sécrétion hormonale de hCG.", "Calcul de la DPA et première prescription d'acide folique."),
      TimelineWeekInfo(2, "1er Trimestre", "Cellule microscopique", "Migration du zygote et fusion des pronoyaux.", "Nidation dans l'endomètre utérin.", "Maintien du taux de progestérone (soutien PMA)."),
      TimelineWeekInfo(3, "1er Trimestre", "Grain de sable (~0.2 mm)", "Formation du blastocyste et différenciation des couches cellulaires.", "Légers spottings d'implantation possibles.", "Confirmation du transfert embryonnaire (TEC)."),
      TimelineWeekInfo(4, "1er Trimestre", "Taille d'un pavot (~1 mm)", "Formation du tube neural, amorce du cœur et du placenta.", "Absence de règles, seins sensibles, fatigue intense.", "Prise de sang Bêta-hCG quantitative de confirmation."),
      TimelineWeekInfo(5, "1er Trimestre", "Graine de sésame (~2 mm)", "Le cœur primitif commence à battre et à pomper le sang.", "Premières nausées matinales et tension mammaire.", "Vérification de l'évolution du taux de hCG."),
      TimelineWeekInfo(6, "1er Trimestre", "Lentille (~4 mm)", "Fermeture du tube neural, ébauche des yeux et des bourgeons des membres.", "Fatigue prononcée, sensibilité olfactive accrue.", "Premier rendez-vous de datation gynécologique."),
      TimelineWeekInfo(7, "1er Trimestre", "Myrtille (~9 mm)", "Développement intense du cerveau et des hémisphères cérébraux.", "Envies fréquentes d'uriner, salivation abondante.", "Planification du bilan prénatal sérologique."),
      TimelineWeekInfo(8, "1er Trimestre", "Framboise (~1.5 cm)", "Les membres s'allongent, les mains et pieds se dessinent.", "Utérus de la taille d'une orange, persistance des nausées.", "Échographie de viabilité précoce (8-10 SA)."),
      TimelineWeekInfo(9, "1er Trimestre", "Raisin (~2.3 cm)", "Premiers mouvements fœtaux spontanés invisibles, formation des valves cardiaques.", "Ventre à peine arrondi, sautes d'humeur hormonales.", "Suivi de la tolérance aux suppléments vitaminiques."),
      TimelineWeekInfo(10, "1er Trimestre", "Figue (~3.1 cm)", "Les organes vitaux majeurs fonctionnent, apparition des ongles et des dents.", "Énergie fluctuante, la peau peut devenir plus sensible.", "Écoute des battements cardiaques au Doppler."),
      TimelineWeekInfo(11, "1er Trimestre", "Citron vert (~4.1 cm)", "Le fœtus bouge activement, le diaphragme se met en place.", "Atténuation progressive des nausées pour certaines.", "Préparation de l'échographie du premier trimestre."),
      TimelineWeekInfo(12, "1er Trimestre", "Prune (~5.4 cm)", "Organes pleinement en place, réflexes de succion et déglutition.", "Le ventre commence à s'arrondir visiblement.", "Échographie du 1er trimestre (11-13 SA), clarté nucale et DPNI."),
      TimelineWeekInfo(13, "1er Trimestre", "Pêche (~7.4 cm)", "Fin du 1er trimestre, les cordes vocales se constituent.", "Regain d'énergie, diminution des nausées.", "Établissement de la déclaration officielle de grossesse."),
      TimelineWeekInfo(14, "2ème Trimestre", "Citron (~8.7 cm)", "Les reins produisent de l'urine, le fœtus commence à faire des grimaces.", "Énergie retrouvée, peau lumineuse (glow de grossesse).", "Consultation prénatale mensuelle du 4e mois."),
      TimelineWeekInfo(15, "2ème Trimestre", "Pomme (~10 cm)", "Le squelette se durcit, le lanugo (duvet protecteur) commence à pousser.", "Premiers mouvements subtils (bulles ou papillons).", "Examen de prévention bucco-dentaire gratuit."),
      TimelineWeekInfo(16, "2ème Trimestre", "Avocat (~11.6 cm)", "Les yeux perçoivent la lumière à travers les paupières closes.", "Ventre bien visible, essoufflement léger lors d'efforts.", "Discussion du bilan sérologique du 2e trimestre."),
      TimelineWeekInfo(17, "2ème Trimestre", "Navet (~13 cm)", "Formation du tissu adipeux brun pour réguler la température corporelle.", "Premiers coups de pied perceptibles pour les mamans attentives.", "Surveillance de la tension artérielle."),
      TimelineWeekInfo(18, "2ème Trimestre", "Poivron (~14.2 cm)", "L'audition se développe, le bébé entend la voix maternelle et les battements cardiaques.", "Élargissement de l'utérus, ligaments utérins étirés.", "Préparation de l'échographie morphologique T2."),
      TimelineWeekInfo(19, "2ème Trimestre", "Tomate (~15.3 cm)", "Sécrétion du vernix caseosa (substance cireuse protégeant la peau dans le liquide).", "Mouvements fœtaux de plus en plus nets et réguliers.", "Échographie morphologique de référence (20-22 SA)."),
      TimelineWeekInfo(20, "2ème Trimestre", "Banane (~16.4 cm)", "Cap d'or de la mi-grossesse ! Cheveux, sourcils et ongles bien formés.", "Ventre rebondi, sensations de jambes lourdes possibles.", "Échographie morphologique niveau II (détail complet des organes)."),
      TimelineWeekInfo(21, "2ème Trimestre", "Carotte (~18 cm)", "Le système digestif absorbe activement le liquide amniotique.", "Légère rétention d'eau, contractions utérines d'entraînement (Braxton-Hicks).", "Consultation prénatale du 5e mois."),
      TimelineWeekInfo(22, "2ème Trimestre", "Papaye (~19 cm)", "Les sens s'affinent, le pancréas commence à produire de l'insuline.", "Prise de poids régulière, besoin d'hydratation accrue.", "Vérification de la croissance et du col utérin."),
      TimelineWeekInfo(23, "2ème Trimestre", "Mangue (~20 cm)", "Les poumons fabriquent le surfactant alvéolaire indispensable à la respiration.", "Dos cambré, légères douleurs lombaires.", "Conseils ergonomiques et de posture."),
      TimelineWeekInfo(24, "2ème Trimestre", "Épi de maïs (~21 cm)", "Viabilité extra-utérine possible en service de néonatalogie spécialisé.", "Rythme de prise de poids plus soutenu, risque de carence en fer.", "Dépistage du diabète gestationnel (HGPO avec glucose)."),
      TimelineWeekInfo(25, "3ème Trimestre", "Chou-fleur (~22 cm)", "Le système nerveux central mature à grande vitesse, réagit aux stimulations sonores.", "Sommeil parfois perturbé, essoufflement.", "Vaccination coqueluche recommandée pour l'entourage."),
      TimelineWeekInfo(26, "3ème Trimestre", "Lutue / Chou (~23 cm)", "Ouverture des yeux, phases de sommeil paradoxal (rêves in utero).", "Crampes dans les mollets, besoin de pauses fréquentes.", "Consultation prénatale du 6e mois."),
      TimelineWeekInfo(27, "3ème Trimestre", "Rutabaga (~24 cm)", "Fin du 2e trimestre, le cerveau coordonne les mouvements respiratoires.", "Contractions d'entraînement plus fréquentes.", "Contrôle sérologique (RAI) si rhésus négatif."),
      TimelineWeekInfo(28, "3ème Trimestre", "Aubergine (~25 cm)", "Le bébé cligne des yeux et tourne la tête vers les sources lumineuses filtrées.", "Essoufflement accru, remontées acides.", "Entrée dans le 3e trimestre, injection d'immunoglobulines si besoin."),
      TimelineWeekInfo(29, "3ème Trimestre", "Courge butternut (~26 cm)", "Stockage intensif de graisse sous-cutanée, régulation thermique autonome.", "Fatigue de fin de journée, jambes lourdes.", "Consultation prénatale du 7e mois."),
      TimelineWeekInfo(30, "3ème Trimestre", "Chou cabus (~27 cm)", "La moelle osseuse produit tous les globules rouges du fœtus.", "Essoufflement, envies fréquentes d'uriner par compression de la vessie.", "Échographie de croissance T3 (30-32 SA)."),
      TimelineWeekInfo(31, "3ème Trimestre", "Noix de coco (~28 cm)", "Le cerveau traite des millions d'informations et connexions synaptiques par seconde.", "Pression pelvienne, insomnies passagères.", "Évaluation de la position fœtale (céphalique/siège)."),
      TimelineWeekInfo(32, "3ème Trimestre", "Courge (~29 cm)", "Les ongles des mains et des pieds atteignent le bout des doigts.", "Ventre haut, contractions de Braxton-Hicks régulières.", "Consultation prénatale du 8e mois."),
      TimelineWeekInfo(33, "3ème Trimestre", "Ananas (~30 cm)", "Le système immunitaire reçoit des anticorps maternels protecteurs.", "Fatigue accentuée, repos fractionné conseillé.", "Surveillance de la tension artérielle et des œdèmes."),
      TimelineWeekInfo(34, "3ème Trimestre", "Melon cantaloup (~31 cm)", "Les poumons sont quasi matures, le bébé adopte sa position de naissance.", "Essoufflement parfois soulagé si la tête descend dans le bassin.", "Entretien prénatal du 8e mois (projet de naissance)."),
      TimelineWeekInfo(35, "3ème Trimestre", "Poire (~32 cm)", "Peau lisse et rebondie, lanugo presque totalement disparu.", "Contractions utérines de préparation plus intenses.", "Prélèvement vaginal de dépistage du Streptocoque B (35-37 SA)."),
      TimelineWeekInfo(36, "3ème Trimestre", "Melon (~33 cm)", "Le bébé prend toute la place utérine et se positionne définitivement tête en bas.", "Relâchement pelvien, pressions sur le col.", "Consultation du 9e mois, consultation avec l'anesthésiste."),
      TimelineWeekInfo(37, "3ème Trimestre", "Bette à carde (~34 cm)", "Bébé à terme ! Organes pleinement fonctionnels et autonomes.", "Contractions plus fréquentes, impatience grandissante.", "Consultation hebdomadaire de fin de terme."),
      TimelineWeekInfo(38, "3ème Trimestre", "Citrouille (~35 cm)", "Perte possible du bouchon muqueux, vernix résiduel sur la peau.", "Sensation de lourdeur pelvienne maximale.", "Monitoring fœtal de contrôle et examen du col."),
      TimelineWeekInfo(39, "3ème Trimestre", "Petite pastèque (~36 cm)", "Prêt pour la naissance, ongles longs, réflexes de préhension robustes.", "Contractions de travail imminentes.", "Vérification de la maturité du col et de la poche des eaux."),
      TimelineWeekInfo(40, "3ème Trimestre", "Pastèque (~37 cm+)", "Terme de la grossesse atteint ! Naissance imminente de votre bébé.", "Impatiente, contractions régulières et rapprochées.", "Arrivée de bébé 🎉, surveillance post-terme tous les 2 jours si dépassement.")
    )
  }

  val birthPlanQuestions = listOf(
    BirthPlanQuestion(
      id = "q1",
      theme = "Accueil",
      question = "Souhaitez-vous tamiser l'éclairage et écouter de la musique pendant le travail ?",
      options = listOf("Oui, ambiance tamisée et playlist personnelle", "Éclairage standard de la salle de naissance", "Pas de préférence particulière")
    ),
    BirthPlanQuestion(
      id = "q2",
      theme = "Travail",
      question = "Quelles méthodes non médicamenteuses de gestion de la douleur privilégiez-vous ?",
      options = listOf("Ballon de grossesse, marche et bains chauds", "Massage et pressions lombaires", "Liberté totale de mouvement selon les sensations")
    ),
    BirthPlanQuestion(
      id = "q3",
      theme = "Péridurale",
      question = "Quelle est votre position concernant la péridurale ?",
      options = listOf("Demande de péridurale dès que possible / souhaitée", "Péridurale ambulatoire (dosée pour garder la mobilité)", "Souhait d'essayer de m'en passer le plus longtemps possible")
    ),
    BirthPlanQuestion(
      id = "q4",
      theme = "Accouchement",
      question = "Comment envisagez-vous la phase d'expulsion ?",
      options = listOf("Poussée dirigée par l'équipe médicale", "Poussée spontanée selon l'envie et les contractions", "Utilisation d'un miroir pour voir la naissance")
    ),
    BirthPlanQuestion(
      id = "q5",
      theme = "Nouveau-né",
      question = "Quels sont vos souhaits pour les premiers moments de bébé ?",
      options = listOf("Peau à peau immédiat et prolongé (si bonne santé)", "Cordon ombilical coupé par le co-parent après arrêt des pulsations", "Premiers soins (bain, mesurée) réalisés après le premier têtée / peau à peau")
    )
  )

  val medicalMilestones = listOf(
    MedicalAppointmentMilestone(
      weeksTextFr = "Semaines 8 à 10",
      weeksTextEn = "Weeks 8 to 10",
      titleFr = "Consultation OBGYN",
      titleEn = "OBGYN Consultation",
      locationFr = "Cabinet du Gynécologue-Obstétricien",
      locationEn = "Obstetrician-Gynecologist Clinic",
      datePeriodFr = "Fin Déc. 2026 – Début Janv. 2027",
      datePeriodEn = "Late Dec. 2026 – Early Jan. 2027",
      descriptionFr = "Transition officielle du centre de fertilité vers le suivi de grossesse standard. Bilan de sortie RMA, transmission du dossier médical complet, et arrêt progressif sous surveillance du soutien hormonal (progestérone / œstrogènes).",
      descriptionEn = "Official transition from the fertility clinic to standard obstetric pregnancy care. RMA discharge summary, complete medical record transfer, and supervised progressive weaning of hormonal support (progesterone / estrogens).",
      keyExamsFr = listOf(
        "Échographie de transition",
        "Examen clinique complet",
        "Arrêt progressif des hormones de soutien"
      ),
      keyExamsEn = listOf(
        "Transition Ultrasound",
        "Complete Clinical Examination",
        "Progressive Weaning of Hormonal Support"
      )
    ),
    MedicalAppointmentMilestone(
      weeksTextFr = "Semaines 11 à 13",
      weeksTextEn = "Weeks 11 to 13",
      titleFr = "Échographie du 1er Trimestre & DPNI",
      titleEn = "1st Trimester Scan & NIPT",
      locationFr = "Centre d'Échographie Obstétricale",
      locationEn = "Obstetric Ultrasound Center",
      datePeriodFr = "Janvier 2027",
      datePeriodEn = "January 2027",
      descriptionFr = "Mesure de la clarté nucale, étude de la morphologie précoce, datation précise de la grossesse et prise de sang pour le dépistage combiné de la trisomie 21 ou DPNI (ADN fœtal circulant).",
      descriptionEn = "Nuchal translucidity measurement, early morphological evaluation, precise pregnancy dating, and blood draw for combined Down syndrome screening or NIPT (circulating fetal DNA).",
      keyExamsFr = listOf(
        "Échographie morphologique T1",
        "Mesure de la clarté nucale",
        "Prise de sang DPNI"
      ),
      keyExamsEn = listOf(
        "First Trimester Morphological Scan",
        "Nuchal Translucency Measurement",
        "NIPT Blood Test"
      )
    ),
    MedicalAppointmentMilestone(
      weeksTextFr = "Semaines 16 à 18",
      weeksTextEn = "Weeks 16 to 18",
      titleFr = "Consultation Prénatale du 4e Mois",
      titleEn = "4th Month Prenatal Visit",
      locationFr = "Cabinet ou Maternité",
      locationEn = "Clinic or Maternity Hospital",
      datePeriodFr = "Février 2027",
      datePeriodEn = "February 2027",
      descriptionFr = "Premier contrôle morphologique intermédiaire, écoute des battements cardiaques fœtaux, vérification de la tension artérielle, du poids et prescription du bilan sérologique du 2e trimestre.",
      descriptionEn = "First intermediate morphological check, fetal heart rate auscultation, blood pressure and weight check, and 2nd trimester blood panel prescription.",
      keyExamsFr = listOf(
        "Examen clinique mensuel",
        "Recherche d'albumine et sucre dans les urines",
        "Prescription bilan T2"
      ),
      keyExamsEn = listOf(
        "Monthly Clinical Examination",
        "Urinalysis for Protein & Sugar",
        "2nd Trimester Lab Work Prescription"
      )
    ),
    MedicalAppointmentMilestone(
      weeksTextFr = "Semaines 20 à 22",
      weeksTextEn = "Weeks 20 to 22",
      titleFr = "Échographie Morphologique T2",
      titleEn = "2nd Trimester Morphological Scan",
      locationFr = "Centre d'Échographie Spécialisé",
      locationEn = "Specialized Ultrasound Center",
      datePeriodFr = "Mars 2027",
      datePeriodEn = "March 2027",
      descriptionFr = "Examen détaillé organe par organe du fœtus (cerveau, cœur, reins, membres, colonne vertébrale), vérification de la quantité de liquide amniotique et de l'insertion placentaire.",
      descriptionEn = "Detailed organ-by-organ fetal examination (brain, heart, kidneys, limbs, spine), amniotic fluid assessment, and placental location check.",
      keyExamsFr = listOf(
        "Échographie morphologique de référence",
        "Étude Doppler des artères utérines",
        "Vérification biométrique complète"
      ),
      keyExamsEn = listOf(
        "Reference Morphological Ultrasound",
        "Uterine Artery Doppler Study",
        "Complete Biometric Evaluation"
      )
    ),
    MedicalAppointmentMilestone(
      weeksTextFr = "Semaines 24 à 28",
      weeksTextEn = "Weeks 24 to 28",
      titleFr = "Test HGPO & Bilan du 6e Mois",
      titleEn = "GTT (Glucose Tolerance Test) & 6M Check",
      locationFr = "Laboratoire d'Analyses Médicales",
      locationEn = "Medical Laboratory",
      datePeriodFr = "Avril – Mai 2027",
      datePeriodEn = "April – May 2027",
      descriptionFr = "Dépistage du diabète gestationnel par ingestion de 75g de glucose (HGPO) avec prises de sang à T0, T1h et T2h. Contrôle de la formule sanguine (NFS) et recherche d'agglutinines irrégulières (RAI) si rhésus négatif.",
      descriptionEn = "Gestational diabetes screening via 75g oral glucose tolerance test (OGTT) with blood draws at fasting, 1h, and 2h. Complete blood count and irregular antibody screen if Rh-negative.",
      keyExamsFr = listOf(
        "HGPO (Glycémie à jeun, 1h, 2h)",
        "NFS / Ferritine",
        "Recherche d'agglutinines irrégulières (RAI)"
      ),
      keyExamsEn = listOf(
        "OGTT (Fasting, 1h, 2h Blood Sugar)",
        "Complete Blood Count / Ferritin",
        "Antibody Screen (RAI)"
      )
    ),
    MedicalAppointmentMilestone(
      weeksTextFr = "Semaines 30 à 32",
      weeksTextEn = "Weeks 30 to 32",
      titleFr = "Échographie de Croissance T3",
      titleEn = "3rd Trimester Growth Scan",
      locationFr = "Centre d'Échographie Obstétricale",
      locationEn = "Obstetric Ultrasound Center",
      datePeriodFr = "Juin 2027",
      datePeriodEn = "June 2027",
      descriptionFr = "Évaluation de la croissance fœtale estimée, position du bébé (céphalique ou siège), quantité de liquide amniotique et bon fonctionnement placentaire.",
      descriptionEn = "Estimated fetal growth evaluation, presentation (cephalic or breech), amniotic fluid volume, and placental function assessment.",
      keyExamsFr = listOf(
        "Échographie de croissance fœtale",
        "Doppler ombilical et fœtal",
        "Vérification de la présentation"
      ),
      keyExamsEn = listOf(
        "Fetal Growth Ultrasound",
        "Umbilical & Fetal Doppler",
        "Presentation Check"
      )
    ),
    MedicalAppointmentMilestone(
      weeksTextFr = "Semaines 34 à 36",
      weeksTextEn = "Weeks 34 to 36",
      titleFr = "Consultation Anesthésie & SGB",
      titleEn = "Anesthesia Consultation & GBS Test",
      locationFr = "Maternité & Laboratoire",
      locationEn = "Maternity Hospital & Lab",
      datePeriodFr = "Juin – Juillet 2027",
      datePeriodEn = "June – July 2027",
      descriptionFr = "Consultation obligatoire avec le médecin anesthésiste pour valider la pose de la péridurale et l'anesthésie potentielle. Réalisation du prélèvement vaginal de dépistage du Streptocoque B (SGB).",
      descriptionEn = "Mandatory consultation with the anesthesiologist to review epidural analgesia and medical history. Group B Streptococcus (GBS) vaginal swab screening.",
      keyExamsFr = listOf(
        "Consultation d'anesthésie",
        "Prélèvement vaginal Streptocoque B",
        "Bilan pré-opératoire (coagulation)"
      ),
      keyExamsEn = listOf(
        "Anesthesia Consultation",
        "Group B Streptococcus Vaginal Swab",
        "Pre-operative Coagulation Panel"
      )
    ),
    MedicalAppointmentMilestone(
      weeksTextFr = "Semaines 37 à 40+",
      weeksTextEn = "Weeks 37 to 40+",
      titleFr = "Consultations de Terme & Naissance 🎉",
      titleEn = "Term Consultations & BABY ARRIVAL 🎉",
      locationFr = "Salle de Naissance de la Maternité",
      locationEn = "Maternity Hospital Delivery Suite",
      datePeriodFr = "Juillet 2027",
      datePeriodEn = "July 2027",
      descriptionFr = "Suivi rapproché hebdomadaire de fin de terme avec monitoring fœtal (NST) et examen du col utérin. Accueil imminent de votre bébé dans la sérénité !",
      descriptionEn = "Weekly close-out term monitoring with non-stress tests (NST) and cervical examination. Serene anticipation of your baby's magnificent arrival!",
      keyExamsFr = listOf(
        "Monitoring fœtal (NST)",
        "Examen clinique du col",
        "Accueil de bébé 👶"
      ),
      keyExamsEn = listOf(
        "Fetal Non-Stress Test (NST)",
        "Cervical Examination",
        "Welcome Baby 👶"
      )
    )
  )
}
