package com.mihai.dailyhabit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

private val dietMimeTypes = arrayOf("text/plain", "application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietApp(viewModel: DietViewModel, trackingViewModel: DailyTrackingViewModel, historyViewModel: HistoryViewModel, themeMode: ThemeMode, onToggleTheme: (ThemeMode) -> Unit) {
    val state by viewModel.state.collectAsState()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? -> uri?.let(viewModel::import) }
    Scaffold(
        topBar = {}
    ) { padding ->
        when (val current = state) {
            DietUiState.Idle -> UploadPlanScreen(Modifier.fillMaxSize().padding(padding), { picker.launch(dietMimeTypes) })
            DietUiState.Extracting, DietUiState.Saving -> LoadingPanel(Modifier.fillMaxSize().padding(padding), if (current is DietUiState.Saving) "Salvataggio del piano…" else "Estrazione del piano…")
            is DietUiState.Review -> ReviewPlanScreen(
                plan = current.plan,
                onBack = viewModel::reset,
                onConfirm = viewModel::save,
                onFoodChange = { day, meal, id, replacement -> viewModel.updateFood(day, meal, id) { replacement } },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            is DietUiState.Saved -> MainScreen(trackingViewModel, historyViewModel, themeMode = themeMode, onReset = viewModel::clearStoredPlan, onToggleTheme = onToggleTheme)
            is DietUiState.Error -> ErrorPanel(Modifier.fillMaxSize().padding(padding), current.message, viewModel::dismissError)
        }
    }
}
enum class MainTab { OGGI, DIARIO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    trackingViewModel: DailyTrackingViewModel,
    historyViewModel: HistoryViewModel,
    themeMode: ThemeMode,
    onReset: () -> Unit,
    onToggleTheme: (ThemeMode) -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "oggi"
    var showResetDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {},
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentRoute == "oggi",
                    onClick = {
                        trackingViewModel.setTrained(null)
                        trackingViewModel.selectMeal(null)
                        navController.navigate("oggi") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = "Diario") },
                    label = { Text("Diario") },
                    selected = currentRoute == "diario",
                    onClick = {
                        navController.navigate("diario") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    )
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "oggi",
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            composable("oggi") {
                DailyTrackingScreen(trackingViewModel, themeMode = themeMode, onNewPlan = { showResetDialog = true }, onToggleTheme = onToggleTheme)
            }
            composable("diario") {
                HistoryScreen(historyViewModel)
            }
        }
    }
    if (showResetDialog) androidx.compose.material3.AlertDialog(
        onDismissRequest = { showResetDialog = false },
        title = { Text("Caricare nuovo piano?") },
        text = { Text("Se carichi un nuovo piano, tutte le informazioni correnti verranno cancellate. Vuoi procedere?") },
        dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Annulla") } },
        confirmButton = { Button(onClick = { showResetDialog = false; onReset() }) { Text("Procedi") } },
    )
}

@Composable private fun LoadingPanel(modifier: Modifier, text: String) = Box(modifier, contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) { CircularProgressIndicator(); Text(text) }
}

@Composable private fun ErrorPanel(modifier: Modifier, message: String, dismiss: () -> Unit) = Box(modifier, contentAlignment = Alignment.Center) {
    ElevatedCard(Modifier.padding(24.dp)) { Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Impossibile leggere il piano", style = MaterialTheme.typography.titleLarge); Text(message); Button(onClick = dismiss) { Text("Riprova") } } }
}
