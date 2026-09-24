package com.gorman.ourmemoryapp.domain.models

data class AudioItem(
    val id: String,
    val url: String,
    val title: String,
    val subtitle: String = ""
)
