package com.gorman.ourmemoryapp.ui.admin.veterans.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.details.models.Reward
import kotlinx.collections.immutable.ImmutableMap

@Composable
fun RewardsEditor(
    rewards: ImmutableMap<Reward, Int>,
    onCountChange: (Reward, Int) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Reward.entries.forEach { reward ->
            val count = rewards[reward] ?: 0
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(
                    painter = painterResource(reward.iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(REWARD_ICON_SIZE)
                        .alpha(if (count > 0) 1f else INACTIVE_ALPHA)
                )
                Text(
                    text = stringResource(reward.nameRes),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onCountChange(reward, -1) }, enabled = count > 0) {
                    Icon(painter = painterResource(R.drawable.remove), contentDescription = null)
                }
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onCountChange(reward, 1) }) {
                    Icon(painter = painterResource(R.drawable.add), contentDescription = null)
                }
            }
        }
    }
}

private val REWARD_ICON_SIZE = 32.dp
private const val INACTIVE_ALPHA = 0.35f
