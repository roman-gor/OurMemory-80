package com.gorman.ourmemoryapp.ui.admin.home.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.repository.AuthRepository
import com.gorman.ourmemoryapp.ui.admin.home.models.AdminHomeUiEvent
import com.gorman.ourmemoryapp.ui.admin.home.models.AdminHomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AdminHomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState = authRepository.observeSession()
        .map { AdminHomeUiState(email = it.email) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = AdminHomeUiState()
        )

    fun onUiEvent(event: AdminHomeUiEvent) {
        when (event) {
            AdminHomeUiEvent.OnSignOutClick -> authRepository.signOut()
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
