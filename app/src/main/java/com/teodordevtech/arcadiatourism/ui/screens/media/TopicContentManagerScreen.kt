package com.teodordevtech.arcadiatourism.ui.screens.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teodordevtech.arcadiatourism.data.model.Topic

@Composable
fun TopicContentManagerScreen(
    topic: Topic?,
    onManageDocuments: () -> Unit,
    onManageQuizzes: () -> Unit,
    onManageQuestions: () -> Unit,
    onUploadMedia: () -> Unit,
    onManageMedia: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Topic Content Manager",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(text = topic?.title ?: "Topic")
        Text(text = "Powered by Teodor Dev Tech")

        Button(onClick = onManageDocuments, modifier = Modifier.fillMaxWidth()) {
            Text("Upload Documents")
        }
        Button(onClick = onManageQuizzes, modifier = Modifier.fillMaxWidth()) {
            Text("Manage Quizzes")
        }
        Button(onClick = onManageQuestions, modifier = Modifier.fillMaxWidth()) {
            Text("Manage Questions")
        }
        Button(onClick = onUploadMedia, modifier = Modifier.fillMaxWidth()) {
            Text("Upload Video Or Audio")
        }
        Button(onClick = onManageMedia, modifier = Modifier.fillMaxWidth()) {
            Text("Manage Media")
        }
        Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
