package com.gorman.ourmemoryapp.ui.myrequests.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.domain.models.RequestStatus
import com.gorman.ourmemoryapp.ui.myrequests.models.labelRes

@Composable
fun RequestStatusLabel(status: RequestStatus) {
    val colors = MaterialTheme.colorScheme
    val (containerColor, contentColor) = when (status) {
        RequestStatus.IN_REVIEW -> colors.surfaceContainerHighest to colors.onSurfaceVariant
        RequestStatus.APPROVED, RequestStatus.REVIEWED -> colors.tertiaryContainer to colors.onTertiaryContainer
        RequestStatus.REJECTED -> colors.errorContainer to colors.onErrorContainer
    }
    Surface(shape = CircleShape, color = containerColor, contentColor = contentColor) {
        Text(
            text = stringResource(status.labelRes),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
