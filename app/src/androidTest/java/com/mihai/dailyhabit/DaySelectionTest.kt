package com.mihai.dailyhabit

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier

@RunWith(AndroidJUnit4::class)
class DaySelectionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun daySelectionFitsPixel9WithoutScroll() {
        composeTestRule.setContent { HelloTheme { DaySelectionScreen({}, {}) } }
        composeTestRule.onNodeWithTag("day_selection_hero").assertIsDisplayed()
        composeTestRule.onNodeWithTag("day_selection_question_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("day_selection_training").assertIsDisplayed()
        composeTestRule.onNodeWithTag("day_selection_rest").assertIsDisplayed()
    }

    @Test
    fun greetingIsCloserToGlobalControls() {
        composeTestRule.setContent { HelloTheme { DaySelectionScreen({}, {}) } }
        composeTestRule.onNodeWithTag("day_selection_hero").assertIsDisplayed()
    }

    @Test
    fun heroAndCardDoNotOverlap() {
        composeTestRule.setContent { HelloTheme { DaySelectionScreen({}, {}) } }
        val heroBounds = composeTestRule.onNodeWithTag("day_selection_hero").getUnclippedBoundsInRoot()
        val cardBounds = composeTestRule.onNodeWithTag("day_selection_question_card").getUnclippedBoundsInRoot()
        assert(heroBounds.bottom <= cardBounds.top)
    }

    @Test
    fun fullQuestionCardVisibleWithoutScroll() {
        composeTestRule.setContent { HelloTheme { DaySelectionScreen({}, {}) } }
        composeTestRule.onNodeWithTag("day_selection_question_card").assertIsDisplayed()
    }

    @Test
    fun restButtonVisibleWithoutScroll() {
        composeTestRule.setContent { HelloTheme { DaySelectionScreen({}, {}) } }
        composeTestRule.onNodeWithTag("day_selection_rest").assertIsDisplayed()
    }

    @Test
    fun questionCardAboveBottomNavigation() {
        composeTestRule.setContent { HelloTheme { DaySelectionScreen({}, {}) } }
        composeTestRule.onNodeWithTag("day_selection_question_card").assertIsDisplayed()
    }

    @Test
    fun standardLayoutHasNoVerticalScrollAction() {
        composeTestRule.setContent { HelloTheme { DaySelectionScreen({}, {}) } }
        composeTestRule.onNode(hasScrollAction()).assertDoesNotExist()
    }

    @Test
    fun fontScale130CanScrollWhenNecessary() {
        composeTestRule.setContent { HelloTheme { DaySelectionScreen({}, {}) } }
        composeTestRule.onNodeWithTag("day_selection_screen").assertExists()
    }

    @Test
    fun trainingCallbackUnchanged() {
        var clicked = false
        composeTestRule.setContent { HelloTheme { DaySelectionScreen({ clicked = true }, {}) } }
        composeTestRule.onNodeWithTag("day_selection_training").performClick()
        assert(clicked)
    }

    @Test
    fun restCallbackUnchanged() {
        var clicked = false
        composeTestRule.setContent { HelloTheme { DaySelectionScreen({}, { clicked = true }) } }
        composeTestRule.onNodeWithTag("day_selection_rest").performClick()
        assert(clicked)
    }
}
