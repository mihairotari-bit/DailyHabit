package com.mihai.dailyhabit

import javax.inject.Inject

enum class ParsedLineKind {
    DAY_HEADER,
    MEAL_HEADER,
    OPTION_MARKER,
    GROUP_MARKER,
    LUNCH_REFERENCE,
    FOOD_CANDIDATE,
    CONTACT_INFO,
    PROFESSIONAL_HEADER,
    DOCUMENT_METADATA,
    NARRATIVE_TEXT,
    NOTES_SECTION,
    UNKNOWN_NOISE
}

data class ClassifiedLine(
    val originalText: String,
    val kind: ParsedLineKind
)

class DietLineClassifier @Inject constructor() {

    private val QUANTITY_FIRST = Regex("(?i)^(?<quantity>\\d+(?:[,.]\\d+)?)\\s*(?<unit>g|gr|ml|pz|pezzi?)\\s+(?<name>.+)$")
    private val QUANTITY_LAST = Regex("(?i)^(?<name>.+?)\\s+(?<quantity>\\d+(?:[,.]\\d+)?)\\s*(?<unit>g|gr|ml|pz|pezzi?)$")

    fun classify(line: String): ParsedLineKind {
        val trimmed = line.trim()
        val lower = trimmed.lowercase()

        // 1. Structural Landmarks
        if (isDayHeader(lower)) return ParsedLineKind.DAY_HEADER
        if (isMealHeader(lower)) return ParsedLineKind.MEAL_HEADER
        if (isOptionMarker(lower)) return ParsedLineKind.OPTION_MARKER
        if (trimmed == "+") return ParsedLineKind.GROUP_MARKER
        if (isLunchReference(lower)) return ParsedLineKind.LUNCH_REFERENCE

        // 2. Noise & False Positives
        if (isContactInfo(trimmed)) return ParsedLineKind.CONTACT_INFO
        if (isProfessionalHeader(lower)) return ParsedLineKind.PROFESSIONAL_HEADER
        if (isDocumentMetadata(lower)) return ParsedLineKind.DOCUMENT_METADATA
        if (isNotesSection(lower)) return ParsedLineKind.NOTES_SECTION
        if (isNarrative(trimmed)) return ParsedLineKind.NARRATIVE_TEXT

        // 3. Food Candidates
        if (isFoodCandidate(trimmed)) return ParsedLineKind.FOOD_CANDIDATE

        return ParsedLineKind.UNKNOWN_NOISE
    }

    private fun isDayHeader(lower: String): Boolean {
        return lower.contains("giorno con allenamento") || 
               lower.contains("giorno senza allenamento") || 
               Regex("(?i)(?:^|\\s)(luned[iì]|marted[iì]|mercoled[iì]|gioved[iì]|venerd[iì]|sabato|domenica)(?:\\s|$)").containsMatchIn(lower)
    }

    private fun isMealHeader(lower: String): Boolean {
        val mealTypes = listOf("colazione", "spuntino", "pranzo", "merenda", "cena", "pre workout", "post workout", "pre-workout", "post-workout")
        return mealTypes.any { lower == it || lower.startsWith(it + " ") }
    }

    private fun isOptionMarker(lower: String): Boolean {
        return lower.startsWith("opzione") || lower.startsWith("alternativa")
    }

    private fun isLunchReference(lower: String): Boolean {
        return lower.contains("vedi alternative del pranzo") || 
               lower.contains("alternative pranzo") || 
               lower.contains("come giorno con allenamento") || 
               lower.contains("come da giorno") ||
               lower.contains("come pranzo")
    }

    private fun isContactInfo(line: String): Boolean {
        val lower = line.lowercase()
        // Email
        if (Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").containsMatchIn(line)) return true
        // URL
        if (Regex("(https?://)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)").containsMatchIn(line)) return true
        // Phone numbers (naive but safe for typical formats)
        if (Regex("(?i)^(cell|tel|telefono|mobile)\\.?\\s*:?\\s*\\+?[0-9\\s-]{8,15}\$").containsMatchIn(line)) return true
        // Addresses
        if (Regex("(?i)^(via|viale|piazza|corso)\\b.*\\d+.*").containsMatchIn(line)) return true
        return false
    }

    private fun isProfessionalHeader(lower: String): Boolean {
        if (Regex("(?i)\\b(nutrizionista|biolog[oa]|dott\\.?|dott\\.ssa|piano nutrizionale|ordine nazionale)\\b").containsMatchIn(lower)) return true
        if (lower.startsWith("paziente") || lower.startsWith("nome:")) return true
        return false
    }

    private fun isDocumentMetadata(lower: String): Boolean {
        // Date formats like DD/MM/YYYY
        if (Regex("^\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}$").matches(lower)) return true
        if (Regex("(?i)^(gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|settembre|ottobre|novembre|dicembre)\\s+\\d{4}$").matches(lower)) return true
        return false
    }

    private fun isNotesSection(lower: String): Boolean {
        val noteHeaders = listOf("note generali", "metodi di cottura:", "frutta:")
        return noteHeaders.any { lower.startsWith(it) } || lower.startsWith("note:")
    }

    private fun isNarrative(line: String): Boolean {
        val lower = line.lowercase()
        // Long sentences without numbers
        if (line.length > 80 && !Regex("\\d").containsMatchIn(line)) {
            if (!lower.contains("oppure") && !lower.contains("alternativa")) return true
        }
        // Explanatory paragraphs
        if (lower.startsWith("questo è ") || lower.contains("si consiglia") || lower.contains("è preferibile")) return true
        return false
    }

    private fun isFoodCandidate(line: String): Boolean {
        val clean = line.replace(Regex("(?i)^(oppure|o)\\s+"), "")
        return QUANTITY_FIRST.matches(clean) || QUANTITY_LAST.matches(clean)
    }
}
