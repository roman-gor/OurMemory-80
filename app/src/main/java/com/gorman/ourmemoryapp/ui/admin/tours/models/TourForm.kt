package com.gorman.ourmemoryapp.ui.admin.tours.models

import com.gorman.ourmemoryapp.domain.models.Tour
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

data class TourForm(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val stops: ImmutableList<TourStopForm> = persistentListOf()
) {
    val isValid = title.isNotBlank() && stops.isNotEmpty()
}

fun Tour.toForm() = TourForm(
    id = id,
    title = title,
    description = description,
    stops = stops.map { it.toForm() }.toPersistentList()
)

fun TourForm.toTour() = Tour(
    id = id,
    title = title.trim(),
    description = description.trim(),
    stops = stops.map { it.toTourStop() }
)
