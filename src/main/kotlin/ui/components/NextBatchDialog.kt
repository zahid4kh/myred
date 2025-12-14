package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun NextBatchDialog(
    viewModel: MainViewModel,
    params: MainViewModel.NextBatchDialogParams,
    uiState: MainViewModel.UiState
) {
    val dialogState = rememberDialogState(
        size = DpSize(400.dp, 300.dp),
        position = WindowPosition(Alignment.Center)
    )

    DialogWindow(
        title = "Load Next Batch",
        state = dialogState,
        onCloseRequest = { viewModel.closeNextBatchDialog() },
        resizable = true,
        alwaysOnTop = true
    ){
        window.minimumSize = Dimension(400, 300)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ){
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "r/${params.currentSubreddit}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = getJetbrainsMonoFamily()
                )

                Text(
                    text = "${params.currentFetchType.name.lowercase()} posts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                OutlinedTextField(
                    value = params.limit,
                    onValueChange = { viewModel.onSetNextBatchLimit(it) },
                    supportingText = {
                        Text(
                            text = "Posts to fetch (max: 100)",
                            fontFamily = getJetbrainsMonoFamily(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = getJetbrainsMonoFamily()),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.errorMessage != null
                )

                uiState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                val enabled = params.limit.isNotEmpty() &&
                        params.limit.toIntOrNull() in 1..100 &&
                        !uiState.isLoading &&
                        params.currentAfter.isNotEmpty()

                OutlinedButton(
                    onClick = { viewModel.fetchNextBatch() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .pointerHoverIcon(PointerIcon.Hand),
                    shape = MaterialTheme.shapes.medium,
                    enabled = enabled
                ){
                    if (uiState.isLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loading...")
                        }
                    } else {
                        Text(
                            text = "Load Next Batch",
                            fontFamily = getJetbrainsMonoFamily(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                if (params.currentAfter.isEmpty()) {
                    Text(
                        text = "No more posts available",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}