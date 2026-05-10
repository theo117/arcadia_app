package com.teodordevtech.arcadiatourism.ui.screens.questions

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
import com.teodordevtech.arcadiatourism.ui.viewmodel.QuestionAnswerUiState

@Composable
fun AskQuestionScreen(
    topicId: String,
    uiState: QuestionAnswerUiState,
    onQuestionTextChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Ask A Question", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = uiState.askQuestionInput,
            onValueChange = onQuestionTextChange,
            label = { Text("Your question") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSubmitClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Send Question")
        }

        Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }

        uiState.message?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.primary)
        }
    }
}
