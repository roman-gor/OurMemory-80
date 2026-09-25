package com.gorman.ourmemoryapp.ui.feedback.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.models.FeedbackDraft
import com.gorman.ourmemoryapp.domain.models.FeedbackType
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.domain.repository.ContentCheckRepository
import com.gorman.ourmemoryapp.domain.repository.FeedbackRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.feedback.models.FeedbackFormStatus
import com.gorman.ourmemoryapp.ui.feedback.models.FeedbackUiIntent
import com.gorman.ourmemoryapp.ui.feedback.models.FeedbackUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val feedbackRepository: FeedbackRepository,
    private val veteransRepository: VeteransRepository,
    private val contentCheckRepository: ContentCheckRepository
) : ViewModel() {

    private val veteranId = savedStateHandle.get<String>(Screen.FeedbackScreen.VETERAN_ID_ARG).orEmpty()
    private val initialState = FeedbackUiState(
        isAboutVeteran = veteranId.isNotEmpty(),
        type = if (veteranId.isNotEmpty()) FeedbackType.DATA_ERROR else FeedbackType.OTHER
    )
    private val formState = MutableStateFlow(initialState)

    val uiState = combine(formState, observeVeteranName()) { form, name ->
        form.copy(veteranName = name)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = initialState
    )

    fun onUiIntent(intent: FeedbackUiIntent) {
        when (intent) {
            is FeedbackUiIntent.OnTypeChange -> formState.update { it.copy(type = intent.type) }
            is FeedbackUiIntent.OnTextChange -> formState.update {
                it.copy(text = intent.text.take(MAX_TEXT_LENGTH), hasTextProfanity = false)
            }
            is FeedbackUiIntent.OnContactChange -> formState.update {
                it.copy(contact = intent.contact, hasContactProfanity = false)
            }
            FeedbackUiIntent.OnSendClick -> send()
        }
    }

    private fun observeVeteranName() = flow {
        emit("")
        if (veteranId.isEmpty()) return@flow
        val name = runCatching { veteransRepository.getAllVeterans().firstOrNull { it.id == veteranId }?.name }
            .getOrNull()
        emit(name.orEmpty())
    }

    private fun send() {
        val state = formState.value
        if (!state.canSend) return
        val hasTextProfanity = contentCheckRepository.containsProfanity(state.text)
        val hasContactProfanity = contentCheckRepository.containsProfanity(state.contact)
        if (hasTextProfanity || hasContactProfanity) {
            formState.update {
                it.copy(hasTextProfanity = hasTextProfanity, hasContactProfanity = hasContactProfanity)
            }
            return
        }
        formState.update { it.copy(status = FeedbackFormStatus.SENDING) }
        viewModelScope.launch {
            runCatching {
                feedbackRepository.send(
                    FeedbackDraft(
                        type = state.type,
                        text = state.text.trim(),
                        contact = state.contact.trim(),
                        veteranId = veteranId
                    )
                )
            }.onSuccess {
                formState.update { it.copy(status = FeedbackFormStatus.SENT) }
            }.onFailure { error ->
                Log.e(LOG_TAG, "Failed to send feedback", error)
                formState.update { it.copy(status = FeedbackFormStatus.FAILED) }
            }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "FeedbackViewModel"
        private const val MAX_TEXT_LENGTH = 3000
    }
}
