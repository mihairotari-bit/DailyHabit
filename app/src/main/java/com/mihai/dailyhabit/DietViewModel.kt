package com.mihai.dailyhabit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DietUiState {
    data object Idle : DietUiState
    data object Extracting : DietUiState
    data class Review(val plan: DietPlan) : DietUiState
    data object Saving : DietUiState
    data class Saved(val planId: Long) : DietUiState
    data class Error(val message: String) : DietUiState
}

@HiltViewModel
class DietViewModel @Inject constructor(
    private val documents: DocumentReader,
    private val textRecognition: TextRecognitionEngine,
    private val parser: DietInferenceEngine,
    private val repository: DietPlanRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<DietUiState>(DietUiState.Idle)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.latestPlanId()?.let {
                _state.value = DietUiState.Saved(it)
            }
        }
    }

    fun import(uri: Uri) {
        documents.persistReadPermission(uri)
        _state.value = DietUiState.Extracting
        viewModelScope.launch {
            runCatching {
                val content = documents.read(uri)
                val inferenceInput = when (content) {
                    is DocumentContent.Text -> DietInferenceInput(content.value, extractionMethod = "TXT Document")
                    is DocumentContent.TextWithPages -> {
                        val fullText = content.pages.joinToString("\n") { it.text }
                        DietInferenceInput(fullText, extractionMethod = "PDF_NATIVE_TEXT", pages = content.pages)
                    }
                    is DocumentContent.Pdf -> {
                        val text = textRecognition.recognize(content.pages)
                        DietInferenceInput(text, extractionMethod = "PdfRenderer + ML Kit OCR")
                    }
                }
                
                val parsed = parser.parse(inferenceInput)
                // Parser Output logging removed for privacy.
                parsed.also { require(it.days.isNotEmpty()) { "Nessun pasto riconosciuto. Prova un TXT con intestazioni di giorno e pasto." } }
            }.onSuccess { _state.value = DietUiState.Review(it) }
                .onFailure { 
                    android.util.Log.e("MLKIT_OCR_OUTPUT", "Error: ${it.message}")
                    _state.value = DietUiState.Error(it.message ?: "Impossibile estrarre il piano alimentare.") 
                }
        }
    }

    fun updateFood(dayIndex: Int, mealIndex: Int, foodId: String, transform: (FoodItem) -> FoodItem) = updatePlan { plan ->
        plan.copy(days = plan.days.mapIndexed { d, day -> if (d != dayIndex) day else day.copy(meals = day.meals.mapIndexed { m, meal ->
            if (m != mealIndex) meal else meal.copy(options = meal.options.map { option ->
                option.copy(groups = option.groups.map { group ->
                    group.copy(alternatives = group.alternatives.map { if (it.clientId == foodId) transform(it) else it })
                })
            })
        }) })
    }

    fun removeFood(dayIndex: Int, mealIndex: Int, foodId: String) {
        updatePlan { plan -> plan.copy(days = plan.days.mapIndexed { d, day -> if (d != dayIndex) day else day.copy(meals = day.meals.mapIndexed { m, meal ->
            if (m != mealIndex) meal else meal.copy(options = meal.options.map { option ->
                option.copy(groups = option.groups.map { group ->
                    group.copy(alternatives = group.alternatives.filterNot { it.clientId == foodId })
                }.filter { it.alternatives.isNotEmpty() })
            }.filter { it.groups.isNotEmpty() })
        }) }) }
    }

    fun save() {
        val plan = (_state.value as? DietUiState.Review)?.plan ?: return
        
        // Valutazione regole:
        if (plan.isTestData || plan.parserEngine == ParserEngine.FAKE_TEST) {
            _state.value = DietUiState.Error("I dati di test non possono essere salvati in produzione.")
            return
        }
        if (plan.days.isEmpty()) {
            _state.value = DietUiState.Error("Il piano deve avere almeno un profilo/giorno.")
            return
        }
        val hasMeals = plan.days.any { it.meals.isNotEmpty() }
        if (!hasMeals) {
            _state.value = DietUiState.Error("Il piano deve avere almeno un pasto.")
            return
        }
        val hasFoods = plan.days.any { day -> day.meals.any { meal -> meal.options.isNotEmpty() || meal.hasLunchAlternatives } }
        if (!hasFoods) {
            _state.value = DietUiState.Error("Nessun alimento o riferimento valido trovato.")
            return
        }
        
        _state.value = DietUiState.Saving
        viewModelScope.launch { runCatching { repository.save(plan) }
            .onSuccess { _state.value = DietUiState.Saved(it) }
            .onFailure { _state.value = DietUiState.Error(it.message ?: "Salvataggio non riuscito.") } }
    }

    fun reset() { _state.value = DietUiState.Idle }
    fun clearStoredPlan() {
        viewModelScope.launch {
            repository.clearAll()
            _state.value = DietUiState.Idle
        }
    }
    fun dismissError() { _state.value = DietUiState.Idle }

    private fun updatePlan(block: (DietPlan) -> DietPlan) {
        val review = _state.value as? DietUiState.Review ?: return
        _state.value = review.copy(plan = block(review.plan))
    }
}
