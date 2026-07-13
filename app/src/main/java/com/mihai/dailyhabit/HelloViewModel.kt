package com.mihai.dailyhabit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReaderUiState(
    val isLoading: Boolean = false,
    val document: DocumentContent? = null,
    val fileName: String? = null,
    val error: String? = null,
)

@HiltViewModel
class HelloViewModel @Inject constructor(
    private val repository: HelloRepository,
    private val documentReader: DocumentReader,
) : ViewModel() {
    private val _greeting = MutableStateFlow(repository.greeting())
    val greeting = _greeting.asStateFlow()
    private val _readerState = MutableStateFlow(ReaderUiState())
    val readerState = _readerState.asStateFlow()

    fun openDocument(uri: Uri) {
        documentReader.persistReadPermission(uri)
        _readerState.value = ReaderUiState(isLoading = true, fileName = uri.lastPathSegment)
        viewModelScope.launch {
            runCatching { documentReader.read(uri) }
                .onSuccess { document -> _readerState.value = ReaderUiState(document = document, fileName = uri.lastPathSegment) }
                .onFailure { throwable -> _readerState.value = ReaderUiState(error = throwable.message ?: "Impossibile leggere il documento.") }
        }
    }

    fun consumeError() = _readerState.update { it.copy(error = null) }
}
