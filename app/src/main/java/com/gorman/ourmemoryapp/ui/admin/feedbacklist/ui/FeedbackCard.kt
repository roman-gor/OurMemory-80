package com.gorman.ourmemoryapp.ui.admin.feedbacklist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.feedbacklist.models.FeedbackItemUi
import com.gorman.ourmemoryapp.ui.feedback.models.labelRes

@Composable
fun FeedbackCard(
    item: FeedbackItemUi,
    onVeteranClick: (String) -> Unit,
    onMarkReviewedClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (item.isReviewed) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isReviewed) 0.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(item.type.labelRes),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
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
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (item.contact.isNotBlank()) {
                Text(
                    text = item.contact,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FeedbackCardActions(item = item, onVeteranClick = onVeteranClick, onMarkReviewedClick = onMarkReviewedClick)
        }
    }
}

@Composable
private fun FeedbackCardActions(
    item: FeedbackItemUi,
    onVeteranClick: (String) -> Unit,
    onMarkReviewedClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        if (item.veteranId.isNotBlank()) {
            TextButton(onClick = { onVeteranClick(item.veteranId) }, modifier = Modifier.weight(1f)) {
                Text(text = item.veteranName.ifBlank { item.veteranId }, maxLines = 1)
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        if (item.isReviewed) {
            Text(
                text = stringResource(R.string.reviewed),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            OutlinedButton(onClick = onMarkReviewedClick) {
                Text(text = stringResource(R.string.mark_as_reviewed))
            }
        }
    }
}
