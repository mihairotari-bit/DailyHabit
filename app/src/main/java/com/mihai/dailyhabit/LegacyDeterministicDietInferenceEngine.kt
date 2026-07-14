package com.mihai.dailyhabit

import javax.inject.Inject

class LegacyDeterministicDietInferenceEngine @Inject constructor(
    private val parser: DietParser
) : DietInferenceEngine {
    override suspend fun parse(input: DietInferenceInput): DietPlan {
        return parser.parse(input).copy(
            parserEngine = ParserEngine.LEGACY_DETERMINISTIC,
            extractionMethod = input.extractionMethod,
            isTestData = false
        )
    }
}
