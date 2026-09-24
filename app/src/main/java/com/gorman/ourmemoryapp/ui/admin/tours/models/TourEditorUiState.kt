package com.gorman.ourmemoryapp.ui.admin.tours.models

import com.gorman.ourmemoryapp.ui.admin.burials.models.AdminBurialItemUi
import com.gorman.ourmemoryapp.ui.tours.models.TourStopUi
import kotlinx.collections.immutable.ImmutableList

sealed interface TourEditorUiState {
    data object Loading : TourEditorUiState
    data object Error : TourEditorUiState
    data class Editing(
        val form: TourForm,
        val burials: ImmutableList<AdminBurialItemUi>,
        val previewStops: ImmutableList<TourStopUi>,
        val isNew: Boolean,
        val isUploading: Boolean,
        val isSaving: Boolean,
        val hasFailed: Boolean,
        val isClosed: Boolean
    ) : TourEditorUiState {
        val isBusy = isUploading || isSaving
        val canSave = form.isValid && !isBusy
    }
}
