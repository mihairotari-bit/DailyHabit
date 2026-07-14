package com.mihai.dailyhabit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Tema dell'applicazione", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = themeMode == ThemeMode.SYSTEM,
                onClick = { onThemeModeChanged(ThemeMode.SYSTEM) }
            )
            Text("Predefinito di sistema", modifier = Modifier.padding(start = 8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = themeMode == ThemeMode.LIGHT,
                onClick = { onThemeModeChanged(ThemeMode.LIGHT) }
            )
            Text("Chiaro", modifier = Modifier.padding(start = 8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = themeMode == ThemeMode.DARK,
                onClick = { onThemeModeChanged(ThemeMode.DARK) }
            )
            Text("Scuro", modifier = Modifier.padding(start = 8.dp))
        }
    }
}
