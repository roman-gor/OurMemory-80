package com.gorman.ourmemoryapp.ui.submission.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.models.PhotoCheckResult
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.domain.models.SubmissionDraft
import com.gorman.ourmemoryapp.domain.repository.ContentCheckRepository
import com.gorman.ourmemoryapp.domain.repository.SubmissionsRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.submission.models.SubmissionStatus
import com.gorman.ourmemoryapp.ui.submission.models.SubmissionUiIntent
import com.gorman.ourmemoryapp.ui.submission.models.SubmissionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubmissionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val submissionsRepository: SubmissionsRepository,
    private val veteransRepository: VeteransRepository,
    private val contentCheckRepository: ContentCheckRepository
) : ViewModel() {

    private val veteranId: String = savedStateHandle[Screen.SubmissionScreen.VETERAN_ID_ARG] ?: ""
    private val formState = MutableStateFlow(SubmissionUiState())

    val uiState = combine(formState, observeVeteranName()) { form, name ->
        form.copy(veteranName = name)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = SubmissionUiState()
    )

    fun onUiIntent(intent: SubmissionUiIntent) {
        when (intent) {
            is SubmissionUiIntent.OnTextChange -> formState.update {
                it.copy(text = intent.text.take(MAX_TEXT_LENGTH), hasTextProfanity = false)
            }
            is SubmissionUiIntent.OnContactChange -> formState.update {
                it.copy(contact = intent.contact, hasContactProfanity = false)
            }
            is SubmissionUiIntent.OnConsentChange -> formState.update { it.copy(hasConsent = intent.hasConsent) }
            is SubmissionUiIntent.OnPhotosPicked -> checkPhotos(intent.uris)
            is SubmissionUiIntent.OnRemovePhoto -> formState.update { state ->
                state.copy(
                    photoUris = state.photoUris
                        .filterIndexed { index, _ -> index != intent.index }
                        .toPersistentList()
                )
            }
            SubmissionUiIntent.OnSendClick -> send()
        }
    }

    private fun observeVeteranName() = flow {
        emit("")
        val name = runCatching { veteransRepository.getAllVeterans().firstOrNull { it.id == veteranId }?.name }
            .getOrNull()
        emit(name.orEmpty())
    }

    private fun checkPhotos(uris: List<String>) {
        val state = formState.value
        val newUris = uris.distinct()
            .filterNot { it in state.photoUris }
            .take(SubmissionUiState.MAX_PHOTOS - state.photoUris.size - state.checkingPhotosCount)
        formState.update { it.copy(checkingPhotosCount = it.checkingPhotosCount + newUris.size, photoRejection = null) }
        newUris.forEach { uri ->
            viewModelScope.launch {
                val result = contentCheckRepository.checkPhoto(uri)
                formState.update { current -> current.withCheckedPhoto(uri, result) }
            }
        }
    }

    private fun SubmissionUiState.withCheckedPhoto(uri: String, result: PhotoCheckResult): SubmissionUiState {
        val isAccepted = result == PhotoCheckResult.ALLOWED && uri !in photoUris
        return copy(
            checkingPhotosCount = checkingPhotosCount - 1,
            photoUris = if (isAccepted) (photoUris + uri).toPersistentList() else photoUris,
            photoRejection = result.takeIf { it != PhotoCheckResult.ALLOWED } ?: photoRejection
        )
    }

    private fun send() {
        val state = formState.value
        if (!state.canSend) return
        val hasTextProfanity = contentCheckRepository.containsOffensiveText(state.text)
        val hasContactProfanity = contentCheckRepository.containsOffensiveText(state.contact)
        if (hasTextProfanity || hasContactProfanity) {
            formState.update {
                it.copy(hasTextProfanity = hasTextProfanity, hasContactProfanity = hasContactProfanity)
            }
            return
        }
        formState.update { it.copy(status = SubmissionStatus.SENDING) }
        viewModelScope.launch {
            runCatching {
                submissionsRepository.submit(
                    SubmissionDraft(
                        veteranId = veteranId,
                        text = state.text.trim(),
                        contact = state.contact.trim(),
                        photoUris = state.photoUris
                    )
                )
            }.onSuccess {
                formState.update { it.copy(status = SubmissionStatus.SENT) }
            }.onFailure { error ->
                Log.e(LOG_TAG, "Failed to send submission for $veteranId", error)
                formState.update { it.copy(status = SubmissionStatus.FAILED) }
            }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "SubmissionViewModel"
        private const val MAX_TEXT_LENGTH = 5000
    }
}
