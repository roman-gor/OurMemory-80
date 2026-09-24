package com.gorman.ourmemoryapp.ui.admin.guide.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.ui.admin.guide.models.EditorGuideUiEvent
import com.gorman.ourmemoryapp.ui.admin.guide.models.EditorGuideUiState
import com.gorman.ourmemoryapp.ui.admin.guide.models.GuideSection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class EditorGuideViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val expandedSections = MutableStateFlow(
        setOfNotNull(
            savedStateHandle.get<String>(Screen.AdminGuideScreen.SECTION_ARG)?.let { name ->
                GuideSection.entries.firstOrNull { it.name == name }
            }
        )
    )

    val uiState = expandedSections
        .map { EditorGuideUiState(expanded = it.toImmutableSet()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = EditorGuideUiState(expanded = expandedSections.value.toImmutableSet())
        )

    fun onUiEvent(event: EditorGuideUiEvent) {
        when (event) {
            is EditorGuideUiEvent.OnSectionClick -> expandedSections.update { expanded ->
                if (event.section in expanded) expanded - event.section else expanded + event.section
            }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
