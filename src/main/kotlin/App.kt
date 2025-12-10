import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import theme.AppTheme
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import jdk.internal.org.jline.utils.Colors.s
import ui.components.FetchedSubredditsDialog
import vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    AppTheme(darkTheme = uiState.darkMode) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        Button(
                            onClick = { viewModel.getHotPosts() }
                        ){
                            Text(
                                text = "Fetch posts"
                            )
                        }

                        Button(
                            onClick = { viewModel.showAvailableSubredditsDialog() }
                        ){
                            Text(
                                text = "Show fetched subreddits"
                            )
                        }

                    }
                )
            }
        ){innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ){

            }


            if(uiState.availableSubredditsDialogShown){
                FetchedSubredditsDialog(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}
