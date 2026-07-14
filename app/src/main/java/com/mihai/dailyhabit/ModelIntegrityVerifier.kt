package com.mihai.dailyhabit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelIntegrityVerifier @Inject constructor() {

    suspend fun verifySha256(file: File, expectedHash: String): Boolean = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext false
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { fis ->
                val buffer = ByteArray(256 * 1024)
                var read = fis.read(buffer)
                while (read > 0) {
                    digest.update(buffer, 0, read)
                    read = fis.read(buffer)
                }
            }
            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            hash.equals(expectedHash, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
}
