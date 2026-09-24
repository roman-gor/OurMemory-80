package com.gorman.ourmemoryapp.ui.admin.tours.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.repository.ToursRepository
import com.gorman.ourmemoryapp.ui.admin.tours.models.AdminToursUiState
import com.gorman.ourmemoryapp.ui.admin.tours.models.toAdminItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AdminToursViewModel @Inject constructor(
    private val toursRepository: ToursRepository
) : ViewModel() {

    private val reloads = MutableStateFlow(0)

    val uiState = observeAdminToursUiState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = AdminToursUiState.Loading
    )

    fun onScreenResumed() {
        reloads.update { it + 1 }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAdminToursUiState() = reloads.flatMapLatest {
        flow {
            val items = toursRepository.getAllTours().sortedBy { it.title }.map { it.toAdminItemUi() }
            emit(AdminToursUiState.Success(items.toPersistentList()))
        }
    }.catch<AdminToursUiState> { error ->
        Log.e(LOG_TAG, "Failed to load tours", error)
        emit(AdminToursUiState.Error)
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "AdminToursViewModel"
    }
}
