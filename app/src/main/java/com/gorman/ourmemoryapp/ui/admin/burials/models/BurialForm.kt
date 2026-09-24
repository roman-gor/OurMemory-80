package com.gorman.ourmemoryapp.ui.admin.burials.models

import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.ui.common.models.BurialType

data class BurialForm(
    val id: String = "",
    val type: BurialType = BurialType.GRAVE,
    val section: String = "",
    val row: String = "",
    val place: String = "",
    val description: String = "",
    val photo: String = "",
    val latitude: String = "",
    val longitude: String = ""
) {
    val latitudeValue = latitude.toLatitudeOrNull()
    val longitudeValue = longitude.toLongitudeOrNull()
    val isValid = latitudeValue != null && longitudeValue != null
}

fun Burial.toForm() = BurialForm(
    id = id,
    type = BurialType.fromValue(type),
    section = section,
    row = row,
    place = place,
    description = description,
    photo = photo,
    latitude = latitude.toCoordinateText(),
    longitude = longitude.toCoordinateText()
)

fun BurialForm.toBurial() = Burial(
    id = id,
    latitude = latitudeValue ?: 0.0,
    longitude = longitudeValue ?: 0.0,
    section = section.trim(),
    row = row.trim(),
    place = place.trim(),
    type = type.value,
    photo = photo,
    description = description.trim()
)
