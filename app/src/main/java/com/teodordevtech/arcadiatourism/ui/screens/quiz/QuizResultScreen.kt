package com.teodordevtech.arcadiatourism.ui.screens.quiz

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

@Composable
fun QuizResultScreen(
    quizTitle: String,
    score: Int,
    total: Int,
    onBackToQuizzes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Quiz Result",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(text = quizTitle)
        Text(text = "Score: $score / $total", style = MaterialTheme.typography.headlineSmall)

        Button(
            onClick = onBackToQuizzes,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back To Quizzes")
        }
    }
}
