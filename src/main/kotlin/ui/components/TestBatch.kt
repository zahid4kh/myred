package ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jogamp.graph.font.typecast.ot.table.Table.post
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import vm.MainViewModel
import java.awt.SystemColor.text
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TestBatch(
    viewModel: MainViewModel,
    uiState: MainViewModel.UiState
){
    LaunchedEffect(Unit){
        while(isActive){
            viewModel.loadTestBatch()
            delay(3000)
        }
    }
    val response = uiState.selectedSubredditBatch?.values?.first()
    val children = response?.data?.children ?: emptyList()
    val listState = rememberLazyListState()

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