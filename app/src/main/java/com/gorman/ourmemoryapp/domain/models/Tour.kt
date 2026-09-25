package com.gorman.ourmemoryapp.domain.models

data class Tour(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val stops: List<TourStop> = emptyList(),
    val translations: Map<String, TourTranslation> = emptyMap()
)
