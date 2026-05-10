package com.teodordevtech.arcadiatourism

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.teodordevtech.arcadiatourism.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.handleDeeplinks
import com.teodordevtech.arcadiatourism.ui.ArcadiaTourismApp
import com.teodordevtech.arcadiatourism.ui.theme.ArcadiaTourismTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SupabaseProvider.client.handleDeeplinks(intent)
        enableEdgeToEdge()
        setContent {
            ArcadiaTourismTheme {
                ArcadiaTourismApp()
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        SupabaseProvider.client.handleDeeplinks(intent)
    }
}
