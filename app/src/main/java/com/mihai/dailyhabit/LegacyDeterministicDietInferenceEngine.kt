package com.mihai.dailyhabit

import javax.inject.Inject

class LegacyDeterministicDietInferenceEngine @Inject constructor(
    private val parser: DietParser
) : DietInferenceEngine {
    override suspend fun parse(input: String): DietPlan {
        val plan = parser.parse(input)
        return plan.copy(
            parserEngine = ParserEngine.LEGACY_DETERMINISTIC,
            extractionMethod = "PdfBox + Heuristics",
            isTestData = false
        )
    }
}
