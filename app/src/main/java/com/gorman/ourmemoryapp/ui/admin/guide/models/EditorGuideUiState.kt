package com.gorman.ourmemoryapp.ui.admin.guide.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList

data class EditorGuideUiState(
    val sections: ImmutableList<GuideSection> = GuideSection.entries.toImmutableList(),
    val expanded: ImmutableSet<GuideSection> = persistentSetOf()
)
