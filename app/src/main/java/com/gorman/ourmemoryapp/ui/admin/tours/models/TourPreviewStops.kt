package com.gorman.ourmemoryapp.ui.admin.tours.models

import com.gorman.ourmemoryapp.ui.admin.burials.models.AdminBurialItemUi
import com.gorman.ourmemoryapp.ui.tours.models.TourStopUi

fun List<TourStopForm>.toPreviewStops(burials: List<AdminBurialItemUi>): List<TourStopUi> {
    val burialsById = burials.associateBy { it.burial.id }
    return mapNotNull { stop -> burialsById[stop.burialId]?.let { stop to it } }
        .mapIndexed { index, (stop, item) ->
            TourStopUi(
                number = index + 1,
                title = item.veteranNames.ifBlank { item.burial.id },
                type = item.type,
                burial = item.burial,
                text = stop.text,
                audio = null
            )
        }
}
