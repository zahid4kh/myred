package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.PlatformContext
import theme.getJetbrainsMonoFamily
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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

                uiState.selectedSubredditBatch?.values?.first()?.data?.children?.first()?.data?.subredditNamePrefixed?.let {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = 10.dp),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = getJetbrainsMonoFamily()
                        ),
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }

            }
            TestBatch(viewModel, listState, lazyRowState, children)
        }
    }

    FullScreenImage(
        uiState = uiState,
        viewModel = viewModel,
        context = context
    )
}