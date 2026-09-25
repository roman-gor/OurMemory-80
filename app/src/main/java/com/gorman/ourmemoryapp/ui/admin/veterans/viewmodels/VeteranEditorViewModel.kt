package com.gorman.ourmemoryapp.ui.admin.veterans.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.domain.repository.BurialsRepository
import com.gorman.ourmemoryapp.domain.repository.ContentEditorRepository
import com.gorman.ourmemoryapp.domain.repository.MediaRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.admin.common.models.moved
import com.gorman.ourmemoryapp.ui.admin.veterans.models.InfoBlock
import com.gorman.ourmemoryapp.ui.admin.veterans.models.VeteranEditorUiIntent
import com.gorman.ourmemoryapp.ui.admin.veterans.models.VeteranEditorUiState
import com.gorman.ourmemoryapp.ui.admin.veterans.models.VeteranForm
import com.gorman.ourmemoryapp.ui.admin.veterans.models.VeteranTextForm
import com.gorman.ourmemoryapp.ui.admin.veterans.models.nextVeteranId
import com.gorman.ourmemoryapp.ui.admin.veterans.models.toForm
import com.gorman.ourmemoryapp.ui.admin.veterans.models.toVeteran
import com.gorman.ourmemoryapp.ui.admin.veterans.models.withText
import com.gorman.ourmemoryapp.ui.common.models.BurialUi
import com.gorman.ourmemoryapp.ui.common.models.toExternalModel
import com.gorman.ourmemoryapp.ui.details.models.Reward
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableMap
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
class VeteranEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val veteransRepository: VeteransRepository,
    private val burialsRepository: BurialsRepository,
    private val contentEditorRepository: ContentEditorRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val veteranId =
        savedStateHandle.get<String>(Screen.AdminVeteranEditorScreen.VETERAN_ID_ARG).orEmpty()
    private val isNew = veteranId.isEmpty()
    private val editedForm = MutableStateFlow<VeteranForm?>(null)
    private val status = MutableStateFlow(EditorStatus())

    private val loadedEditor = flow { emit(runCatching { loadEditor() }) }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), replay = 1)

    val uiState = observeVeteranEditorUiState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = VeteranEditorUiState.Loading
    )

    fun onUiIntent(intent: VeteranEditorUiIntent) {
        when (intent) {
            is VeteranEditorUiIntent.OnPortraitPicked,
            is VeteranEditorUiIntent.OnAudioPicked,
            VeteranEditorUiIntent.OnAudioRemove,
            is VeteranEditorUiIntent.OnMediaPicked -> onMediaIntent(intent)
            VeteranEditorUiIntent.OnAddParagraph,
            VeteranEditorUiIntent.OnCopyBlocksFromOriginal,
            is VeteranEditorUiIntent.OnBlockChange,
            is VeteranEditorUiIntent.OnBlockMove,
            is VeteranEditorUiIntent.OnBlockRemove -> onBlockIntent(intent)
            VeteranEditorUiIntent.OnSaveClick -> save()
            VeteranEditorUiIntent.OnDeleteConfirm -> delete()
            else -> onFieldIntent(intent)
        }
    }

    private fun onFieldIntent(intent: VeteranEditorUiIntent) {
        when (intent) {
            is VeteranEditorUiIntent.OnLanguageChange -> updateForm { it.copy(language = intent.language) }
            is VeteranEditorUiIntent.OnNameChange -> updateText { it.copy(name = intent.name) }
            is VeteranEditorUiIntent.OnYearsChange -> updateForm { it.copy(years = intent.years) }
            is VeteranEditorUiIntent.OnCategoryChange -> updateForm { it.copy(category = intent.category) }
            is VeteranEditorUiIntent.OnBaseInfoChange -> updateText { it.copy(baseInfo = intent.text) }
            is VeteranEditorUiIntent.OnAllInfoChange -> updateText { it.copy(allInfo = intent.text) }
            is VeteranEditorUiIntent.OnRewardCountChange -> changeRewardCount(intent.reward, intent.delta)
            is VeteranEditorUiIntent.OnBirthDateChange -> updateForm { it.copy(birthDate = intent.date) }
            is VeteranEditorUiIntent.OnDeathDateChange -> updateForm { it.copy(deathDate = intent.date) }
            is VeteranEditorUiIntent.OnBurialChange -> updateForm { it.copy(burialId = intent.burialId) }
            else -> Unit
        }
    }

    private fun onMediaIntent(intent: VeteranEditorUiIntent) {
        when (intent) {
            is VeteranEditorUiIntent.OnPortraitPicked -> upload({ uploadPhoto(intent.uri, it) }) { form, url ->
                form.copy(portrait = url)
            }
            is VeteranEditorUiIntent.OnAudioPicked -> upload({ uploadAudio(intent.uri, it) }) { form, url ->
                form.copy(audioUrl = url)
            }
            VeteranEditorUiIntent.OnAudioRemove -> updateForm { it.copy(audioUrl = "") }
            is VeteranEditorUiIntent.OnMediaPicked -> upload({ uploadPhoto(intent.uri, it) }) { form, url ->
                form.withText { it.copy(blocks = (it.blocks + InfoBlock.Media(url = url, caption = "")).toPersistentList()) }
            }
            else -> Unit
        }
    }

    private fun onBlockIntent(intent: VeteranEditorUiIntent) {
        when (intent) {
            VeteranEditorUiIntent.OnAddParagraph -> updateBlocks { it + InfoBlock.Paragraph("") }
            VeteranEditorUiIntent.OnCopyBlocksFromOriginal -> updateForm { form ->
                form.withText { it.copy(blocks = form.blocks) }
            }
            is VeteranEditorUiIntent.OnBlockChange -> updateBlocks { blocks ->
                blocks.mapIndexed { index, block -> if (index == intent.index) intent.block else block }
            }
            is VeteranEditorUiIntent.OnBlockMove -> updateBlocks { it.moved(intent.index, intent.offset) }
            is VeteranEditorUiIntent.OnBlockRemove -> updateBlocks { blocks ->
                blocks.filterIndexed { index, _ -> index != intent.index }
            }
            else -> Unit
        }
    }

    private suspend fun loadEditor(): LoadedEditor {
        if (isNew) veteransRepository.invalidate()
        val veterans = veteransRepository.getOriginalVeterans()
        val burials = runCatching { burialsRepository.getAllBurials() }
            .onFailure { Log.e(LOG_TAG, "Failed to load burials", it) }
            .getOrDefault(emptyList())
        val form = if (isNew) {
            VeteranForm(id = veterans.nextVeteranId())
        } else {
            veterans.first { it.id == veteranId }.toForm()
        }
        return LoadedEditor(form = form, burials = burials.map { it.toExternalModel() }.toPersistentList())
    }

    private fun observeVeteranEditorUiState() = combine(loadedEditor, editedForm, status) { result, form, status ->
        val loaded = result.getOrThrow()
        VeteranEditorUiState.Editing(
            form = form ?: loaded.form,
            burials = loaded.burials,
            isNew = isNew,
            isUploading = status.uploads > 0,
            isSaving = status.isSaving,
            hasFailed = status.hasFailed,
            isClosed = status.isClosed
        )
    }.catch<VeteranEditorUiState> { error ->
        Log.e(LOG_TAG, "Failed to open veteran $veteranId", error)
        emit(VeteranEditorUiState.Error)
    }

    private fun currentForm() = editedForm.value ?: loadedEditor.replayCache.firstOrNull()?.getOrNull()?.form

    private fun updateForm(transform: (VeteranForm) -> VeteranForm) {
        val form = currentForm() ?: return
        editedForm.value = transform(form)
    }

    private fun updateText(transform: (VeteranTextForm) -> VeteranTextForm) {
        updateForm { it.withText(transform) }
    }

    private fun updateBlocks(transform: (List<InfoBlock>) -> List<InfoBlock>) {
        updateText { it.copy(blocks = transform(it.blocks).toPersistentList()) }
    }

    private fun changeRewardCount(reward: Reward, delta: Int) {
        updateForm { form ->
            val count = ((form.rewards[reward] ?: 0) + delta).coerceAtLeast(0)
            val rewards = if (count == 0) form.rewards - reward else form.rewards + (reward to count)
            form.copy(rewards = rewards.toImmutableMap())
        }
    }

    private fun upload(
        request: suspend MediaRepository.(String) -> String,
        apply: (VeteranForm, String) -> VeteranForm
    ) {
        val form = currentForm() ?: return
        status.update { it.copy(uploads = it.uploads + 1, hasFailed = false) }
        viewModelScope.launch {
            runCatching { mediaRepository.request("$MEDIA_FOLDER/${form.id}") }
                .onSuccess { url -> updateForm { apply(it, url) } }
                .onFailure { error ->
                    Log.e(LOG_TAG, "Failed to upload media for ${form.id}", error)
                    status.update { it.copy(hasFailed = true) }
                }
            status.update { it.copy(uploads = it.uploads - 1) }
        }
    }

    private fun save() {
        val form = currentForm() ?: return
        if (!form.isValid) return
        runWrite { contentEditorRepository.saveVeteran(form.toVeteran()) }
    }

    private fun delete() {
        if (isNew) return
        runWrite { contentEditorRepository.deleteVeteran(veteranId) }
    }

    private fun runWrite(write: suspend () -> Unit) {
        if (status.value.isSaving || status.value.uploads > 0) return
        status.update { it.copy(isSaving = true, hasFailed = false) }
        viewModelScope.launch {
            runCatching { write() }
                .onSuccess { status.update { it.copy(isSaving = false, isClosed = true) } }
                .onFailure { error ->
                    Log.e(LOG_TAG, "Failed to save veteran $veteranId", error)
                    status.update { it.copy(isSaving = false, hasFailed = true) }
                }
        }
    }

    private data class LoadedEditor(
        val form: VeteranForm,
        val burials: ImmutableList<BurialUi>
    )

    private data class EditorStatus(
        val uploads: Int = 0,
        val isSaving: Boolean = false,
        val hasFailed: Boolean = false,
        val isClosed: Boolean = false
    )

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "VeteranEditorViewModel"
        private const val MEDIA_FOLDER = "veterans"
    }
}
