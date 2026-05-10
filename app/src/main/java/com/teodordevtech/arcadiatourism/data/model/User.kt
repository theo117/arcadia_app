package com.teodordevtech.arcadiatourism.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class User(
    @SerialName("uid")
    val uid: String = "",
    @SerialName("full_name")
    val fullName: String = "",
    @SerialName("email")
    val email: String = "",
    @SerialName("role")
    val role: String = "",
    @SerialName("grade")
    val grade: String = "",
    @SerialName("fcm_token")
    val fcmToken: String = ""
)
