package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import ui.components.FetchSettingsDialog
import ui.components.FetchedSubredditsDialog
import vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyScreen(
    viewModel: MainViewModel,
    uiState: MainViewModel.UiState,
    fetchParams: MainViewModel.FetchSettingsDialogParams,
    onNavigateToSelectedBatch: () -> Unit
){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedCard(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable{ viewModel.showFetchSettingsDialog() }
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text(
                    text = "Fetch Posts",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedCard(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable{ viewModel.showAvailableSubredditsDialog() }
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text(
                    text = "Show fetched subreddits",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row {
                IconButton(
                    onClick = { viewModel.toggleDarkMode() },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ){
                    Icon(
                        imageVector = Icons.Default.LightMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if(uiState.showFetchSettingsDialog){
        FetchSettingsDialog(
            viewModel = viewModel,
            fetchParams = fetchParams,
            uiState = uiState,
            onNavigateToFetchedBatch = { onNavigateToSelectedBatch() }
        )
    }

    if(uiState.availableSubredditsDialogShown){
        FetchedSubredditsDialog(
            viewModel = viewModel,
            uiState = uiState,
            onNavigateToSelectedBatch = { onNavigateToSelectedBatch() }
        )
    }
}