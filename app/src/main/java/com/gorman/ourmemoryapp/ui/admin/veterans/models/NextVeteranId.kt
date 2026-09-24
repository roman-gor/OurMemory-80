package com.gorman.ourmemoryapp.ui.admin.veterans.models

import com.gorman.ourmemoryapp.domain.models.Veteran

fun List<Veteran>.nextVeteranId(): String = ((mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: 0) + 1).toString()
