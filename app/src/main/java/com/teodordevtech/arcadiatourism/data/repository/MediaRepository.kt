package com.teodordevtech.arcadiatourism.data.repository

import com.teodordevtech.arcadiatourism.data.model.MediaItem
import com.teodordevtech.arcadiatourism.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import java.util.Locale
import java.util.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class MediaRepository {
    private val supabase = SupabaseProvider.client
    private val bucket = supabase.storage.from("topic-media")

    private fun detectMediaType(fileName: String, fallbackType: String): String {
        val normalizedName = fileName.lowercase(Locale.ROOT)
        return when {
            normalizedName.endsWith(".mp3") ||
                normalizedName.endsWith(".wav") ||
                normalizedName.endsWith(".m4a") ||
                normalizedName.endsWith(".aac") ||
                normalizedName.endsWith(".ogg") -> "audio"
            normalizedName.endsWith(".mp4") ||
                normalizedName.endsWith(".webm") ||
                normalizedName.endsWith(".mkv") ||
                normalizedName.endsWith(".mov") -> "video"
            else -> fallbackType
        }
    }

    suspend fun getMediaForTopic(topicId: String): Result<List<MediaItem>> {
        return try {
            Result.success(selectMediaList {
                filter {
                    eq("topic_id", topicId)
                }
                order(column = "uploaded_at", order = Order.DESCENDING)
            })
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun getMediaById(mediaId: String): Result<MediaItem?> {
        return try {
            Result.success(selectMediaItem {
                filter {
                    eq("media_id", mediaId)
                }
            })
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun uploadMedia(
        topicId: String,
        title: String,
        description: String,
        fileName: String,
        fileBytes: ByteArray,
        selectedMediaType: String,
        uploadedBy: String
    ): Result<Unit> {
        return try {
            val mediaId = UUID.randomUUID().toString()
            val safeFileName = fileName.substringAfterLast('/').ifBlank { "$mediaId.bin" }
            val storagePath = "$topicId/$mediaId-$safeFileName"
            val mediaType = detectMediaType(safeFileName, selectedMediaType)

            bucket.upload(storagePath, fileBytes) {
                upsert = false
            }
            try {
                val publicUrl = bucket.publicUrl(storagePath)

                val mediaItem = MediaItem(
                    mediaId = mediaId,
                    topicId = topicId,
                    title = title,
                    description = description,
                    mediaType = mediaType,
                    fileUrl = publicUrl,
                    storagePath = storagePath,
                    fileSizeBytes = fileBytes.size.toLong(),
                    uploadedBy = uploadedBy,
                    uploadedAt = System.currentTimeMillis()
                )

                insertMediaItem(mediaItem)
            } catch (error: Exception) {
                runCatching {
                    bucket.delete(storagePath)
                }
                throw error
            }

            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun deleteMedia(mediaItem: MediaItem): Result<Unit> {
        return try {
            mediaItem.withDerivedStoragePath().storagePath.ifBlank { null }?.let { storagePath ->
                bucket.delete(storagePath)
            }

            supabase.from("media").delete {
                filter {
                    eq("media_id", mediaItem.mediaId)
                }
            }

            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private suspend fun selectMediaList(
        request: io.github.jan.supabase.postgrest.query.request.SelectRequestBuilder.() -> Unit
    ): List<MediaItem> {
        return try {
            supabase.from("media")
                .select(request = request)
                .decodeList<MediaItem>()
        } catch (error: Exception) {
            if (!isMissingStorageMetadataColumn(error)) throw error
            supabase.from("media")
                .select(columns = legacyMediaColumns, request = request)
                .decodeList<MediaItem>()
                .map { it.withDerivedStoragePath() }
        }
    }

    private suspend fun selectMediaItem(
        request: io.github.jan.supabase.postgrest.query.request.SelectRequestBuilder.() -> Unit
    ): MediaItem? {
        return try {
            supabase.from("media")
                .select(request = request)
                .decodeSingleOrNull<MediaItem>()
        } catch (error: Exception) {
            if (!isMissingStorageMetadataColumn(error)) throw error
            supabase.from("media")
                .select(columns = legacyMediaColumns, request = request)
                .decodeSingleOrNull<MediaItem>()
                ?.withDerivedStoragePath()
        }
    }

    private suspend fun insertMediaItem(mediaItem: MediaItem) {
        try {
            supabase.from("media").insert(mediaItem)
        } catch (error: Exception) {
            if (!isMissingStorageMetadataColumn(error)) throw error
            supabase.from("media").insert(
                LegacyMediaInsert(
                    mediaId = mediaItem.mediaId,
                    topicId = mediaItem.topicId,
                    title = mediaItem.title,
                    description = mediaItem.description,
                    mediaType = mediaItem.mediaType,
                    fileUrl = mediaItem.fileUrl,
                    uploadedBy = mediaItem.uploadedBy,
                    uploadedAt = mediaItem.uploadedAt
                )
            )
        }
    }

    @Serializable
    private data class LegacyMediaInsert(
        @SerialName("media_id")
        val mediaId: String,
        @SerialName("topic_id")
        val topicId: String,
        @SerialName("title")
        val title: String,
        @SerialName("description")
        val description: String,
        @SerialName("media_type")
        val mediaType: String,
        @SerialName("file_url")
        val fileUrl: String,
        @SerialName("uploaded_by")
        val uploadedBy: String,
        @SerialName("uploaded_at")
        val uploadedAt: Long
    )
}
