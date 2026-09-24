package com.gorman.ourmemoryapp.ui.admin.feedbacklist.models

import kotlinx.collections.immutable.ImmutableList

sealed interface FeedbackListUiState {
    data object Loading : FeedbackListUiState
    data object Error : FeedbackListUiState
    data class Success(val items: ImmutableList<FeedbackItemUi>) : FeedbackListUiState
}
