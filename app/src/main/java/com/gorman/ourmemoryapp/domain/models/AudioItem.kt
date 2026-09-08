package com.gorman.ourmemoryapp.domain.models

data class AudioItem(
    val id: Int,
    val title: String,
    val fileName: String,
    val rawResourceId: Int,
    val itemId: Int = 0
)
