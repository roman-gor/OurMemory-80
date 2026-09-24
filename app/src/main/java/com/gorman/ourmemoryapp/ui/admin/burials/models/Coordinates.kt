package com.gorman.ourmemoryapp.ui.admin.burials.models

import java.util.Locale

private const val COORDINATE_FORMAT = "%.6f"
private const val MAX_LATITUDE = 90.0
private const val MAX_LONGITUDE = 180.0

fun Double.toCoordinateText(): String = String.format(Locale.US, COORDINATE_FORMAT, this)

fun String.toLatitudeOrNull() = replace(',', '.').trim().toDoubleOrNull()?.takeIf { it in -MAX_LATITUDE..MAX_LATITUDE }

fun String.toLongitudeOrNull() =
    replace(',', '.').trim().toDoubleOrNull()?.takeIf { it in -MAX_LONGITUDE..MAX_LONGITUDE }
