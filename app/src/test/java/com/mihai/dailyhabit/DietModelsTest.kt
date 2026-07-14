package com.mihai.dailyhabit

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class DietModelsTest {

    @Test
    fun `test legacy json with groups is parsed correctly to options`() {
        val jsonString = """
        {
            "type": "LUNCH",
            "groups": [
                {
                    "id": "group1",
                    "alternatives": [
                        { "clientId": "food1", "name": "Pasta", "quantity": "100g" }
                    ]
                }
            ],
            "hasLunchAlternatives": false
        }
        """.trimIndent()

        val json = Json { ignoreUnknownKeys = true }
        val meal = json.decodeFromString<Meal>(jsonString)

        assertEquals(MealType.LUNCH, meal.type)
        assertEquals(false, meal.hasLunchAlternatives)
        
        // Ensure options list contains exactly one option, which contains the legacy group
        assertEquals(1, meal.options.size)
        assertEquals(1, meal.options[0].groups.size)
        assertEquals("group1", meal.options[0].groups[0].id)
        assertEquals("Pasta", meal.options[0].groups[0].alternatives[0].name)
    }

    @Test
    fun `test modern json with options is parsed correctly`() {
        val jsonString = """
        {
            "type": "DINNER",
            "options": [
                {
                    "id": "opt1",
                    "groups": [
                        {
                            "id": "group2",
                            "alternatives": [
                                { "clientId": "food2", "name": "Pesce", "quantity": "200g" }
                            ]
                        }
                    ]
                }
            ],
            "hasLunchAlternatives": true
        }
        """.trimIndent()

        val json = Json { ignoreUnknownKeys = true }
        val meal = json.decodeFromString<Meal>(jsonString)

        assertEquals(MealType.DINNER, meal.type)
        assertEquals(true, meal.hasLunchAlternatives)
        
        assertEquals(1, meal.options.size)
        assertEquals("opt1", meal.options[0].id)
        assertEquals(1, meal.options[0].groups.size)
        assertEquals("group2", meal.options[0].groups[0].id)
        assertEquals("Pesce", meal.options[0].groups[0].alternatives[0].name)
    }
}
