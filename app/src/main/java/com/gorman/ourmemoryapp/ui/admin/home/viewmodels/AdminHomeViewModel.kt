package com.gorman.ourmemoryapp.ui.admin.home.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.di.annotation.IoDispatcher
import com.gorman.ourmemoryapp.domain.models.ModerationStatus
import com.gorman.ourmemoryapp.domain.repository.AuthRepository
import com.gorman.ourmemoryapp.domain.repository.BurialsRepository
import com.gorman.ourmemoryapp.domain.repository.FeedbackRepository
import com.gorman.ourmemoryapp.domain.repository.ModerationRepository
import com.gorman.ourmemoryapp.domain.repository.ToursRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.admin.home.models.AdminHomeUiEvent
import com.gorman.ourmemoryapp.ui.admin.home.models.AdminHomeUiState
import com.gorman.ourmemoryapp.ui.admin.home.models.ContentCounts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AdminHomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val feedbackRepository: FeedbackRepository,
    private val moderationRepository: ModerationRepository,
    private val veteransRepository: VeteransRepository,
    private val burialsRepository: BurialsRepository,
    private val toursRepository: ToursRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    val uiState = observeAdminHomeUiState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = AdminHomeUiState()
    )

    fun onUiEvent(event: AdminHomeUiEvent) {
        when (event) {
            AdminHomeUiEvent.OnSignOutClick -> authRepository.signOut()
        }
    }

    private fun observeAdminHomeUiState(): Flow<AdminHomeUiState> = combine(
        authRepository.observeSession(),
        moderationRepository.observeSubmissions().countOf { it.status == ModerationStatus.PENDING },
        feedbackRepository.observeFeedback().countOf { !it.isReviewed },
        observeContentCounts()
    ) { session, pendingSubmissionsCount, newFeedbackCount, contentCounts ->
        AdminHomeUiState(
            email = session.email,
            isSuperAdmin = session.isSuperAdmin,
            pendingSubmissionsCount = pendingSubmissionsCount,
            newFeedbackCount = newFeedbackCount,
            contentCounts = contentCounts
        )
    }

    private fun observeContentCounts() = flow {
        emit(
            ContentCounts(
                veterans = veteransRepository.getAllVeterans().size,
                burials = burialsRepository.getAllBurials().size,
                tours = toursRepository.getAllTours().size
            )
        )
    }
        .onStart { emit(ContentCounts()) }
        .catch { emit(ContentCounts()) }
        .flowOn(ioDispatcher)

    private fun <T> Flow<List<T>>.countOf(predicate: (T) -> Boolean) = map { items -> items.count(predicate) }
        .onStart { emit(0) }
        .catch { emit(0) }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
