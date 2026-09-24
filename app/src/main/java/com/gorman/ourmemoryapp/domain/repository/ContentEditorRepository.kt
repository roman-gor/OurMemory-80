package com.gorman.ourmemoryapp.domain.repository

import com.gorman.ourmemoryapp.domain.models.Veteran

interface ContentEditorRepository {
    suspend fun saveVeteran(veteran: Veteran)
    suspend fun deleteVeteran(veteranId: String)
}
