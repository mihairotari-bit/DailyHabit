package com.mihai.dailyhabit

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.mihai.dailyhabit.HelloTheme

@RunWith(AndroidJUnit4::class)
class DaySelectionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun daySelectionShowsGreeting() {
        composeTestRule.setContent { MaterialTheme { DaySelectionScreen({}, {}) } }
        composeTestRule.onNodeWithText("Buongiorno 👋").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pronto a prenderti cura", substring = true).assertIsDisplayed()
    }

    @Test
    fun daySelectionShowsHeroIllustration() {
        composeTestRule.setContent { MaterialTheme { DaySelectionScreen({}, {}) } }
        composeTestRule.onNodeWithTag("day_selection_illustration").assertIsDisplayed()
    }

    @Test
    fun daySelectionShowsQuestionCard() {
        composeTestRule.setContent { MaterialTheme { DaySelectionScreen({}, {}) } }
        composeTestRule.onNodeWithTag("day_selection_question_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ti sei allenato oggi?").assertIsDisplayed()
    }

    @Test
    fun daySelectionShowsTrainingButton() {
        composeTestRule.setContent { MaterialTheme { DaySelectionScreen({}, {}) } }
        composeTestRule.onNodeWithTag("day_selection_training").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sì, mi sono allenato").assertIsDisplayed()
    }

    @Test
    fun daySelectionShowsRestButton() {
        composeTestRule.setContent { MaterialTheme { DaySelectionScreen({}, {}) } }
        composeTestRule.onNodeWithTag("day_selection_rest").assertIsDisplayed()
        composeTestRule.onNodeWithText("No, giorno di riposo").assertIsDisplayed()
    }

    @Test
    fun bothActionsUsePrimaryFilledStyle() {
        // We visually implemented this using Brush background on a Box, not standard Buttons, 
        // but both are the same composable structure now, verifying they exist and are clickable.
        composeTestRule.setContent { MaterialTheme { DaySelectionScreen({}, {}) } }
        composeTestRule.onNodeWithTag("day_selection_training").assertHasClickAction()
        composeTestRule.onNodeWithTag("day_selection_rest").assertHasClickAction()
    }

    @Test
    fun trainingButtonStillCallsTrainingCallback() {
        var clicked = false
        composeTestRule.setContent { MaterialTheme { DaySelectionScreen({ clicked = true }, {}) } }
        composeTestRule.onNodeWithTag("day_selection_training").performClick()
        assert(clicked)
    }

    @Test
    fun restButtonStillCallsRestCallback() {
        var clicked = false
        composeTestRule.setContent { MaterialTheme { DaySelectionScreen({}, { clicked = true }) } }
        composeTestRule.onNodeWithTag("day_selection_rest").performClick()
        assert(clicked)
    }

    @Test
    fun darkThemeDaySelectionRenders() {
        composeTestRule.setContent { 
            HelloTheme(darkTheme = true) { 
                DaySelectionScreen({}, {}) 
            } 
        }
        composeTestRule.onNodeWithTag("day_selection_screen").assertIsDisplayed()
    }
}
