package com.teodordevtech.arcadiatourism.data.repository

import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.teodordevtech.arcadiatourism.data.model.NotificationItem
import com.teodordevtech.arcadiatourism.data.model.normalizedRole
import com.teodordevtech.arcadiatourism.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class NotificationRepository {
    private val supabase = SupabaseProvider.client

    suspend fun syncFcmTokenForUser(userId: String) {
        val token = suspendCancellableCoroutine<String> { continuation ->
            Firebase.messaging.token
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }

        supabase.from("users").update(
            {
                set("fcm_token", token)
            }
        ) {
            filter {
                eq("uid", userId)
            }
        }
    }

    suspend fun subscribeToRoleTopic(role: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            Firebase.messaging.subscribeToTopic("grade12_${role.normalizedRole()}")
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    suspend fun createNotification(
        topicId: String,
        title: String,
        message: String,
        eventType: String,
        createdBy: String,
        targetRole: String = "student"
    ): Result<Unit> {
        return try {
            val notification = NotificationItem(
                notificationId = UUID.randomUUID().toString(),
                topicId = topicId,
                title = title,
                message = message,
                eventType = eventType,
                targetRole = targetRole,
                createdBy = createdBy,
                createdAt = System.currentTimeMillis()
            )
            supabase.from("notifications").insert(notification)
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
