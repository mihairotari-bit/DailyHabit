package com.mihai.dailyhabit

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LegacyDeterministicDietInferenceEngineTest {

    private val preprocessor = DietTextPreprocessor(DietLineClassifier())
    private val parser = DietParser(preprocessor, DietLineClassifier(), DietStructureTokenizer())
    private val engine = LegacyDeterministicDietInferenceEngine(parser)

    @Test
    fun `engine wraps parsed plan with metadata`() = runBlocking {
        val input = "Lunedì\nColazione\n150 g mela"
        val result = engine.parse(DietInferenceInput(input))
        
        assertEquals(ParserEngine.LEGACY_DETERMINISTIC, result.parserEngine)
        assertEquals("Unknown", result.extractionMethod)
        assertFalse(result.isTestData)
        assertEquals(1, result.days.size)
        assertEquals("Lunedì", result.days[0].day)
    }
}
