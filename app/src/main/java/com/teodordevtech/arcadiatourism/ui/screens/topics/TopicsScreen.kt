package com.teodordevtech.arcadiatourism.ui.screens.topics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teodordevtech.arcadiatourism.data.model.isTeacherRole
import com.teodordevtech.arcadiatourism.ui.viewmodel.TopicUiState

@Composable
fun TopicsScreen(
    role: String,
    uiState: TopicUiState,
    onLoadTopics: () -> Unit,
    onTopicClick: (String) -> Unit,
    onDeleteTopic: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val isTeacher = role.isTeacherRole()

    LaunchedEffect(Unit) {
        onLoadTopics()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isTeacher) "Manage Topics" else "Tourism Topics",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Button(onClick = onBackClick) {
            Text("Back")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.topics) { topic ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTopicClick(topic.topicId) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = topic.title, fontWeight = FontWeight.SemiBold)
                        Text(text = topic.description)
                        if (isTeacher) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onDeleteTopic(topic.topicId) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Delete Topic")
                            }
                        }
                    }
                }
            }
        }
    }
}
