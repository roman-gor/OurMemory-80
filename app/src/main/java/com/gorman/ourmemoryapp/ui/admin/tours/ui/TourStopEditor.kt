package com.gorman.ourmemoryapp.ui.admin.tours.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.ContentLanguage
import com.gorman.ourmemoryapp.ui.admin.tours.models.TourEditorUiIntent
import com.gorman.ourmemoryapp.ui.admin.tours.models.TourStopForm

@Composable
fun TourStopEditor(
    index: Int,
    lastIndex: Int,
    title: String,
    stop: TourStopForm,
    language: ContentLanguage?,
    isEnabled: Boolean,
    onUiIntent: (TourEditorUiIntent) -> Unit
) {
    val text = stop.textIn(language)
    val pickAudio = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { onUiIntent(TourEditorUiIntent.OnStopAudioPicked(index, it.toString())) }
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${index + 1}. $title",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = text.text,
                onValueChange = { onUiIntent(TourEditorUiIntent.OnStopTextChange(index, it)) },
                label = { Text(text = stringResource(R.string.stop_text)) },
                placeholder = if (language == null || stop.text.isBlank()) {
                    null
                } else {
                    { Text(text = stop.text, maxLines = PLACEHOLDER_MAX_LINES) }
                },
                minLines = TEXT_MIN_LINES,
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(if (text.audioUrl.isBlank()) R.string.no_audio else R.string.audio_attached),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                if (text.audioUrl.isNotBlank()) {
                    TextButton(
                        onClick = { onUiIntent(TourEditorUiIntent.OnStopAudioRemove(index)) },
                        enabled = isEnabled
                    ) {
                        Text(text = stringResource(R.string.delete))
                    }
                }
                TextButton(onClick = { pickAudio.launch(arrayOf(AUDIO_MIME_TYPE)) }, enabled = isEnabled) {
                    Text(text = stringResource(R.string.add_audio))
                }
            }
            Row {
                IconButton(onClick = { onUiIntent(TourEditorUiIntent.OnStopMove(index, -1)) }, enabled = index > 0) {
                    Icon(
                        painter = painterResource(R.drawable.keyboard_arrow_up),
                        contentDescription = stringResource(R.string.move_up),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { onUiIntent(TourEditorUiIntent.OnStopMove(index, 1)) },
                    enabled = index < lastIndex
                ) {
                    Icon(
                        painter = painterResource(R.drawable.keyboard_arrow_down),
                        contentDescription = stringResource(R.string.move_down),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onUiIntent(TourEditorUiIntent.OnStopRemove(index)) }) {
                    Icon(
                        painter = painterResource(R.drawable.close),
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private const val TEXT_MIN_LINES = 2
private const val PLACEHOLDER_MAX_LINES = 3
private const val AUDIO_MIME_TYPE = "audio/*"
