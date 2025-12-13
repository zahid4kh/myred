import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.github.panpf.sketch.LocalPlatformContext
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
import theme.AppTheme
import ui.components.FetchedSubredditsDialog
import ui.components.TestBatch
import vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalPlatformContext.current
    AppTheme(darkTheme = uiState.darkMode) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        Button(
                            onClick = { viewModel.getHotPosts() }
                        ){
                            Text(
                                text = "Fetch posts"
                            )
                        }

                        Button(
                            onClick = { viewModel.showAvailableSubredditsDialog() }
                        ){
                            Text(
                                text = "Show fetched subreddits"
                            )
                        }

                    }
                )
            }
        ){innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ){
                TestBatch(viewModel, uiState)
            }

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

            if(uiState.availableSubredditsDialogShown){
                FetchedSubredditsDialog(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}
