package com.mihai.dailyhabit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DailyTrackingViewModel @Inject constructor(
    private val repository: DietPlanRepository,
    val profileResolver: ActiveDayProfileResolver
) : ViewModel() {
    private val _plan = MutableStateFlow<DietPlan?>(null)
    val plan = _plan.asStateFlow()

    private val _trained = MutableStateFlow<Boolean?>(null)
    val trained = _trained.asStateFlow()

    private val _selectedMeal = MutableStateFlow<MealType?>(null)
    val selectedMeal = _selectedMeal.asStateFlow()

    private val _consumedFoods = MutableStateFlow<Map<String, String>>(emptyMap())
    val consumedFoods = _consumedFoods.asStateFlow()

    fun loadLatestPlan() {
        viewModelScope.launch {
            _plan.value = repository.latestPlan()
        }
    }

    fun setTrained(trained: Boolean?) { _trained.value = trained }
    fun selectMeal(meal: MealType?) { _selectedMeal.value = meal }

    fun toggleFood(groupId: String, foodName: String) {
        val current = _consumedFoods.value.toMutableMap()
        if (current[groupId] == foodName) current.remove(groupId) else current[groupId] = foodName
        _consumedFoods.value = current
    }

    fun saveLog(onSuccess: () -> Unit) {
        val t = _trained.value ?: return
        viewModelScope.launch {
            val json = JSONObject(_consumedFoods.value).toString()
            repository.latestPlanId()?.let { planId ->
                repository.saveDailyLog(DailyLogEntity(
                    date = LocalDate.now().toString(),
                    planId = planId,
                    trained = t,
                    logJson = json
                ))
            }
            _selectedMeal.value = null
            _consumedFoods.value = emptyMap()
            onSuccess()
        }
    }
}

