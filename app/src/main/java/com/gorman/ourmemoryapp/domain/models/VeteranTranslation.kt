package com.gorman.ourmemoryapp.domain.models

data class VeteranTranslation(
    val name: String = "",
    val baseInfo: String = "",
    val allInfo: String = "",
    val veteransInfo: List<String> = emptyList()
)
