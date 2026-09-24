package com.gorman.ourmemoryapp.ui.favorites.models

import com.gorman.ourmemoryapp.domain.models.Veteran

data class FavoriteVeteranUi(
    val id: String,
    val name: String,
    val years: String,
    val portrait: String
)

fun Veteran.toFavoriteUi() = FavoriteVeteranUi(id = id, name = name, years = years, portrait = portrait)