@Composable
fun SavedAreaScreen(
    trackingViewModel: DailyTrackingViewModel,
    historyViewModel: HistoryViewModel,
    onNewPlan: () -> Unit
) {
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: SavedDestination.DaySelection.route
    
    val plan by trackingViewModel.plan.collectAsState()
    LaunchedEffect(Unit) { trackingViewModel.loadLatestPlan() }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .testTag("saved_bottom_navigation")
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                val isHomeSelected = currentRoute == SavedDestination.DaySelection.route || 
                                     currentRoute == SavedDestination.MealSelection.route || 
                                     currentRoute == SavedDestination.MealDetail.route
                
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = isHomeSelected,
                    onClick = {
                        trackingViewModel.selectMeal(null)
                        trackingViewModel.setTrained(null)
                        nestedNavController.navigate(SavedDestination.DaySelection.route) {
                            popUpTo(nestedNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.testTag("bottom_home"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.History, contentDescription = "Diario") },
                    label = { Text("Diario") },
                    selected = currentRoute == SavedDestination.Journal.route,
                    onClick = {
                        nestedNavController.navigate(SavedDestination.Journal.route) {
                            popUpTo(nestedNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.testTag("bottom_journal"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { paddingValues ->
        if (plan == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                CircularProgressIndicator() 
            }
            return@Scaffold
        }
        
        NavHost(
            navController = nestedNavController,
            startDestination = SavedDestination.DaySelection.route,
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            composable(SavedDestination.DaySelection.route) {
                DaySelectionScreen(
                    onTrainingSelected = {
                        trackingViewModel.setTrained(true)
                        nestedNavController.navigate(SavedDestination.MealSelection.route)
                    },
                    onRestSelected = {
                        trackingViewModel.setTrained(false)
                        nestedNavController.navigate(SavedDestination.MealSelection.route)
                    }
                )
            }
            composable(SavedDestination.MealSelection.route) {
                MealSelectionScreen(
                    trackingViewModel = trackingViewModel,
                    plan = plan!!,
                    onMealSelected = {
                        trackingViewModel.selectMeal(it)
                        nestedNavController.navigate(SavedDestination.MealDetail.route)
                    }
                )
            }
            composable(SavedDestination.MealDetail.route) {
                MealDetailScreen(
                    trackingViewModel = trackingViewModel,
                    plan = plan!!,
                    onSaved = {
                        nestedNavController.popBackStack(SavedDestination.MealSelection.route, inclusive = false)
                    },
                    onBack = {
                        nestedNavController.popBackStack()
                    }
                )
            }
            composable(SavedDestination.Journal.route) {
                HistoryScreen(historyViewModel)
            }
        }
    }
}

@Composable
fun DaySelectionScreen(onTrainingSelected: () -> Unit, onRestSelected: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Buongiorno 👋", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Text("Seleziona il tuo profilo di oggi.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(Modifier.height(48.dp))
        
        ElevatedCard(
            onClick = onTrainingSelected,
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(16.dp))
                Text("Giorno con allenamento", style = MaterialTheme.typography.titleLarge)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        ElevatedCard(
            onClick = onRestSelected,
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Chair, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(16.dp))
                Text("Giorno senza allenamento", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
fun MealSelectionScreen(trackingViewModel: DailyTrackingViewModel, plan: DietPlan, onMealSelected: (MealType) -> Unit) {
    val trained by trackingViewModel.trained.collectAsState()
    val reqType = if (trained == true) DayProfileType.TRAINING else DayProfileType.REST
    val resolvedDay = trackingViewModel.profileResolver.resolve(plan, reqType)
    val meals = resolvedDay?.meals ?: emptyList()

    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(72.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Che pasto\nstai facendo?", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text("Seleziona il pasto che vuoi registrare.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // A simple placeholder for illustration
            Box(Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp), 
            modifier = Modifier.weight(1f).testTag("meal_selection_list")
        ) {
            items(meals) { meal ->
                val icon = when (meal.type) {
                    MealType.PRE_WORKOUT -> Icons.Rounded.Bolt
                    MealType.POST_WORKOUT -> Icons.Rounded.FitnessCenter
                    MealType.BREAKFAST -> Icons.Rounded.LocalCafe
                    MealType.MORNING_SNACK -> Icons.Rounded.Coffee
                    MealType.LUNCH -> Icons.Rounded.RestaurantMenu
                    MealType.SNACK -> Icons.Rounded.Eco
                    MealType.DINNER -> Icons.Rounded.Nightlight
                    else -> Icons.Rounded.Restaurant
                }
                
                Card(
                    onClick = { onMealSelected(meal.type) },
                    modifier = Modifier.fillMaxWidth().height(92.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(meal.type.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun MealDetailScreen(trackingViewModel: DailyTrackingViewModel, plan: DietPlan, onSaved: () -> Unit, onBack: () -> Unit) {
    val selectedMeal by trackingViewModel.selectedMeal.collectAsState()
    val consumedFoods by trackingViewModel.consumedFoods.collectAsState()
    val trained by trackingViewModel.trained.collectAsState()
    
    val reqType = if (trained == true) DayProfileType.TRAINING else DayProfileType.REST
    val options = trackingViewModel.profileResolver.resolve(plan, reqType)?.meals?.find { it.type == selectedMeal }?.options ?: emptyList()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(Modifier.height(72.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Indietro")
            }
            Text(selectedMeal?.label ?: "", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEachIndexed { optionIndex, option ->
                if (options.size > 1) {
                    item {
                        Text("Opzione ${optionIndex + 1}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                option.groups.forEach { group ->
                    item {
                        Text("Scegli un'alternativa:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    items(group.alternatives) { food ->
                        val isSelected = consumedFoods[group.id] == food.name
                        val containerColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer)
                        val elevation by animateDpAsState(if (isSelected) 8.dp else 2.dp)
                        
                        ElevatedCard(
                            onClick = { trackingViewModel.toggleFood(group.id, food.name) },
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation)
                        ) {
                            Row(
                                Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(food.name, style = MaterialTheme.typography.titleMedium)
                                    Text(food.quantity, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                AnimatedVisibility(
                                    visible = isSelected,
                                    enter = fadeIn() + scaleIn(),
                                    exit = fadeOut() + scaleOut()
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Selezionato", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { trackingViewModel.saveLog(onSuccess = onSaved) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Salva pasto")
        }
    }
}
