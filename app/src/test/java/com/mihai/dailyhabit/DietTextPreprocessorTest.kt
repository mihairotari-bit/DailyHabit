package com.mihai.dailyhabit

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DietTextPreprocessorTest {
    private val preprocessor = DietTextPreprocessor()

    @Test
    fun `strips emails and urls`() {
        val text = """
            COLAZIONE
            info@example.com
            www.nutrizionista.it
            http://example.com/dieta
            Opzione 1
        """.trimIndent()
        
        val result = preprocessor.preprocess(text)
        assertTrue(result.contains("COLAZIONE"))
        assertTrue(result.contains("Opzione 1"))
        assertFalse(result.contains("info@example.com"))
        assertFalse(result.contains("www.nutrizionista.it"))
        assertFalse(result.contains("http://example.com/dieta"))
    }

    @Test
    fun `strips names and headers`() {
        val text = """
            ELISABETTA MORONI
            NUTRIZIONISTA BIOLOGA
            PIANO NUTRIZIONALE
            GIORNO CON ALLENAMENTO
        """.trimIndent()
        
        val result = preprocessor.preprocess(text)
        assertFalse(result.lowercase().contains("elisabetta moroni"))
        assertFalse(result.lowercase().contains("piano nutrizionale"))
        assertTrue(result.contains("GIORNO CON ALLENAMENTO"))
    }

    @Test
    fun `strips narrative paragraphs`() {
        val text = """
            GIORNO SENZA ALLENAMENTO
            COLAZIONE
            Questo è un paragrafo discorsivo molto lungo che la nutrizionista ha inserito per spiegare i metodi di cottura e le regole generali della dieta senza usare i numeri o i formati standard previsti.
            100g pollo
        """.trimIndent()
        
        val result = preprocessor.preprocess(text)
        assertTrue(result.contains("COLAZIONE"))
        assertTrue(result.contains("100g pollo"))
        assertFalse(result.contains("Questo è un paragrafo"))
    }
}
