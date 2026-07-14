package com.mihai.dailyhabit

import com.mihai.dailyhabit.DietParser
import com.mihai.dailyhabit.DietTextPreprocessor
import org.junit.Test
import org.junit.Assert.*

class DietParserTest {
    @Test
    fun testParse() {
        val preprocessor = DietTextPreprocessor(DietLineClassifier())
        val parser = DietParser(preprocessor, DietLineClassifier(), DietStructureTokenizer())
        val text = """
GIORNO CON ALLENAMENTO
PREWOUT
50g maltodestrine enervit
POSTALLENAMENTO
30g whey idrolizzate
COLAZIONE
Opzione 1
40g biscotti frollini buoni così galbusera
oppure 50g fette biscottate integrali con 40g hero light marmellata
oppure 50g fiocchi di avena
oppure 50g fiocchi di mais cornflakes
+
220g latte di vacca parzialmente scremato pastorizzato
+
100g frutta fresca
        """.trimIndent()
        val plan = parser.parse(DietInferenceInput(text))
        assertTrue(plan.days.isNotEmpty())
    }
}
