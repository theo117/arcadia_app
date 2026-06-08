package com.teodordevtech.arcadiatourism

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.teodordevtech.arcadiatourism.auth.AuthCallbackRegistry
import com.teodordevtech.arcadiatourism.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.handleDeeplinks
import com.teodordevtech.arcadiatourism.ui.ArcadiaTourismApp
import com.teodordevtech.arcadiatourism.ui.theme.ArcadiaTourismTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processAuthIntent(intent)
        enableEdgeToEdge()
        setContent {
            ArcadiaTourismTheme {
                ArcadiaTourismApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processAuthIntent(intent)
    }

    private fun processAuthIntent(intent: Intent) {
        val data = intent.data
        if (data?.scheme == SupabaseProvider.AUTH_SCHEME && data.host == SupabaseProvider.AUTH_HOST) {
            AuthCallbackRegistry.publish(data)
        }
        SupabaseProvider.client.handleDeeplinks(intent)
    }
}
