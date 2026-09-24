package com.gorman.ourmemoryapp.ui.admin.tours.models

import com.gorman.ourmemoryapp.domain.models.Tour

data class AdminTourItemUi(
    val id: String,
    val title: String,
    val stopCount: Int
)

fun Tour.toAdminItemUi() = AdminTourItemUi(id = id, title = title, stopCount = stops.size)
