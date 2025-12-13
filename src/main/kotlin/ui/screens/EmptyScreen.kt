package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import ui.components.FetchedSubredditsDialog
import vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyScreen(
    viewModel: MainViewModel,
    uiState: MainViewModel.UiState,
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
                    .clickable{}
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

    if(uiState.availableSubredditsDialogShown){
        FetchedSubredditsDialog(
            viewModel = viewModel,
            uiState = uiState,
            onNavigateToSelectedBatch = { onNavigateToSelectedBatch() }
        )
    }
}