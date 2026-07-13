package com.mihai.dailyhabit

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyDeterministicDietInferenceEngineTest {
    private val parser = DietParser()
    private val engine = LegacyDeterministicDietInferenceEngine(parser)

    @Test
    fun `engine wraps parsed plan with metadata`() = runBlocking {
        val input = "Lunedì\nColazione\n1 mela"
        val plan = engine.parse(input)
        
        assertEquals(ParserEngine.LEGACY_DETERMINISTIC, plan.parserEngine)
        assertEquals("PdfBox + Heuristics", plan.extractionMethod)
        assertEquals(false, plan.isTestData)
        assertEquals(1, plan.days.size)
        assertEquals("Lunedì", plan.days[0].day)
    }
}
