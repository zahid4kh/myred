package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import theme.getJetbrainsMonoFamily
import vm.MainViewModel

@Composable
fun RedditScreenTopBar(
    uiState: MainViewModel.UiState,
    onGoBackToEntry: () -> Unit
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