package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.PlatformContext
import ui.components.FullScreenImage
import vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedditScreen(
    uiState: MainViewModel.UiState,
    viewModel: MainViewModel,
    context: PlatformContext,
    onGoBackToEntry: () -> Unit
){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ){
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(2.dp)
            ) {
                IconButton(
                    onClick = { onGoBackToEntry() },
                    modifier = Modifier
                ){
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
            TestBatch(viewModel, uiState)
        }
    }

    FullScreenImage(
        uiState = uiState,
        viewModel = viewModel,
        context = context
    )
}