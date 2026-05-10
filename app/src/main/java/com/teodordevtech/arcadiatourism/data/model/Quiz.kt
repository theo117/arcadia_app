package com.teodordevtech.arcadiatourism.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Quiz(
    @SerialName("quiz_id")
    val quizId: String = "",
    @SerialName("topic_id")
    val topicId: String = "",
    @SerialName("title")
    val title: String = "",
    @SerialName("created_by")
    val createdBy: String = "",
    @SerialName("created_at")
    val createdAt: Long = 0L
)
