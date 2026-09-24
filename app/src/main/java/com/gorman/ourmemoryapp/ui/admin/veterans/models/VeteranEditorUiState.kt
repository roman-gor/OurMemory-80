package com.gorman.ourmemoryapp.ui.admin.veterans.models

import com.gorman.ourmemoryapp.ui.common.models.BurialUi
import kotlinx.collections.immutable.ImmutableList

sealed interface VeteranEditorUiState {
    data object Loading : VeteranEditorUiState
    data object Error : VeteranEditorUiState
    data class Editing(
        val form: VeteranForm,
        val burials: ImmutableList<BurialUi>,
        val isNew: Boolean,
        val isUploading: Boolean,
        val isSaving: Boolean,
        val hasFailed: Boolean,
        val isClosed: Boolean
    ) : VeteranEditorUiState {
        val isBusy = isUploading || isSaving
        val canSave = form.isValid && !isBusy
    }
}
