package com.gorman.ourmemoryapp.ui.tours.models

import com.gorman.ourmemoryapp.domain.models.AudioItem
import com.gorman.ourmemoryapp.ui.common.models.BurialType
import com.gorman.ourmemoryapp.ui.common.models.BurialUi

data class TourStopUi(
    val number: Int,
    val title: String,
    val type: BurialType,
    val burial: BurialUi,
    val text: String,
    val audio: AudioItem?
)
