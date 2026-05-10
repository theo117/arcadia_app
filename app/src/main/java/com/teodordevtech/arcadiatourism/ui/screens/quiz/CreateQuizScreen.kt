package com.teodordevtech.arcadiatourism.ui.screens.quiz

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
import com.teodordevtech.arcadiatourism.ui.viewmodel.QuizUiState

@Composable
fun CreateQuizScreen(
    topicId: String,
    uiState: QuizUiState,
    onTitleChange: (String) -> Unit,
    onCreateQuizClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Create Quiz", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Topic ID: $topicId")

        OutlinedTextField(
            value = uiState.quizTitleInput,
            onValueChange = onTitleChange,
            label = { Text("Quiz title") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onCreateQuizClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Save Quiz")
        }

        Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }

        uiState.message?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.primary)
        }
    }
}
