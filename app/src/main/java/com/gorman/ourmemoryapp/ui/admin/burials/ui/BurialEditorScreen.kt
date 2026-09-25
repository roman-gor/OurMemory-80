package com.gorman.ourmemoryapp.ui.admin.burials.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.burials.models.BurialEditorUiIntent
import com.gorman.ourmemoryapp.ui.admin.burials.models.BurialEditorUiState
import com.gorman.ourmemoryapp.ui.admin.burials.viewmodels.BurialEditorViewModel
import com.gorman.ourmemoryapp.ui.common.models.BurialType
import com.gorman.ourmemoryapp.ui.common.ui.ErrorContent
import com.gorman.ourmemoryapp.ui.common.ui.LoadingContent
import com.gorman.ourmemoryapp.ui.common.ui.TopBarScaffold
import com.gorman.ourmemoryapp.ui.common.ui.TopBarSpacer
import com.gorman.ourmemoryapp.ui.common.ui.bottomBarContentPadding

@Composable
fun BurialEditorScreen(
    onBackClick: () -> Unit,
    burialEditorViewModel: BurialEditorViewModel = hiltViewModel()
) {
    val state by burialEditorViewModel.uiState.collectAsStateWithLifecycle()
    val current = state

    LaunchedEffect(current) {
        if ((current as? BurialEditorUiState.Editing)?.isClosed == true) onBackClick()
    }

    val isNew = (current as? BurialEditorUiState.Editing)?.isNew == true
    val title = stringResource(if (isNew) R.string.new_burial_place else R.string.burial_place)
    TopBarScaffold(title = title, onBackClick = onBackClick) {
        when (current) {
            BurialEditorUiState.Loading -> LoadingContent()
            BurialEditorUiState.Error -> ErrorContent()
            is BurialEditorUiState.Editing -> BurialEditorContent(
                state = current,
                onUiIntent = burialEditorViewModel::onUiIntent
            )
        }
    }
}

@Composable
private fun BurialEditorContent(
    state: BurialEditorUiState.Editing,
    onUiIntent: (BurialEditorUiIntent) -> Unit
) {
    val form = state.form
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 32.dp + bottomBarContentPadding())
    ) {
        TopBarSpacer()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BurialType.entries.forEach { type ->
                FilterChip(
                    selected = form.type == type,
                    onClick = { onUiIntent(BurialEditorUiIntent.OnTypeChange(type)) },
                    label = { Text(text = stringResource(type.nameRes)) }
                )
            }
        }
        PlotFields(state = state, onUiIntent = onUiIntent)
        OutlinedTextField(
            value = form.description,
            onValueChange = { onUiIntent(BurialEditorUiIntent.OnDescriptionChange(it)) },
            label = { Text(text = stringResource(R.string.description)) },
            minLines = DESCRIPTION_MIN_LINES,
            modifier = Modifier.fillMaxWidth()
        )
        BurialPhoto(state = state, onUiIntent = onUiIntent)
        BurialLocationPicker(
            latitude = form.latitudeValue,
            longitude = form.longitudeValue,
            onPointPicked = { lat, lon -> onUiIntent(BurialEditorUiIntent.OnPointPicked(lat, lon)) }
        )
        CoordinateFields(state = state, onUiIntent = onUiIntent)
        BurialEditorFooter(state = state, onSaveClick = { onUiIntent(BurialEditorUiIntent.OnSaveClick) })
    }
}

@Composable
private fun PlotFields(state: BurialEditorUiState.Editing, onUiIntent: (BurialEditorUiIntent) -> Unit) {
    val numberKeyboard = KeyboardOptions(keyboardType = KeyboardType.Number)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.form.section,
            onValueChange = { onUiIntent(BurialEditorUiIntent.OnSectionChange(it)) },
            label = { Text(text = stringResource(R.string.section)) },
            singleLine = true,
            keyboardOptions = numberKeyboard,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = state.form.row,
            onValueChange = { onUiIntent(BurialEditorUiIntent.OnRowChange(it)) },
            label = { Text(text = stringResource(R.string.row)) },
            singleLine = true,
            keyboardOptions = numberKeyboard,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = state.form.place,
            onValueChange = { onUiIntent(BurialEditorUiIntent.OnPlaceChange(it)) },
            label = { Text(text = stringResource(R.string.place)) },
            singleLine = true,
            keyboardOptions = numberKeyboard,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CoordinateFields(state: BurialEditorUiState.Editing, onUiIntent: (BurialEditorUiIntent) -> Unit) {
    val decimalKeyboard = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.form.latitude,
            onValueChange = { onUiIntent(BurialEditorUiIntent.OnLatitudeChange(it)) },
            label = { Text(text = stringResource(R.string.latitude)) },
            isError = state.form.latitudeValue == null,
            singleLine = true,
            keyboardOptions = decimalKeyboard,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = state.form.longitude,
            onValueChange = { onUiIntent(BurialEditorUiIntent.OnLongitudeChange(it)) },
            label = { Text(text = stringResource(R.string.longitude)) },
            isError = state.form.longitudeValue == null,
            singleLine = true,
            keyboardOptions = decimalKeyboard,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BurialPhoto(state: BurialEditorUiState.Editing, onUiIntent: (BurialEditorUiIntent) -> Unit) {
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onUiIntent(BurialEditorUiIntent.OnPhotoPicked(it.toString())) }
    }
    if (state.form.photo.isNotBlank()) {
        AsyncImage(
            model = state.form.photo,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(PHOTO_HEIGHT)
                .clip(RoundedCornerShape(12.dp))
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            enabled = !state.isBusy,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(R.string.add_photo))
        }
        if (state.form.photo.isNotBlank()) {
            TextButton(onClick = { onUiIntent(BurialEditorUiIntent.OnPhotoRemove) }, enabled = !state.isBusy) {
                Text(text = stringResource(R.string.delete))
            }
        }
    }
}

@Composable
private fun BurialEditorFooter(state: BurialEditorUiState.Editing, onSaveClick: () -> Unit) {
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
        onClick = onSaveClick,
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
}

private val PHOTO_HEIGHT = 180.dp
private const val DESCRIPTION_MIN_LINES = 2
