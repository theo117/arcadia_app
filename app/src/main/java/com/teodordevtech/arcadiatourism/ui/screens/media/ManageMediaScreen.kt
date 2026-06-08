package com.teodordevtech.arcadiatourism.ui.screens.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teodordevtech.arcadiatourism.data.model.MediaItem
import com.teodordevtech.arcadiatourism.ui.viewmodel.MediaUiState

@Composable
fun ManageMediaScreen(
    topicId: String,
    uiState: MediaUiState,
    onLoadMedia: () -> Unit,
    onPreviewMedia: (MediaItem) -> Unit,
    onDeleteMedia: (MediaItem) -> Unit,
    onBackClick: () -> Unit
) {
    LaunchedEffect(topicId) {
        onLoadMedia()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Manage Media", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onBackClick) {
            Text("Back")
        }

        uiState.message?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.primary)
        }

        LazyColumn(
            modifier = Modifier.weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.mediaItems) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = item.title, fontWeight = FontWeight.SemiBold)
                        Text(text = item.description)
                        Text(text = "Type: ${item.mediaType}")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onPreviewMedia(item) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (item.mediaType == "audio") {
                                        "Preview Audio"
                                    } else {
                                        "Preview Video"
                                    }
                                )
                            }
                            Button(
                                onClick = { onDeleteMedia(item) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Delete Media")
                            }
                        }
                    }
                }
            }
        }
    }
}
