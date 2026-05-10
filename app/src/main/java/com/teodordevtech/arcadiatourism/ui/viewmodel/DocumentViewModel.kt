package com.teodordevtech.arcadiatourism.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teodordevtech.arcadiatourism.data.model.AppDocument
import com.teodordevtech.arcadiatourism.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DocumentUiState(
    val documents: List<AppDocument> = emptyList(),
    val isLoading: Boolean = false,
    val titleInput: String = "",
    val selectedFileUri: Uri? = null,
    val message: String? = null
)

class DocumentViewModel(
    application: Application,
    private val documentRepository: DocumentRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DocumentUiState())
    val uiState: StateFlow<DocumentUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(titleInput = title) }
    }

    fun updateSelectedFile(fileUri: Uri?) {
        _uiState.update { it.copy(selectedFileUri = fileUri) }
    }

    fun loadDocuments(topicId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }

            val result = documentRepository.getDocumentsForTopic(topicId)
            result.onSuccess { documents ->
                _uiState.update { it.copy(isLoading = false, documents = documents) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = error.message ?: "Could not load documents."
                    )
                }
            }
        }
    }

    fun uploadDocument(topicId: String, uploadedBy: String, onSuccess: () -> Unit = {}) {
        val fileUri = uiState.value.selectedFileUri ?: return
        val title = uiState.value.titleInput.trim()
        val contentResolver = getApplication<Application>().contentResolver

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }

            val fileBytes = contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
            if (fileBytes == null) {
                _uiState.update {
                    it.copy(isLoading = false, message = "Could not read the selected document.")
                }
                return@launch
            }

            val fileName = contentResolver.query(fileUri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
                }
                ?: fileUri.lastPathSegment.orEmpty()

            val result = documentRepository.uploadDocument(
                topicId = topicId,
                title = title,
                fileName = fileName,
                fileBytes = fileBytes,
                uploadedBy = uploadedBy
            )

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        titleInput = "",
                        selectedFileUri = null,
                        message = "Document uploaded."
                    )
                }
                loadDocuments(topicId)
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = error.message ?: "Upload failed."
                    )
                }
            }
        }
    }
}

class DocumentViewModelFactory(
    private val application: Application,
    private val documentRepository: DocumentRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DocumentViewModel(application, documentRepository) as T
    }
}
