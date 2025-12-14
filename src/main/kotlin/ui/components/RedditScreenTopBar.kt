package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import myred.resources.Res
import myred.resources.next_batch
import org.jetbrains.compose.resources.painterResource
import theme.getJetbrainsMonoFamily
import vm.MainViewModel

@Composable
fun RedditScreenTopBar(
    uiState: MainViewModel.UiState,
    onGoBackToEntry: () -> Unit,
    onLoadNextBatch: () -> Unit
){
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

        IconButton(
            onClick = { onLoadNextBatch() },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ){
            Icon(
                painter = painterResource(Res.drawable.next_batch),
                contentDescription = null
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
}