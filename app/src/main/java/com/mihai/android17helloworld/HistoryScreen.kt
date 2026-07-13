package com.mihai.android17helloworld

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: DietPlanRepository
) : ViewModel() {
    private val _logs = MutableStateFlow<List<DailyLogEntity>>(emptyList())
    val logs = _logs.asStateFlow()

    fun loadLogs() {
        viewModelScope.launch {
            _logs.value = repository.getDailyLogs()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val logs by viewModel.logs.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadLogs()
    }

    Box(Modifier.fillMaxSize()) {
        if (logs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Nessun pasto salvato.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(logs) { log ->
                    var expanded by remember { mutableStateOf(false) }
                    ElevatedCard(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth().animateContentSize()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(log.date, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Text(if (log.trained) "Allenamento: Sì" else "Allenamento: No", style = MaterialTheme.typography.bodySmall)
                            
                            if (expanded) {
                                Spacer(Modifier.height(12.dp))
                                Text("Alimenti consumati:", style = MaterialTheme.typography.labelLarge)
                                Spacer(Modifier.height(4.dp))
                                val foods = try {
                                    val json = JSONObject(log.logJson)
                                    val list = mutableListOf<String>()
                                    json.keys().forEach { list.add(json.getString(it)) }
                                    list
                                } catch (e: Exception) { emptyList() }
                                
                                if (foods.isEmpty()) {
                                    Text("Nessuna informazione.", style = MaterialTheme.typography.bodySmall)
                                } else {
                                    foods.forEach { food ->
                                        Text("• $food", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
