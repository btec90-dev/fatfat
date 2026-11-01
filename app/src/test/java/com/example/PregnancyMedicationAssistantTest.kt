package com.example

import com.example.data.PregnancyMedicationAssistant
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PregnancyMedicationAssistantTest {

  private val validTiers = setOf("Safe", "Caution (Consult Doctor)", "Unsafe/Avoid")

  @Test
  fun testParacetamolOutputIsStrictValidJson() {
    val jsonString = PregnancyMedicationAssistant.processSearchQueryAsJson("Paracetamol")
    assertNotNull(jsonString)

    // Verify it parses as strict JSON without throwing
    val json = JSONObject(jsonString)
    assertTrue(json.has("searchItem"))
    assertTrue(json.has("pregnancySafetyTier"))
    assertTrue(json.has("fdaCategory"))
    assertTrue(json.has("clinicalSummary"))
    assertTrue(json.has("trimesterSpecificNotes"))
    assertTrue(json.has("fetalRisks"))
    assertTrue(json.has("pregnancySafeAlternatives"))

    // Verify tier
    val tier = json.getString("pregnancySafetyTier")
    assertTrue("Tier must be in strict set, got: $tier", validTiers.contains(tier))
    assertEquals("Safe", tier)

    // Verify trimesters
    val trimesters = json.getJSONObject("trimesterSpecificNotes")
    assertTrue(trimesters.has("firstTrimester"))
    assertTrue(trimesters.has("secondTrimester"))
    assertTrue(trimesters.has("thirdTrimester"))

    // Verify arrays
    val risks = json.getJSONArray("fetalRisks")
    assertNotNull(risks)
    val alternatives = json.getJSONArray("pregnancySafeAlternatives")
    assertNotNull(alternatives)
  }

  @Test
  fun testIbuprofenSafetyIsUnsafeAvoid() {
    val jsonString = PregnancyMedicationAssistant.processSearchQueryAsJson("Ibuprofen")
    val json = JSONObject(jsonString)
    val tier = json.getString("pregnancySafetyTier")
    assertEquals("Unsafe/Avoid", tier)

    val clinicalSummary = json.getString("clinicalSummary")
    assertTrue(clinicalSummary.contains("ductus arteriosus", ignoreCase = true) || clinicalSummary.contains("oligohydramnios", ignoreCase = true))

    val alternatives = json.getJSONArray("pregnancySafeAlternatives")
    assertTrue(alternatives.length() > 0)
  }

  @Test
  fun testIsotretinoinIsUnsafeAvoidWithCategoryX() {
    val jsonString = PregnancyMedicationAssistant.processSearchQueryAsJson("Roaccutane")
    val json = JSONObject(jsonString)
    assertEquals("Unsafe/Avoid", json.getString("pregnancySafetyTier"))
    assertTrue(json.getString("fdaCategory").contains("Category X", ignoreCase = true))
  }

  @Test
  fun testSpasfonIsSafe() {
    val jsonString = PregnancyMedicationAssistant.processSearchQueryAsJson("Spasfon")
    val json = JSONObject(jsonString)
    assertEquals("Safe", json.getString("pregnancySafetyTier"))
  }

  @Test
  fun testUnknownMedicationFallsBackToCautionWithConsultDoctor() {
    val jsonString = PregnancyMedicationAssistant.processSearchQueryAsJson("UnknownExperimentalCompound99")
    val json = JSONObject(jsonString)
    assertEquals("Caution (Consult Doctor)", json.getString("pregnancySafetyTier"))
    assertTrue(json.getString("clinicalSummary").contains("obstetrician", ignoreCase = true) || json.getString("clinicalSummary").contains("doctor", ignoreCase = true))
  }
}
