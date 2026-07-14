package com.mihai.dailyhabit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import javax.inject.Inject

@AndroidEntryPoint
class ModelDownloadJobService : JobService() {

    @Inject
    lateinit var stateRepository: ModelStateRepository

    private var jobJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val JOB_ID = 1001
        private const val CHANNEL_ID = "model_download_channel"
        private const val NOTIFICATION_ID = 2002
        private const val TAG = "ModelDownloadJob"

        val downloadMutex = Mutex()

        fun startDownload(context: Context) {
            val componentName = ComponentName(context, ModelDownloadJobService::class.java)
            val builder = JobInfo.Builder(JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                builder.setUserInitiated(true)
                builder.setEstimatedNetworkBytes(ModelManifest.EXACT_SIZE_BYTES, JobInfo.NETWORK_BYTES_UNKNOWN.toLong())
            }

            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as android.app.job.JobScheduler
            jobScheduler.schedule(builder.build())
        }

        fun stopDownload(context: Context) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as android.app.job.JobScheduler
            jobScheduler.cancel(JOB_ID)
        }
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            params?.let {
                setNotification(it, NOTIFICATION_ID, createNotification(0, "Preparazione download..."), JobService.JOB_END_NOTIFICATION_POLICY_DETACH)
            }
        }

        jobJob = scope.launch {
            try {
                if (downloadMutex.tryLock()) {
                    try {
                        stateRepository.updateState(ModelState.DOWNLOADING)
                        downloadModel(params)
                    } finally {
                        downloadMutex.unlock()
                    }
                } else {
                    // Gia in download o in import SAF
                    stateRepository.updateState(ModelState.FAILED, "Operazione già in corso")
                    jobFinished(params, false)
                }
            } catch (e: CancellationException) {
                stateRepository.updateState(ModelState.PAUSED, "Annullato dall'utente")
                jobFinished(params, false)
            } catch (e: Exception) {
                stateRepository.updateState(ModelState.FAILED, "Errore di rete: ${e.message}")
                jobFinished(params, true) // Retry permitted for network errors
            }
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        jobJob?.cancel()
        scope.launch {
            stateRepository.updateState(ModelState.PAUSED, "Job interrotto dal sistema")
        }
        return true
    }

    private suspend fun downloadModel(params: JobParameters?) = withContext(Dispatchers.IO) {
        val partFile = ModelManifest.getModelPartFile(this@ModelDownloadJobService)
        val finalFile = ModelManifest.getModelFile(this@ModelDownloadJobService)

        if (finalFile.exists() && finalFile.length() == ModelManifest.EXACT_SIZE_BYTES) {
            stateRepository.setReady(
                ModelManifest.MODEL_ID,
                ModelManifest.REVISION,
                ModelManifest.EXACT_SIZE_BYTES,
                ModelManifest.EXPECTED_SHA256
            )
            jobFinished(params, false)
            return@withContext
        }

        var downloadedBytes = partFile.length()
        if (downloadedBytes > ModelManifest.EXACT_SIZE_BYTES) {
            partFile.delete()
            downloadedBytes = 0L
        }

        val metadata = stateRepository.metadataFlow.first()
        var serverETag: String? = null
        var serverLastModified: String? = null

        // 1. Verifica preliminare HEAD
        try {
            val url = URL(ModelManifest.downloadUrl)
            var headConn = url.openConnection() as HttpURLConnection
            headConn.requestMethod = "HEAD"
            headConn.setRequestProperty("Range", "bytes=0-0")
            headConn.connect()
            serverETag = headConn.getHeaderField("ETag")
            serverLastModified = headConn.getHeaderField("Last-Modified")
            val acceptRanges = headConn.getHeaderField("Accept-Ranges")
            
            if ((metadata.eTag != null && serverETag != null && metadata.eTag != serverETag) ||
                (metadata.lastModified != null && serverLastModified != null && metadata.lastModified != serverLastModified)) {
                // File remoto cambiato
                partFile.delete()
                downloadedBytes = 0L
            } else if (acceptRanges != "bytes" && downloadedBytes > 0) {
                // Il server non supporta Range
                partFile.delete()
                downloadedBytes = 0L
            }
            headConn.disconnect()
        } catch (e: Exception) {
            // Se HEAD fallisce, proseguiamo con GET e gestiamo lì
        }

        // 2. Connessione GET effettiva
        var retryCount = 0
        val maxRetries = 3
        var success = false

        while (retryCount < maxRetries && !success) {
            ensureActive()
            try {
                val url = URL(ModelManifest.downloadUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                
                if (downloadedBytes > 0) {
                    connection.setRequestProperty("Range", "bytes=$downloadedBytes-")
                    if (serverETag != null) {
                        connection.setRequestProperty("If-Range", serverETag)
                    } else if (serverLastModified != null) {
                        connection.setRequestProperty("If-Range", serverLastModified)
                    }
                }
                
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                    // Ripresa OK
                    val contentRange = connection.getHeaderField("Content-Range")
                    if (contentRange != null && !contentRange.startsWith("bytes $downloadedBytes-")) {
                        // Range non soddisfacibile correttamente, ripartiamo
                        connection.disconnect()
                        partFile.delete()
                        downloadedBytes = 0L
                        continue
                    }
                } else if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Download completo da zero (il server ha ignorato il range)
                    if (downloadedBytes > 0) {
                        partFile.delete()
                        downloadedBytes = 0L
                    }
                } else if (responseCode == 416) {
                    // Range Not Satisfiable
                    connection.disconnect()
                    if (downloadedBytes == ModelManifest.EXACT_SIZE_BYTES) {
                        success = true
                        break // Vai alla verifica
                    } else {
                        partFile.delete()
                        downloadedBytes = 0L
                        continue
                    }
                } else {
                    throw Exception("HTTP Error $responseCode")
                }

                // Salvataggio dei metadati del server
                serverETag = connection.getHeaderField("ETag") ?: serverETag
                serverLastModified = connection.getHeaderField("Last-Modified") ?: serverLastModified
                stateRepository.updateServerMetadata(serverETag, serverLastModified)

                val inputStream: InputStream = connection.inputStream
                val outputStream = FileOutputStream(partFile, downloadedBytes > 0)

                try {
                    val buffer = ByteArray(64 * 1024)
                    var read: Int
                    var lastUpdate = System.currentTimeMillis()

                    while (inputStream.read(buffer).also { read = it } != -1) {
                        ensureActive()
                        outputStream.write(buffer, 0, read)
                        downloadedBytes += read

                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 1000) {
                            lastUpdate = now
                            stateRepository.updateDownloadProgress(downloadedBytes)
                            val progressPercent = if (ModelManifest.EXACT_SIZE_BYTES > 0)
                                (downloadedBytes * 100 / ModelManifest.EXACT_SIZE_BYTES).toInt() else 0

                            updateNotification(createNotification(progressPercent, "Scaricato ${downloadedBytes / 1024 / 1024} MB di ${ModelManifest.EXACT_SIZE_BYTES / 1024 / 1024} MB"))
                        }
                    }
                    outputStream.flush()
                    outputStream.fd.sync() // fsync obbligatorio
                    success = true
                } finally {
                    outputStream.close()
                    inputStream.close()
                    connection.disconnect()
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                retryCount++
                if (retryCount >= maxRetries) {
                    throw e
                }
                delay(2000L * retryCount) // Backoff
            }
        }

        // 3. Verifica Integrità e Commit Atomico
        if (downloadedBytes == ModelManifest.EXACT_SIZE_BYTES) {
            stateRepository.updateState(ModelState.VERIFYING)
            updateNotification(createNotification(100, "Verifica SHA-256 in corso..."))

            val hash = calculateSha256(partFile)
            if (hash.equals(ModelManifest.EXPECTED_SHA256, ignoreCase = true)) {
                if (partFile.renameTo(finalFile)) {
                    stateRepository.setReady(
                        ModelManifest.MODEL_ID,
                        ModelManifest.REVISION,
                        ModelManifest.EXACT_SIZE_BYTES,
                        ModelManifest.EXPECTED_SHA256
                    )
                } else {
                    stateRepository.updateState(ModelState.FAILED, "Errore nel rename atomico")
                }
            } else {
                partFile.delete()
                stateRepository.updateState(ModelState.CORRUPTED, "Checksum mismatch: atteso ${ModelManifest.EXPECTED_SHA256}, calcolato $hash")
            }
        } else {
            stateRepository.updateState(ModelState.FAILED, "Dimensione scaricata non corrispondente all'atteso.")
        }

        jobFinished(params, false)
    }

    private suspend fun calculateSha256(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buffer = ByteArray(256 * 1024)
            var read = fis.read(buffer)
            while (read > 0) {
                ensureActive()
                digest.update(buffer, 0, read)
                read = fis.read(buffer)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Model Download",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download del modello Gemma 4 E2B"
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun createNotification(progress: Int, text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download Gemma 4 E2B")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(notification: Notification) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }
}
