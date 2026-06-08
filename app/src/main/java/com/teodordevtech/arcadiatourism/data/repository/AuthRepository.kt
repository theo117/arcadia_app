package com.teodordevtech.arcadiatourism.data.repository

import com.teodordevtech.arcadiatourism.data.model.User
import com.teodordevtech.arcadiatourism.data.model.normalizedRole
import com.teodordevtech.arcadiatourism.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.URI
import java.net.UnknownHostException

class AuthRepository {
    private val supabase = SupabaseProvider.client
    private val supabaseHost = URI(SupabaseProvider.client.supabaseUrl).host.orEmpty()

    fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val uid = currentUserId().orEmpty()
            val appUser = getUserProfile(uid)
                ?: return Result.failure(
                    IllegalStateException(
                        "User profile not found in Supabase users table for uid: $uid"
                    )
                )

            Result.success(appUser.copy(role = appUser.role.normalizedRole()))
        } catch (error: Exception) {
            Result.failure(error.toReadableAuthError(supabaseHost))
        }
    }

    suspend fun getCurrentUserProfile(): User? {
        val uid = currentUserId() ?: return null
        return getUserProfile(uid)?.let { user ->
            user.copy(role = user.role.normalizedRole())
        }
    }

    suspend fun logout() {
        supabase.auth.signOut()
    }

    suspend fun signUp(
        fullName: String,
        email: String,
        password: String,
        grade: String
    ): Result<User?> {
        return try {
            supabase.auth.signUpWith(Email, redirectUrl = SupabaseProvider.emailConfirmationRedirectUrl) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", fullName)
                    put("role", "student")
                    put("grade", grade)
                }
            }

            Result.success(getCurrentUserProfile())
        } catch (error: Exception) {
            Result.failure(error.toReadableAuthError(supabaseHost))
        }
    }

    private suspend fun getUserProfile(uid: String): User? {
        return supabase.from("users")
            .select {
                filter {
                    eq("uid", uid)
                }
            }
            .decodeSingleOrNull<User>()
    }
}

private fun Throwable.toReadableAuthError(host: String): Throwable {
    val message = message.orEmpty()
    return when (this) {
        is UnknownHostException -> IllegalStateException(
            "Could not reach $host. Check your internet connection, DNS, or the SUPABASE_URL setting.",
            this
        )
        else -> when {
            message.contains("requested path is invalid", ignoreCase = true) -> IllegalStateException(
                "Supabase rejected the request path. Verify that SUPABASE_URL points to the correct project and that the auth/database setup from SUPABASE_CLASSROOM_SCHEMA.sql has been applied.",
                this
            )
            else -> this
        }
    }
}
