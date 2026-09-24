package com.gorman.ourmemoryapp.ui.more.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.models.GoogleSignInResult
import com.gorman.ourmemoryapp.domain.repository.MyRequestsRepository
import com.gorman.ourmemoryapp.domain.repository.ReminderScheduler
import com.gorman.ourmemoryapp.domain.repository.SettingsRepository
import com.gorman.ourmemoryapp.domain.repository.VisitorAccountRepository
import com.gorman.ourmemoryapp.ui.more.models.MoreUiIntent
import com.gorman.ourmemoryapp.ui.more.models.MoreUiState
import com.gorman.ourmemoryapp.ui.more.models.SignInStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
    private val visitorAccountRepository: VisitorAccountRepository,
    myRequestsRepository: MyRequestsRepository
) : ViewModel() {

    private val signInStatus = MutableStateFlow(SignInStatus.IDLE)

    val uiState = combine(
        settingsRepository.observeSettings(),
        myRequestsRepository.observeUnseenCount(),
        visitorAccountRepository.observeAccount().catch { emit(null) },
        signInStatus
    ) { settings, unseenCount, account, status ->
        MoreUiState(
            settings = settings,
            unseenRequestsCount = unseenCount,
            account = account,
            signInStatus = status
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = MoreUiState()
    )

    fun onUiIntent(intent: MoreUiIntent) {
        when (intent) {
            MoreUiIntent.OnGoogleSignInStarted -> signInStatus.value = SignInStatus.IN_PROGRESS
            is MoreUiIntent.OnGoogleSignInFailed -> signInStatus.value = intent.status
            is MoreUiIntent.OnGoogleIdToken -> signInWithGoogle(intent.idToken)
            MoreUiIntent.OnSignOutClick -> {
                signInStatus.value = SignInStatus.IDLE
                visitorAccountRepository.signOut()
            }
            else -> updateSettings(intent)
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            val result = visitorAccountRepository.signInWithGoogle(idToken)
            signInStatus.value = if (result == GoogleSignInResult.SUCCESS) SignInStatus.IDLE else SignInStatus.FAILED
        }
    }

    private fun updateSettings(intent: MoreUiIntent) {
        viewModelScope.launch {
            when (intent) {
                is MoreUiIntent.OnThemeModeChange -> settingsRepository.setThemeMode(intent.mode)
                is MoreUiIntent.OnTextScaleChange -> settingsRepository.setTextScale(intent.scale)
                is MoreUiIntent.OnVictoryDayReminderChange -> {
                    settingsRepository.setVictoryDayReminder(intent.isEnabled)
                    reminderScheduler.setVictoryDayReminder(intent.isEnabled)
                }
                is MoreUiIntent.OnFavoriteRemindersChange ->
                    settingsRepository.setFavoriteReminders(intent.isEnabled)
                else -> Unit
            }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
