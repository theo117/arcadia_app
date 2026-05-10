package com.teodordevtech.arcadiatourism.ui.screens.quiz

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
import com.teodordevtech.arcadiatourism.ui.viewmodel.QuizUiState

@Composable
fun QuizListScreen(
    topicId: String,
    role: String,
    uiState: QuizUiState,
    onLoadQuizzes: () -> Unit,
    onCreateQuizClick: () -> Unit,
    onQuizClick: (String, String) -> Unit,
    onDeleteQuizClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val isTeacher = role.isTeacherRole()

    LaunchedEffect(topicId) {
        onLoadQuizzes()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isTeacher) "Quiz Management" else "Topic Quizzes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Button(onClick = onBackClick) {
            Text("Back")
        }

        if (isTeacher) {
            Button(
                onClick = onCreateQuizClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Quiz")
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.message?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.primary)
        }

        LazyColumn(
            modifier = Modifier.weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.quizzes) { quiz ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onQuizClick(quiz.quizId, quiz.title) },
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = quiz.title, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (isTeacher) {
                                    "Open to add or review questions"
                                } else {
                                    "Open to start quiz"
                                }
                            )
                        }

                        if (isTeacher) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onDeleteQuizClick(quiz.quizId) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Delete Quiz")
                            }
                        }
                    }
                }
            }
        }
    }
}
