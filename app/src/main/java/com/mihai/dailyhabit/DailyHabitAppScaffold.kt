package com.mihai.dailyhabit

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@Composable
fun DrawerNavigationItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = contentColor
        )
    }
}

@Composable
fun AnimatedThemeSwitch(
    themeMode: ThemeMode,
    onToggleTheme: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = themeMode == ThemeMode.DARK
    val thumbOffset by animateDpAsState(if (isDark) 48.dp else 0.dp, label = "theme_switch_offset")

    Box(
        modifier = modifier
            .width(96.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(role = Role.Switch) {
                onToggleTheme(if (isDark) ThemeMode.LIGHT else ThemeMode.DARK)
            }
            .semantics { contentDescription = "Selettore tema" }
            .testTag("drawer_theme_switch"),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.LightMode, contentDescription = null, tint = if (!isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(
            modifier = Modifier
                .padding(4.dp)
                .size(40.dp)
                .offset(x = thumbOffset)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun BotanicalDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, size.height)
            quadraticBezierTo(size.width * 0.25f, size.height * 0.5f, size.width * 0.5f, size.height * 0.8f)
            quadraticBezierTo(size.width * 0.75f, size.height * 1.1f, size.width, size.height * 0.6f)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(path, color = Color(0xFFE6F4EA).copy(alpha = 0.6f))
    }
}

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

    val config = LocalConfiguration.current
    val drawerWidth = (config.screenWidthDp * 0.85f).coerceAtMost(360f).dp

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.3f),
        modifier = Modifier.testTag("navigation_drawer"),
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidth),
                drawerShape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 32.dp, bottomEnd = 32.dp),
                drawerContainerColor = MaterialTheme.colorScheme.background,
            ) {
                Box(Modifier.fillMaxSize()) {
                    BotanicalDecoration(Modifier.fillMaxWidth().height(120.dp).align(Alignment.BottomCenter))
                    
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 24.dp, bottom = 24.dp)
                    ) {
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DrawerNavigationItem(
                                label = "Home",
                                icon = Icons.Rounded.Home,
                                selected = currentRoute == AppDestination.SavedArea.route,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    if (hasPlan) {
                                        if (currentRoute != AppDestination.SavedArea.route) {
                                            navController.navigate(AppDestination.SavedArea.route) { launchSingleTop = true; restoreState = true }
                                        }
                                    } else {
                                        if (currentRoute != AppDestination.DataFlow.route) {
                                            navController.navigate(AppDestination.DataFlow.route) { launchSingleTop = true; restoreState = true }
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("drawer_home")
                            )
                            DrawerNavigationItem(
                                label = "Carica nuovo piano",
                                icon = Icons.Rounded.CloudUpload,
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    if (hasPlan) {
                                        showNewPlanDialog = true
                                    } else {
                                        navController.navigate(AppDestination.DataFlow.route) { launchSingleTop = true; restoreState = true }
                                    }
                                },
                                modifier = Modifier.testTag("drawer_new_plan")
                            )
                            DrawerNavigationItem(
                                label = if (hasPlan) "Diario" else "Diario (Nessun piano)",
                                icon = Icons.Rounded.History,
                                selected = false, // Will be handled better when SavedArea exposes current sub-route
                                onClick = {
                                    if (hasPlan) {
                                        scope.launch { drawerState.close() }
                                        // TODO: Pass intent to nested NavHost via a shared flow or just rely on the bottom bar navigation
                                    }
                                },
                                modifier = Modifier.testTag("drawer_journal").let { if (!hasPlan) it.background(Color.Transparent) else it }
                            )
                            DrawerNavigationItem(
                                label = "Gestione Modello LLM",
                                icon = Icons.Rounded.Memory,
                                selected = currentRoute == AppDestination.ModelManagement.route,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    if (currentRoute != AppDestination.ModelManagement.route) {
                                        navController.navigate(AppDestination.ModelManagement.route) { launchSingleTop = true; restoreState = true }
                                    }
                                },
                                modifier = Modifier.testTag("drawer_model_management")
                            )
                            
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            
                            DrawerNavigationItem(
                                label = "Impostazioni",
                                icon = Icons.Rounded.Settings,
                                selected = currentRoute == AppDestination.Settings.route,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    if (currentRoute != AppDestination.Settings.route) {
                                        navController.navigate(AppDestination.Settings.route) { launchSingleTop = true; restoreState = true }
                                    }
                                },
                                modifier = Modifier.testTag("drawer_settings")
                            )
                            DrawerNavigationItem(
                                label = "Informazioni",
                                icon = Icons.Rounded.Info,
                                selected = currentRoute == AppDestination.About.route,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    if (currentRoute != AppDestination.About.route) {
                                        navController.navigate(AppDestination.About.route) { launchSingleTop = true; restoreState = true }
                                    }
                                },
                                modifier = Modifier.testTag("drawer_about")
                            )
                        }
                        
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            AnimatedThemeSwitch(themeMode, onToggleTheme)
                        }
                    }
                }
            }
        }
    ) {
        Box(Modifier.fillMaxSize()) {
            
            LaunchedEffect(hasPlan) {
                if (hasPlan && currentRoute == AppDestination.DataFlow.route) {
                    navController.navigate(AppDestination.SavedArea.route) {
                        popUpTo(AppDestination.DataFlow.route) { inclusive = true }
                        launchSingleTop = true
                    }
                } else if (!hasPlan && currentRoute == AppDestination.SavedArea.route) {
                    navController.navigate(AppDestination.DataFlow.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            
            NavHost(
                navController = navController,
                startDestination = if (hasPlan) AppDestination.SavedArea.route else AppDestination.DataFlow.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(AppDestination.DataFlow.route) {
                    DietAppDataFlow(dietViewModel, modifier = Modifier.fillMaxSize())
                }
                composable(AppDestination.SavedArea.route) {
                    SavedAreaScreen(trackingViewModel, historyViewModel, { showNewPlanDialog = true })
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
            
            // Global Hamburger
            FloatingActionButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp, start = 16.dp)
                    .size(56.dp)
                    .testTag("global_hamburger"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Rounded.Menu, contentDescription = "Apri menu")
            }
            
            // Global Plus Button (only in SavedArea)
            if (currentRoute == AppDestination.SavedArea.route && hasPlan) {
                FloatingActionButton(
                    onClick = { showNewPlanDialog = true },
                    modifier = Modifier
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp, end = 16.dp)
                        .align(Alignment.TopEnd)
                        .size(56.dp)
                        .testTag("global_new_plan"),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Nuovo piano")
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
