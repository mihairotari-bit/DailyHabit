package com.mihai.dailyhabit

import javax.inject.Inject
import javax.inject.Singleton

enum class NativeTextQualityReason {
    NATIVE_VALID,
    EMPTY_TEXT,
    TOO_FEW_TOKENS,
    CORRUPTED_CHARACTERS,
    LOW_PRINTABLE_RATIO,
    OCR_REQUIRED
}

data class NativeTextQualityResult(
    val isValid: Boolean,
    val reason: NativeTextQualityReason
)

@Singleton
class NativeTextQualityEvaluator @Inject constructor() {
    
    fun evaluate(pageText: String): NativeTextQualityResult {
        if (pageText.isBlank()) return NativeTextQualityResult(false, NativeTextQualityReason.EMPTY_TEXT)
        
        val totalChars = pageText.length
        if (totalChars < 50) return NativeTextQualityResult(false, NativeTextQualityReason.TOO_FEW_TOKENS)
        
        val printableChars = pageText.count { !it.isISOControl() && !it.isWhitespace() }
        val printableRatio = printableChars.toDouble() / totalChars
        
        if (printableRatio < 0.3) return NativeTextQualityResult(false, NativeTextQualityReason.LOW_PRINTABLE_RATIO)
        
        val replacementCharCount = pageText.count { it == '\uFFFD' || it == '?' }
        if (replacementCharCount > 10) return NativeTextQualityResult(false, NativeTextQualityReason.CORRUPTED_CHARACTERS)
        
        val words = pageText.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.size < 10) return NativeTextQualityResult(false, NativeTextQualityReason.TOO_FEW_TOKENS)
        
        val alphanumericCount = pageText.count { it.isLetterOrDigit() }
        val alphanumericRatio = alphanumericCount.toDouble() / printableChars.coerceAtLeast(1)
        if (alphanumericRatio < 0.5) return NativeTextQualityResult(false, NativeTextQualityReason.OCR_REQUIRED)
        
        return NativeTextQualityResult(true, NativeTextQualityReason.NATIVE_VALID)
    }
}
