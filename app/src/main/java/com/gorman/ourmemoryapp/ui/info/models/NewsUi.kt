package com.gorman.ourmemoryapp.ui.info.models

import androidx.annotation.DrawableRes

data class NewsUi(
    @param:DrawableRes val iconRes: Int,
    val title: String,
    val url: String
)
