package com.gorman.ourmemoryapp.domain.repository

import com.gorman.ourmemoryapp.domain.models.Burial

interface BurialsRepository {
    suspend fun getAllBurials(): List<Burial>
}
