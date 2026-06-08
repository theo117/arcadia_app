package com.teodordevtech.arcadiatourism.data.repository

import com.teodordevtech.arcadiatourism.data.model.AppDocument
import com.teodordevtech.arcadiatourism.data.model.MediaItem
import io.github.jan.supabase.postgrest.query.Columns
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

internal val legacyDocumentColumns: Columns = Columns.list(
    "document_id",
    "topic_id",
    "title",
    "file_url",
    "file_type",
    "uploaded_by",
    "uploaded_at"
)

internal val legacyMediaColumns: Columns = Columns.list(
    "media_id",
    "topic_id",
    "title",
    "description",
    "media_type",
    "file_url",
    "uploaded_by",
    "uploaded_at"
)

internal fun isMissingStorageMetadataColumn(error: Exception): Boolean {
    val message = error.message.orEmpty()
    val mentionsMissingColumn = message.contains("Could not find the", ignoreCase = true) ||
        message.contains("schema cache", ignoreCase = true)
    val mentionsExpectedColumns = message.contains("storage_path") ||
        message.contains("file_size_bytes")
    return mentionsMissingColumn && mentionsExpectedColumns
}

internal fun AppDocument.withDerivedStoragePath(): AppDocument {
    if (storagePath.isNotBlank()) return this
    return copy(storagePath = extractStoragePath(fileUrl, "topic-documents").orEmpty())
}

internal fun MediaItem.withDerivedStoragePath(): MediaItem {
    if (storagePath.isNotBlank()) return this
    return copy(storagePath = extractStoragePath(fileUrl, "topic-media").orEmpty())
}

private fun extractStoragePath(fileUrl: String, bucketName: String): String? {
    val marker = "/object/public/$bucketName/"
    val index = fileUrl.indexOf(marker)
    if (index < 0) return null

    val encodedPath = fileUrl.substring(index + marker.length)
    return URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.name())
}
