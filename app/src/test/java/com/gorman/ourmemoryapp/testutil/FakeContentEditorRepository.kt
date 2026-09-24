package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.domain.repository.ContentEditorRepository

class FakeContentEditorRepository(
    private val error: Throwable? = null
) : ContentEditorRepository {

    val savedVeterans = mutableListOf<Veteran>()
    val deletedVeteranIds = mutableListOf<String>()

    override suspend fun saveVeteran(veteran: Veteran) {
        error?.let { throw it }
        savedVeterans += veteran
    }

    override suspend fun deleteVeteran(veteranId: String) {
        error?.let { throw it }
        deletedVeteranIds += veteranId
    }
}
