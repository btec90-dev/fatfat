package com.example.data

/**
 * Structured notes for each trimester of pregnancy.
 */
data class TrimesterNotes(
  val firstTrimester: String,
  val secondTrimester: String,
  val thirdTrimester: String
)

/**
 * Structured response model representing the official pregnancy safety evaluation
 * conforming strictly to the requested backend JSON schema:
 * {
 *   "searchItem": "Name of drug or topic",
 *   "pregnancySafetyTier": "Safe" | "Caution (Consult Doctor)" | "Unsafe/Avoid",
 *   "fdaCategory": "String or N/A",
 *   "clinicalSummary": "Short, accessible explanation of how it affects pregnancy or the fetus.",
 *   "trimesterSpecificNotes": {
 *     "firstTrimester": "Notes or specific risks",
 *     "secondTrimester": "Notes or specific risks",
 *     "thirdTrimester": "Notes or specific risks"
 *   },
 *   "fetalRisks": ["List", "of", "known", "risks", "or", "None"],
 *   "pregnancySafeAlternatives": ["Alternative 1", "Alternative 2"]
 * }
 */
data class MedicationSafetyResponse(
  val searchItem: String,
  val pregnancySafetyTier: String, // "Safe", "Caution (Consult Doctor)", or "Unsafe/Avoid"
  val fdaCategory: String,
  val clinicalSummary: String,
  val trimesterSpecificNotes: TrimesterNotes,
  val fetalRisks: List<String>,
  val pregnancySafeAlternatives: List<String>,
  // Localized fields for French UI rendering
  val searchItemFr: String? = null,
  val clinicalSummaryFr: String? = null,
  val trimesterSpecificNotesFr: TrimesterNotes? = null,
  val fetalRisksFr: List<String>? = null,
  val pregnancySafeAlternativesFr: List<String>? = null,
  val categoryGroup: String = "Medication",
  val commonBrands: List<String> = emptyList()
) {
  /**
   * Produces ONLY valid JSON matching the exact required schema,
   * with zero markdown formatting or conversational filler.
   * Pure Kotlin implementation guarantees strict validity across both JVM and Android runtimes.
   */
  fun toJsonString(indent: Int = 2): String {
    val pad = if (indent > 0) " ".repeat(indent) else ""
    val pad2 = if (indent > 0) " ".repeat(indent * 2) else ""
    val nl = if (indent > 0) "\n" else ""

    fun escape(s: String): String {
      return s.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\b", "\\b")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
    }

    val risksFormatted = if (fetalRisks.isEmpty()) "" else {
      fetalRisks.joinToString(separator = ",$nl$pad2") { "\"${escape(it)}\"" }
    }
    val alternativesFormatted = if (pregnancySafeAlternatives.isEmpty()) "" else {
      pregnancySafeAlternatives.joinToString(separator = ",$nl$pad2") { "\"${escape(it)}\"" }
    }

    return buildString {
      append("{$nl")
      append("$pad\"searchItem\": \"${escape(searchItem)}\",$nl")
      append("$pad\"pregnancySafetyTier\": \"${escape(pregnancySafetyTier)}\",$nl")
      append("$pad\"fdaCategory\": \"${escape(fdaCategory)}\",$nl")
      append("$pad\"clinicalSummary\": \"${escape(clinicalSummary)}\",$nl")
      append("$pad\"trimesterSpecificNotes\": {$nl")
      append("$pad2\"firstTrimester\": \"${escape(trimesterSpecificNotes.firstTrimester)}\",$nl")
      append("$pad2\"secondTrimester\": \"${escape(trimesterSpecificNotes.secondTrimester)}\",$nl")
      append("$pad2\"thirdTrimester\": \"${escape(trimesterSpecificNotes.thirdTrimester)}\"$nl")
      append("$pad},$nl")
      append("$pad\"fetalRisks\": [")
      if (risksFormatted.isNotEmpty()) {
        append("$nl$pad2$risksFormatted$nl$pad")
      }
      append("],$nl")
      append("$pad\"pregnancySafeAlternatives\": [")
      if (alternativesFormatted.isNotEmpty()) {
        append("$nl$pad2$alternativesFormatted$nl$pad")
      }
      append("]$nl")
      append("}")
    }
  }

  /**
   * Localized version for French if needed.
   */
  fun localized(isEnglish: Boolean): MedicationSafetyResponse {
    if (isEnglish) return this
    return this.copy(
      searchItem = searchItemFr ?: searchItem,
      clinicalSummary = clinicalSummaryFr ?: clinicalSummary,
      trimesterSpecificNotes = trimesterSpecificNotesFr ?: trimesterSpecificNotes,
      fetalRisks = fetalRisksFr ?: fetalRisks,
      pregnancySafeAlternatives = pregnancySafeAlternativesFr ?: pregnancySafeAlternatives
    )
  }
}
