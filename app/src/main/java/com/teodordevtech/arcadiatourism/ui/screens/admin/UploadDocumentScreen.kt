package com.teodordevtech.arcadiatourism.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.teodordevtech.arcadiatourism.ui.viewmodel.DocumentUiState

@Composable
fun UploadDocumentScreen(
    topicId: String,
    uiState: DocumentUiState,
    onTitleChange: (String) -> Unit,
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
        Text(text = "Upload Document", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = uiState.titleInput,
            onValueChange = onTitleChange,
            label = { Text("Document title") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { pickerLauncher.launch("*/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.selectedFileUri == null) "Choose Document" else "Document Selected")
        }

        Button(
            onClick = onUploadClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.selectedFileUri != null && uiState.titleInput.isNotBlank() && !uiState.isLoading
        ) {
            Text("Upload")
        }

        Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }

        uiState.message?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.primary)
        }
    }
}
