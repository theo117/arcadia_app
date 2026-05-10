package com.teodordevtech.arcadiatourism.ui.screens.topics

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teodordevtech.arcadiatourism.data.model.Topic
import com.teodordevtech.arcadiatourism.ui.viewmodel.DocumentUiState

@Composable
fun TopicDetailScreen(
    role: String,
    topic: Topic?,
    documentState: DocumentUiState,
    onLoadDocuments: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onAskQuestion: () -> Unit,
    onViewQuestions: () -> Unit,
    onOpenMedia: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(topic?.topicId) {
        if (topic != null) {
            onLoadDocuments()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onBackClick) {
            Text("Back")
        }

        Text(
            text = topic?.title ?: "Topic Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(text = topic?.description ?: "No description available.")

        Button(onClick = onOpenQuizzes, modifier = Modifier.fillMaxWidth()) {
            Text("Take Quiz")
        }
        Button(onClick = onOpenMedia, modifier = Modifier.fillMaxWidth()) {
            Text("View Videos And Audio")
        }
        Button(onClick = onAskQuestion, modifier = Modifier.fillMaxWidth()) {
            Text("Ask A Question")
        }
        Button(onClick = onViewQuestions, modifier = Modifier.fillMaxWidth()) {
            Text("View Questions And Answers")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Documents",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        LazyColumn(
            modifier = Modifier.weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(documentState.documents) { document ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(document.fileUrl)))
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = document.title, fontWeight = FontWeight.SemiBold)
                        Text(text = "Type: ${document.fileType}")
                    }
                }
            }
        }
    }
}
