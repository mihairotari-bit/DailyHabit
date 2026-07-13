package com.mihai.dailyhabit

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RotariSanitizedGoldenTest {
    private val parser = DietParser()
    private val engine = LegacyDeterministicDietInferenceEngine(parser)

    @Test
    fun `parse sanitized golden test data`() = runBlocking {
        val input = """
            Giorno con allenamento
            Pre workout
            10 g EAA
            Colazione
            200 ml latte
            30 g whey
            Pranzo
            100 g riso
            150 g pollo
            Merenda
            1 pz mela
            Cena
            Vedi le alternative del pranzo
            
            Giorno senza allenamento
            Colazione
            200 ml latte
            30 g whey
            Pranzo
            80 g riso
            150 g pollo
            Merenda
            1 pz pera
            Cena
            Vedi le alternative del pranzo
        """.trimIndent()

        val plan = engine.parse(input)

        assertEquals(ParserEngine.LEGACY_DETERMINISTIC, plan.parserEngine)
        assertEquals(DayProfileType.TRAINING, plan.days[0].profileType)
        assertEquals(DayProfileType.REST, plan.days[1].profileType)
        
        val trainingMeals = plan.days[0].meals
        assertTrue(trainingMeals.any { it.type == MealType.PRE_WORKOUT })
        assertTrue(trainingMeals.any { it.type == MealType.BREAKFAST })
        assertTrue(trainingMeals.any { it.type == MealType.LUNCH })
        assertTrue(trainingMeals.any { it.type == MealType.SNACK })
        assertTrue(trainingMeals.any { it.type == MealType.DINNER })

        val restMeals = plan.days[1].meals
        assertTrue(restMeals.any { it.type == MealType.BREAKFAST })
        assertTrue(restMeals.any { it.type == MealType.LUNCH })
        assertTrue(restMeals.any { it.type == MealType.SNACK })
        assertTrue(restMeals.any { it.type == MealType.DINNER })

        val trainingDinner = trainingMeals.find { it.type == MealType.DINNER }
        assertNotNull(trainingDinner)
        assertTrue(trainingDinner!!.hasLunchAlternatives)
    }
}
