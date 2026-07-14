package com.mihai.dailyhabit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val dietViewModel: DietViewModel by viewModels()
    private val trackingViewModel: DailyTrackingViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePreferences = ThemePreferences(this)
        setContent {
            val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            
            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

            HelloTheme(darkTheme = isDarkTheme) {
                DietApp(dietViewModel, trackingViewModel, historyViewModel, themeMode) { newMode ->
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        themePreferences.setThemeMode(newMode)
                    }
                }
            }
        }
    }
}
