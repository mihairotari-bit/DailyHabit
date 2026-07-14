package com.mihai.dailyhabit

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

import dagger.hilt.android.qualifiers.ApplicationContext

enum class ModelState {
    NOT_INSTALLED,
    CHECKING_STORAGE,
    WAITING_FOR_NETWORK,
    DOWNLOADING,
    PAUSED,
    VERIFYING,
    READY,
    CORRUPTED,
    INSUFFICIENT_STORAGE,
    FAILED
}

data class ModelMetadata(
    val modelId: String = "",
    val revision: String = "",
    val sizeBytes: Long = 0L,
    val checksum: String = "",
    val downloadedBytes: Long = 0L,
    val eTag: String? = null,
    val lastModified: String? = null,
    val state: ModelState = ModelState.NOT_INSTALLED,
    val lastError: String? = null
)

private val Context.modelDataStore: DataStore<Preferences> by preferencesDataStore(name = "model_metadata")

@Singleton
class ModelStateRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val MODEL_ID = stringPreferencesKey("model_id")
    private val REVISION = stringPreferencesKey("revision")
    private val SIZE_BYTES = longPreferencesKey("size_bytes")
    private val CHECKSUM = stringPreferencesKey("checksum")
    private val DOWNLOADED_BYTES = longPreferencesKey("downloaded_bytes")
    private val ETAG = stringPreferencesKey("etag")
    private val LAST_MODIFIED = stringPreferencesKey("last_modified")
    private val STATE = stringPreferencesKey("state")
    private val LAST_ERROR = stringPreferencesKey("last_error")

    val metadataFlow: Flow<ModelMetadata> = context.modelDataStore.data.map { prefs ->
        ModelMetadata(
            modelId = prefs[MODEL_ID] ?: "",
            revision = prefs[REVISION] ?: "",
            sizeBytes = prefs[SIZE_BYTES] ?: 0L,
            checksum = prefs[CHECKSUM] ?: "",
            downloadedBytes = prefs[DOWNLOADED_BYTES] ?: 0L,
            eTag = prefs[ETAG],
            lastModified = prefs[LAST_MODIFIED],
            state = prefs[STATE]?.let { enumValueOf<ModelState>(it) } ?: ModelState.NOT_INSTALLED,
            lastError = prefs[LAST_ERROR]
        )
    }

    suspend fun updateState(state: ModelState, lastError: String? = null) {
        context.modelDataStore.edit { prefs ->
            prefs[STATE] = state.name
            if (lastError != null) prefs[LAST_ERROR] = lastError else prefs.remove(LAST_ERROR)
        }
    }

    suspend fun updateDownloadProgress(downloadedBytes: Long) {
        context.modelDataStore.edit { prefs ->
            prefs[DOWNLOADED_BYTES] = downloadedBytes
        }
    }

    suspend fun updateServerMetadata(eTag: String?, lastModified: String?) {
        context.modelDataStore.edit { prefs ->
            if (eTag != null) prefs[ETAG] = eTag else prefs.remove(ETAG)
            if (lastModified != null) prefs[LAST_MODIFIED] = lastModified else prefs.remove(LAST_MODIFIED)
        }
    }

    suspend fun setReady(modelId: String, revision: String, sizeBytes: Long, checksum: String) {
        context.modelDataStore.edit { prefs ->
            prefs[MODEL_ID] = modelId
            prefs[REVISION] = revision
            prefs[SIZE_BYTES] = sizeBytes
            prefs[CHECKSUM] = checksum
            prefs[STATE] = ModelState.READY.name
            prefs.remove(LAST_ERROR)
        }
    }

    suspend fun clearMetadata() {
        context.modelDataStore.edit { it.clear() }
    }
}
