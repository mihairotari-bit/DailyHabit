package com.mihai.dailyhabit

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LegacyDeterministicDietInferenceEngineTest {

    private val preprocessor = DietTextPreprocessor()
    private val parser = DietParser(preprocessor)
    private val engine = LegacyDeterministicDietInferenceEngine(parser)

    @Test
    fun `engine wraps parsed plan with metadata`() = runBlocking {
        val input = "Lunedì\nColazione\n1 mela"
        val result = engine.parse(input)
        
        assertEquals(ParserEngine.LEGACY_DETERMINISTIC, result.parserEngine)
        assertEquals("ML Kit OCR + LegacyDeterministicParser", result.extractionMethod)
        assertFalse(result.isTestData)
        assertEquals(1, result.days.size)
        assertEquals("Lunedì", result.days[0].day)
    }
}
