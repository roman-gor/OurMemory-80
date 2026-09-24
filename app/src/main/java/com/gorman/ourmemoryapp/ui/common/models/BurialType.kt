package com.gorman.ourmemoryapp.ui.common.models

import androidx.annotation.StringRes
import com.gorman.ourmemoryapp.R

enum class BurialType(val value: String, @param:StringRes val nameRes: Int) {
    GRAVE("GRAVE", R.string.grave),
    MASS_GRAVE("MASS_GRAVE", R.string.mass_grave),
    MONUMENT("MONUMENT", R.string.monument);

    companion object {
        fun fromValue(value: String) = entries.firstOrNull { it.value == value } ?: GRAVE
    }
}
