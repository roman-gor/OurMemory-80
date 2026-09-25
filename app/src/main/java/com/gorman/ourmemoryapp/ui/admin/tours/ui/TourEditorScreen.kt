package com.gorman.ourmemoryapp.ui.admin.tours.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.common.ui.ContentLanguageSelector
import com.gorman.ourmemoryapp.ui.admin.tours.models.TourEditorUiIntent
import com.gorman.ourmemoryapp.ui.admin.tours.models.TourEditorUiState
import com.gorman.ourmemoryapp.ui.admin.tours.viewmodels.TourEditorViewModel
import com.gorman.ourmemoryapp.ui.common.ui.ErrorContent
import com.gorman.ourmemoryapp.ui.common.ui.LoadingContent
import com.gorman.ourmemoryapp.ui.common.ui.SectionTitle
import com.gorman.ourmemoryapp.ui.common.ui.TopBarScaffold
import com.gorman.ourmemoryapp.ui.common.ui.TopBarSpacer
import com.gorman.ourmemoryapp.ui.common.ui.bottomBarContentPadding
import com.gorman.ourmemoryapp.ui.tours.ui.TourMap

@Composable
fun TourEditorScreen(
    onBackClick: () -> Unit,
    tourEditorViewModel: TourEditorViewModel = hiltViewModel()
) {
    val state by tourEditorViewModel.uiState.collectAsStateWithLifecycle()
    val current = state

    LaunchedEffect(current) {
        if ((current as? TourEditorUiState.Editing)?.isClosed == true) onBackClick()
    }

    val isNew = (current as? TourEditorUiState.Editing)?.isNew == true
    TopBarScaffold(
        title = stringResource(if (isNew) R.string.new_tour else R.string.tours),
        onBackClick = onBackClick
    ) {
        when (current) {
            TourEditorUiState.Loading -> LoadingContent()
            TourEditorUiState.Error -> ErrorContent()
            is TourEditorUiState.Editing -> TourEditorContent(
                state = current,
                onUiIntent = tourEditorViewModel::onUiIntent
            )
        }
    }
}

@Composable
private fun TourEditorContent(
    state: TourEditorUiState.Editing,
    onUiIntent: (TourEditorUiIntent) -> Unit
) {
    var isChoosingStop by rememberSaveable { mutableStateOf(false) }
    val titles = state.burials.associate { it.burial.id to it.veteranNames.ifBlank { it.burial.id } }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp + bottomBarContentPadding()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        item { TopBarSpacer() }
        item {
            ContentLanguageSelector(
                selected = state.form.language,
                onSelect = { onUiIntent(TourEditorUiIntent.OnLanguageChange(it)) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        item { TourMainFields(state = state, onUiIntent = onUiIntent) }
        if (state.previewStops.isNotEmpty()) {
            item {
                TourMap(
                    stops = state.previewStops,
                    selectedStopIndex = null,
                    onStopClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(PREVIEW_HEIGHT)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
        item { SectionTitle(text = stringResource(R.string.stops)) }
        itemsIndexed(state.form.stops) { index, stop ->
            TourStopEditor(
                index = index,
                lastIndex = state.form.stops.lastIndex,
                title = titles[stop.burialId] ?: stop.burialId,
                stop = stop,
                language = state.form.language,
                isEnabled = !state.isBusy,
                onUiIntent = onUiIntent
            )
        }
        item {
            OutlinedButton(
                onClick = { isChoosingStop = true },
                enabled = !state.isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = stringResource(R.string.add_stop))
            }
        }
        item { TourEditorFooter(state = state, onUiIntent = onUiIntent) }
    }

    if (isChoosingStop) {
        BurialChoiceDialog(
            burials = state.burials,
            onChoose = {
                isChoosingStop = false
                onUiIntent(TourEditorUiIntent.OnAddStop(it))
            },
            onDismiss = { isChoosingStop = false }
        )
    }
}

@Composable
private fun TourMainFields(state: TourEditorUiState.Editing, onUiIntent: (TourEditorUiIntent) -> Unit) {
    val form = state.form
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = form.text.title,
            onValueChange = { onUiIntent(TourEditorUiIntent.OnTitleChange(it)) },
            label = { Text(text = stringResource(R.string.title)) },
            placeholder = originalPlaceholder(form.language != null, form.title),
            isError = form.language == null && form.title.isBlank(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = form.text.description,
            onValueChange = { onUiIntent(TourEditorUiIntent.OnDescriptionChange(it)) },
            label = { Text(text = stringResource(R.string.description)) },
            placeholder = originalPlaceholder(form.language != null, form.description),
            minLines = DESCRIPTION_MIN_LINES,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TourEditorFooter(state: TourEditorUiState.Editing, onUiIntent: (TourEditorUiIntent) -> Unit) {
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
            onClick = { onUiIntent(TourEditorUiIntent.OnSaveClick) },
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
            text = { Text(text = stringResource(R.string.delete_tour_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleteDialogOpen = false
                        onUiIntent(TourEditorUiIntent.OnDeleteConfirm)
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

private fun originalPlaceholder(isTranslation: Boolean, original: String): (@Composable () -> Unit)? =
    if (!isTranslation || original.isBlank()) null else { { Text(text = original, maxLines = PLACEHOLDER_MAX_LINES) } }

private val PREVIEW_HEIGHT = 240.dp
private const val PLACEHOLDER_MAX_LINES = 3
private const val DESCRIPTION_MIN_LINES = 2
