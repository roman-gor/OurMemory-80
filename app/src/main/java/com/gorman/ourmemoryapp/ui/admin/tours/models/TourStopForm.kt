package com.gorman.ourmemoryapp.ui.admin.tours.models

import com.gorman.ourmemoryapp.domain.models.TourStop

data class TourStopForm(
    val burialId: String,
    val text: String = "",
    val audioUrl: String = ""
)

fun TourStop.toForm() = TourStopForm(burialId = burialId, text = text, audioUrl = audioUrl)

fun TourStopForm.toTourStop() = TourStop(burialId = burialId, text = text.trim(), audioUrl = audioUrl)
