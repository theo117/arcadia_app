package com.teodordevtech.arcadiatourism.ui.screens.admin

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teodordevtech.arcadiatourism.ui.viewmodel.DocumentUiState

@Composable
fun UploadDocumentScreen(
    topicId: String,
    uiState: DocumentUiState,
    onLoadDocuments: () -> Unit,
    onTitleChange: (String) -> Unit,
    onFileSelected: (Uri?) -> Unit,
    onUploadClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = onFileSelected
    )

    LaunchedEffect(topicId) {
        onLoadDocuments()
    }

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

        uiState.selectedFileUri?.let { selectedUri ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Selected file ready to preview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = selectedUri.lastPathSegment ?: selectedUri.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, selectedUri).apply {
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Preview Selected File")
                    }
                }
            }
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

        Text(
            text = "Uploaded Documents",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        LazyColumn(
            modifier = Modifier.weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.documents) { document ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(document.fileUrl)))
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = document.title, fontWeight = FontWeight.SemiBold)
                        Text(text = "Type: ${document.fileType}")
                        Button(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(document.fileUrl)))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Preview Uploaded Document")
                        }
                    }
                }
            }
        }
    }
}
