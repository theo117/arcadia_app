package com.teodordevtech.arcadiatourism.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class NotificationItem(
    @SerialName("notification_id")
    val notificationId: String = "",
    @SerialName("topic_id")
    val topicId: String = "",
    @SerialName("title")
    val title: String = "",
    @SerialName("message")
    val message: String = "",
    @SerialName("event_type")
    val eventType: String = "",
    @SerialName("target_role")
    val targetRole: String = "student",
    @SerialName("created_by")
    val createdBy: String = "",
    @SerialName("created_at")
    val createdAt: Long = 0L
)
