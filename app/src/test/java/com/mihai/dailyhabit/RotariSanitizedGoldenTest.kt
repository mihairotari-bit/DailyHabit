package com.mihai.dailyhabit

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RotariSanitizedGoldenTest {
    private val parser = DietParser()
    private val engine = LegacyDeterministicDietInferenceEngine(parser)

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parse sanitized golden test data matches expected json`() = runBlocking {
        // Read input and expected output
        val fixtureText = File("src/test/resources/fixtures/rotari_sanitized_extracted_text.txt").readText()
        val expectedJsonText = File("src/test/resources/fixtures/rotari_sanitized_expected.json").readText()
        val expectedPlan = json.decodeFromString<DietPlan>(expectedJsonText)

        val plan = engine.parse(fixtureText)

        // General assertions
        assertEquals(expectedPlan.parserEngine, plan.parserEngine)
        assertEquals(expectedPlan.type, plan.type)
        assertEquals(expectedPlan.days.size, plan.days.size)

        // Profiles verification
        val trainingDay = plan.days.find { it.profileType == DayProfileType.TRAINING }
        assertNotNull("Training profile should be present", trainingDay)
        
        val restDay = plan.days.find { it.profileType == DayProfileType.REST }
        assertNotNull("Rest profile should be present", restDay)

        // Meals verification for TRAINING
        val tMeals = trainingDay!!.meals
        assertTrue("Should have PRE_WORKOUT", tMeals.any { it.type == MealType.PRE_WORKOUT })
        assertTrue("Should have POST_WORKOUT", tMeals.any { it.type == MealType.POST_WORKOUT })
        assertTrue("Should have BREAKFAST", tMeals.any { it.type == MealType.BREAKFAST })
        assertTrue("Should have MORNING_SNACK", tMeals.any { it.type == MealType.MORNING_SNACK })
        assertTrue("Should have LUNCH", tMeals.any { it.type == MealType.LUNCH })
        assertTrue("Should have SNACK", tMeals.any { it.type == MealType.SNACK })
        assertTrue("Should have DINNER", tMeals.any { it.type == MealType.DINNER })

        // Meals verification for REST
        val rMeals = restDay!!.meals
        assertTrue("Should have BREAKFAST", rMeals.any { it.type == MealType.BREAKFAST })
        assertTrue("Should have LUNCH", rMeals.any { it.type == MealType.LUNCH })
        assertTrue("Should have SNACK", rMeals.any { it.type == MealType.SNACK })
        assertTrue("Should have DINNER", rMeals.any { it.type == MealType.DINNER })

        // Detailed Checks (quantities, names)
        val preWorkout = tMeals.first { it.type == MealType.PRE_WORKOUT }
        assertTrue("Pre-workout should have EAA", preWorkout.groups.any { group -> group.alternatives.any { it.name.contains("EAA", true) } })
        
        val restLunch = rMeals.first { it.type == MealType.LUNCH }
        assertTrue("Rest Lunch should have Riso basmati 80 g", restLunch.groups.any { group -> 
            group.alternatives.any { it.name.contains("Riso") && it.quantity.contains("80") } 
        })

        val trainingDinner = tMeals.first { it.type == MealType.DINNER }
        assertTrue("Dinner should reference lunch alternatives", trainingDinner.hasLunchAlternatives)
        
        // Assert absence of fake data
        assertFalse(plan.days.any { it.day.contains("Lunedì") })
    }
    
    private fun assertFalse(condition: Boolean) = org.junit.Assert.assertFalse(condition)
}
