package com.mihai.dailyhabit

import android.content.ContentResolver
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ExtractedPage(
    val pageNumber: Int,
    val text: String
)

@Singleton
class PdfNativeTextExtractor @Inject constructor() {

    suspend fun extract(resolver: ContentResolver, uri: Uri): List<ExtractedPage>? = withContext(Dispatchers.IO) {
        var document: PDDocument? = null
        try {
            val inputStream = resolver.openInputStream(uri) ?: return@withContext null
            document = PDDocument.load(inputStream)
            
            val stripper = PDFTextStripper()
            val pages = mutableListOf<ExtractedPage>()
            
            for (p in 1..document.numberOfPages) {
                stripper.startPage = p
                stripper.endPage = p
                val text = stripper.getText(document).trim()
                pages.add(ExtractedPage(p, text))
            }
            
            if (isTextValid(pages)) {
                return@withContext pages
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        } finally {
            document?.close()
        }
    }
    
    private fun isTextValid(pages: List<ExtractedPage>): Boolean {
        val fullText = pages.joinToString("\n") { it.text }.lowercase()
        // A minimal valid diet PDF must contain some key words. 
        // If it's a scanned PDF, PDFBox returns empty or garbage.
        val hasDay = fullText.contains("giorno") || fullText.contains("luned") || fullText.contains("marted")
        val hasMeal = fullText.contains("colazione") || fullText.contains("pranzo") || fullText.contains("cena")
        
        return hasDay && hasMeal
    }
}
