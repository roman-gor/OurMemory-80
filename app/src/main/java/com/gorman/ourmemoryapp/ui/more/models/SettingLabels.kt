package com.gorman.ourmemoryapp.ui.more.models

import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.TextScale
import com.gorman.ourmemoryapp.domain.models.ThemeMode

val ThemeMode.labelRes
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.as_in_system
        ThemeMode.LIGHT -> R.string.light
        ThemeMode.DARK -> R.string.dark
    }

val TextScale.labelRes
    get() = when (this) {
        TextScale.NORMAL -> R.string.normal
        TextScale.LARGE -> R.string.large
        TextScale.EXTRA_LARGE -> R.string.extra_large
    }
