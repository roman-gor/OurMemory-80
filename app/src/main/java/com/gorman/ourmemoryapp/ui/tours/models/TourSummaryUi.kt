package com.gorman.ourmemoryapp.ui.tours.models

import com.gorman.ourmemoryapp.domain.models.Tour

data class TourSummaryUi(
    val id: String,
    val title: String,
    val description: String,
    val stopsCount: Int,
    val visitedCount: Int = 0
)

fun Tour.toSummaryUi() = TourSummaryUi(
    id = id,
    title = title,
    description = description,
    stopsCount = stops.size
)
