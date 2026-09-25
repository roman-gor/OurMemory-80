package com.gorman.ourmemoryapp.ui.admin.burials.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.domain.repository.BurialsRepository
import com.gorman.ourmemoryapp.domain.repository.ContentEditorRepository
import com.gorman.ourmemoryapp.domain.repository.MediaRepository
import com.gorman.ourmemoryapp.ui.admin.burials.models.BurialEditorUiIntent
import com.gorman.ourmemoryapp.ui.admin.burials.models.BurialEditorUiState
import com.gorman.ourmemoryapp.ui.admin.burials.models.BurialForm
import com.gorman.ourmemoryapp.ui.admin.burials.models.toBurial
import com.gorman.ourmemoryapp.ui.admin.burials.models.toCoordinateText
import com.gorman.ourmemoryapp.ui.admin.burials.models.toForm
import com.gorman.ourmemoryapp.ui.admin.burials.models.withDescription
import com.gorman.ourmemoryapp.ui.common.models.CemeteryLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BurialEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val burialsRepository: BurialsRepository,
    private val contentEditorRepository: ContentEditorRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val burialId = savedStateHandle.get<String>(Screen.AdminBurialEditorScreen.BURIAL_ID_ARG).orEmpty()
    private val isNew = burialId.isEmpty()
    private val editedForm = MutableStateFlow<BurialForm?>(null)
    private val status = MutableStateFlow(EditorStatus())

    private val loadedForm = flow { emit(runCatching { loadForm() }) }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), replay = 1)

    val uiState = observeBurialEditorUiState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = BurialEditorUiState.Loading
    )

    fun onUiIntent(intent: BurialEditorUiIntent) {
        when (intent) {
            is BurialEditorUiIntent.OnLanguageChange -> updateForm { it.copy(language = intent.language) }
            is BurialEditorUiIntent.OnTypeChange -> updateForm { it.copy(type = intent.type) }
            is BurialEditorUiIntent.OnSectionChange -> updateForm { it.copy(section = intent.section) }
            is BurialEditorUiIntent.OnRowChange -> updateForm { it.copy(row = intent.row) }
            is BurialEditorUiIntent.OnPlaceChange -> updateForm { it.copy(place = intent.place) }
            is BurialEditorUiIntent.OnDescriptionChange -> updateForm { it.withDescription(intent.description) }
            is BurialEditorUiIntent.OnLatitudeChange -> updateForm { it.copy(latitude = intent.latitude) }
            is BurialEditorUiIntent.OnLongitudeChange -> updateForm { it.copy(longitude = intent.longitude) }
            is BurialEditorUiIntent.OnPointPicked -> updateForm {
                it.copy(latitude = intent.latitude.toCoordinateText(), longitude = intent.longitude.toCoordinateText())
            }
            is BurialEditorUiIntent.OnPhotoPicked -> uploadPhoto(intent.uri)
            BurialEditorUiIntent.OnPhotoRemove -> updateForm { it.copy(photo = "") }
            BurialEditorUiIntent.OnSaveClick -> save()
        }
    }

    private suspend fun loadForm(): BurialForm = if (isNew) {
        BurialForm(
            id = contentEditorRepository.newBurialId(),
            latitude = CemeteryLocation.LATITUDE.toCoordinateText(),
            longitude = CemeteryLocation.LONGITUDE.toCoordinateText()
        )
    } else {
        burialsRepository.getOriginalBurials().first { it.id == burialId }.toForm()
    }

    private fun observeBurialEditorUiState() = combine(loadedForm, editedForm, status) { result, form, status ->
        BurialEditorUiState.Editing(
            form = form ?: result.getOrThrow(),
            isNew = isNew,
            isUploading = status.isUploading,
            isSaving = status.isSaving,
            hasFailed = status.hasFailed,
            isClosed = status.isClosed
        )
    }.catch<BurialEditorUiState> { error ->
        Log.e(LOG_TAG, "Failed to open burial $burialId", error)
        emit(BurialEditorUiState.Error)
    }

    private fun currentForm() = editedForm.value ?: loadedForm.replayCache.firstOrNull()?.getOrNull()

    private fun updateForm(transform: (BurialForm) -> BurialForm) {
        val form = currentForm() ?: return
        editedForm.value = transform(form)
    }

    private fun uploadPhoto(uri: String) {
        val form = currentForm() ?: return
        if (status.value.isUploading) return
        status.update { it.copy(isUploading = true, hasFailed = false) }
        viewModelScope.launch {
            runCatching { mediaRepository.uploadPhoto(uri, "$MEDIA_FOLDER/${form.id}") }
                .onSuccess { url -> updateForm { it.copy(photo = url) } }
                .onFailure { error ->
                    Log.e(LOG_TAG, "Failed to upload photo for ${form.id}", error)
                    status.update { it.copy(hasFailed = true) }
                }
            status.update { it.copy(isUploading = false) }
        }
    }

    private fun save() {
        val form = currentForm() ?: return
        if (!form.isValid || status.value.isSaving || status.value.isUploading) return
        status.update { it.copy(isSaving = true, hasFailed = false) }
        viewModelScope.launch {
            runCatching { contentEditorRepository.saveBurial(form.toBurial()) }
                .onSuccess { status.update { it.copy(isSaving = false, isClosed = true) } }
                .onFailure { error ->
                    Log.e(LOG_TAG, "Failed to save burial ${form.id}", error)
                    status.update { it.copy(isSaving = false, hasFailed = true) }
                }
        }
    }

    private data class EditorStatus(
        val isUploading: Boolean = false,
        val isSaving: Boolean = false,
        val hasFailed: Boolean = false,
        val isClosed: Boolean = false
    )

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "BurialEditorViewModel"
        private const val MEDIA_FOLDER = "burials"
    }
}
