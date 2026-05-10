package com.teodordevtech.arcadiatourism.ui.screens.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem as PlayerMediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.teodordevtech.arcadiatourism.data.model.MediaItem

@Composable
fun AudioPlayerScreen(
    mediaItem: MediaItem?,
    isLoading: Boolean,
    message: String?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isPlaying = remember { mutableStateOf(false) }
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

        Text(text = mediaItem?.title ?: "Audio", style = MaterialTheme.typography.headlineSmall)
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

        Button(
            onClick = {
                if (isPlaying.value) {
                    player.pause()
                } else {
                    player.play()
                }
                isPlaying.value = !isPlaying.value
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isPlaying.value) "Pause Audio" else "Play Audio")
        }

        Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
