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
    val text: String,
    val isNativeValid: Boolean
)

@Singleton
class PdfNativeTextExtractor @Inject constructor(
    private val qualityEvaluator: NativeTextQualityEvaluator
) {

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
                val result = qualityEvaluator.evaluate(text)
                pages.add(ExtractedPage(p, text, result.isValid))
            }
            
            return@withContext pages
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        } finally {
            document?.close()
        }
    }
}
