package com.gorman.ourmemoryapp.domain.models

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    fun isDark(isSystemDark: Boolean) = when (this) {
        SYSTEM -> isSystemDark
        LIGHT -> false
        DARK -> true
    }
}
