package com.mihai.dailyhabit

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DietTextPreprocessor @Inject constructor(
    private val classifier: DietLineClassifier
) {

    fun preprocess(text: String): String {
        var inDietBody = false
        var inNotesSection = false
        
        return text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filter { line ->
                val kind = classifier.classify(line)
                
                if (kind == ParsedLineKind.DAY_HEADER || kind == ParsedLineKind.MEAL_HEADER) {
                    inDietBody = true
                    inNotesSection = false
                }
                
                if (kind == ParsedLineKind.NOTES_SECTION) {
                    inNotesSection = true
                }
                
                if (!inDietBody) return@filter false
                if (inNotesSection) return@filter false
                
                // Filter out generic info even inside diet body
                when (kind) {
                    ParsedLineKind.CONTACT_INFO,
                    ParsedLineKind.PROFESSIONAL_HEADER,
                    ParsedLineKind.DOCUMENT_METADATA,
                    ParsedLineKind.NARRATIVE_TEXT -> false
                    else -> true
                }
            }
            .joinToString("\n")
    }
}
