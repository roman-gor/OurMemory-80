package com.gorman.ourmemoryapp.ui.more.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.repository.ReminderScheduler
import com.gorman.ourmemoryapp.domain.repository.SettingsRepository
import com.gorman.ourmemoryapp.ui.more.models.MoreUiIntent
import com.gorman.ourmemoryapp.ui.more.models.MoreUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    val uiState = settingsRepository.observeSettings()
        .map { MoreUiState(settings = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = MoreUiState()
        )

    fun onUiIntent(intent: MoreUiIntent) {
        viewModelScope.launch {
            when (intent) {
                is MoreUiIntent.OnThemeModeChange -> settingsRepository.setThemeMode(intent.mode)
                is MoreUiIntent.OnTextScaleChange -> settingsRepository.setTextScale(intent.scale)
                is MoreUiIntent.OnVictoryDayReminderChange -> {
                    settingsRepository.setVictoryDayReminder(intent.isEnabled)
                    reminderScheduler.setVictoryDayReminder(intent.isEnabled)
                }
                is MoreUiIntent.OnFavoriteRemindersChange -> settingsRepository.setFavoriteReminders(intent.isEnabled)
            }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
