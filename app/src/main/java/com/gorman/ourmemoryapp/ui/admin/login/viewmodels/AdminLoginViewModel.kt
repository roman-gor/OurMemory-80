package com.gorman.ourmemoryapp.ui.admin.login.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.models.SignInResult
import com.gorman.ourmemoryapp.domain.repository.AuthRepository
import com.gorman.ourmemoryapp.ui.admin.login.models.AdminLoginError
import com.gorman.ourmemoryapp.ui.admin.login.models.AdminLoginUiIntent
import com.gorman.ourmemoryapp.ui.admin.login.models.AdminLoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminLoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminLoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onUiIntent(intent: AdminLoginUiIntent) {
        when (intent) {
            is AdminLoginUiIntent.OnEmailChange -> _uiState.update { it.copy(email = intent.email, error = null) }
            is AdminLoginUiIntent.OnPasswordChange -> _uiState.update {
                it.copy(password = intent.password, error = null)
            }
            AdminLoginUiIntent.OnSignInClick -> signIn()
        }
    }

    private fun signIn() {
        val state = _uiState.value
        if (!state.canSignIn) return
        _uiState.update { it.copy(isSigningIn = true, error = null) }
        viewModelScope.launch {
            runCatching { authRepository.signIn(state.email.trim(), state.password) }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isSigningIn = false,
                            isSignedIn = result == SignInResult.ADMIN,
                            error = result.toError()
                        )
                    }
                }
                .onFailure { error ->
                    Log.e(LOG_TAG, "Failed to sign in", error)
                    _uiState.update { it.copy(isSigningIn = false, error = AdminLoginError.CONNECTION) }
                }
        }
    }

    private fun SignInResult.toError() = when (this) {
        SignInResult.ADMIN -> null
        SignInResult.NOT_ADMIN -> AdminLoginError.NO_ADMIN_RIGHTS
        SignInResult.WRONG_CREDENTIALS -> AdminLoginError.WRONG_CREDENTIALS
    }

    companion object {
        private const val LOG_TAG = "AdminLoginViewModel"
    }
}
