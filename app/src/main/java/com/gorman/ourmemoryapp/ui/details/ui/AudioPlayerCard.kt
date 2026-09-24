package com.gorman.ourmemoryapp.ui.details.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.AudioPlaybackState
import com.gorman.ourmemoryapp.ui.details.models.AudioAction
import java.util.Locale

@Composable
fun AudioPlayerCard(
    playbackState: AudioPlaybackState,
    onAudioAction: (AudioAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var draggedPosition by remember { mutableStateOf<Float?>(null) }
    val isStarted = playbackState.currentAudio != null
    val duration = playbackState.duration.toFloat()
    val position = (draggedPosition ?: playbackState.currentPosition.toFloat()).coerceIn(0f, duration)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledIconButton(
                    onClick = { onAudioAction(playbackState.nextAction()) },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            if (playbackState.isPlaying) R.drawable.pause else R.drawable.play_arrow
                        ),
                        contentDescription = stringResource(
                            if (playbackState.isPlaying) R.string.pause else R.string.play
                        )
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.listen_to_biography),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (isStarted) {
                        Text(
                            text = "${formatTime(position)} / ${formatTime(duration)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Slider(
                value = position,
                onValueChange = { draggedPosition = it },
                onValueChangeFinished = {
                    draggedPosition?.let { onAudioAction(AudioAction.SeekTo(it.toInt())) }
                    draggedPosition = null
                },
                valueRange = 0f..duration.coerceAtLeast(1f),
                enabled = isStarted && duration > 0f
            )
        }
    }
}

private fun AudioPlaybackState.nextAction() = when {
    isPlaying -> AudioAction.Pause
    currentAudio != null -> AudioAction.Resume
    else -> AudioAction.Play
}

private fun formatTime(millis: Float): String {
    val totalSeconds = (millis / MILLIS_IN_SECOND).toInt()
    return String.format(Locale.ROOT, "%d:%02d", totalSeconds / SECONDS_IN_MINUTE, totalSeconds % SECONDS_IN_MINUTE)
}

private const val MILLIS_IN_SECOND = 1000
private const val SECONDS_IN_MINUTE = 60
