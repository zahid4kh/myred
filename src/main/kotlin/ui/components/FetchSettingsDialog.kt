package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import theme.getJetbrainsMonoFamily
import vm.MainViewModel
import java.awt.Dimension

@Composable
fun FetchSettingsDialog(
    viewModel: MainViewModel,
    fetchParams: MainViewModel.FetchSettingsDialogParams,
    uiState: MainViewModel.UiState,
    onNavigateToFetchedBatch: () -> Unit
) {
    val dialogState = rememberDialogState(
        size = DpSize(450.dp, 400.dp),
        position = WindowPosition(Alignment.Center)
    )

    DialogWindow(
        title = "Fetch Settings",
        state = dialogState,
        onCloseRequest = { viewModel.closeFetchSettingsDialog() },
        resizable = false,
        alwaysOnTop = true
    ){
        window.minimumSize = Dimension(450, 400)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ){
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = fetchParams.subreddit,
                    onValueChange = { viewModel.onSetSubredditToFetch(it.trim()) },
                    supportingText = {
                        Text(
                            text = "Enter a subreddit to fetch",
                            fontFamily = getJetbrainsMonoFamily(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = getJetbrainsMonoFamily()),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    enabled = !uiState.isLoading
                )

                OutlinedTextField(
                    value = fetchParams.limit,
                    onValueChange = { viewModel.onSetPostLimitToFetch(it.trim()) },
                    supportingText = {
                        Text(
                            text = "How many posts to fetch? (max: 100)",
                            fontFamily = getJetbrainsMonoFamily(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = getJetbrainsMonoFamily()),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.errorMessage != null && uiState.errorMessage!!.contains("limit"),
                    enabled = !uiState.isLoading
                )

                // Error message display
                uiState.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                if (fetchParams.fetchType == MainViewModel.FetchType.HOT)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.tertiaryContainer
                            )
                            .padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = "Hot",
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = getJetbrainsMonoFamily(),
                            color = if (fetchParams.fetchType == MainViewModel.FetchType.HOT)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        RadioButton(
                            selected = fetchParams.fetchType == MainViewModel.FetchType.HOT,
                            onClick = { viewModel.onSetFetchType(MainViewModel.FetchType.HOT) },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                            enabled = !uiState.isLoading
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                if (fetchParams.fetchType == MainViewModel.FetchType.NEW)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.tertiaryContainer
                            )
                            .padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = "New",
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = getJetbrainsMonoFamily(),
                            color = if (fetchParams.fetchType == MainViewModel.FetchType.NEW)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        RadioButton(
                            selected = fetchParams.fetchType == MainViewModel.FetchType.NEW,
                            onClick = { viewModel.onSetFetchType(MainViewModel.FetchType.NEW) },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                            enabled = !uiState.isLoading
                        )
                    }
                }

                val isValidLimit = fetchParams.limit.toIntOrNull() in 1..100
                val enabled = fetchParams.subreddit.isNotEmpty() &&
                        fetchParams.limit.isNotEmpty() &&
                        isValidLimit &&
                        !uiState.isLoading

                OutlinedButton(
                    onClick = {
                        when(fetchParams.fetchType) {
                            MainViewModel.FetchType.HOT -> {
                                viewModel.getHotPosts(onNavigateToFetchedBatch)
                            }
                            MainViewModel.FetchType.NEW -> {
                                viewModel.getNewPosts(onNavigateToFetchedBatch)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .pointerHoverIcon(PointerIcon.Hand),
                    shape = MaterialTheme.shapes.medium,
                    enabled = enabled
                ){
                    if (uiState.isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Fetching...",
                                fontFamily = getJetbrainsMonoFamily(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        Text(
                            text = "Fetch",
                            fontFamily = getJetbrainsMonoFamily(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (!enabled) {
                                Color.Gray.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}