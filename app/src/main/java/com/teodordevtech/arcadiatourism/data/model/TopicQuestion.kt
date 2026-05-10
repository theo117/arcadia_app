package com.teodordevtech.arcadiatourism.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class TopicQuestion(
    @SerialName("question_id")
    val questionId: String = "",
    @SerialName("topic_id")
    val topicId: String = "",
    @SerialName("student_id")
    val studentId: String = "",
    @SerialName("question_text")
    val questionText: String = "",
    @SerialName("status")
    val status: String = "unanswered",
    @SerialName("created_at")
    val createdAt: Long = 0L
)
