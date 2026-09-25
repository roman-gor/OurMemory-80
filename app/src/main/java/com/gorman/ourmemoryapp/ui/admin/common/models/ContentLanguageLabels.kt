package com.gorman.ourmemoryapp.ui.admin.common.models

import androidx.annotation.StringRes
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.ContentLanguage

val editableContentLanguages: List<ContentLanguage?> = listOf(null) + ContentLanguage.entries

@get:StringRes
val ContentLanguage?.labelRes: Int
    get() = when (this) {
        null -> R.string.russian
        ContentLanguage.BELARUSIAN -> R.string.belarusian
        ContentLanguage.ENGLISH -> R.string.english
        ContentLanguage.CHINESE -> R.string.chinese
    }
