package com.teodordevtech.arcadiatourism.data.repository

import com.teodordevtech.arcadiatourism.data.model.AppDocument
import com.teodordevtech.arcadiatourism.data.model.MediaItem
import com.teodordevtech.arcadiatourism.data.model.Topic
import com.teodordevtech.arcadiatourism.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.storage
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID

class TopicRepository {
    companion object {
        const val STORAGE_LIMIT_BYTES: Long = 1024L * 1024L * 1024L
    }

    private val supabase = SupabaseProvider.client
    private val documentBucket = supabase.storage.from("topic-documents")
    private val mediaBucket = supabase.storage.from("topic-media")

    suspend fun getTopics(): Result<List<Topic>> {
        return try {
            val topics = supabase.from("topics")
                .select {
                    order(column = "created_at", order = Order.ASCENDING)
                }
                .decodeList<Topic>()
            Result.success(topics)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun createTopic(title: String, description: String, createdBy: String): Result<Unit> {
        return try {
            val topic = Topic(
                topicId = UUID.randomUUID().toString(),
                title = title,
                description = description,
                createdBy = createdBy,
                createdAt = System.currentTimeMillis()
            )
            supabase.from("topics").insert(topic)
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun deleteTopic(topicId: String): Result<Unit> {
        return try {
            val documents = supabase.from("documents")
                .select {
                    filter {
                        eq("topic_id", topicId)
                    }
                }
                .decodeList<AppDocument>()

            val mediaItems = supabase.from("media")
                .select {
                    filter {
                        eq("topic_id", topicId)
                    }
                }
                .decodeList<MediaItem>()

            deleteStoredFiles(
                documentBucket,
                documents.mapNotNull { document ->
                    document.storagePath.ifBlank {
                        extractStoragePath(document.fileUrl, "topic-documents")
                    }
                }
            )
            deleteStoredFiles(
                mediaBucket,
                mediaItems.mapNotNull { mediaItem ->
                    mediaItem.storagePath.ifBlank {
                        extractStoragePath(mediaItem.fileUrl, "topic-media")
                    }
                }
            )

            supabase.from("topics").delete {
                filter {
                    eq("topic_id", topicId)
                }
            }

            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun getStorageUsageBytes(): Result<Long> {
        return try {
            val documents = supabase.from("documents")
                .select()
                .decodeList<AppDocument>()
            val mediaItems = supabase.from("media")
                .select()
                .decodeList<MediaItem>()

            Result.success(
                documents.sumOf { it.fileSizeBytes } + mediaItems.sumOf { it.fileSizeBytes }
            )
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private suspend fun deleteStoredFiles(bucket: BucketApi, paths: List<String>) {
        if (paths.isEmpty()) return

        paths.distinct().forEach { path ->
            runCatching {
                bucket.delete(path)
            }
        }
    }

    private fun extractStoragePath(fileUrl: String, bucketName: String): String? {
        val marker = "/object/public/$bucketName/"
        val index = fileUrl.indexOf(marker)
        if (index < 0) return null

        val encodedPath = fileUrl.substring(index + marker.length)
        return URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.name())
    }
}
