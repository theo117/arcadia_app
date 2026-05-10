package com.teodordevtech.arcadiatourism.ui.screens.quiz

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teodordevtech.arcadiatourism.ui.viewmodel.QuizUiState

@Composable
fun AddQuestionToQuizScreen(
    quizId: String,
    uiState: QuizUiState,
    onLoadQuestions: () -> Unit,
    onQuestionTextChange: (String) -> Unit,
    onOptionAChange: (String) -> Unit,
    onOptionBChange: (String) -> Unit,
    onOptionCChange: (String) -> Unit,
    onOptionDChange: (String) -> Unit,
    onCorrectAnswerChange: (String) -> Unit,
    onAddQuestionClick: () -> Unit,
    onBackClick: () -> Unit
) {
    LaunchedEffect(quizId) {
        onLoadQuestions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Quiz Builder", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = uiState.questionTextInput,
            onValueChange = onQuestionTextChange,
            label = { Text("Question text") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.optionAInput,
            onValueChange = onOptionAChange,
            label = { Text("Option A") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.optionBInput,
            onValueChange = onOptionBChange,
            label = { Text("Option B") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.optionCInput,
            onValueChange = onOptionCChange,
            label = { Text("Option C") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.optionDInput,
            onValueChange = onOptionDChange,
            label = { Text("Option D") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.correctAnswerInput,
            onValueChange = onCorrectAnswerChange,
            label = { Text("Correct answer: A, B, C or D") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onAddQuestionClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Add Question")
        }

        Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text("Done")
        }

        uiState.message?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.primary)
        }

        LazyColumn(
            modifier = Modifier.weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.questions) { question ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${question.questionOrder}. ${question.questionText}",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(text = "Correct Answer: ${question.correctAnswer}")
                    }
                }
            }

            if (uiState.results.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Student Grades",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(uiState.results) { result ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = result.studentName.ifBlank { result.studentEmail.ifBlank { result.studentId } },
                            fontWeight = FontWeight.SemiBold
                        )
                        if (result.studentEmail.isNotBlank()) {
                            Text(text = result.studentEmail)
                        }
                        Text(text = "Score: ${result.score} / ${result.totalQuestions}")
                        Text(text = "Grade: ${result.percentage}%")
                    }
                }
            }
        }
    }
}
