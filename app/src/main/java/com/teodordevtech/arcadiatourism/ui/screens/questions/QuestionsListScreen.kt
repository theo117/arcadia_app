package com.teodordevtech.arcadiatourism.ui.screens.questions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.teodordevtech.arcadiatourism.ui.viewmodel.QuestionAnswerUiState

@Composable
fun QuestionsListScreen(
    topicId: String,
    uiState: QuestionAnswerUiState,
    onLoadQuestions: () -> Unit,
    onBackClick: () -> Unit
) {
    LaunchedEffect(topicId) {
        onLoadQuestions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Questions And Answers", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onBackClick) {
            Text("Back")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        LazyColumn(
            modifier = Modifier.weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.questions) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = item.question.questionText, fontWeight = FontWeight.SemiBold)
                        Text(text = "Status: ${item.question.status}")
                        Text(
                            text = item.answer?.answerText ?: "No answer yet.",
                            color = if (item.answer == null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
            }
        }
    }
}
