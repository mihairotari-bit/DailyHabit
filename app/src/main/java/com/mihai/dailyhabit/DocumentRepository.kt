package com.mihai.dailyhabit

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

sealed interface PdfPage {
    data class NativeText(val pageNumber: Int, val text: String) : PdfPage
    data class OcrRequired(val pageNumber: Int, val bitmap: Bitmap) : PdfPage
}

sealed interface DocumentContent {
    data class Text(val value: String) : DocumentContent
    data class HybridPdf(val pages: List<PdfPage>) : DocumentContent
}

interface DocumentReader {
    suspend fun read(uri: Uri): DocumentContent
    fun persistReadPermission(uri: Uri)
}

@Singleton
class DocumentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : DocumentReader {
    private val resolver: ContentResolver get() = context.contentResolver

    override fun persistReadPermission(uri: Uri) {
        runCatching {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    @Inject lateinit var nativeExtractor: PdfNativeTextExtractor

    override suspend fun read(uri: Uri): DocumentContent = withContext(Dispatchers.IO) {
        when (documentKind(uri)) {
            DocumentKind.TEXT -> DocumentContent.Text(readText(uri))
            DocumentKind.DOCX -> DocumentContent.Text(readDocxParagraphs(uri))
            DocumentKind.PDF -> {
                val nativePages = nativeExtractor.extract(resolver, uri)
                if (nativePages != null) {
                    val bitmaps = lazy { renderPdf(uri) }
                    val hybridPages = nativePages.mapIndexed { index, extractedPage ->
                        if (extractedPage.isNativeValid) {
                            PdfPage.NativeText(extractedPage.pageNumber, extractedPage.text)
                        } else {
                            val bitmap = bitmaps.value.getOrNull(index) ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                            PdfPage.OcrRequired(extractedPage.pageNumber, bitmap)
                        }
                    }
                    DocumentContent.HybridPdf(hybridPages)
                } else {
                    val bitmaps = renderPdf(uri)
                    val hybridPages = bitmaps.mapIndexed { index, bitmap -> PdfPage.OcrRequired(index + 1, bitmap) }
                    DocumentContent.HybridPdf(hybridPages)
                }
            }
            DocumentKind.UNKNOWN -> error("Formato non supportato. Scegli TXT, PDF o DOCX.")
        }
    }

    private fun documentKind(uri: Uri): DocumentKind {
        val mime = resolver.getType(uri).orEmpty()
        val name = uri.lastPathSegment.orEmpty().lowercase()
        return when {
            mime == "text/plain" || name.endsWith(".txt") -> DocumentKind.TEXT
            mime == "application/pdf" || name.endsWith(".pdf") -> DocumentKind.PDF
            mime == DOCX_MIME || name.endsWith(".docx") -> DocumentKind.DOCX
            else -> DocumentKind.UNKNOWN
        }
    }

    private fun readText(uri: Uri): String = resolver.openInputStream(uri)?.bufferedReader().use { reader ->
        requireNotNull(reader) { "Impossibile aprire il file TXT." }
        reader.readText()
    }

    /** Lightweight DOCX fallback: extracts text nodes from word/document.xml. */
    private fun readDocxParagraphs(uri: Uri): String {
        val input = requireNotNull(resolver.openInputStream(uri)) { "Impossibile aprire il file DOCX." }
        input.use { stream ->
            ZipInputStream(stream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null && entry.name != "word/document.xml") entry = zip.nextEntry
                requireNotNull(entry) { "DOCX non valido: manca word/document.xml." }
                val parser = XmlPullParserFactory.newInstance().newPullParser().apply { setInput(zip, "UTF-8") }
                val paragraphs = mutableListOf<String>()
                val paragraph = StringBuilder()
                while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                    when (parser.eventType) {
                        XmlPullParser.TEXT -> paragraph.append(parser.text)
                        XmlPullParser.END_TAG -> if (parser.name == "p") {
                            paragraph.toString().trim().takeIf(String::isNotEmpty)?.let(paragraphs::add)
                            paragraph.clear()
                        }
                    }
                    parser.next()
                }
                return paragraphs.joinToString("\n\n").ifBlank { "Il DOCX non contiene paragrafi di testo leggibili." }
            }
        }
    }

    private fun renderPdf(uri: Uri): List<Bitmap> {
        val descriptor = requireNotNull(resolver.openFileDescriptor(uri, "r")) { "Impossibile aprire il PDF." }
        descriptor.use { fileDescriptor ->
            PdfRenderer(fileDescriptor).use { renderer ->
                return List(renderer.pageCount) { index ->
                    renderer.openPage(index).use { page ->
                        val width = 1440
                        val height = (width * page.height.toFloat() / page.width).toInt().coerceAtLeast(1)
                        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
                            bitmap.eraseColor(android.graphics.Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        }
                    }
                }
            }
        }
    }

    private enum class DocumentKind { TEXT, PDF, DOCX, UNKNOWN }

    private companion object {
        const val DOCX_MIME = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    }
}
