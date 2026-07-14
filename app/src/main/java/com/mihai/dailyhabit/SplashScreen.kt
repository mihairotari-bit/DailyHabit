package com.mihai.dailyhabit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay

@Composable
fun DailyHabitRoot(diet: DietViewModel, tracking: DailyTrackingViewModel, history: HistoryViewModel) = HelloTheme {
    var splash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(1_000); splash = false }
    val alpha by animateFloatAsState(if (splash) 1f else 0f, label = "splashAlpha")
    if (splash) Box(Modifier.fillMaxSize().background(androidx.compose.material3.MaterialTheme.colorScheme.surface), Alignment.Center) {
        Image(painterResource(R.drawable.logo), contentDescription = "DailyHabit", modifier = Modifier.alpha(alpha))
    } else DailyHabitAppScaffold(diet, tracking, history, themeMode = ThemeMode.SYSTEM, onToggleTheme = {})
}
