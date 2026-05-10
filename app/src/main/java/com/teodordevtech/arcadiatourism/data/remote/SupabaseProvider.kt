package com.teodordevtech.arcadiatourism.data.remote

import com.teodordevtech.arcadiatourism.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.ExternalAuthAction
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import java.net.URI

object SupabaseProvider {
    const val AUTH_SCHEME = "arcadia"
    const val AUTH_HOST = "login-callback"
    const val AUTH_REDIRECT_URL = "$AUTH_SCHEME://$AUTH_HOST"

    val client: SupabaseClient by lazy {
        val supabaseUrl = BuildConfig.SUPABASE_URL
        val supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY

        require(supabaseUrl.isNotBlank()) {
            "SUPABASE_URL is missing. Add it to local.properties."
        }
        require(supabaseAnonKey.isNotBlank()) {
            "SUPABASE_ANON_KEY is missing. Add it to local.properties."
        }
        val supabaseHost = runCatching { URI(supabaseUrl).host }.getOrNull()
        require(!supabaseHost.isNullOrBlank()) {
            "SUPABASE_URL is invalid. Expected a URL like https://your-project-ref.supabase.co."
        }

        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseAnonKey
        ) {
            install(Auth) {
                scheme = AUTH_SCHEME
                host = AUTH_HOST
                defaultExternalAuthAction = ExternalAuthAction.CustomTabs()
            }
            install(Postgrest)
            install(Storage)
        }
    }
}
