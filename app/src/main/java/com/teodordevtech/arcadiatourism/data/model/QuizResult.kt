package com.teodordevtech.arcadiatourism.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class QuizResult(
    @SerialName("result_id")
    val resultId: String = "",
    @SerialName("student_id")
    val studentId: String = "",
    @SerialName("quiz_id")
    val quizId: String = "",
    @SerialName("score")
    val score: Int = 0,
    @SerialName("total_questions")
    val totalQuestions: Int = 0,
    @SerialName("submitted_at")
    val submittedAt: Long = 0L
)
