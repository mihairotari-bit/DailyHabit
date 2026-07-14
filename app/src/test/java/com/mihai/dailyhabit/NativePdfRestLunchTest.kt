package com.mihai.dailyhabit

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NativePdfRestLunchTest {
    
    private val tokenizer = DietStructureTokenizer()
    private val preprocessor = DietTextPreprocessor(DietLineClassifier())
    private val parser = DietParser(preprocessor, DietLineClassifier(), tokenizer)

    private fun loadFixture(name: String): String {
        return File("src/test/resources/fixtures/$name").readText()
    }

    @Test
    fun `test NativePdfRestLunchTest`() = runBlocking {
        val text = loadFixture("rest_lunch_real_native_text.txt")
        val plan = parser.parse(DietInferenceInput(text))
        
        val restDay = plan.days.first { it.day.contains("senza allenamento", ignoreCase = true) }
        val lunch = restDay.meals.first { it.type == MealType.LUNCH }
        
        assertTrue(plan.parseReport?.restProfileDetected == true)
        assertTrue(plan.parseReport?.restLunchHeaderDetected == true)
        assertTrue((plan.parseReport?.restLunchOptionCount ?: 0) >= 2)
        assertTrue((plan.parseReport?.restLunchGroupCount ?: 0) > 0)
        assertTrue((plan.parseReport?.restLunchFoodCount ?: 0) > 0)
        
        // Flatten all alternatives in lunch to search for the required foods
        val allFoods = lunch.options.flatMap { it.groups }.flatMap { it.alternatives }
        
        fun assertHasFood(name: String, quantity: String) {
            val found = allFoods.any { it.name.contains(name, ignoreCase = true) && it.quantity.contains(quantity, ignoreCase = true) }
            assertTrue("Missing food: $quantity $name in ${allFoods.map { it.quantity + " " + it.name }}", found)
        }
        
        assertHasFood("pane integrale", "120 g")
        assertHasFood("pasta di semola integrale", "90 g")
        assertHasFood("cous cous", "90 g")
        assertHasFood("pasta di legumi", "70 g")
        assertHasFood("tonno al naturale", "120 g")
        assertHasFood("verdure o ortaggi", "200 g")
        assertHasFood("olio extravergine", "10 g")
    }

    @Test
    fun `test MergedPranzoHeaderTest`() = runBlocking {
        val text = loadFixture("rest_lunch_merged_headers.txt")
        val plan = parser.parse(DietInferenceInput(text))
        
        val restDay = plan.days.first { it.day.contains("senza allenamento", ignoreCase = true) }
        val lunch = restDay.meals.first { it.type == MealType.LUNCH }
        assertEquals(2, lunch.options.size)
    }

    @Test
    fun `test SplitPranzoHeaderTest`() = runBlocking {
        val text = loadFixture("rest_lunch_split_header.txt")
        val plan = parser.parse(DietInferenceInput(text))
        
        val restDay = plan.days.first { it.day.contains("senza allenamento", ignoreCase = true) }
        val lunch = restDay.meals.first { it.type == MealType.LUNCH }
        assertEquals(2, lunch.options.size)
    }

    @Test
    fun `test MissingNewlineRestLunchTest`() = runBlocking {
        val text = loadFixture("rest_lunch_missing_newlines.txt")
        val plan = parser.parse(DietInferenceInput(text))
        
        val restDay = plan.days.first { it.day.contains("senza allenamento", ignoreCase = true) }
        val lunch = restDay.meals.first { it.type == MealType.LUNCH }
        assertEquals(2, lunch.options.size)
        
        val opt2 = lunch.options[1]
        assertEquals(4, opt2.groups.size) // pasta di legumi + tonno + verdure + olio
    }
}
