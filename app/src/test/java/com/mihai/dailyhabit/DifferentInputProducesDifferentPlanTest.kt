package com.mihai.dailyhabit

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DifferentInputProducesDifferentPlanTest {
    private val preprocessor = DietTextPreprocessor()
    private val parser = DietParser(preprocessor)
    private val engine = LegacyDeterministicDietInferenceEngine(parser)

    @Test
    fun `different input produces different plan`() = runBlocking {
        val input1 = """
            Lunedì
            Colazione
            200 ml latte
            30 g fette biscottate
        """.trimIndent()

        val input2 = """
            Giorno con allenamento
            Pre workout
            50 g maltodestrine
            Post workout
            30 g whey
        """.trimIndent()

        val plan1 = engine.parse(input1)
        val plan2 = engine.parse(input2)

        assertNotEquals(plan1.days, plan2.days)
        assertNotEquals(plan1.type, plan2.type)
    }
}
