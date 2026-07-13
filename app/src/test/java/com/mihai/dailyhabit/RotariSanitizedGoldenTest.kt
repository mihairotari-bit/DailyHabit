package com.mihai.dailyhabit

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RotariSanitizedGoldenTest {
    private val parser = DietParser()
    private val engine = LegacyDeterministicDietInferenceEngine(parser)

    @Test
    fun `parse real sanitized golden test data matches expected structure`() = runBlocking {
        // Read real fixture
        val fixtureText = File("src/test/resources/fixtures/rotari_real_sanitized_extracted_text.txt").readText()

        val plan = engine.parse(fixtureText)

        // General assertions
        assertEquals(ParserEngine.LEGACY_DETERMINISTIC, plan.parserEngine)
        assertEquals("ML Kit OCR + LegacyDeterministicParser", plan.extractionMethod)
        assertFalse(plan.isTestData)
        
        // Profiles verification
        val trainingDay = plan.days.find { it.profileType == DayProfileType.TRAINING }
        assertNotNull("Training profile should be present", trainingDay)
        
        val restDay = plan.days.find { it.profileType == DayProfileType.REST }
        assertNotNull("Rest profile should be present", restDay)

        // ====== TRAINING ASSERTIONS ======
        val tMeals = trainingDay!!.meals
        assertTrue("Should have PRE_WORKOUT", tMeals.any { it.type == MealType.PRE_WORKOUT })
        assertTrue("Should have POST_WORKOUT", tMeals.any { it.type == MealType.POST_WORKOUT })
        assertTrue("Should have BREAKFAST", tMeals.any { it.type == MealType.BREAKFAST })
        assertFalse("Should NOT have MORNING_SNACK in real pdf", tMeals.any { it.type == MealType.MORNING_SNACK })
        assertTrue("Should have LUNCH", tMeals.any { it.type == MealType.LUNCH })
        assertTrue("Should have SNACK", tMeals.any { it.type == MealType.SNACK })
        assertTrue("Should have DINNER", tMeals.any { it.type == MealType.DINNER })

        val preWorkout = tMeals.first { it.type == MealType.PRE_WORKOUT }
        assertTrue("Pre-workout should have 50 g maltodestrine enervit", preWorkout.groups.any { group -> 
            group.alternatives.any { it.name.contains("maltodestrine", true) && it.quantity.contains("50") } 
        })

        val postWorkout = tMeals.first { it.type == MealType.POST_WORKOUT }
        assertTrue("Post-workout should have 30 g whey idrolizzate", postWorkout.groups.any { group -> 
            group.alternatives.any { it.name.contains("whey idrolizzate", true) && it.quantity.contains("30") } 
        })
        
        val trainingBreakfast = tMeals.first { it.type == MealType.BREAKFAST }
        assertTrue("Colazione training: 220 g latte", trainingBreakfast.groups.any { group ->
            group.alternatives.any { it.name.contains("latte", true) && it.quantity.contains("220") }
        })

        val trainingSnack = tMeals.first { it.type == MealType.SNACK }
        assertTrue("Merenda training: 25 g frutta secca", trainingSnack.groups.any { g -> g.alternatives.any { it.name.contains("frutta secca", true) && it.quantity.contains("25") } })
        assertTrue("Merenda training: 100 g pane di grano duro", trainingSnack.groups.any { g -> g.alternatives.any { it.name.contains("pane di grano duro", true) && it.quantity.contains("100") } })
        assertTrue("Merenda training: 40 g marmellata", trainingSnack.groups.any { g -> g.alternatives.any { it.name.contains("marmellata", true) && it.quantity.contains("40") } })

        val trainingDinner = tMeals.first { it.type == MealType.DINNER }
        assertTrue("Dinner should reference lunch alternatives", trainingDinner.hasLunchAlternatives)

        assertTrue("Calorie training: 2643", fixtureText.contains("Calorie: 2643 kcal"))

        // ====== REST ASSERTIONS ======
        val rMeals = restDay!!.meals
        assertFalse("Should NOT have PRE_WORKOUT in rest day", rMeals.any { it.type == MealType.PRE_WORKOUT })
        assertFalse("Should NOT have POST_WORKOUT in rest day", rMeals.any { it.type == MealType.POST_WORKOUT })
        assertTrue("Should have BREAKFAST", rMeals.any { it.type == MealType.BREAKFAST })
        assertTrue("Should have LUNCH", rMeals.any { it.type == MealType.LUNCH })
        assertTrue("Should have SNACK", rMeals.any { it.type == MealType.SNACK })
        assertTrue("Should have DINNER", rMeals.any { it.type == MealType.DINNER })

        val restSnack = rMeals.first { it.type == MealType.SNACK }
        assertTrue("Merenda rest: 80 g pane di grano duro", restSnack.groups.any { g -> g.alternatives.any { it.name.contains("pane di grano duro", true) && it.quantity.contains("80") } })
        assertTrue("Merenda rest: 20 g marmellata", restSnack.groups.any { g -> g.alternatives.any { it.name.contains("marmellata", true) && it.quantity.contains("20") } })
        assertTrue("Merenda rest: 15 g frutta secca", restSnack.groups.any { g -> g.alternatives.any { it.name.contains("frutta secca", true) && it.quantity.contains("15") } })

        assertTrue("Calorie rest: 2007", fixtureText.contains("Calorie: 2007 kcal"))

        // Checks for invalid parsing
        val allAlternatives = plan.days.flatMap { it.meals }.flatMap { it.groups }.flatMap { it.alternatives }
        assertFalse("oppure non diventa FoodItem", allAlternatives.any { it.name.lowercase().startsWith("oppure") })
        assertFalse("le note non diventano alimenti", allAlternatives.any { it.name.lowercase().startsWith("note:") })
        assertFalse("nessun Lunedì inventato", plan.days.any { it.day.contains("Lunedì") })
    }
    
    private fun assertFalse(message: String, condition: Boolean) = org.junit.Assert.assertFalse(message, condition)
    private fun assertFalse(condition: Boolean) = org.junit.Assert.assertFalse(condition)
}
