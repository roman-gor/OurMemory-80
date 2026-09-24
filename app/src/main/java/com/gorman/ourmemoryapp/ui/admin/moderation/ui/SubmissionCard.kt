package com.gorman.ourmemoryapp.ui.admin.moderation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.ModerationStatus
import com.gorman.ourmemoryapp.ui.admin.moderation.models.SubmissionItemUi
import com.gorman.ourmemoryapp.ui.admin.moderation.models.labelRes

@Composable
fun SubmissionCard(item: SubmissionItemUi, onClick: () -> Unit) {
    val isPending = item.status == ModerationStatus.PENDING
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isPending) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPending) 2.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.veteranName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = item.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = PREVIEW_MAX_LINES,
                overflow = TextOverflow.Ellipsis
            )
            Row {
                Text(
                    text = stringResource(item.status.labelRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isPending) MaterialTheme.colorScheme.primary else mutedColor,
                    modifier = Modifier.weight(1f)
                )
                if (item.photoCount > 0) {
                    Text(
                        text = pluralStringResource(R.plurals.photos_count, item.photoCount, item.photoCount),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private const val PREVIEW_MAX_LINES = 3
