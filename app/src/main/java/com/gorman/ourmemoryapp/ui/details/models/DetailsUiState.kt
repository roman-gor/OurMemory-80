package com.gorman.ourmemoryapp.ui.details.models

import com.gorman.ourmemoryapp.domain.models.AudioItem
import com.gorman.ourmemoryapp.domain.models.Veteran
import kotlinx.collections.immutable.ImmutableList

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Success(
        val veteran: Veteran,
        val rewards: ImmutableList<Reward>,
        val paragraphs: ImmutableList<String>,
        val media: ImmutableList<MediaUi>,
        val audio: AudioItem?,
        val burial: BurialUi?
    ) : DetailsUiState
    data object Error : DetailsUiState
}
