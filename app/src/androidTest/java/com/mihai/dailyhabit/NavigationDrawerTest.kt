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
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.unit.dp

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

    // LAYOUT / OVERLAP
    @Test
    fun uploadTitleBelowHamburger() {
        composeTestRule.onNodeWithTag("global_hamburger").assertIsDisplayed()
        val hamburgerBounds = composeTestRule.onNodeWithTag("global_hamburger").getUnclippedBoundsInRoot()
        val titleBounds = composeTestRule.onNodeWithText("Carica il tuo", substring = true).getUnclippedBoundsInRoot()
        assert(titleBounds.top >= hamburgerBounds.bottom + 8.dp) 
    }

    @Test
    fun hamburgerHasTopClearanceFromStatusBar() {
        val hamburgerBounds = composeTestRule.onNodeWithTag("global_hamburger").getUnclippedBoundsInRoot()
        // Status bar is usually around 24dp. Our box adds 10dp padding. 
        // We just ensure it's not 0 and has enough clearance.
        assert(hamburgerBounds.top >= 24.dp)
    }

    @Test
    fun globalPlusDoesNotExist() {
        composeTestRule.onNodeWithTag("global_new_plan").assertDoesNotExist()
    }

    @Test
    fun drawerNewPlanStillExists() {
        composeTestRule.onNodeWithTag("global_hamburger").performClick()
        composeTestRule.onNodeWithTag("drawer_new_plan").assertIsDisplayed()
    }

    @Test
    fun privacyBannerStillVisible() {
        composeTestRule.onNodeWithText("La tua privacy", substring = true).assertIsDisplayed()
    }
    // THEME
    @Test
    fun drawerContainsSingleThemeSwitch() {
        composeTestRule.onNodeWithTag("global_hamburger").performClick()
        composeTestRule.onNodeWithTag("drawer_theme_switch").assertIsDisplayed()
    }

    @Test
    fun settingsContainsNoThemeControls() {
        composeTestRule.onNodeWithTag("global_hamburger").performClick()
        composeTestRule.onNodeWithTag("drawer_settings").performClick()
        composeTestRule.onNodeWithText("Tema dell'applicazione").assertDoesNotExist()
        composeTestRule.onNodeWithText("Preferenze").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nessuna impostazione aggiuntiva disponibile.").assertIsDisplayed()
    }

    // REGRESSION
    @Test
    fun drawerOpensModelManagement() {
        composeTestRule.onNodeWithTag("global_hamburger").performClick()
        composeTestRule.onNodeWithTag("drawer_model_management").performClick()
        composeTestRule.onNodeWithText("Dimensione:").assertIsDisplayed()
    }
}
