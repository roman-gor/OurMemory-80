package com.gorman.ourmemoryapp.ui.admin.admins.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.models.AddAdminResult
import com.gorman.ourmemoryapp.domain.repository.AdminsRepository
import com.gorman.ourmemoryapp.domain.repository.AuthRepository
import com.gorman.ourmemoryapp.ui.admin.admins.models.AddAdminStatus
import com.gorman.ourmemoryapp.ui.admin.admins.models.AdminItemUi
import com.gorman.ourmemoryapp.ui.admin.admins.models.AdminsUiIntent
import com.gorman.ourmemoryapp.ui.admin.admins.models.AdminsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminsViewModel @Inject constructor(
    private val adminsRepository: AdminsRepository,
    authRepository: AuthRepository
) : ViewModel() {

    private val addStatus = MutableStateFlow(AddAdminStatus.IDLE)

    val uiState = observeAdminsUiState(authRepository).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = AdminsUiState.Loading
    )

    fun onUiIntent(intent: AdminsUiIntent) {
        when (intent) {
            is AdminsUiIntent.OnAddClick -> addAdmin(intent.email)
            is AdminsUiIntent.OnRemoveConfirm -> removeAdmin(intent.uid)
            AdminsUiIntent.OnAddStatusShown -> addStatus.value = AddAdminStatus.IDLE
        }
    }

    private fun observeAdminsUiState(authRepository: AuthRepository): Flow<AdminsUiState> = combine(
        adminsRepository.observeAdmins(),
        authRepository.observeSession(),
        addStatus
    ) { admins, session, status ->
        AdminsUiState.Success(
            items = admins.map { admin ->
                AdminItemUi(
                    uid = admin.uid,
                    email = admin.email,
                    isSuperAdmin = admin.isSuperAdmin,
                    isCurrentUser = admin.uid == session.uid
                )
            }.toPersistentList(),
            addStatus = status
        ) as AdminsUiState
    }.catch { error ->
        Log.e(LOG_TAG, "Failed to load administrators", error)
        emit(AdminsUiState.Error)
    }

    private fun addAdmin(email: String) {
        if (email.isBlank() || addStatus.value == AddAdminStatus.ADDING) return
        addStatus.value = AddAdminStatus.ADDING
        viewModelScope.launch {
            addStatus.value = runCatching { adminsRepository.addAdmin(email.trim()) }
                .map { result ->
                    when (result) {
                        AddAdminResult.ADDED -> AddAdminStatus.ADDED
                        AddAdminResult.ACCOUNT_NOT_FOUND -> AddAdminStatus.ACCOUNT_NOT_FOUND
                        AddAdminResult.ALREADY_ADMIN -> AddAdminStatus.ALREADY_ADMIN
                    }
                }
                .onFailure { Log.e(LOG_TAG, "Failed to add administrator", it) }
                .getOrDefault(AddAdminStatus.FAILED)
        }
    }

    private fun removeAdmin(uid: String) {
        viewModelScope.launch {
            runCatching { adminsRepository.removeAdmin(uid) }
                .onFailure { Log.e(LOG_TAG, "Failed to remove administrator $uid", it) }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "AdminsViewModel"
    }
}
