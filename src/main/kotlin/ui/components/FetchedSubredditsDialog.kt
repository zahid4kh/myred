package ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import deskit.dialogs.info.InfoDialog
import vm.MainViewModel

@Composable
fun FetchedSubredditsDialog(
    viewModel: MainViewModel,
    uiState: MainViewModel.UiState,
    onNavigateToSelectedBatch: () -> Unit
){
    val gridState = rememberLazyStaggeredGridState()
    var dialogHeight by remember { mutableStateOf(200.dp) }
    var resizable by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.fetchedSubreddits){
        if(uiState.fetchedSubreddits.isNotEmpty()){
            dialogHeight = 420.dp
            resizable = true
        }
    }

    InfoDialog(
        onClose = { viewModel.closeAvailableSubredditsDialog() },
        title ="Fetched Subreddits",
        height = dialogHeight,
        resizable = resizable
    ){
        LazyVerticalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
            columns = StaggeredGridCells.Adaptive(250.dp),
            state = gridState,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if(uiState.fetchedSubreddits.isEmpty()){
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center){
                        Text(
                            text = "You haven't fetched any subreddits :(",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }else{
                items(items = uiState.fetchedSubreddits) { fetchedSubreddit ->
                    Column(
                        modifier = Modifier.animateItem(placementSpec = tween())
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.toggleSubredditExtended(fetchedSubreddit)
                            },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if(fetchedSubreddit.isExtended) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondary,
                                contentColor = if(fetchedSubreddit.isExtended) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondary
                            ),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .pointerHoverIcon(PointerIcon.Hand)
                                .fillMaxWidth()
                        ){
                            Text(
                                text = fetchedSubreddit.subredditFolder?.name ?: "noname",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        AnimatedVisibility(
                            visible = fetchedSubreddit.isExtended,
                            modifier = Modifier
                                .animateContentSize()
                        ){
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .height(100.dp)
                                    .animateContentSize()
                            ){
                                items(items = uiState.fetchedPostBatches){ file ->
                                    Text(
                                        text = file.nameWithoutExtension,
                                        modifier = Modifier
                                            .padding(5.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                            .clickable(onClick = {
                                                viewModel.loadSelectedBatch(batch = file)
                                                onNavigateToSelectedBatch()
                                            })
                                            .padding(5.dp)
                                            .pointerHoverIcon(PointerIcon.Hand)
                                    )
                                }
                            }
                        }
                    }

                }

            }
        }
    }
}