package com.mihai.dailyhabit

data class DietInferenceInput(
    val rawText: String,
    val extractionMethod: String = "Unknown",
    val pages: List<ExtractedPage> = emptyList() // If available
)

interface DietInferenceEngine {
    /**
     * Parses the given text input and returns a structured JSON string.
     */
    suspend fun parse(input: DietInferenceInput): DietPlan
}
