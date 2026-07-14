package com.mihai.dailyhabit

import javax.inject.Inject

class LiteRtDietInferenceEngine @Inject constructor() : DietInferenceEngine {
    override suspend fun parse(input: DietInferenceInput): DietPlan {
        throw NotImplementedError("LiteRT-LM inference (Gemma 4) is not yet implemented. Please refer to Milestone 2.")
    }
}
