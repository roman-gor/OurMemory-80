package com.gorman.ourmemoryapp.ui.admin.veterans.models

import com.gorman.ourmemoryapp.domain.models.Veteran

data class AdminVeteranItemUi(
    val id: String,
    val name: String,
    val years: String,
    val portrait: String
)

fun Veteran.toAdminItemUi() = AdminVeteranItemUi(id = id, name = name, years = years, portrait = portrait)
