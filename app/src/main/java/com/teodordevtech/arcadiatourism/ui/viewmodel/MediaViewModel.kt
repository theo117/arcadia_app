package com.teodordevtech.arcadiatourism.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teodordevtech.arcadiatourism.data.model.MediaItem
import com.teodordevtech.arcadiatourism.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MediaUiState(
    val mediaItems: List<MediaItem> = emptyList(),
    val selectedMediaItem: MediaItem? = null,
    val isLoading: Boolean = false,
    val titleInput: String = "",
    val descriptionInput: String = "",
    val selectedFileUri: Uri? = null,
    val selectedMediaType: String = "video",
    val message: String? = null
)

class MediaViewModel(
    application: Application,
    private val mediaRepository: MediaRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MediaUiState())
    val uiState: StateFlow<MediaUiState> = _uiState.asStateFlow()

    fun updateTitle(value: String) {
        _uiState.update { it.copy(titleInput = value) }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(descriptionInput = value) }
    }

    fun updateMediaType(value: String) {
        _uiState.update { current ->
            current.copy(selectedMediaType = value, selectedFileUri = null)
        }
    }

    fun updateSelectedFile(fileUri: Uri?) {
        _uiState.update { it.copy(selectedFileUri = fileUri) }
    }

    fun loadMedia(topicId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = mediaRepository.getMediaForTopic(topicId)
            result.onSuccess { items ->
                _uiState.update { current ->
                    val selectedMediaItem = current.selectedMediaItem?.let { selected ->
                        items.firstOrNull { it.mediaId == selected.mediaId } ?: selected
                    }
                    current.copy(
                        isLoading = false,
                        mediaItems = items,
                        selectedMediaItem = selectedMediaItem
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, message = error.message ?: "Could not load media.")
                }
            }
        }
    }

    fun loadMediaItem(mediaId: String) {
        viewModelScope.launch {
            val existingItem = uiState.value.mediaItems.firstOrNull { it.mediaId == mediaId }
            if (existingItem != null) {
                _uiState.update { it.copy(selectedMediaItem = existingItem, message = null) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, message = null, selectedMediaItem = null) }
            val result = mediaRepository.getMediaById(mediaId)
            result.onSuccess { item ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedMediaItem = item,
                        message = if (item == null) "Media item not found." else null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, message = error.message ?: "Could not load media item.")
                }
            }
        }
    }

    fun uploadMedia(topicId: String, uploadedBy: String, onSuccess: () -> Unit = {}) {
        val state = uiState.value
        val fileUri = state.selectedFileUri
        if (state.titleInput.isBlank() || state.descriptionInput.isBlank() || fileUri == null) {
            _uiState.update {
                it.copy(message = "Complete all media fields and choose an audio or video file.")
            }
            return
        }

        val contentResolver = getApplication<Application>().contentResolver

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }

            val fileBytes = contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
            if (fileBytes == null) {
                _uiState.update {
                    it.copy(isLoading = false, message = "Could not read the selected media file.")
                }
                return@launch
            }

            val fileName = contentResolver.query(
                fileUri,
                arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
            } ?: fileUri.lastPathSegment.orEmpty()

            val result = mediaRepository.uploadMedia(
                topicId = topicId,
                title = state.titleInput.trim(),
                description = state.descriptionInput.trim(),
                fileName = fileName,
                fileBytes = fileBytes,
                selectedMediaType = state.selectedMediaType,
                uploadedBy = uploadedBy
            )
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        titleInput = "",
                        descriptionInput = "",
                        selectedFileUri = null,
                        message = "Media uploaded."
                    )
                }
                loadMedia(topicId)
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, message = error.message ?: "Could not upload media.")
                }
            }
        }
    }

    fun deleteMedia(topicId: String, mediaItem: MediaItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = mediaRepository.deleteMedia(mediaItem)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, message = "Media deleted.") }
                loadMedia(topicId)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, message = error.message ?: "Could not delete media.")
                }
            }
        }
    }
}

class MediaViewModelFactory(
    private val application: Application,
    private val mediaRepository: MediaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MediaViewModel(application, mediaRepository) as T
    }
}
