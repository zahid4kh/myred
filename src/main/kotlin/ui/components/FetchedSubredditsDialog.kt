package ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.onClick
import androidx.compose.material.AlertDialog
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import vm.MainViewModel
import java.awt.SystemColor.text

@Composable
fun FetchedSubredditsDialog(
    viewModel: MainViewModel,
    uiState: MainViewModel.UiState
){
    AlertDialog(
        onDismissRequest = { viewModel.closeAvailableSubredditsDialog() },
        confirmButton = {
            Button(
                onClick = { viewModel.closeAvailableSubredditsDialog() },
                shape = MaterialTheme.shapes.medium
            ){
                Text("Close")
            }
        },
        title = {
            Text(
                text = "Fetched Subreddits",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn(

            ) {
                items(items = uiState.fetchedSubreddits) { subredditFolder ->
                    Column {
                        TextButton(
                            onClick = {
                                viewModel.toggleShowPostBatches()
                                viewModel.onSelectSubredditFolder(subredditFolder)
                            },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                        ){
                            Text(
                                text = subredditFolder.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        AnimatedVisibility(
                            visible = uiState.showSubredditPostBatches,
                            modifier = Modifier
                                .animateContentSize()
                        ){
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .heightIn(min = 100.dp, max = 200.dp)
                                    .animateContentSize()
                            ){
                                items(items = uiState.fetchedPostBatches){ file ->
                                    Text(
                                        text = file.nameWithoutExtension,
                                        modifier = Modifier
                                            .padding(5.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                            .clickable(onClick = { viewModel.loadSelectedBatch(batch = file) })
                                            .padding(5.dp)
                                            .pointerHoverIcon(PointerIcon.Hand)
                                    )
                                }
                            }
                        }
                    }

                }
            }
        },
        shape = MaterialTheme.shapes.medium
    )
}