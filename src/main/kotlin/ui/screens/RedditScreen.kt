package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.panpf.sketch.PlatformContext
import ui.components.FullScreenImage
import ui.components.NextBatchDialog
import ui.components.RedditScreenTopBar
import vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedditScreen(
    uiState: MainViewModel.UiState,
    viewModel: MainViewModel,
    context: PlatformContext,
    nextBatchParams: MainViewModel.NextBatchDialogParams,
    onGoBackToEntry: () -> Unit
){
    val response = uiState.selectedSubredditBatch?.values?.first()
    val children = response?.data?.children ?: emptyList()
    val listState = rememberLazyListState()
    val lazyRowState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ){
        Column {
            RedditScreenTopBar(
                uiState = uiState,
                onGoBackToEntry = { onGoBackToEntry() },
                onLoadNextBatch = { viewModel.showNextBatchDialog() },
                onRefreshBatch = { viewModel.refreshCurrentBatch() }
            )
            TestBatch(viewModel, listState, lazyRowState, children)
        }
    }

    FullScreenImage(
        uiState = uiState,
        viewModel = viewModel,
        context = context
    )

    if (nextBatchParams.isShown) {
        NextBatchDialog(
            viewModel = viewModel,
            params = nextBatchParams,
            uiState = uiState
        )
    }
}