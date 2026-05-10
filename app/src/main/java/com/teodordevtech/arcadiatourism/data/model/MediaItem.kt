package com.teodordevtech.arcadiatourism.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class MediaItem(
    @SerialName("media_id")
    val mediaId: String = "",
    @SerialName("topic_id")
    val topicId: String = "",
    @SerialName("title")
    val title: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("media_type")
    val mediaType: String = "",
    @SerialName("file_url")
    val fileUrl: String = "",
    @SerialName("storage_path")
    val storagePath: String = "",
    @SerialName("file_size_bytes")
    val fileSizeBytes: Long = 0L,
    @SerialName("uploaded_by")
    val uploadedBy: String = "",
    @SerialName("uploaded_at")
    val uploadedAt: Long = 0L
)
