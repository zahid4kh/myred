@file:JvmName("MyRed")
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import api.RedditApi
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.supportGif
import com.github.panpf.sketch.decode.supportSkiaGif
import di.appModule
import moe.tlaster.precompose.ProvidePreComposeLocals
import myred.resources.Res
import myred.resources.appIcon
import org.jetbrains.compose.resources.painterResource
import theme.AppTheme
import java.awt.Dimension
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin
import vm.MainViewModel


fun main() {
    startKoin {
        modules(appModule)
    }

    application{
        val context = LocalPlatformContext.current
        SingletonSketch.setUnsafe(
            Sketch.Builder(context).apply {
                components {
                    supportSkiaGif()
                    supportGif()
                }
            }.build()
        )

        val api = RedditApi()
        val viewModel = getKoin().get<MainViewModel>()

        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(size = DpSize(800.dp, 600.dp)),
            alwaysOnTop = true,
            title = "MyRed",
            icon = painterResource(Res.drawable.appIcon)
        ) {
            window.minimumSize = Dimension(800, 600)

            ProvidePreComposeLocals {
                App(
                    viewModel = viewModel
                )
            }
        }

    }
}