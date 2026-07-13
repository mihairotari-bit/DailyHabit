package com.mihai.android17helloworld

import org.junit.Test
import org.junit.Assert.*

class DietParserTest {
    @Test
    fun testParse() {
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
        val parser = DietParser()
        val plan = parser.parse(text)
        assertTrue(plan.days.isNotEmpty())
    }
}
