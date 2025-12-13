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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import deskit.dialogs.info.InfoDialog
import vm.MainViewModel
import java.awt.SystemColor.text

@Composable
fun FetchedSubredditsDialog(
    viewModel: MainViewModel,
    uiState: MainViewModel.UiState,
    onNavigateToSelectedBatch: () -> Unit
){
    InfoDialog(
        onClose = { viewModel.closeAvailableSubredditsDialog() },
        title ="Fetched Subreddits",
        height = 420.dp,
        resizable = true
    ){
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = uiState.fetchedSubreddits) { fetchedSubreddit ->
                Column {
                    TextButton(
                        onClick = {
                            viewModel.toggleSubredditExtended(fetchedSubreddit)
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if(fetchedSubreddit.isExtended) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if(fetchedSubreddit.isExtended) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
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