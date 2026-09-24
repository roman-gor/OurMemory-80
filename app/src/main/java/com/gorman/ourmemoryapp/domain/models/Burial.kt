package com.gorman.ourmemoryapp.domain.models

data class Burial(
    val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val section: String = "",
    val row: String = "",
    val place: String = "",
    val type: String = "",
    val photo: String = "",
    val description: String = ""
)
