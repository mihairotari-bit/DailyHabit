package com.mihai.dailyhabit

interface DietInferenceEngine {
    /**
     * Parses the given text input and returns a structured JSON string.
     */
    suspend fun parse(input: String): DietPlan
}
