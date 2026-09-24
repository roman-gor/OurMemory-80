package com.gorman.ourmemoryapp.ui.admin.moderation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.repository.ModerationRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.admin.moderation.models.ModerationListUiState
import com.gorman.ourmemoryapp.ui.admin.moderation.models.toItemUi
import com.gorman.ourmemoryapp.ui.common.models.observeVeteranNames
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ModerationListViewModel @Inject constructor(
    private val moderationRepository: ModerationRepository,
    private val veteransRepository: VeteransRepository
) : ViewModel() {

    val uiState = observeModerationListUiState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = ModerationListUiState.Loading
    )

    private fun observeModerationListUiState() =
        combine(
            moderationRepository.observeSubmissions(),
            veteransRepository.observeVeteranNames()
        ) { submissions, names ->
            ModerationListUiState.Success(
                submissions.map { it.toItemUi(veteranName = names[it.veteranId].orEmpty()) }.toPersistentList()
            )
        }.catch<ModerationListUiState> { error ->
            Log.e(LOG_TAG, "Failed to load submissions", error)
            emit(ModerationListUiState.Error)
        }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "ModerationListViewModel"
    }
}
