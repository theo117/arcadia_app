package com.teodordevtech.arcadiatourism.ui.screens.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem as PlayerMediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.teodordevtech.arcadiatourism.data.model.MediaItem

@Composable
fun VideoPlayerScreen(
    mediaItem: MediaItem?,
    isLoading: Boolean,
    message: String?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val player = remember(mediaItem?.fileUrl) {
        ExoPlayer.Builder(context).build().apply {
            mediaItem?.fileUrl?.let { url ->
                setMediaItem(PlayerMediaItem.fromUri(url))
                prepare()
            }
        }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        }

        Text(text = mediaItem?.title ?: "Video", style = MaterialTheme.typography.headlineSmall)
        Text(text = mediaItem?.description.orEmpty())

        if (message != null) {
            Text(text = message, color = MaterialTheme.colorScheme.primary)
        }

        if (mediaItem == null) {
            Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
            return@Column
        }

        AndroidView(
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    this.player = player
                    useController = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )
        Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
