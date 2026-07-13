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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val dietViewModel: DietViewModel by viewModels()
    private val trackingViewModel: DailyTrackingViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The Android 12+ system splash already displays the launcher icon. Do not
        // layer a second Compose logo on top of it.
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }
            HelloTheme(darkTheme = isDarkTheme) {
                DietApp(dietViewModel, trackingViewModel, historyViewModel, isDarkTheme) { isDarkTheme = !isDarkTheme }
            }
        }
    }
}
