package com.mihai.dailyhabit

import org.junit.Assert.assertEquals
import org.junit.Test

class LunchReferenceIsNotSplitTest {

    @Test
    fun `test lunch reference is not split by tokenizer`() {
        val tokenizer = DietStructureTokenizer()
        
        val input = "CENA\n100g pollo\nvedi alternative del pranzo"
        val tokens = tokenizer.tokenize(input)
        
        // Tokens should be: ["CENA", "100g pollo", "vedi alternative del pranzo"]
        assertEquals(3, tokens.size)
        assertEquals("CENA", tokens[0])
        assertEquals("100g pollo", tokens[1])
        assertEquals("vedi alternative del pranzo", tokens[2])
    }
}
