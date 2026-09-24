package com.gorman.ourmemoryapp.ui.admin.common.models

import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import kotlinx.coroutines.flow.flow

fun VeteransRepository.observeVeteranNames() = flow {
    emit(emptyMap())
    val names = runCatching { getAllVeterans().associate { it.id to it.name } }.getOrDefault(emptyMap())
    emit(names)
}
