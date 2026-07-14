package com.mihai.dailyhabit

import android.content.Context
import java.io.File

object ModelManifest {
    const val MODEL_ID = "gemma-4-e2b-it-litertlm"
    const val REPOSITORY = "litert-community/gemma-4-E2B-it-litert-lm"
    const val FILE_NAME = "gemma-4-E2B-it.litertlm"
    const val REVISION = "6e5c4f1e395deb959c494953478fa5cec4b8008f"
    const val EXACT_SIZE_BYTES = 2588147712L
    const val EXPECTED_SHA256 = "181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c"
    const val LICENSE = "Apache-2.0"

    // Construct the direct download URL for HuggingFace
    val downloadUrl: String
        get() = "https://huggingface.co/$REPOSITORY/resolve/$REVISION/$FILE_NAME"

    /**
     * Ritorna la cartella di destinazione canonica (es. noBackupFilesDir/models/gemma-4-e2b-it/)
     */
    fun getModelDirectory(context: Context): File {
        val dir = File(context.noBackupFilesDir, "models/gemma-4-e2b-it")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Ritorna il file canonico per il modello.
     */
    fun getModelFile(context: Context): File {
        return File(getModelDirectory(context), FILE_NAME)
    }

    /**
     * Ritorna il file .part temporaneo.
     */
    fun getModelPartFile(context: Context): File {
        return File(getModelDirectory(context), "$FILE_NAME.part")
    }
}
