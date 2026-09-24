package com.gorman.ourmemoryapp.ui.admin.veterans.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.admin.veterans.models.AdminVeteransUiEvent
import com.gorman.ourmemoryapp.ui.admin.veterans.models.AdminVeteransUiState
import com.gorman.ourmemoryapp.ui.admin.veterans.models.toAdminItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AdminVeteransViewModel @Inject constructor(
    private val veteransRepository: VeteransRepository
) : ViewModel() {

    private val search = MutableStateFlow("")
    private val reloads = MutableStateFlow(0)

    val uiState = observeAdminVeteransUiState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = AdminVeteransUiState.Loading
    )

    fun onUiEvent(event: AdminVeteransUiEvent) {
        when (event) {
            is AdminVeteransUiEvent.OnSearchChange -> search.value = event.search
            AdminVeteransUiEvent.OnScreenResumed -> reloads.update { it + 1 }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAdminVeteransUiState() = reloads
        .flatMapLatest { flow { emit(veteransRepository.getAllVeterans()) } }
        .combine(search) { veterans, search ->
            AdminVeteransUiState.Success(
                items = veterans
                    .filter { it.name.contains(search.trim(), ignoreCase = true) }
                    .sortedBy { it.id.toIntOrNull() ?: Int.MAX_VALUE }
                    .map { it.toAdminItemUi() }
                    .toPersistentList(),
                search = search
            )
        }
        .catch<AdminVeteransUiState> { error ->
            Log.e(LOG_TAG, "Failed to load veterans", error)
            emit(AdminVeteransUiState.Error)
        }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "AdminVeteransViewModel"
    }
}
