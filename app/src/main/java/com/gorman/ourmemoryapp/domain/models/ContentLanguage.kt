package com.gorman.ourmemoryapp.domain.models

enum class ContentLanguage(val key: String) {
    BELARUSIAN("be"),
    ENGLISH("en"),
    CHINESE("zh");

    companion object {
        fun fromLanguage(language: String): ContentLanguage? = entries.firstOrNull { it.key == language }
    }
}
