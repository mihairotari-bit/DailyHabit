package com.mihai.dailyhabit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
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
                setNotification(it, NOTIFICATION_ID, createNotification(0, "Avvio download..."), JobService.JOB_END_NOTIFICATION_POLICY_DETACH)
            }
        }

        jobJob = scope.launch {
            try {
                stateRepository.updateState(ModelState.DOWNLOADING)
                downloadModel(params)
            } catch (e: CancellationException) {
                stateRepository.updateState(ModelState.PAUSED, "Download in pausa o annullato")
                jobFinished(params, false)
            } catch (e: Exception) {
                stateRepository.updateState(ModelState.FAILED, e.message ?: "Errore sconosciuto")
                jobFinished(params, false) // False means do not retry automatically, let user retry
            }
        }
        return true // Work is asynchronous
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        jobJob?.cancel()
        scope.launch {
            stateRepository.updateState(ModelState.PAUSED, "Job interrotto dal sistema")
        }
        return true // Reschedule
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
        val url = URL(ModelManifest.downloadUrl)
        var connection = url.openConnection() as HttpURLConnection

        // Gestione ETag e LastModified
        var currentETag: String? = null
        var currentLastModified: String? = null

        try {
            connection.requestMethod = "HEAD"
            connection.connect()
            currentETag = connection.getHeaderField("ETag")
            currentLastModified = connection.getHeaderField("Last-Modified")
            
            // TODO: Se ETag è cambiato, ripartire da zero
            // Per semplicità qui facciamo il check se non supporta range
            val acceptRanges = connection.getHeaderField("Accept-Ranges")
            if (acceptRanges != "bytes" || downloadedBytes > ModelManifest.EXACT_SIZE_BYTES) {
                downloadedBytes = 0
                partFile.delete()
            }
        } catch (e: Exception) {
            // Ignora errori HEAD e prova GET
        } finally {
            connection.disconnect()
        }

        connection = url.openConnection() as HttpURLConnection
        if (downloadedBytes > 0) {
            connection.setRequestProperty("Range", "bytes=$downloadedBytes-")
        }
        connection.connect()

        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
            throw IllegalStateException("Server error HTTP ${responseCode}")
        }

        // Se ha ignorato il range, riparti da zero
        if (downloadedBytes > 0 && responseCode == HttpURLConnection.HTTP_OK) {
            downloadedBytes = 0
            partFile.delete()
        }

        val inputStream: InputStream = connection.inputStream
        val outputStream = FileOutputStream(partFile, downloadedBytes > 0)

        try {
            val buffer = ByteArray(64 * 1024) // 64KB buffer
            var read: Int
            var lastUpdate = System.currentTimeMillis()

            while (inputStream.read(buffer).also { read = it } != -1) {
                ensureActive() // Throw CancellationException if job stopped
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
            outputStream.fd.sync() // fsync
        } finally {
            outputStream.close()
            inputStream.close()
            connection.disconnect()
        }

        if (downloadedBytes == ModelManifest.EXACT_SIZE_BYTES) {
            stateRepository.updateState(ModelState.VERIFYING)
            updateNotification(createNotification(100, "Verifica in corso..."))
            
            val hash = calculateSha256(partFile)
            if (hash.equals(ModelManifest.EXPECTED_SHA256, ignoreCase = true)) {
                partFile.renameTo(finalFile)
                stateRepository.setReady(
                    ModelManifest.MODEL_ID,
                    ModelManifest.REVISION,
                    ModelManifest.EXACT_SIZE_BYTES,
                    ModelManifest.EXPECTED_SHA256
                )
            } else {
                partFile.delete()
                stateRepository.updateState(ModelState.CORRUPTED, "Checksum mismatch")
            }
        } else {
            stateRepository.updateState(ModelState.FAILED, "Dimensione finale non corrispondente")
        }
        
        jobFinished(params, false)
    }

    private suspend fun calculateSha256(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buffer = ByteArray(64 * 1024)
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

    private fun createNotification(progress: Int, text: String): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download Gemma 4 E2B")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification(notification: android.app.Notification) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }
}
