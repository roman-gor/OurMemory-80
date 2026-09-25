package com.gorman.ourmemoryapp.ui.admin.veterans.models

import com.gorman.ourmemoryapp.domain.models.ContentLanguage
import com.gorman.ourmemoryapp.ui.details.models.Reward

sealed interface VeteranEditorUiIntent {
    data class OnLanguageChange(val language: ContentLanguage?) : VeteranEditorUiIntent
    data object OnCopyBlocksFromOriginal : VeteranEditorUiIntent
    data class OnNameChange(val name: String) : VeteranEditorUiIntent
    data class OnYearsChange(val years: String) : VeteranEditorUiIntent
    data class OnCategoryChange(val category: VeteranCategory) : VeteranEditorUiIntent
    data class OnBaseInfoChange(val text: String) : VeteranEditorUiIntent
    data class OnAllInfoChange(val text: String) : VeteranEditorUiIntent
    data class OnRewardCountChange(val reward: Reward, val delta: Int) : VeteranEditorUiIntent
    data class OnBirthDateChange(val date: String) : VeteranEditorUiIntent
    data class OnDeathDateChange(val date: String) : VeteranEditorUiIntent
    data class OnBurialChange(val burialId: String) : VeteranEditorUiIntent
    data class OnPortraitPicked(val uri: String) : VeteranEditorUiIntent
    data class OnAudioPicked(val uri: String) : VeteranEditorUiIntent
    data object OnAudioRemove : VeteranEditorUiIntent
    data object OnAddParagraph : VeteranEditorUiIntent
    data class OnMediaPicked(val uri: String) : VeteranEditorUiIntent
    data class OnBlockChange(val index: Int, val block: InfoBlock) : VeteranEditorUiIntent
    data class OnBlockMove(val index: Int, val offset: Int) : VeteranEditorUiIntent
    data class OnBlockRemove(val index: Int) : VeteranEditorUiIntent
    data object OnSaveClick : VeteranEditorUiIntent
    data object OnDeleteConfirm : VeteranEditorUiIntent
}
