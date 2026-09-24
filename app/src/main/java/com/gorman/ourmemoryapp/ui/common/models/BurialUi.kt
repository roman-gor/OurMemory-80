package com.gorman.ourmemoryapp.ui.common.models

import com.gorman.ourmemoryapp.domain.models.Burial

data class BurialUi(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val section: String,
    val row: String,
    val place: String
) {
    val hasPlotNumber = section.isNotBlank() && row.isNotBlank() && place.isNotBlank()
}

fun Burial.toExternalModel() = BurialUi(
    id = id,
    latitude = latitude,
    longitude = longitude,
    section = section,
    row = row,
    place = place
)
