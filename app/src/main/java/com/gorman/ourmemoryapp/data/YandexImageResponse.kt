package com.gorman.ourmemoryapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class YandexImageResponse(
    val method: String = "",
    val href: String = "",
    val templated: String = ""
): Parcelable
