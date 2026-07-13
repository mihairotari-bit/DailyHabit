package com.mihai.dailyhabit

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DietTextPreprocessor @Inject constructor() {

    fun preprocess(text: String): String {
        return text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filter { !isContactInfo(it) }
            .filter { !isHeaderOrTitle(it) }
            .filter { !isGeneralNoteOrNarrative(it) }
            .joinToString("\n")
    }

    private fun isContactInfo(line: String): Boolean {
        val lower = line.lowercase()
        // Email
        if (lower.contains("@") && lower.contains(".")) return true
        // URL
        if (lower.startsWith("http") || lower.startsWith("www.") || lower.endsWith(".com") || lower.endsWith(".it")) return true
        // Phone numbers (heuristics for Italian formats like +39 333 1234567 or 333-1234567)
        if (Regex("(?i)(tel\\.?|telefono|cell\\.?|cellulare)\\s*:?\\s*\\+?\\d+").containsMatchIn(line)) return true
        // If it looks like just a phone number (e.g. +393...)
        if (Regex("^\\+?\\d{9,13}$").matches(line.replace(Regex("[\\s-]"), ""))) return true
        // Addresses (heuristics like "Via Roma 1", "Piazza", "Viale")
        if (Regex("(?i)^(via|viale|piazza|corso)\\s+[a-zA-Z\\s]+\\d+").containsMatchIn(line)) return true
        return false
    }

    private fun isHeaderOrTitle(line: String): Boolean {
        val lower = line.lowercase()
        // Specific names from the user's report
        val blockedNames = listOf("elisabetta moroni", "nutrizionista biologa", "ordine nazionale biologi", "piano nutrizionale")
        if (blockedNames.any { lower.contains(it) }) return true
        
        // Month/Year combinations like "Ottobre 2024"
        if (Regex("(?i)^(gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|settembre|ottobre|novembre|dicembre)\\s+\\d{4}$").matches(line)) return true
        
        // "Paziente: Nome Cognome"
        if (lower.startsWith("paziente") || lower.startsWith("nome:")) return true

        return false
    }

    private fun isGeneralNoteOrNarrative(line: String): Boolean {
        val lower = line.lowercase()
        
        // Block known narrative headers
        val blockHeaders = listOf(
            "note:", "note generali", "frutta:", "condimento:", 
            "metodi di cottura:", "pesce:", "uova:", "pasto libero:"
        )
        if (blockHeaders.any { lower.startsWith(it) }) return true

        // Extremely long narrative paragraphs without numbers (heuristics: > 80 chars, no digits)
        if (line.length > 80 && !Regex("\\d").containsMatchIn(line)) {
            // Check if it's not a list of foods (which usually has commas and "oppure")
            if (!lower.contains("oppure") && !lower.contains("alternativa")) {
                return true
            }
        }

        return false
    }
}
