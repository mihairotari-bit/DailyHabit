package com.mihai.dailyhabit

import org.junit.Assert.assertEquals
import org.junit.Test

class DietLineClassifierTest {
    private val classifier = DietLineClassifier()

    @Test
    fun testClassifyStructural() {
        assertEquals(ParsedLineKind.DAY_HEADER, classifier.classify("GIORNO CON ALLENAMENTO"))
        assertEquals(ParsedLineKind.DAY_HEADER, classifier.classify("Lunedì"))
        assertEquals(ParsedLineKind.MEAL_HEADER, classifier.classify("Colazione"))
        assertEquals(ParsedLineKind.MEAL_HEADER, classifier.classify("PRE WORKOUT"))
        assertEquals(ParsedLineKind.OPTION_MARKER, classifier.classify("Opzione 1:"))
        assertEquals(ParsedLineKind.GROUP_MARKER, classifier.classify("+"))
        assertEquals(ParsedLineKind.LUNCH_REFERENCE, classifier.classify("vedi alternative del pranzo"))
    }

    @Test
    fun testClassifyFood() {
        assertEquals(ParsedLineKind.FOOD_CANDIDATE, classifier.classify("10 g Olio d'oliva"))
        assertEquals(ParsedLineKind.FOOD_CANDIDATE, classifier.classify("Olio d'oliva 10 g"))
        assertEquals(ParsedLineKind.FOOD_CANDIDATE, classifier.classify("oppure 200 ml Latte parzialmente scremato"))
    }

    @Test
    fun testClassifyNoise() {
        assertEquals(ParsedLineKind.CONTACT_INFO, classifier.classify("test@example.com"))
        assertEquals(ParsedLineKind.CONTACT_INFO, classifier.classify("www.miosito.it"))
        assertEquals(ParsedLineKind.CONTACT_INFO, classifier.classify("Tel: 3331234567"))
        assertEquals(ParsedLineKind.CONTACT_INFO, classifier.classify("Via Roma 12"))
        
        assertEquals(ParsedLineKind.PROFESSIONAL_HEADER, classifier.classify("Elisabetta Moroni - Nutrizionista"))
        assertEquals(ParsedLineKind.PROFESSIONAL_HEADER, classifier.classify("Paziente: Mario Rossi"))
        
        assertEquals(ParsedLineKind.DOCUMENT_METADATA, classifier.classify("12/10/2023"))
        assertEquals(ParsedLineKind.DOCUMENT_METADATA, classifier.classify("Gennaio 2024"))
        
        assertEquals(ParsedLineKind.NOTES_SECTION, classifier.classify("Note generali:"))
        assertEquals(ParsedLineKind.NOTES_SECTION, classifier.classify("Metodi di cottura:"))
        
        val longText = "Questo è un testo molto lungo che serve a spiegare come funziona la dieta e non contiene numeri ma solo parole descrittive."
        assertEquals(ParsedLineKind.NARRATIVE_TEXT, classifier.classify(longText))
    }
}
