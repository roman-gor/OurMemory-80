package com.gorman.ourmemoryapp.ui.feedback.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import com.gorman.ourmemoryapp.domain.models.FeedbackType
import com.gorman.ourmemoryapp.ui.common.ui.FloatingTopBar
import com.gorman.ourmemoryapp.ui.common.ui.SentContent
import com.gorman.ourmemoryapp.ui.common.ui.offensiveWordsHint
import com.gorman.ourmemoryapp.ui.feedback.models.FeedbackFormStatus
import com.gorman.ourmemoryapp.ui.feedback.models.FeedbackUiIntent
import com.gorman.ourmemoryapp.ui.feedback.models.FeedbackUiState
import com.gorman.ourmemoryapp.ui.feedback.models.labelRes
import com.gorman.ourmemoryapp.ui.feedback.viewmodels.FeedbackViewModel

@Composable
fun FeedbackScreen(
    onBackClick: () -> Unit,
    feedbackViewModel: FeedbackViewModel = hiltViewModel()
) {
    val state by feedbackViewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.status == FeedbackFormStatus.SENT) {
            SentContent(
                message = stringResource(R.string.thank_you_message_sent_msg),
                onDoneClick = onBackClick
            )
        } else {
            FeedbackForm(state = state, onUiIntent = feedbackViewModel::onUiIntent)
        }
        FloatingTopBar(
            title = stringResource(if (state.isAboutVeteran) R.string.report_an_error else R.string.write_to_us),
            isCollapsed = true,
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun FeedbackForm(
    state: FeedbackUiState,
    onUiIntent: (FeedbackUiIntent) -> Unit
) {
    val isEditable = state.status != FeedbackFormStatus.SENDING

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        item { Spacer(modifier = Modifier.statusBarsPadding().height(TOP_BAR_HEIGHT)) }
        if (state.veteranName.isNotBlank()) {
            item {
                Text(
                    text = state.veteranName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        item {
            FeedbackTypeChips(
                selected = state.type,
                isEditable = isEditable,
                onSelect = { onUiIntent(FeedbackUiIntent.OnTypeChange(it)) }
            )
        }
        item { FeedbackFields(state = state, isEditable = isEditable, onUiIntent = onUiIntent) }
        if (state.status == FeedbackFormStatus.FAILED) {
            item {
                Text(
                    text = stringResource(R.string.failed_to_send_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        item { FeedbackSendButton(state = state, onClick = { onUiIntent(FeedbackUiIntent.OnSendClick) }) }
        item { Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
    }
}

@Composable
private fun FeedbackTypeChips(
    selected: FeedbackType,
    isEditable: Boolean,
    onSelect: (FeedbackType) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        FeedbackType.entries.forEach { type ->
            FilterChip(
                selected = type == selected,
                onClick = { onSelect(type) },
                enabled = isEditable,
                label = { Text(text = stringResource(type.labelRes)) }
            )
        }
    }
}

@Composable
private fun FeedbackFields(
    state: FeedbackUiState,
    isEditable: Boolean,
    onUiIntent: (FeedbackUiIntent) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = state.text,
            onValueChange = { onUiIntent(FeedbackUiIntent.OnTextChange(it)) },
            label = { Text(text = stringResource(R.string.message)) },
            enabled = isEditable,
            isError = state.hasTextProfanity,
            supportingText = offensiveWordsHint(state.hasTextProfanity),
            minLines = MESSAGE_MIN_LINES,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.contact,
            onValueChange = { onUiIntent(FeedbackUiIntent.OnContactChange(it)) },
            label = { Text(text = stringResource(R.string.contact_for_reply)) },
            enabled = isEditable,
            isError = state.hasContactProfanity,
            supportingText = offensiveWordsHint(state.hasContactProfanity),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FeedbackSendButton(state: FeedbackUiState, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = state.canSend,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(52.dp)
    ) {
        if (state.status == FeedbackFormStatus.SENDING) {
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
private const val MESSAGE_MIN_LINES = 5
