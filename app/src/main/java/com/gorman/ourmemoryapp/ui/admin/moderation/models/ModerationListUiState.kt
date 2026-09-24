package com.gorman.ourmemoryapp.ui.admin.moderation.models

import kotlinx.collections.immutable.ImmutableList

sealed interface ModerationListUiState {
    data object Loading : ModerationListUiState
    data object Error : ModerationListUiState
    data class Success(val items: ImmutableList<SubmissionItemUi>) : ModerationListUiState
}
