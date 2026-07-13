package com.mihai.dailyhabit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DailyTrackingViewModel @Inject constructor(
    private val repository: DietPlanRepository
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

    fun saveLog() {
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTrackingScreen(viewModel: DailyTrackingViewModel, onNewPlan: () -> Unit, onToggleTheme: () -> Unit) {
    val plan by viewModel.plan.collectAsState()
    val trained by viewModel.trained.collectAsState()
    val selectedMeal by viewModel.selectedMeal.collectAsState()
    val consumedFoods by viewModel.consumedFoods.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadLatestPlan() }

    if (plan == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { 
            CircularProgressIndicator() 
        }
        return
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (trained == null) {
                DailyWelcomeScreen(onWorkout = { viewModel.setTrained(true) }, onRest = { viewModel.setTrained(false) }, onNewPlan = onNewPlan, onToggleTheme = onToggleTheme)
            } else if (selectedMeal == null) {
                Text("Che pasto stai facendo?", style = MaterialTheme.typography.headlineMedium)
                val dayLabel = if (trained == true) "Giorno con allenamento" else "Giorno senza allenamento"
                val meals = plan?.days?.find { it.day.equals(dayLabel, true) }?.meals ?: emptyList()
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(meals) { meal ->
                        ElevatedCard(onClick = { viewModel.selectMeal(meal.type) }, Modifier.fillMaxWidth()) {
                            Text(meal.type.label, Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
                TextButton(onClick = { viewModel.setTrained(null) }, Modifier.fillMaxWidth()) {
                    Text("Indietro")
                }
            } else {
                Text(selectedMeal!!.label, style = MaterialTheme.typography.headlineMedium)
                val dayLabel = if (trained == true) "Giorno con allenamento" else "Giorno senza allenamento"
                val groups = plan?.days?.find { it.day.equals(dayLabel, true) }?.meals?.find { it.type == selectedMeal }?.groups ?: emptyList()
                
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(groups) { group ->
                        Text("Scegli un'opzione:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        group.alternatives.forEach { food ->
                            val isSelected = consumedFoods[group.id] == food.name
                            val containerColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                            val elevation by animateDpAsState(if (isSelected) 8.dp else 2.dp)
                            ElevatedCard(
                                onClick = { viewModel.toggleFood(group.id, food.name) },
                                Modifier.fillMaxWidth(),
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
                
                Button(onClick = { viewModel.saveLog() }, Modifier.fillMaxWidth()) {
                    Text("Salva pasto")
                }
                TextButton(onClick = { viewModel.selectMeal(null) }, Modifier.fillMaxWidth()) {
                    Text("Indietro")
                }
            }
        }
    }
}
