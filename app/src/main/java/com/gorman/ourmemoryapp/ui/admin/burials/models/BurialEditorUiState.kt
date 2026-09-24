package com.gorman.ourmemoryapp.ui.admin.burials.models

sealed interface BurialEditorUiState {
    data object Loading : BurialEditorUiState
    data object Error : BurialEditorUiState
    data class Editing(
        val form: BurialForm,
        val isNew: Boolean,
        val isUploading: Boolean,
        val isSaving: Boolean,
        val hasFailed: Boolean,
        val isClosed: Boolean
    ) : BurialEditorUiState {
        val isBusy = isUploading || isSaving
        val canSave = form.isValid && !isBusy
    }
}
