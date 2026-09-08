package com.gorman.ourmemoryapp.data.datasource

import com.gorman.ourmemoryapp.domain.models.Veteran

interface FirebaseDB {
    suspend fun getAllVeterans(): List<Veteran>
}
