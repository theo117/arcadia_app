package com.teodordevtech.arcadiatourism.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class QuizQuestion(
    @SerialName("question_id")
    val questionId: String = "",
    @SerialName("quiz_id")
    val quizId: String = "",
    @SerialName("question_text")
    val questionText: String = "",
    @SerialName("option_a")
    val optionA: String = "",
    @SerialName("option_b")
    val optionB: String = "",
    @SerialName("option_c")
    val optionC: String = "",
    @SerialName("option_d")
    val optionD: String = "",
    @SerialName("correct_answer")
    val correctAnswer: String = "",
    @SerialName("question_order")
    val questionOrder: Int = 0
)
