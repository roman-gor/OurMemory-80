package com.gorman.ourmemoryapp.data.mapper

import com.gorman.ourmemoryapp.data.models.YandexImageResponse
import com.gorman.ourmemoryapp.domain.models.YandexImage

fun YandexImageResponse.toDomain(): YandexImage = YandexImage(
    href = href
)