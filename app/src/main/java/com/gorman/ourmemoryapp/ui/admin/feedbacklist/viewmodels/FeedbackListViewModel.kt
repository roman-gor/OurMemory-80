package com.gorman.ourmemoryapp.ui.admin.feedbacklist.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.repository.FeedbackRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.admin.feedbacklist.models.FeedbackListUiEvent
import com.gorman.ourmemoryapp.ui.admin.feedbacklist.models.FeedbackListUiState
import com.gorman.ourmemoryapp.ui.admin.feedbacklist.models.toUi
import com.gorman.ourmemoryapp.ui.common.models.observeVeteranNames
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackListViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository,
    private val veteransRepository: VeteransRepository
) : ViewModel() {

    val uiState = observeFeedbackListUiState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = FeedbackListUiState.Loading
    )

    fun onUiEvent(event: FeedbackListUiEvent) {
        when (event) {
            is FeedbackListUiEvent.OnMarkReviewedClick -> markReviewed(event.feedbackId)
            is FeedbackListUiEvent.OnReplyClick -> reply(event.feedbackId, event.reply)
        }
    }

    private fun observeFeedbackListUiState(): Flow<FeedbackListUiState> =
        combine(feedbackRepository.observeFeedback(), veteransRepository.observeVeteranNames()) { items, names ->
            FeedbackListUiState.Success(
                items.map { it.toUi(veteranName = names[it.veteranId].orEmpty()) }.toPersistentList()
            )
        }.catch<FeedbackListUiState> { error ->
            Log.e(LOG_TAG, "Failed to load feedback", error)
            emit(FeedbackListUiState.Error)
        }

    private fun markReviewed(feedbackId: String) {
        viewModelScope.launch {
            runCatching { feedbackRepository.markReviewed(feedbackId) }
                .onFailure { Log.e(LOG_TAG, "Failed to mark $feedbackId as reviewed", it) }
        }
    }

    private fun reply(feedbackId: String, reply: String) {
        if (reply.isBlank()) return
        viewModelScope.launch {
            runCatching { feedbackRepository.reply(feedbackId, reply.trim()) }
                .onFailure { Log.e(LOG_TAG, "Failed to reply to $feedbackId", it) }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "FeedbackListViewModel"
    }
}
