package com.gorman.ourmemoryapp.data.burials.datasource.remote

import com.google.firebase.database.FirebaseDatabase
import com.gorman.ourmemoryapp.domain.models.Burial
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BurialsRemoteDataSourceImpl @Inject constructor(
    private val database: FirebaseDatabase
) : BurialsRemoteDataSource {

    override suspend fun getAllBurials(): List<Burial> {
        val snapshot = database.getReference(BURIALS_PATH).get().await()
        return snapshot.children.mapNotNull { it.getValue(Burial::class.java) }
    }

    companion object {
        private const val BURIALS_PATH = "Burials"
    }
}
