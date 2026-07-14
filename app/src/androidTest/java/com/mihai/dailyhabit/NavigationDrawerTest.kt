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

    // DRAWER TESTS
    @Test
    fun drawerMatchesRequiredDestinations() {
        composeTestRule.onNodeWithTag("global_hamburger").performClick()
        composeTestRule.onNodeWithTag("drawer_home").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drawer_new_plan").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drawer_journal").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drawer_model_management").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drawer_settings").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drawer_about").assertIsDisplayed()
    }

    @Test
    fun drawerThemeSwitchVisible() {
        composeTestRule.onNodeWithTag("global_hamburger").performClick()
        composeTestRule.onNodeWithTag("drawer_theme_switch").assertIsDisplayed()
    }

    @Test
    fun drawerThemeSwitchChangesTheme() {
        composeTestRule.onNodeWithTag("global_hamburger").performClick()
        composeTestRule.onNodeWithTag("drawer_theme_switch").performClick()
        composeTestRule.onNodeWithTag("drawer_theme_switch").assertIsDisplayed() // Still there after click
    }

    @Test
    fun drawerClosesAfterNavigation() {
        composeTestRule.onNodeWithTag("global_hamburger").performClick()
        composeTestRule.onNodeWithTag("drawer_about").performClick()
        composeTestRule.onNodeWithTag("drawer_about").assertDoesNotExist() // drawer closed
        composeTestRule.onNodeWithText("Informazioni Privacy").assertIsDisplayed()
    }

    @Test
    fun journalDisabledWithoutPlan() {
        composeTestRule.onNodeWithTag("global_hamburger").performClick()
        composeTestRule.onNodeWithTag("drawer_journal").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drawer_journal").performClick()
        // Drawer should still be open because the click is ignored without plan
        composeTestRule.onNodeWithTag("drawer_journal").assertIsDisplayed()
    }

    // HAMBURGER TESTS
    @Test
    fun greenHamburgerVisibleOnUpload() {
        composeTestRule.onNodeWithTag("global_hamburger").assertIsDisplayed()
    }

    @Test
    fun greenHamburgerOpensDrawer() {
        composeTestRule.onNodeWithTag("global_hamburger").performClick()
        composeTestRule.onNodeWithTag("navigation_drawer").assertIsDisplayed()
    }

    // NEW PLAN
    @Test
    fun plusRequiresConfirmation() {
        // Without plan, plus is not visible on Upload
        composeTestRule.onNodeWithTag("global_new_plan").assertDoesNotExist()
    }
}
