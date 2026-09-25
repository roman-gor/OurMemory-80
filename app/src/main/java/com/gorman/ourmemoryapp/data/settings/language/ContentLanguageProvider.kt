package com.gorman.ourmemoryapp.data.settings.language

import com.gorman.ourmemoryapp.domain.models.ContentLanguage

fun interface ContentLanguageProvider {
    fun current(): ContentLanguage?
}
