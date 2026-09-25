package com.gorman.ourmemoryapp.ui.admin.tours.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.models.ContentLanguage
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.domain.models.TourStopTranslation
import com.gorman.ourmemoryapp.domain.repository.BurialsRepository
import com.gorman.ourmemoryapp.domain.repository.ContentEditorRepository
import com.gorman.ourmemoryapp.domain.repository.MediaRepository
import com.gorman.ourmemoryapp.domain.repository.ToursRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.admin.burials.models.AdminBurialItemUi
import com.gorman.ourmemoryapp.ui.admin.burials.models.toAdminItemUi
import com.gorman.ourmemoryapp.ui.admin.common.models.moved
import com.gorman.ourmemoryapp.ui.admin.tours.models.TourEditorUiIntent
import com.gorman.ourmemoryapp.ui.admin.tours.models.TourEditorUiState
import com.gorman.ourmemoryapp.ui.admin.tours.models.TourForm
import com.gorman.ourmemoryapp.ui.admin.tours.models.TourStopForm
import com.gorman.ourmemoryapp.ui.admin.tours.models.toForm
import com.gorman.ourmemoryapp.ui.admin.tours.models.toPreviewStops
import com.gorman.ourmemoryapp.ui.admin.tours.models.toTour
import com.gorman.ourmemoryapp.ui.admin.tours.models.withText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
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
class TourEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val toursRepository: ToursRepository,
    private val burialsRepository: BurialsRepository,
    private val veteransRepository: VeteransRepository,
    private val contentEditorRepository: ContentEditorRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val tourId = savedStateHandle.get<String>(Screen.AdminTourEditorScreen.TOUR_ID_ARG).orEmpty()
    private val isNew = tourId.isEmpty()
    private val editedForm = MutableStateFlow<TourForm?>(null)
    private val status = MutableStateFlow(EditorStatus())

    private val loadedEditor = flow { emit(runCatching { loadEditor() }) }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), replay = 1)

    val uiState = observeTourEditorUiState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = TourEditorUiState.Loading
    )

    fun onUiIntent(intent: TourEditorUiIntent) {
        when (intent) {
            is TourEditorUiIntent.OnLanguageChange -> updateForm { it.copy(language = intent.language) }
            is TourEditorUiIntent.OnTitleChange -> updateForm { form ->
                form.withText { it.copy(title = intent.title) }
            }
            is TourEditorUiIntent.OnDescriptionChange -> updateForm { form ->
                form.withText { it.copy(description = intent.description) }
            }
            is TourEditorUiIntent.OnAddStop -> updateStops { it + TourStopForm(burialId = intent.burialId) }
            is TourEditorUiIntent.OnStopTextChange -> updateStopText(intent.index) { it.copy(text = intent.text) }
            is TourEditorUiIntent.OnStopAudioPicked -> uploadStopAudio(intent.index, intent.uri)
            is TourEditorUiIntent.OnStopAudioRemove -> updateStopText(intent.index) { it.copy(audioUrl = "") }
            is TourEditorUiIntent.OnStopMove -> updateStops { it.moved(intent.index, intent.offset) }
            is TourEditorUiIntent.OnStopRemove -> updateStops { stops ->
                stops.filterIndexed { index, _ -> index != intent.index }
            }
            TourEditorUiIntent.OnSaveClick -> save()
            TourEditorUiIntent.OnDeleteConfirm -> delete()
        }
    }

    private suspend fun loadEditor(): LoadedEditor {
        val veterans = runCatching { veteransRepository.getAllVeterans() }.getOrDefault(emptyList())
        val namesByBurial = veterans.filter { it.burialId.isNotBlank() }.groupBy({ it.burialId }, { it.name })
        val burials = burialsRepository.getAllBurials().map { burial ->
            burial.toAdminItemUi(veteranNames = namesByBurial[burial.id].orEmpty().joinToString(NAMES_SEPARATOR))
        }
        val form = if (isNew) {
            TourForm(id = contentEditorRepository.newTourId())
        } else {
            toursRepository.getOriginalTours().first { it.id == tourId }.toForm()
        }
        return LoadedEditor(form = form, burials = burials.toPersistentList())
    }

    private fun observeTourEditorUiState() = combine(loadedEditor, editedForm, status) { result, form, status ->
        val loaded = result.getOrThrow()
        val currentForm = form ?: loaded.form
        TourEditorUiState.Editing(
            form = currentForm,
            burials = loaded.burials,
            previewStops = currentForm.stops.toPreviewStops(loaded.burials).toPersistentList(),
            isNew = isNew,
            isUploading = status.uploads > 0,
            isSaving = status.isSaving,
            hasFailed = status.hasFailed,
            isClosed = status.isClosed
        )
    }.catch<TourEditorUiState> { error ->
        Log.e(LOG_TAG, "Failed to open tour $tourId", error)
        emit(TourEditorUiState.Error)
    }

    private fun currentForm() = editedForm.value ?: loadedEditor.replayCache.firstOrNull()?.getOrNull()?.form

    private fun updateForm(transform: (TourForm) -> TourForm) {
        val form = currentForm() ?: return
        editedForm.value = transform(form)
    }

    private fun updateStops(transform: (List<TourStopForm>) -> List<TourStopForm>) {
        updateForm { it.copy(stops = transform(it.stops).toPersistentList()) }
    }

    private fun updateStop(index: Int, transform: (TourStopForm) -> TourStopForm) {
        updateStops { stops ->
            stops.mapIndexed { stopIndex, stop -> if (stopIndex == index) transform(stop) else stop }
        }
    }

    private fun updateStopText(
        index: Int,
        language: ContentLanguage? = currentForm()?.language,
        transform: (TourStopTranslation) -> TourStopTranslation
    ) {
        updateStop(index) { it.withText(language, transform) }
    }

    private fun uploadStopAudio(index: Int, uri: String) {
        val form = currentForm() ?: return
        val language = form.language
        status.update { it.copy(uploads = it.uploads + 1, hasFailed = false) }
        viewModelScope.launch {
            runCatching { mediaRepository.uploadAudio(uri, "$MEDIA_FOLDER/${form.id}") }
                .onSuccess { url -> updateStopText(index, language) { it.copy(audioUrl = url) } }
                .onFailure { error ->
                    Log.e(LOG_TAG, "Failed to upload audio for ${form.id}", error)
                    status.update { it.copy(hasFailed = true) }
                }
            status.update { it.copy(uploads = it.uploads - 1) }
        }
    }

    private fun save() {
        val form = currentForm() ?: return
        if (!form.isValid) return
        runWrite { contentEditorRepository.saveTour(form.toTour()) }
    }

    private fun delete() {
        if (isNew) return
        runWrite { contentEditorRepository.deleteTour(tourId) }
    }

    private fun runWrite(write: suspend () -> Unit) {
        if (status.value.isSaving || status.value.uploads > 0) return
        status.update { it.copy(isSaving = true, hasFailed = false) }
        viewModelScope.launch {
            runCatching { write() }
                .onSuccess { status.update { it.copy(isSaving = false, isClosed = true) } }
                .onFailure { error ->
                    Log.e(LOG_TAG, "Failed to save tour $tourId", error)
                    status.update { it.copy(isSaving = false, hasFailed = true) }
                }
        }
    }

    private data class LoadedEditor(
        val form: TourForm,
        val burials: ImmutableList<AdminBurialItemUi>
    )

    private data class EditorStatus(
        val uploads: Int = 0,
        val isSaving: Boolean = false,
        val hasFailed: Boolean = false,
        val isClosed: Boolean = false
    )

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "TourEditorViewModel"
        private const val MEDIA_FOLDER = "tours"
        private const val NAMES_SEPARATOR = ", "
    }
}
