package ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.disallowAnimatedImage
import com.github.panpf.sketch.request.repeatCount
import com.github.panpf.sketch.resize.Precision
import data.allImageUrls
import data.getLinkUrl
import vm.MainViewModel
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun TestBatch(
    viewModel: MainViewModel,
    uiState: MainViewModel.UiState
){
    val response = uiState.selectedSubredditBatch?.values?.first()
    val children = response?.data?.children ?: emptyList()
    val listState = rememberLazyListState()
    val lazyRowState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = listState
    ) {
        items(children){ post ->
            OutlinedCard(
                modifier = Modifier
                    .padding(10.dp)
                    .animateContentSize()
            ) {
                Column(
                    modifier = Modifier
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "u/${post.data.author} at "
                            )

                            val formattedTime = formatRedditTime(post.data.createdAt)
                            Text(
                                text = formattedTime,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.SemiBold
                            )
                        }


                    }

                    Text(
                        text = post.data.title,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = post.data.selftext,
                        style = MaterialTheme.typography.bodySmall,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.animateContentSize(animationSpec = spring())
                    )

                    val postId = post.data.id
                    val subredditName = post.data.subreddit
                    val folder = File("${System.getProperty("user.home")}/.myred/$subredditName/images/$postId")

                    if (folder.exists()) {
                        val files = folder.listFiles()?.filter {
                            it.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "webp")
                        }?.sortedBy { it.name }

                        if (!files.isNullOrEmpty()) {
                            Box {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    state = lazyRowState
                                ) {
                                    items(files) { imgFile ->
                                        val platformContext = LocalPlatformContext.current
                                        AsyncImage(
                                            request = ImageRequest(platformContext, imgFile.absolutePath) {
                                                disallowAnimatedImage(false)
                                                repeatCount(-1)
                                                precision(Precision.SMALLER_SIZE)
                                            },
                                            contentDescription = "photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(200.dp)
                                                .clip(MaterialTheme.shapes.medium)
                                                .clickable(
                                                    onClick = { viewModel.showImageFullScreen(imgFile) }
                                                )
                                                .pointerHoverIcon(PointerIcon.Hand)
                                        )
                                    }
                                }

                                HorizontalScrollbar(
                                    adapter = rememberScrollbarAdapter(lazyRowState),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomStart)
                                )
                            }

                        }
                    } else {
                        val allUrls = post.data.allImageUrls()
                        if (allUrls.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    Text(
                                        text = "Downloading ${allUrls.size} image${if (allUrls.size != 1) "s" else ""}...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }


                    val linkUrl = post.data.getLinkUrl()
                    val desktop = Desktop.getDesktop()
                    if (linkUrl != null) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "🔗 External Link",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = linkUrl,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Blue.copy(alpha = 0.7f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable{
                                        desktop.browse(URI(linkUrl))
                                    }.pointerHoverIcon(PointerIcon.Hand)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatRedditTime(seconds: Double): String {
    val millis = (seconds * 1000).toLong()

    val formatter = DateTimeFormatter.ofPattern("MMM. dd, HH:mm", Locale.ENGLISH)
        .withZone(ZoneId.of("Europe/Berlin"))

    return formatter.format(Instant.ofEpochMilli(millis))
}