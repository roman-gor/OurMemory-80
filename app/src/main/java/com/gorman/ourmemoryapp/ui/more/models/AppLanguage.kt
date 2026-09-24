package com.gorman.ourmemoryapp.ui.more.models

import androidx.annotation.StringRes
import com.gorman.ourmemoryapp.R

enum class AppLanguage(val tag: String, @param:StringRes val labelRes: Int) {
    RUSSIAN("ru", R.string.russian),
    BELARUSIAN("be", R.string.belarusian)
}
