package com.gorman.ourmemoryapp.ui.tours.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.ui.PlotNumberText
import com.gorman.ourmemoryapp.ui.tours.models.TourStopUi

@Composable
fun TourStopRow(
    stop: TourStopUi,
    isSelected: Boolean,
    isPlaying: Boolean,
    isVisited: Boolean,
    onClick: () -> Unit,
    onAudioClick: () -> Unit,
    onVisitedToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                }
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = stop.number.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = stop.title.ifBlank { stringResource(stop.type.nameRes) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            PlotNumberText(burial = stop.burial)
            if (stop.text.isNotBlank()) {
                Text(
                    text = stop.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isSelected) Int.MAX_VALUE else COLLAPSED_TEXT_LINES,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable(onClick = onVisitedToggle)
            ) {
                Checkbox(checked = isVisited, onCheckedChange = { onVisitedToggle() })
                Text(
                    text = stringResource(R.string.visited),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (stop.audio != null) {
            FilledIconButton(onClick = onAudioClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play_arrow),
                    contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play)
                )
            }
        }
    }
}

private const val COLLAPSED_TEXT_LINES = 3
