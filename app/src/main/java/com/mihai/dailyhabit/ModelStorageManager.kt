package com.mihai.dailyhabit

import android.content.Context
import android.net.Uri
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelStorageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stateRepository: ModelStateRepository,
    private val integrityVerifier: ModelIntegrityVerifier
) {

    fun hasEnoughSpace(requiredBytes: Long): Boolean {
        val dir = ModelManifest.getModelDirectory(context)
        val stat = StatFs(dir.path)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        return availableBytes > requiredBytes
    }

    suspend fun reconcileState() = withContext(Dispatchers.IO) {
        val metadata = stateRepository.metadataFlow.first()
        val finalFile = ModelManifest.getModelFile(context)
        val partFile = ModelManifest.getModelPartFile(context)

        // Se file finale c'è
        if (finalFile.exists()) {
            if (finalFile.length() != ModelManifest.EXACT_SIZE_BYTES) {
                // Dimensione errata
                finalFile.delete()
                stateRepository.updateState(ModelState.CORRUPTED, "Dimensione file finale errata all'avvio.")
            } else if (metadata.state != ModelState.READY) {
                // Recupera READY
                stateRepository.setReady(
                    ModelManifest.MODEL_ID,
                    ModelManifest.REVISION,
                    ModelManifest.EXACT_SIZE_BYTES,
                    ModelManifest.EXPECTED_SHA256
                )
            }
            if (partFile.exists()) partFile.delete()
        } else {
            // File finale non c'è
            if (metadata.state == ModelState.READY) {
                stateRepository.updateState(ModelState.NOT_INSTALLED, "File mancante.")
            }
            if (partFile.exists()) {
                if (partFile.length() > ModelManifest.EXACT_SIZE_BYTES) {
                    partFile.delete()
                    stateRepository.updateState(ModelState.NOT_INSTALLED)
                } else if (metadata.state != ModelState.PAUSED && metadata.state != ModelState.DOWNLOADING) {
                    stateRepository.updateState(ModelState.PAUSED, "Download interrotto recuperato.")
                }
            }
        }
    }

    suspend fun importModelFromUri(uri: Uri) = withContext(Dispatchers.IO) {
        if (!ModelDownloadJobService.downloadMutex.tryLock()) {
            stateRepository.updateState(ModelState.FAILED, "Operazione di rete già in corso.")
            return@withContext
        }

        try {
            stateRepository.updateState(ModelState.DOWNLOADING)
            
            val partFile = ModelManifest.getModelPartFile(context)
            val finalFile = ModelManifest.getModelFile(context)

            if (partFile.exists()) partFile.delete()

            val contentResolver = context.contentResolver
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(partFile).use { outputStream ->
                    val buffer = ByteArray(256 * 1024)
                    var read: Int
                    var copiedBytes = 0L
                    var lastUpdate = System.currentTimeMillis()

                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                        copiedBytes += read

                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 1000) {
                            lastUpdate = now
                            stateRepository.updateDownloadProgress(copiedBytes)
                        }
                    }
                    outputStream.flush()
                    outputStream.fd.sync()
                }
            }

            if (partFile.length() == ModelManifest.EXACT_SIZE_BYTES) {
                stateRepository.updateState(ModelState.VERIFYING)
                val isValid = integrityVerifier.verifySha256(partFile, ModelManifest.EXPECTED_SHA256)
                if (isValid) {
                    if (partFile.renameTo(finalFile)) {
                        stateRepository.setReady(
                            ModelManifest.MODEL_ID,
                            ModelManifest.REVISION,
                            ModelManifest.EXACT_SIZE_BYTES,
                            ModelManifest.EXPECTED_SHA256
                        )
                    } else {
                        stateRepository.updateState(ModelState.FAILED, "Errore rinomina file.")
                    }
                } else {
                    partFile.delete()
                    stateRepository.updateState(ModelState.CORRUPTED, "Checksum mismatch del file importato.")
                }
            } else {
                partFile.delete()
                stateRepository.updateState(ModelState.FAILED, "Dimensione del file importato errata.")
            }
        } catch (e: Exception) {
            stateRepository.updateState(ModelState.FAILED, "Errore importazione SAF: ${e.message}")
        } finally {
            ModelDownloadJobService.downloadMutex.unlock()
        }
    }
}
