package com.gorman.ourmemoryapp.ui.admin.veterans.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R

@Composable
fun AudioEditor(
    audioUrl: String,
    isEnabled: Boolean,
    onAudioPicked: (String) -> Unit,
    onAudioRemove: () -> Unit
) {
    val pickAudio = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { onAudioPicked(it.toString()) }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(if (audioUrl.isBlank()) R.string.no_audio else R.string.audio_attached),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (audioUrl.isNotBlank()) {
            TextButton(onClick = onAudioRemove, enabled = isEnabled) {
                Text(text = stringResource(R.string.delete))
            }
        }
        OutlinedButton(onClick = { pickAudio.launch(arrayOf(AUDIO_MIME_TYPE)) }, enabled = isEnabled) {
            Text(text = stringResource(R.string.add_audio))
        }
    }
}

private const val AUDIO_MIME_TYPE = "audio/*"
