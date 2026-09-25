package com.gorman.ourmemoryapp.domain.models

data class Veteran(
    val id: String = "",
    val name: String = "",
    val portrait: String = "",
    val baseInfo: String = "",
    val allInfo: String = "",
    val years: String = "",
    val category: String = "",
    val rewards: String = "",
    val veteransInfo: List<String> = emptyList(),
    val burialId: String = "",
    val audioUrl: String = "",
    val birthDate: String = "",
    val deathDate: String = "",
    val translations: Map<String, VeteranTranslation> = emptyMap()
)
