package testingVideoPlayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
private fun VideoPlayerScreen(
    videoUrl: String,
    isPlaying: Boolean,
    onPlayerReady: (PlayerManager) -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VlcVideoPlayer(
            url = videoUrl,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .weight(1f),
            onPlayerManagerReady = onPlayerReady
        )

        Spacer(Modifier.height(16.dp))

        PlayerControls(
            isPlaying = isPlaying,
            onPlay = onPlay,
            onPause = onPause,
            onRestart = onRestart
        )
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onRestart: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
    ) {
        Button(onClick = onRestart) {
            Text("Restart")
        }

        Spacer(Modifier.width(16.dp))

        Button(
            onClick = if (isPlaying) onPause else onPlay,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (isPlaying) {
                    MaterialTheme.colors.error
                } else {
                    MaterialTheme.colors.primary
                }
            )
        ) {
            Text(if (isPlaying) "Pause" else "Play")
        }
    }
}