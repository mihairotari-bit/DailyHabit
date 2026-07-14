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
        
        assertEquals(2, lunch.options.size)
        assertTrue(plan.parseReport?.restLunchHeaderDetected == true)
        
        // Option 1 has 1 group with 3 alternatives + 1 group with 1 alternative
        assertEquals(2, lunch.options[0].groups.size)
        assertEquals(3, lunch.options[0].groups[0].alternatives.size)
        
        // Check food content
        val carbAlternatives = lunch.options[0].groups[0].alternatives.map { it.name }
        assertTrue(carbAlternatives.contains("pane integrale"))
        assertTrue(carbAlternatives.contains("pasta di semola integrale"))
        assertTrue(carbAlternatives.contains("cous cous"))
        
        assertEquals("100 g", lunch.options[0].groups[1].alternatives[0].quantity)
        assertEquals("petto di pollo", lunch.options[0].groups[1].alternatives[0].name)
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
