package com.mihai.dailyhabit

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DietTextPreprocessor @Inject constructor() {

    fun preprocess(text: String): String {
        var inDietBody = false
        var inNotesSection = false
        
        return text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filter { line ->
                val lower = line.lowercase()
                
                // Detect start of diet plan
                if (lower.contains("giorno con allenamento") || lower.contains("giorno senza allenamento") || 
                    Regex("(?i)(?:^|\\s)(luned[iì]|marted[iì]|mercoled[iì]|gioved[iì]|venerd[iì]|sabato|domenica)(?:\\s|$)").containsMatchIn(line)) {
                    inDietBody = true
                    inNotesSection = false
                }
                
                // If we hit a meal, we exit notes section and enter diet body
                val mealTypes = listOf("colazione", "spuntino", "pranzo", "merenda", "cena", "pre workout", "post workout", "pre-workout", "post-workout")
                if (mealTypes.any { lower == it || lower.startsWith(it + " ") }) {
                    inDietBody = true
                    inNotesSection = false
                }
                
                // Detect notes section
                val noteHeaders = listOf("note generali", "metodi di cottura:", "frutta:")
                if (noteHeaders.any { lower.startsWith(it) }) {
                    inNotesSection = true
                }
                
                if (!inDietBody) return@filter false
                if (inNotesSection) return@filter false
                
                // Filter out generic info even inside diet body
                !isContactInfo(line) && !isGenericHeader(line) && !isNarrative(line)
            }
            .joinToString("\n")
    }

    private fun isContactInfo(line: String): Boolean {
        val lower = line.lowercase()
        if (lower.contains("@") && lower.contains(".")) return true
        if (lower.startsWith("http") || lower.startsWith("www.") || lower.endsWith(".com") || lower.endsWith(".it")) return true
        if (Regex("(?i)(tel\\.?|telefono|cell\\.?|cellulare)\\s*:?\\s*\\+?\\d+").containsMatchIn(line)) return true
        if (Regex("^\\+?\\d{9,13}$").matches(line.replace(Regex("[\\s-]"), ""))) return true
        if (Regex("(?i)^(via|viale|piazza|corso)\\s+[a-zA-Z\\s]+\\d+").containsMatchIn(line)) return true
        return false
    }

    private fun isGenericHeader(line: String): Boolean {
        val lower = line.lowercase()
        // Generic classifications instead of hardcoded names
        if (Regex("(?i)\\b(nutrizionista|biolog[oa]|dott\\.?|dott\\.ssa|piano nutrizionale|ordine nazionale)\\b").containsMatchIn(line)) return true
        if (Regex("(?i)^(gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|settembre|ottobre|novembre|dicembre)\\s+\\d{4}$").matches(line)) return true
        if (lower.startsWith("paziente") || lower.startsWith("nome:")) return true
        // Date formats like DD/MM/YYYY
        if (Regex("^\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}$").matches(line)) return true
        return false
    }

    private fun isNarrative(line: String): Boolean {
        if (line.length > 80 && !Regex("\\d").containsMatchIn(line)) {
            val lower = line.lowercase()
            if (!lower.contains("oppure") && !lower.contains("alternativa")) {
                return true
            }
        }
        return false
    }
}
