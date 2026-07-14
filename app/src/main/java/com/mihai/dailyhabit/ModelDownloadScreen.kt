package com.mihai.dailyhabit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModelDownloadViewModel @Inject constructor(
    private val stateRepository: ModelStateRepository,
    private val storageManager: ModelStorageManager,
    private val integrityVerifier: ModelIntegrityVerifier,
    private val context: android.app.Application
) : ViewModel() {

    val modelMetadata = stateRepository.metadataFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ModelMetadata()
    )

    fun startDownload() {
        if (!storageManager.hasEnoughSpace(ModelManifest.EXACT_SIZE_BYTES + 1024L * 1024L * 1024L)) {
            viewModelScope.launch {
                stateRepository.updateState(ModelState.INSUFFICIENT_STORAGE, "Spazio insufficiente (richiesto Modello + 1GB)")
            }
            return
        }
        ModelDownloadJobService.startDownload(context)
    }

    fun stopDownload() {
        ModelDownloadJobService.stopDownload(context)
        viewModelScope.launch {
            stateRepository.updateState(ModelState.PAUSED, "In pausa dall'utente")
        }
    }

    fun importModel(uri: Uri) {
        viewModelScope.launch {
            storageManager.importModelFromUri(uri)
        }
    }

    fun deleteModel() {
        viewModelScope.launch {
            if (ModelDownloadJobService.downloadMutex.tryLock()) {
                try {
                    ModelManifest.getModelFile(context).delete()
                    ModelManifest.getModelPartFile(context).delete()
                    stateRepository.updateState(ModelState.NOT_INSTALLED)
                } finally {
                    ModelDownloadJobService.downloadMutex.unlock()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelDownloadScreen(
    viewModel: ModelDownloadViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val metadata by viewModel.modelMetadata.collectAsState()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importModel(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestione Modello LLM") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Modello: ${ModelManifest.MODEL_ID}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Dimensione: ${ModelManifest.EXACT_SIZE_BYTES / 1024 / 1024} MB",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Stato: ${metadata.state.name}",
                style = MaterialTheme.typography.titleLarge,
                color = when (metadata.state) {
                    ModelState.READY -> MaterialTheme.colorScheme.primary
                    ModelState.FAILED, ModelState.CORRUPTED, ModelState.INSUFFICIENT_STORAGE -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            if (metadata.lastError != null) {
                Text(
                    text = "Errore: ${metadata.lastError}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (metadata.state == ModelState.DOWNLOADING || metadata.state == ModelState.VERIFYING) {
                val progress = if (ModelManifest.EXACT_SIZE_BYTES > 0) {
                    metadata.downloadedBytes.toFloat() / ModelManifest.EXACT_SIZE_BYTES.toFloat()
                } else 0f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("${metadata.downloadedBytes / 1024 / 1024} MB / ${ModelManifest.EXACT_SIZE_BYTES / 1024 / 1024} MB")

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { viewModel.stopDownload() }) {
                    Text("Pausa Download")
                }
            } else if (metadata.state != ModelState.READY) {
                Button(onClick = { viewModel.startDownload() }) {
                    Text(if (metadata.state == ModelState.PAUSED) "Riprendi Download" else "Scarica Modello")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { filePickerLauncher.launch(arrayOf("*/*")) }) {
                    Text("Importa file .litertlm")
                }
            } else {
                Button(onClick = { viewModel.deleteModel() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Delete, contentDescription = "Elimina")
                    Spacer(Modifier.width(8.dp))
                    Text("Elimina Modello")
                }
            }
        }
    }
}
