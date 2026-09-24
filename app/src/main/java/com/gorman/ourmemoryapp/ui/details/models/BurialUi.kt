package com.gorman.ourmemoryapp.ui.details.models

import com.gorman.ourmemoryapp.domain.models.Burial

data class BurialUi(
    val latitude: Double,
    val longitude: Double,
    val section: String,
    val row: String,
    val place: String
) {
    val hasPlotNumber = section.isNotBlank() && row.isNotBlank() && place.isNotBlank()
}

fun Burial.toExternalModel() = BurialUi(
    latitude = latitude,
    longitude = longitude,
    section = section,
    row = row,
    place = place
)
