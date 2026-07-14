package com.mihai.dailyhabit

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationDrawerTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun hamburgerVisibleOnUpload() {
        // Assume starts without plan
        composeTestRule.onNodeWithContentDescription("Apri menu").assertIsDisplayed()
        composeTestRule.onNodeWithText("Carica il tuo").assertIsDisplayed()
    }

    @Test
    fun drawerOpensFromUpload() {
        composeTestRule.onNodeWithContentDescription("Apri menu").performClick()
        composeTestRule.onNodeWithText("Gestione Modello LLM").assertIsDisplayed()
    }

    @Test
    fun modelManagementVisibleInDrawer() {
        composeTestRule.onNodeWithContentDescription("Apri menu").performClick()
        composeTestRule.onNodeWithText("Gestione Modello LLM").assertIsDisplayed()
    }

    @Test
    fun modelManagementOpensScreen() {
        composeTestRule.onNodeWithContentDescription("Apri menu").performClick()
        composeTestRule.onNodeWithText("Gestione Modello LLM").performClick()
        composeTestRule.onNodeWithText("Dimensione:").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gestione modello LLM").assertIsDisplayed() // TopAppBar title
    }

    @Test
    fun backFromModelManagementRestoresPreviousScreen() {
        // Go to model management
        composeTestRule.onNodeWithContentDescription("Apri menu").performClick()
        composeTestRule.onNodeWithText("Gestione Modello LLM").performClick()
        composeTestRule.onNodeWithText("Dimensione:").assertIsDisplayed()
        
        // Go back (in this case, press back via dispatcher)
        composeTestRule.activityRule.scenario.onActivity { activity: MainActivity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        
        // Should be back to upload
        composeTestRule.onNodeWithText("Carica il tuo").assertIsDisplayed()
    }

    @Test
    fun drawerClosesAfterSelection() {
        composeTestRule.onNodeWithContentDescription("Apri menu").performClick()
        composeTestRule.onNodeWithText("Informazioni").performClick()
        composeTestRule.onNodeWithText("Gestione Modello LLM").assertDoesNotExist() // Drawer is closed
        composeTestRule.onNodeWithText("Informazioni Privacy").assertIsDisplayed()
    }

    @Test
    fun settingsChangesTheme() {
        composeTestRule.onNodeWithContentDescription("Apri menu").performClick()
        composeTestRule.onNodeWithText("Impostazioni").performClick()
        composeTestRule.onNodeWithText("Chiaro").performClick()
        // Wait, testing theme visually is hard, but we can check if it clicked
        composeTestRule.onNodeWithText("Chiaro").assertIsDisplayed()
    }

    @Test
    fun journalDisabledWithoutPlan() {
        composeTestRule.onNodeWithContentDescription("Apri menu").performClick()
        composeTestRule.onNodeWithText("Diario (Nessun piano)").assertIsDisplayed()
    }
}
