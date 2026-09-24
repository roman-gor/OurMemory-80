package com.gorman.ourmemoryapp.ui.map.models

import com.gorman.ourmemoryapp.ui.common.models.BurialUi
import kotlinx.collections.immutable.ImmutableList

data class BurialDetailsUi(
    val burial: BurialUi,
    val type: BurialType,
    val photo: String,
    val description: String,
    val veterans: ImmutableList<VeteranShortUi>
)
