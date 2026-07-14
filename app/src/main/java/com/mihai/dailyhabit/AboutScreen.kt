package com.mihai.dailyhabit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Text("DailyHabit", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Versione: 1.0", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Informazioni Privacy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("I tuoi dati sono processati in locale e rimangono sul dispositivo.", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Stato Parser", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("Attualmente è in uso il Parser Deterministico legacy. LiteRT LLM non è ancora attivo.", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Licenze", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("Il modello LLM è fornito da Google MediaPipe / LiteRT Community ed è soggetto alla licenza Apache 2.0.", style = MaterialTheme.typography.bodyMedium)
    }
}
