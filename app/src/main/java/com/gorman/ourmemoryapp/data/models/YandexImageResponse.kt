package com.gorman.ourmemoryapp.data.models

import kotlinx.serialization.Serializable

@Serializable
data class YandexImageResponse(
    val method: String = "",
    val href: String = "",
    val templated: String = ""
)
