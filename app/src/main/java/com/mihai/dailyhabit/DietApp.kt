package com.mihai.dailyhabit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val dietMimeTypes = arrayOf("text/plain", "application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")

@Composable
fun DietAppDataFlow(viewModel: DietViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsState()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? -> uri?.let(viewModel::import) }
    
    Box(modifier = modifier) {
        when (val current = state) {
            DietUiState.Idle -> UploadPlanScreen(Modifier.fillMaxSize(), { picker.launch(dietMimeTypes) })
            DietUiState.Extracting, DietUiState.Saving -> LoadingPanel(Modifier.fillMaxSize(), if (current is DietUiState.Saving) "Salvataggio del piano…" else "Estrazione del piano…")
            is DietUiState.Review -> ReviewPlanScreen(
                plan = current.plan,
                onBack = viewModel::reset,
                onConfirm = viewModel::save,
                onFoodChange = { day, meal, id, replacement -> viewModel.updateFood(day, meal, id) { replacement } },
                modifier = Modifier.fillMaxSize()
            )
            is DietUiState.Saved -> {
                // Not handled here. Handled by AppScaffold navigating to Home.
            }
            is DietUiState.Error -> ErrorPanel(Modifier.fillMaxSize(), current.message, viewModel::dismissError)
        }
    }
}

@Composable 
fun LoadingPanel(modifier: Modifier, text: String) = Box(modifier, contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) { CircularProgressIndicator(); Text(text) }
}

@Composable 
fun ErrorPanel(modifier: Modifier, message: String, dismiss: () -> Unit) = Box(modifier, contentAlignment = Alignment.Center) {
    ElevatedCard(Modifier.padding(24.dp)) { Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Impossibile leggere il piano", style = MaterialTheme.typography.titleLarge); Text(message); Button(onClick = dismiss) { Text("Riprova") } } }
}
