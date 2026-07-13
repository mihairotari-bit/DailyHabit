package com.mihai.android17helloworld

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class TextRecognitionEngine @Inject constructor() {
    suspend fun recognize(pages: List<Bitmap>): String = withContext(Dispatchers.IO) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            pages.joinToString("\n") { bitmap -> 
                val result = Tasks.await(recognizer.process(InputImage.fromBitmap(bitmap, 0)))
                val lines = result.textBlocks.flatMap { it.lines }.sortedBy { it.boundingBox?.centerY() ?: 0 }
                val rows = mutableListOf<MutableList<com.google.mlkit.vision.text.Text.Line>>()
                for (line in lines) {
                    val centerY = line.boundingBox?.centerY() ?: 0
                    val height = line.boundingBox?.height() ?: 20
                    val matchedRow = rows.find { row -> 
                        val rowCenterY = row.first().boundingBox?.centerY() ?: 0
                        Math.abs(rowCenterY - centerY) < height / 2
                    }
                    if (matchedRow != null) {
                        matchedRow.add(line)
                    } else {
                        rows.add(mutableListOf(line))
                    }
                }
                rows.joinToString("\n") { row ->
                    row.sortedBy { it.boundingBox?.left ?: 0 }.joinToString(" ") { it.text }
                }
            }
        } finally {
            recognizer.close()
        }
    }
}
