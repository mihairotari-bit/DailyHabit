package com.mihai.android17helloworld

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

private val documentMimeTypes = arrayOf(
    "text/plain",
    "application/pdf",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
)

@Composable
fun HelloWorldApp(activity: Activity, viewModel: HelloViewModel) = HelloTheme {
    val state by viewModel.readerState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let(viewModel::openDocument)
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHost.showSnackbar(it); viewModel.consumeError() }
    }
    val isLarge = LocalConfiguration.current.screenWidthDp >= 600
    Scaffold(snackbarHost = { SnackbarHost(snackbarHost) }) { padding ->
        if (isLarge) {
            Row(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                ReaderToolbar(Modifier.widthIn(max = 300.dp), onOpen = { picker.launch(documentMimeTypes) })
                ReaderContent(Modifier.weight(1f), state)
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ReaderToolbar(Modifier.fillMaxWidth(), onOpen = { picker.launch(documentMimeTypes) })
                ReaderContent(Modifier.weight(1f), state)
            }
        }
    }
}

@Composable private fun ReaderToolbar(modifier: Modifier, onOpen: () -> Unit) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Rounded.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Lettore documenti", style = MaterialTheme.typography.headlineSmall)
            Text("Apri TXT, PDF o DOCX con il selettore file Android.", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onOpen) {
                Icon(Icons.Rounded.FolderOpen, contentDescription = null)
                Text("Apri file", Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable private fun ReaderContent(modifier: Modifier, state: ReaderUiState) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .92f))) {
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.document is DocumentContent.Text -> TextDocument(Modifier.fillMaxSize(), state.fileName, state.document.value)
            state.document is DocumentContent.Pdf -> PdfDocument(Modifier.fillMaxSize(), state.fileName, state.document.pages)
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Seleziona un documento per iniziare.", style = MaterialTheme.typography.bodyLarge) }
        }
    }
}

@Composable private fun TextDocument(modifier: Modifier, fileName: String?, text: String) {
    Column(modifier.padding(24.dp)) {
        Text(fileName ?: "Documento", style = MaterialTheme.typography.titleLarge)
        SelectionContainer { Text(text, Modifier.padding(top = 16.dp).verticalScroll(rememberScrollState()), style = MaterialTheme.typography.bodyLarge) }
    }
}

@Composable private fun PdfDocument(modifier: Modifier, fileName: String?, pages: List<android.graphics.Bitmap>) {
    LazyColumn(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text(fileName ?: "PDF", style = MaterialTheme.typography.titleLarge) }
        items(pages) { page -> Image(page.asImageBitmap(), contentDescription = "Pagina PDF", modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth) }
    }
}
