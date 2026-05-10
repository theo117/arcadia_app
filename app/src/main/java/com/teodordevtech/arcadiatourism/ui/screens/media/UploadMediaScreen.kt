package com.teodordevtech.arcadiatourism.ui.screens.media

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teodordevtech.arcadiatourism.ui.viewmodel.MediaUiState

@Composable
fun UploadMediaScreen(
    topicId: String,
    uiState: MediaUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onMediaTypeChange: (String) -> Unit,
    onFileSelected: (Uri?) -> Unit,
    onUploadClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = onFileSelected
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Add Media Link", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = uiState.titleInput,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.descriptionInput,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { onMediaTypeChange("video") },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (uiState.selectedMediaType == "video") "Video Selected" else "Video")
            }
            Button(
                onClick = { onMediaTypeChange("audio") },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (uiState.selectedMediaType == "audio") "Audio Selected" else "Audio")
            }
        }

        Button(
            onClick = {
                pickerLauncher.launch(
                    if (uiState.selectedMediaType == "audio") "audio/*" else "video/*"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.selectedFileUri == null) "Choose Media File" else "Media File Selected")
        }

        Button(
            onClick = onUploadClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.selectedFileUri != null &&
                uiState.titleInput.isNotBlank() &&
                uiState.descriptionInput.isNotBlank() &&
                !uiState.isLoading
        ) {
            Text(if (uiState.isLoading) "Uploading..." else "Upload Media")
        }

        Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }

        uiState.message?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.primary)
        }
    }
}
