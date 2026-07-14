package com.mihai.dailyhabit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RestLunchReviewVisibilityTest {

    @Test
    fun `test UI state exposes REST lunch with options`() {
        val parser = DietParser(
            DietTextPreprocessor(DietLineClassifier()), 
            DietLineClassifier(), 
            DietStructureTokenizer()
        )
        
        val text = java.io.File("src/test/resources/fixtures/rest_lunch_real_native_text.txt").readText()
        val plan = parser.parse(DietInferenceInput(text))
        
        // This is the model exposed to the UI (DietUiState.Review(plan).plan)
        val restDay = plan.days.first { it.profileType == DayProfileType.REST }
        
        // Ensure that the UI will iterate over this lunch
        val lunchMeal = restDay.meals.firstOrNull { it.type == MealType.LUNCH }
        
        assertTrue("Lunch must be present in REST profile", lunchMeal != null)
        assertTrue("Lunch options must not be empty, otherwise UI hides it", lunchMeal!!.options.isNotEmpty())
        
        // Verify that options are populated enough that ReviewPlanScreen would render OptionCards
        val optionCount = lunchMeal.options.size
        assertTrue("ReviewPlanScreen needs at least one option to render", optionCount >= 2)
        
        // Check exact quantities so UI renders the exact food required
        val firstOptionFoods = lunchMeal.options[0].groups.flatMap { it.alternatives }
        val hasPaneIntegrale = firstOptionFoods.any { it.name.contains("pane integrale") && it.quantity.contains("120 g") }
        assertTrue("UI should receive 'pane integrale' 120 g", hasPaneIntegrale)
    }
}
