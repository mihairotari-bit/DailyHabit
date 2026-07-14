package com.mihai.dailyhabit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyHabitAppScaffold(
    dietViewModel: DietViewModel,
    trackingViewModel: DailyTrackingViewModel,
    historyViewModel: HistoryViewModel,
    themeMode: ThemeMode,
    onToggleTheme: (ThemeMode) -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: AppDestination.DataFlow.route
    
    val dietState by dietViewModel.state.collectAsState()
    val hasPlan = dietState is DietUiState.Saved

    var showNewPlanDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = currentRoute == AppDestination.Home.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (currentRoute != AppDestination.Home.route) {
                            if (hasPlan) navController.navigate(AppDestination.Home.route) { launchSingleTop = true; restoreState = true }
                            else navController.navigate(AppDestination.DataFlow.route) { launchSingleTop = true; restoreState = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Carica nuovo piano") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (hasPlan) {
                            showNewPlanDialog = true
                        } else {
                            navController.navigate(AppDestination.DataFlow.route) { launchSingleTop = true; restoreState = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(if (hasPlan) "Diario" else "Diario (Nessun piano)") },
                    selected = currentRoute == AppDestination.Journal.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (hasPlan && currentRoute != AppDestination.Journal.route) {
                            navController.navigate(AppDestination.Journal.route) { launchSingleTop = true; restoreState = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Gestione Modello LLM") },
                    selected = currentRoute == AppDestination.ModelManagement.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (currentRoute != AppDestination.ModelManagement.route) {
                            navController.navigate(AppDestination.ModelManagement.route) { launchSingleTop = true; restoreState = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Impostazioni") },
                    selected = currentRoute == AppDestination.Settings.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (currentRoute != AppDestination.Settings.route) {
                            navController.navigate(AppDestination.Settings.route) { launchSingleTop = true; restoreState = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Informazioni") },
                    selected = currentRoute == AppDestination.About.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (currentRoute != AppDestination.About.route) {
                            navController.navigate(AppDestination.About.route) { launchSingleTop = true; restoreState = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                // Determine title and actions based on current route and diet state
                val title = when (currentRoute) {
                    AppDestination.ModelManagement.route -> "Gestione modello LLM"
                    AppDestination.Settings.route -> "Impostazioni"
                    AppDestination.About.route -> "Informazioni"
                    AppDestination.Journal.route -> "Diario"
                    AppDestination.Home.route -> ""
                    AppDestination.DataFlow.route -> if (dietState is DietUiState.Review) "Revisione piano" else ""
                    else -> ""
                }
                
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Rounded.Menu, contentDescription = "Apri menu")
                        }
                    },
                    actions = {
                        if (currentRoute == AppDestination.Home.route && hasPlan) {
                            IconButton(onClick = { showNewPlanDialog = true }) {
                                Icon(Icons.Rounded.Add, contentDescription = "Nuovo piano")
                            }
                        }
                        if (currentRoute == AppDestination.DataFlow.route && dietState is DietUiState.Review) {
                            IconButton(onClick = { dietViewModel.reset() }) {
                                Icon(Icons.Rounded.ArrowBack, contentDescription = "Indietro")
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            // Top-level NavHost.
            // Note: DataFlow handles Upload, Review, etc.
            // When plan is saved, we navigate to Home.
            
            LaunchedEffect(hasPlan) {
                if (hasPlan && currentRoute == AppDestination.DataFlow.route) {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.DataFlow.route) { inclusive = true }
                        launchSingleTop = true
                    }
                } else if (!hasPlan && (currentRoute == AppDestination.Home.route || currentRoute == AppDestination.Journal.route)) {
                    navController.navigate(AppDestination.DataFlow.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            
            NavHost(
                navController = navController,
                startDestination = if (hasPlan) AppDestination.Home.route else AppDestination.DataFlow.route,
                modifier = Modifier.padding(paddingValues).fillMaxSize()
            ) {
                composable(AppDestination.DataFlow.route) {
                    DietAppDataFlow(dietViewModel, modifier = Modifier.fillMaxSize())
                }
                composable(AppDestination.Home.route) {
                    DailyTrackingScreen(trackingViewModel, themeMode, { showNewPlanDialog = true }, onToggleTheme)
                }
                composable(AppDestination.Journal.route) {
                    HistoryScreen(historyViewModel)
                }
                composable(AppDestination.ModelManagement.route) {
                    ModelDownloadScreenWrapper(modifier = Modifier.fillMaxSize())
                }
                composable(AppDestination.Settings.route) {
                    SettingsScreen(themeMode, onToggleTheme)
                }
                composable(AppDestination.About.route) {
                    AboutScreen()
                }
            }
        }
    }
    
    if (showNewPlanDialog) {
        AlertDialog(
            onDismissRequest = { showNewPlanDialog = false },
            title = { Text("Caricare nuovo piano?") },
            text = { Text("Se carichi un nuovo piano, il piano corrente verrà sostituito.") },
            dismissButton = { TextButton(onClick = { showNewPlanDialog = false }) { Text("Annulla") } },
            confirmButton = {
                Button(onClick = {
                    showNewPlanDialog = false
                    dietViewModel.clearStoredPlan()
                }) { Text("Procedi") }
            }
        )
    }
}
