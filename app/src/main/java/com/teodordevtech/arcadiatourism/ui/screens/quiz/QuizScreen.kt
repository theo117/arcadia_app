package com.teodordevtech.arcadiatourism.ui.screens.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teodordevtech.arcadiatourism.ui.viewmodel.QuizUiState

@Composable
fun QuizScreen(
    quizId: String,
    quizTitle: String,
    uiState: QuizUiState,
    onLoadQuestions: () -> Unit,
    onSelectAnswer: (String, String) -> Unit,
    onSubmitClick: () -> Unit,
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
        Text(text = quizTitle, style = MaterialTheme.typography.headlineSmall)
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
            itemsIndexed(uiState.questions) { index, question ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${index + 1}. ${question.questionText}",
                            fontWeight = FontWeight.SemiBold
                        )
                        listOf(
                            "A" to question.optionA,
                            "B" to question.optionB,
                            "C" to question.optionC,
                            "D" to question.optionD
                        ).forEach { (label, value) ->
                            RowOption(
                                selected = uiState.selectedAnswers[question.questionId] == label,
                                text = "$label. $value",
                                onClick = { onSelectAnswer(question.questionId, label) }
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onSubmitClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Submit Quiz")
        }
    }
}

@Composable
private fun RowOption(
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = text)
    }
}
