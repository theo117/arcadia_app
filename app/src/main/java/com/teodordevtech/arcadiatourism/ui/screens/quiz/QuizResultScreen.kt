package com.teodordevtech.arcadiatourism.ui.screens.quiz

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun QuizResultScreen(
    quizTitle: String,
    score: Int,
    totalQuestions: Int,
    onBackToQuizzes: () -> Unit
) {
    val context = LocalContext.current
    val percentage = if (totalQuestions > 0) (score.toFloat() / totalQuestions * 100).toInt() else 0

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
        Text(
            text = quizTitle,
            style = MaterialTheme.typography.titleLarge
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Score",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "$score / $totalQuestions",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Text(
            text = "Well done!",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Your answers have been submitted. Your teacher can view your detailed results from their account.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "My Quiz Result: $quizTitle")
                    putExtra(Intent.EXTRA_TEXT, "I just completed the quiz '$quizTitle' and scored $score out of $totalQuestions ($percentage%)!")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share your result"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share/Email Result")
        }

        Button(
            onClick = onBackToQuizzes,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back To Quizzes")
        }
    }
}
