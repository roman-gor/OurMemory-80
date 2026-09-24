package com.gorman.ourmemoryapp.ui.admin.tours.models

sealed interface TourEditorUiIntent {
    data class OnTitleChange(val title: String) : TourEditorUiIntent
    data class OnDescriptionChange(val description: String) : TourEditorUiIntent
    data class OnAddStop(val burialId: String) : TourEditorUiIntent
    data class OnStopTextChange(val index: Int, val text: String) : TourEditorUiIntent
    data class OnStopAudioPicked(val index: Int, val uri: String) : TourEditorUiIntent
    data class OnStopAudioRemove(val index: Int) : TourEditorUiIntent
    data class OnStopMove(val index: Int, val offset: Int) : TourEditorUiIntent
    data class OnStopRemove(val index: Int) : TourEditorUiIntent
    data object OnSaveClick : TourEditorUiIntent
    data object OnDeleteConfirm : TourEditorUiIntent
}
