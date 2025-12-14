import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.panpf.sketch.LocalPlatformContext
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import theme.AppTheme
import ui.screens.EmptyScreen
import ui.screens.RedditScreen
import vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val fetchParams by viewModel.fetchSettingsDialogState.collectAsState()

    val context = LocalPlatformContext.current
    PreComposeApp {
        val navigator = rememberNavigator()
        AppTheme(darkTheme = uiState.darkMode) {
            NavHost(
                navigator = navigator,
                initialRoute = "/empty"
            ){
                scene("/empty"){
                    EmptyScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        onNavigateToSelectedBatch = { navigator.navigate("/reddit") },
                        fetchParams = fetchParams
                    )
                }

                scene("/reddit"){
                    val nextBatchParams by viewModel.nextBatchDialogState.collectAsState()
                    RedditScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        context = context,
                        nextBatchParams = nextBatchParams,
                        onGoBackToEntry = {
                            navigator.goBack()
                            viewModel.resetSelectedSubredditBatch()
                        }
                    )
                }
            }
        }
    }
}
