package com.gorman.ourmemoryapp.ui.admin.guide.models

sealed interface EditorGuideUiEvent {
    data class OnSectionClick(val section: GuideSection) : EditorGuideUiEvent
}
