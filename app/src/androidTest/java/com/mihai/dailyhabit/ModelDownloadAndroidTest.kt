package com.mihai.dailyhabit

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.MockResponse
import java.io.File
import android.net.Uri

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ModelDownloadAndroidTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context
    private lateinit var stateRepository: ModelStateRepository
    private lateinit var storageManager: ModelStorageManager
    private lateinit var integrityVerifier: ModelIntegrityVerifier

    @Before
    fun setup() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        stateRepository = ModelStateRepository(context)
        integrityVerifier = ModelIntegrityVerifier()
        storageManager = ModelStorageManager(context, stateRepository, integrityVerifier)
        
        runBlocking {
            stateRepository.clearMetadata()
        }
        
        val dir = ModelManifest.getModelDirectory(context)
        dir.listFiles()?.forEach { it.delete() }
    }

    @Test
    fun testReconcileReadyWithoutFile() = runBlocking {
        stateRepository.setReady("model", "rev", 100L, "hash")
        storageManager.reconcileState()
        
        val state = stateRepository.metadataFlow.first().state
        assertEquals(ModelState.NOT_INSTALLED, state)
    }

    @Test
    fun testSafImportWrongSize() = runBlocking {
        // Create a fake URI or just test the logic
        // We will just verify that the function rejects wrong size
        val fakeFile = File(context.cacheDir, "fake.litertlm")
        fakeFile.writeBytes(ByteArray(100))
        val uri = Uri.fromFile(fakeFile)
        
        storageManager.importModelFromUri(uri)
        
        val state = stateRepository.metadataFlow.first().state
        assertEquals(ModelState.FAILED, state)
    }
    
}
