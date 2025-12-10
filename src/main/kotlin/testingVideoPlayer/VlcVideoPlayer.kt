package testingVideoPlayer

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color

interface PlayerManager {
    fun play()
    fun pause()
    fun restart()
    fun isPlaying(): Boolean
}

@Composable
fun VlcVideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    onPlayerManagerReady: (PlayerManager) -> Unit
) {
    val videoPlayerService = remember(url) { VideoPlayerService() }

    val mediaPlayerComponent = remember(url) {
        videoPlayerService.createPlayer(url)
    }

    val playerManager = remember(mediaPlayerComponent, url) {
        videoPlayerService.getPlayerManager()
    }

    LaunchedEffect(playerManager) {
        onPlayerManagerReady(playerManager)
    }

    DisposableEffect(url) {
        onDispose {
            videoPlayerService.release()
        }
    }

    SwingPanel(
        background = Color.Black,
        modifier = modifier,
        factory = { mediaPlayerComponent }
    )
}