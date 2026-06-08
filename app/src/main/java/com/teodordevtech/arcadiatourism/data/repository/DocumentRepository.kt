package com.teodordevtech.arcadiatourism.data.repository

import com.teodordevtech.arcadiatourism.data.model.AppDocument
import com.teodordevtech.arcadiatourism.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import java.util.Locale
import java.util.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class DocumentRepository {
    private val supabase = SupabaseProvider.client
    private val bucket = supabase.storage.from("topic-documents")

    private fun detectFileType(fileName: String): String {
        val path = fileName.lowercase(Locale.ROOT)
        return when {
            path.endsWith(".pdf") -> "pdf"
            path.endsWith(".docx") -> "docx"
            path.endsWith(".doc") -> "doc"
            else -> "document"
        }
    }

    suspend fun getDocumentsForTopic(topicId: String): Result<List<AppDocument>> {
        return try {
            Result.success(
                selectDocuments {
                    filter {
                        eq("topic_id", topicId)
                    }
                    order(column = "uploaded_at", order = Order.DESCENDING)
                }
            )
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun uploadDocument(
        topicId: String,
        title: String,
        fileName: String,
        fileBytes: ByteArray,
        uploadedBy: String
    ): Result<Unit> {
        return try {
            val documentId = UUID.randomUUID().toString()
            val safeFileName = fileName.substringAfterLast('/').ifBlank { "$documentId.bin" }
            val storagePath = "$topicId/$documentId-$safeFileName"

            bucket.upload(storagePath, fileBytes) {
                upsert = false
            }
            try {
                val publicUrl = bucket.publicUrl(storagePath)

                val document = AppDocument(
                    documentId = documentId,
                    topicId = topicId,
                    title = title,
                    fileUrl = publicUrl,
                    storagePath = storagePath,
                    fileSizeBytes = fileBytes.size.toLong(),
                    fileType = detectFileType(safeFileName),
                    uploadedBy = uploadedBy,
                    uploadedAt = System.currentTimeMillis()
                )

                insertDocument(document)
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

    private suspend fun selectDocuments(
        request: io.github.jan.supabase.postgrest.query.request.SelectRequestBuilder.() -> Unit
    ): List<AppDocument> {
        return try {
            supabase.from("documents")
                .select(request = request)
                .decodeList<AppDocument>()
        } catch (error: Exception) {
            if (!isMissingStorageMetadataColumn(error)) throw error
            supabase.from("documents")
                .select(columns = legacyDocumentColumns, request = request)
                .decodeList<AppDocument>()
                .map { it.withDerivedStoragePath() }
        }
    }

    private suspend fun insertDocument(document: AppDocument) {
        try {
            supabase.from("documents").insert(document)
        } catch (error: Exception) {
            if (!isMissingStorageMetadataColumn(error)) throw error
            supabase.from("documents").insert(
                LegacyDocumentInsert(
                    documentId = document.documentId,
                    topicId = document.topicId,
                    title = document.title,
                    fileUrl = document.fileUrl,
                    fileType = document.fileType,
                    uploadedBy = document.uploadedBy,
                    uploadedAt = document.uploadedAt
                )
            )
        }
    }

    @Serializable
    private data class LegacyDocumentInsert(
        @SerialName("document_id")
        val documentId: String,
        @SerialName("topic_id")
        val topicId: String,
        @SerialName("title")
        val title: String,
        @SerialName("file_url")
        val fileUrl: String,
        @SerialName("file_type")
        val fileType: String,
        @SerialName("uploaded_by")
        val uploadedBy: String,
        @SerialName("uploaded_at")
        val uploadedAt: Long
    )
}
