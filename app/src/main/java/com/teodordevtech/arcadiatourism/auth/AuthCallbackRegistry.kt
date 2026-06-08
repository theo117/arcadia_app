package com.teodordevtech.arcadiatourism.auth

import android.net.Uri
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthCallbackEvent(
    val id: Long,
    val uri: Uri
)

object AuthCallbackRegistry {
    private val _event = MutableStateFlow<AuthCallbackEvent?>(null)
    val event: StateFlow<AuthCallbackEvent?> = _event.asStateFlow()

    fun publish(uri: Uri) {
        _event.value = AuthCallbackEvent(
            id = System.nanoTime(),
            uri = uri
        )
    }

    fun clear(eventId: Long) {
        if (_event.value?.id == eventId) {
            _event.value = null
        }
    }
}

fun AuthCallbackEvent.isSignUpConfirmation(): Boolean {
    return uri.authCallbackParameter("type").equals("signup", ignoreCase = true)
}

fun AuthCallbackEvent.errorDescription(): String? {
    return uri.authCallbackParameter("error_description")
        ?: uri.authCallbackParameter("error")
}

private fun Uri.authCallbackParameter(name: String): String? {
    val values = linkedMapOf<String, String>()
    listOf(encodedQuery, encodedFragment).forEach { encodedPart ->
        encodedPart
            ?.split("&")
            ?.filter { it.isNotBlank() }
            ?.forEach { pair ->
                val separatorIndex = pair.indexOf('=')
                val encodedKey = if (separatorIndex >= 0) {
                    pair.substring(0, separatorIndex)
                } else {
                    pair
                }
                val encodedValue = if (separatorIndex >= 0) {
                    pair.substring(separatorIndex + 1)
                } else {
                    ""
                }
                values[decode(encodedKey)] = decode(encodedValue)
            }
    }
    return values[name]
}

private fun decode(value: String): String {
    return URLDecoder.decode(value, StandardCharsets.UTF_8.name())
}
