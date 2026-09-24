package com.gorman.ourmemoryapp.ui.admin.veterans.models

import androidx.annotation.StringRes
import com.gorman.ourmemoryapp.R

enum class VeteranCategory(val value: String, @param:StringRes val labelRes: Int) {
    WAR("War", R.string.heroUSSR),
    ART("Art", R.string.art);

    companion object {
        fun fromValue(value: String) = entries.firstOrNull { it.value == value } ?: WAR
    }
}
