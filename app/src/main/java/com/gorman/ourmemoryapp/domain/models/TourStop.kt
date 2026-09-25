package com.gorman.ourmemoryapp.domain.models

data class TourStop(
    val burialId: String = "",
    val text: String = "",
    val audioUrl: String = "",
    val translations: Map<String, TourStopTranslation> = emptyMap()
)
