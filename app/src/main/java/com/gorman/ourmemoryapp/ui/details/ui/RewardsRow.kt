package com.gorman.ourmemoryapp.ui.details.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.ui.SectionTitle
import com.gorman.ourmemoryapp.ui.details.models.RewardUi
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsRow(rewards: ImmutableList<RewardUi>, modifier: Modifier = Modifier) {
    var selectedReward by remember { mutableStateOf<RewardUi?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(text = stringResource(R.string.awards))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rewards, key = { it.reward.id }) { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedReward = item }
                ) {
                    Image(
                        painter = painterResource(item.reward.iconRes),
                        contentDescription = stringResource(item.reward.nameRes),
                        modifier = Modifier.size(64.dp)
                    )
                    if (item.count > 1) {
                        RewardCount(count = item.count, modifier = Modifier.padding(start = 4.dp, end = 4.dp))
                    }
                }
            }
        }
    }

    selectedReward?.let { item ->
        ModalBottomSheet(onDismissRequest = { selectedReward = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(item.reward.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp)
                )
                Text(
                    text = stringResource(item.reward.nameRes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
                if (item.count > 1) {
                    RewardCount(count = item.count, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun RewardCount(count: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.times_count, count),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}
