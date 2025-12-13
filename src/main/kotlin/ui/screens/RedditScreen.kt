package ui.screens

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import ui.components.FetchedSubredditsDialog
import ui.components.FullScreenImage
import vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedditScreen(
    uiState: MainViewModel.UiState,
    viewModel: MainViewModel,
    context: PlatformContext
){
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
    ){ innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ){
            TestBatch(viewModel, uiState)
        }

        FullScreenImage(
            uiState = uiState,
            viewModel = viewModel,
            context = context
        )

        if(uiState.availableSubredditsDialogShown){
            FetchedSubredditsDialog(
                viewModel = viewModel,
                uiState = uiState
            )
        }
    }
}