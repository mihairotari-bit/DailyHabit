package com.mihai.dailyhabit

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Test

class HybridDocumentTest {

    @Test
    fun `test hybrid document preserves page order`() {
        val bitmapMock = io.mockk.mockk<Bitmap>(relaxed = true)
        
        val hybridPages = listOf(
            PdfPage.NativeText(1, "Pagina 1 nativa"),
            PdfPage.OcrRequired(2, bitmapMock),
            PdfPage.NativeText(3, "Pagina 3 nativa")
        )
        
        val content = DocumentContent.HybridPdf(hybridPages)
        
        val extractedPages = mutableListOf<ExtractedPage>()
        for (page in content.pages) {
            when (page) {
                is PdfPage.NativeText -> extractedPages.add(ExtractedPage(page.pageNumber, page.text, true))
                is PdfPage.OcrRequired -> {
                    val textFromOcrMock = "Pagina ${page.pageNumber} OCR"
                    extractedPages.add(ExtractedPage(page.pageNumber, textFromOcrMock, false))
                }
            }
        }
        
        val fullText = extractedPages.joinToString("\n") { it.text }
        val expectedText = "Pagina 1 nativa\nPagina 2 OCR\nPagina 3 nativa"
        
        assertEquals(3, extractedPages.size)
        assertEquals(1, extractedPages[0].pageNumber)
        assertEquals(2, extractedPages[1].pageNumber)
        assertEquals(3, extractedPages[2].pageNumber)
        assertEquals(expectedText, fullText)
    }
}
