package com.gorman.ourmemoryapp.ui.submission.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.ui.FloatingTopBar
import com.gorman.ourmemoryapp.ui.common.ui.SentContent
import com.gorman.ourmemoryapp.ui.common.ui.offensiveWordsHint
import com.gorman.ourmemoryapp.ui.submission.models.SubmissionStatus
import com.gorman.ourmemoryapp.ui.submission.models.SubmissionUiIntent
import com.gorman.ourmemoryapp.ui.submission.models.SubmissionUiState
import com.gorman.ourmemoryapp.ui.submission.models.messageRes
import com.gorman.ourmemoryapp.ui.submission.viewmodels.SubmissionViewModel

@Composable
fun SubmissionScreen(
    onBackClick: () -> Unit,
    submissionViewModel: SubmissionViewModel = hiltViewModel()
) {
    val state by submissionViewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.status == SubmissionStatus.SENT) {
            SentContent(
                message = stringResource(R.string.thank_you_material_sent_msg),
                onDoneClick = onBackClick
            )
        } else {
            SubmissionForm(state = state, onUiIntent = submissionViewModel::onUiIntent)
        }
        FloatingTopBar(
            title = stringResource(R.string.add_to_history),
            isCollapsed = true,
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun SubmissionForm(
    state: SubmissionUiState,
    onUiIntent: (SubmissionUiIntent) -> Unit
) {
    val isEditable = state.status != SubmissionStatus.SENDING
    val pickPhotos = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(SubmissionUiState.MAX_PHOTOS)
    ) { uris -> onUiIntent(SubmissionUiIntent.OnPhotosPicked(uris.map { it.toString() })) }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        item { Spacer(modifier = Modifier.statusBarsPadding().height(TOP_BAR_HEIGHT)) }
        item { SubmissionIntro(veteranName = state.veteranName) }
        item { SubmissionFields(state = state, isEditable = isEditable, onUiIntent = onUiIntent) }
        item {
            PhotosSection(
                state = state,
                onAddClick = {
                    pickPhotos.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onRemoveClick = { onUiIntent(SubmissionUiIntent.OnRemovePhoto(it)) }
            )
        }
        item {
            ConsentRow(
                hasConsent = state.hasConsent,
                isEditable = isEditable,
                onConsentChange = { onUiIntent(SubmissionUiIntent.OnConsentChange(it)) }
            )
        }
        if (state.status == SubmissionStatus.FAILED) {
            item {
                Text(
                    text = stringResource(R.string.failed_to_send_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        item { SendButton(state = state, onClick = { onUiIntent(SubmissionUiIntent.OnSendClick) }) }
        item { Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
    }
}

@Composable
private fun SubmissionIntro(veteranName: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (veteranName.isNotBlank()) {
            Text(
                text = veteranName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = stringResource(R.string.share_materials_msg),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun SubmissionFields(
    state: SubmissionUiState,
    isEditable: Boolean,
    onUiIntent: (SubmissionUiIntent) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = state.text,
            onValueChange = { onUiIntent(SubmissionUiIntent.OnTextChange(it)) },
            label = { Text(text = stringResource(R.string.memories)) },
            enabled = isEditable,
            isError = state.hasTextProfanity,
            supportingText = offensiveWordsHint(state.hasTextProfanity),
            minLines = MEMORIES_MIN_LINES,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.contact,
            onValueChange = { onUiIntent(SubmissionUiIntent.OnContactChange(it)) },
            label = { Text(text = stringResource(R.string.your_name_and_contact)) },
            enabled = isEditable,
            isError = state.hasContactProfanity,
            supportingText = offensiveWordsHint(state.hasContactProfanity),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PhotosSection(
    state: SubmissionUiState,
    onAddClick: () -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    Column {
        Text(
            text = stringResource(
                R.string.photos_count_of_max,
                state.photoUris.size,
                SubmissionUiState.MAX_PHOTOS
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )
        PhotoPickerRow(
            photoUris = state.photoUris,
            checkingPhotosCount = state.checkingPhotosCount,
            canAddPhotos = state.canAddPhotos,
            onAddClick = onAddClick,
            onRemoveClick = onRemoveClick
        )
        state.photoRejection?.let { rejection ->
            Text(
                text = stringResource(rejection.messageRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
            )
        }
    }
}

@Composable
private fun ConsentRow(
    hasConsent: Boolean,
    isEditable: Boolean,
    onConsentChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEditable) { onConsentChange(!hasConsent) }
            .padding(horizontal = 4.dp)
    ) {
        Checkbox(
            checked = hasConsent,
            onCheckedChange = onConsentChange,
            enabled = isEditable
        )
        Text(
            text = stringResource(R.string.consent_to_contact_msg),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(end = 12.dp)
        )
    }
}

@Composable
private fun SendButton(state: SubmissionUiState, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = state.canSend,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(52.dp)
    ) {
        if (state.status == SubmissionStatus.SENDING) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(text = stringResource(R.string.send))
        }
    }
}

private val TOP_BAR_HEIGHT = 56.dp
private const val MEMORIES_MIN_LINES = 5
