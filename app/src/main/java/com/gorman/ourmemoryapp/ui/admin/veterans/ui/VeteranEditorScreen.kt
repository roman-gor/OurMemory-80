package com.gorman.ourmemoryapp.ui.admin.veterans.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.veterans.models.VeteranEditorUiIntent
import com.gorman.ourmemoryapp.ui.admin.veterans.models.VeteranEditorUiState
import com.gorman.ourmemoryapp.ui.admin.veterans.viewmodels.VeteranEditorViewModel
import com.gorman.ourmemoryapp.ui.common.ui.ErrorContent
import com.gorman.ourmemoryapp.ui.common.ui.LoadingContent
import com.gorman.ourmemoryapp.ui.common.ui.SectionTitle
import com.gorman.ourmemoryapp.ui.common.ui.TopBarScaffold
import com.gorman.ourmemoryapp.ui.common.ui.TopBarSpacer

@Composable
fun VeteranEditorScreen(
    onBackClick: () -> Unit,
    veteranEditorViewModel: VeteranEditorViewModel = hiltViewModel()
) {
    val state by veteranEditorViewModel.uiState.collectAsStateWithLifecycle()
    val current = state

    LaunchedEffect(current) {
        if ((current as? VeteranEditorUiState.Editing)?.isClosed == true) onBackClick()
    }

    val title = when {
        current is VeteranEditorUiState.Editing && current.isNew -> stringResource(R.string.new_veteran)
        current is VeteranEditorUiState.Editing -> current.form.name
        else -> ""
    }
    TopBarScaffold(title = title, onBackClick = onBackClick) {
        when (current) {
            VeteranEditorUiState.Loading -> LoadingContent()
            VeteranEditorUiState.Error -> ErrorContent()
            is VeteranEditorUiState.Editing -> VeteranEditorContent(
                state = current,
                onUiIntent = veteranEditorViewModel::onUiIntent
            )
        }
    }
}

@Composable
private fun VeteranEditorContent(
    state: VeteranEditorUiState.Editing,
    onUiIntent: (VeteranEditorUiIntent) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        item { TopBarSpacer() }
        item { VeteranMainFields(form = state.form, isEnabled = !state.isBusy, onUiIntent = onUiIntent) }
        item { VeteranDatesAndPlace(state = state, onUiIntent = onUiIntent) }
        item { SectionTitle(text = stringResource(R.string.awards)) }
        item {
            RewardsEditor(
                rewards = state.form.rewards,
                onCountChange = { reward, delta ->
                    onUiIntent(
                    VeteranEditorUiIntent.OnRewardCountChange(reward, delta)
                )
                }
            )
        }
        item { SectionTitle(text = stringResource(R.string.listen_to_biography)) }
        item {
            AudioEditor(
                audioUrl = state.form.audioUrl,
                isEnabled = !state.isBusy,
                onAudioPicked = { onUiIntent(VeteranEditorUiIntent.OnAudioPicked(it)) },
                onAudioRemove = { onUiIntent(VeteranEditorUiIntent.OnAudioRemove) }
            )
        }
        item { SectionTitle(text = stringResource(R.string.biography_and_media)) }
        infoBlocks(state = state, onUiIntent = onUiIntent)
        item { AddBlockButtons(isEnabled = !state.isBusy, onUiIntent = onUiIntent) }
        item { EditorFooter(state = state, onUiIntent = onUiIntent) }
    }
}

@Composable
private fun VeteranDatesAndPlace(
    state: VeteranEditorUiState.Editing,
    onUiIntent: (VeteranEditorUiIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
        VeteranDateField(
            label = stringResource(R.string.date_of_birth),
            date = state.form.birthDate,
            isValid = state.form.isBirthDateValid,
            onDateChange = { onUiIntent(VeteranEditorUiIntent.OnBirthDateChange(it)) }
        )
        VeteranDateField(
            label = stringResource(R.string.date_of_death),
            date = state.form.deathDate,
            isValid = state.form.isDeathDateValid,
            onDateChange = { onUiIntent(VeteranEditorUiIntent.OnDeathDateChange(it)) }
        )
        BurialPicker(
            burials = state.burials,
            selectedId = state.form.burialId,
            onSelect = { onUiIntent(VeteranEditorUiIntent.OnBurialChange(it)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun LazyListScope.infoBlocks(
    state: VeteranEditorUiState.Editing,
    onUiIntent: (VeteranEditorUiIntent) -> Unit
) {
    val blocks = state.form.blocks
    itemsIndexed(blocks) { index, block ->
        InfoBlockEditor(
            block = block,
            canMoveUp = index > 0,
            canMoveDown = index < blocks.lastIndex,
            onChange = { onUiIntent(VeteranEditorUiIntent.OnBlockChange(index, it)) },
            onMove = { onUiIntent(VeteranEditorUiIntent.OnBlockMove(index, it)) },
            onRemove = { onUiIntent(VeteranEditorUiIntent.OnBlockRemove(index)) }
        )
    }
}

@Composable
private fun AddBlockButtons(isEnabled: Boolean, onUiIntent: (VeteranEditorUiIntent) -> Unit) {
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onUiIntent(VeteranEditorUiIntent.OnMediaPicked(it.toString())) }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
        OutlinedButton(
            onClick = { onUiIntent(VeteranEditorUiIntent.OnAddParagraph) },
            enabled = isEnabled,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(R.string.add_paragraph))
        }
        OutlinedButton(
            onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            enabled = isEnabled,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(R.string.add_photo))
        }
    }
}

@Composable
private fun EditorFooter(
    state: VeteranEditorUiState.Editing,
    onUiIntent: (VeteranEditorUiIntent) -> Unit
) {
    var isDeleteDialogOpen by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
        if (state.hasFailed) {
            Text(
                text = stringResource(R.string.failed_to_save_msg),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (state.isUploading) {
            Text(
                text = stringResource(R.string.uploading_file),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(
            onClick = { onUiIntent(VeteranEditorUiIntent.OnSaveClick) },
            enabled = state.canSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(text = stringResource(R.string.save))
            }
        }
        if (!state.isNew) {
            TextButton(
                onClick = { isDeleteDialogOpen = true },
                enabled = !state.isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (isDeleteDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogOpen = false },
            text = { Text(text = stringResource(R.string.delete_veteran_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleteDialogOpen = false
                        onUiIntent(VeteranEditorUiIntent.OnDeleteConfirm)
                    }
                ) {
                    Text(text = stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleteDialogOpen = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}
