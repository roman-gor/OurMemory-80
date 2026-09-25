package com.gorman.ourmemoryapp.ui.admin.burials.models

import com.gorman.ourmemoryapp.domain.models.ContentLanguage
import com.gorman.ourmemoryapp.ui.common.models.BurialType

sealed interface BurialEditorUiIntent {
    data class OnLanguageChange(val language: ContentLanguage?) : BurialEditorUiIntent
    data class OnTypeChange(val type: BurialType) : BurialEditorUiIntent
    data class OnSectionChange(val section: String) : BurialEditorUiIntent
    data class OnRowChange(val row: String) : BurialEditorUiIntent
    data class OnPlaceChange(val place: String) : BurialEditorUiIntent
    data class OnDescriptionChange(val description: String) : BurialEditorUiIntent
    data class OnLatitudeChange(val latitude: String) : BurialEditorUiIntent
    data class OnLongitudeChange(val longitude: String) : BurialEditorUiIntent
    data class OnPointPicked(val latitude: Double, val longitude: Double) : BurialEditorUiIntent
    data class OnPhotoPicked(val uri: String) : BurialEditorUiIntent
    data object OnPhotoRemove : BurialEditorUiIntent
    data object OnSaveClick : BurialEditorUiIntent
}
