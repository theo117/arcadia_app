package com.teodordevtech.arcadiatourism.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Answer(
    @SerialName("answer_id")
    val answerId: String = "",
    @SerialName("question_id")
    val questionId: String = "",
    @SerialName("teacher_id")
    val teacherId: String = "",
    @SerialName("answer_text")
    val answerText: String = "",
    @SerialName("created_at")
    val createdAt: Long = 0L
)
