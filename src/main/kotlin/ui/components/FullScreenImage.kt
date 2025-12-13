package ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.painter.EquitablePainter
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.cacheDecodeTimeoutFrame
import com.github.panpf.sketch.request.disallowAnimatedImage
import com.github.panpf.sketch.request.repeatCount
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.sketch
import com.github.panpf.sketch.state.PainterStateImage
import com.github.panpf.sketch.util.Size
import myred.resources.Res
import myred.resources.appIcon
import myred.resources.error
import org.jetbrains.compose.resources.painterResource
import testingVideoPlayer.PlayerManager
import testingVideoPlayer.VlcVideoPlayer
import vm.MainViewModel

@Composable
fun FullScreenImage(
    uiState: MainViewModel.UiState,
    viewModel: MainViewModel,
    context: PlatformContext
){
    uiState.clickedImage?.let { mediaFile ->
        LaunchedEffect(mediaFile) {
            if (mediaFile.extension.lowercase() == "gif") {
                val preloadRequest = ImageRequest(context, mediaFile.absolutePath) {
                    disallowAnimatedImage(false)
                    size(Size.Origin)
                    precision(Precision.EXACTLY)
                    cacheDecodeTimeoutFrame(true)
                }
                context.sketch.execute(preloadRequest)
            }
        }

        AlertDialog(
            onDismissRequest = { viewModel.exitFullScreenImage() },
            confirmButton = {
                OutlinedButton(
                    onClick = { viewModel.exitFullScreenImage() },
                    shape = MaterialTheme.shapes.medium
                ){
                    Text(
                        text = "Close",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            shape = MaterialTheme.shapes.medium,
            text = {
                if(mediaFile.extension.lowercase() in listOf("mp4", "webm")){
                    var playerManager by remember { mutableStateOf<PlayerManager?>(null) }
                    var isPlaying by remember { mutableStateOf(false) }

                    Column{
                        VlcVideoPlayer(
                            url = mediaFile.absolutePath,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(
                                    ratio = 16f / 9f,
                                    matchHeightConstraintsFirst = true
                                )
                                .weight(1f),
                            onPlayerManagerReady = { manager ->
                                playerManager = manager
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    playerManager?.restart()
                                    isPlaying = true
                                },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = "Restart",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            OutlinedButton(
                                onClick = {
                                    playerManager?.let { manager ->
                                        if (isPlaying) {
                                            manager.pause()
                                            isPlaying = false
                                        } else {
                                            manager.play()
                                            isPlaying = true
                                        }
                                    }
                                },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = if (isPlaying) "Pause" else "Play",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }else{
                    val placehodler = painterResource(Res.drawable.appIcon)
                    val error = painterResource(Res.drawable.error)

                    AsyncImage(
                        request = ImageRequest(
                            context = context,
                            uri = mediaFile.absolutePath
                        ) {
                            disallowAnimatedImage(false)
                            repeatCount(-1)
                            size(Size.Origin)
                            precision(Precision.EXACTLY)

                            memoryCachePolicy(CachePolicy.ENABLED)
                            resultCachePolicy(CachePolicy.ENABLED)

                            placeholder(PainterStateImage(EquitablePainter(placehodler, placehodler)))
                            error(PainterStateImage(EquitablePainter(error,error)))
                        },
                        contentDescription = "photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
            }
        )
    }
}