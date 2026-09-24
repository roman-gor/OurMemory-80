package com.gorman.ourmemoryapp.ui.map.models

import com.gorman.ourmemoryapp.domain.models.Veteran

data class VeteranShortUi(
    val id: String,
    val name: String,
    val years: String,
    val portrait: String
)

fun Veteran.toShortUi() = VeteranShortUi(
    id = id,
    name = name,
    years = years,
    portrait = portrait
)
