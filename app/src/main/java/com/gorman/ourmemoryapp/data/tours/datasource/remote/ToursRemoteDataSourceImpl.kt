package com.gorman.ourmemoryapp.data.tours.datasource.remote

import com.google.firebase.database.FirebaseDatabase
import com.gorman.ourmemoryapp.domain.models.Tour
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ToursRemoteDataSourceImpl @Inject constructor(
    private val database: FirebaseDatabase
) : ToursRemoteDataSource {

    override suspend fun getAllTours(): List<Tour> {
        val snapshot = database.getReference(TOURS_PATH).get().await()
        return snapshot.children.mapNotNull { it.getValue(Tour::class.java) }
    }

    companion object {
        private const val TOURS_PATH = "Tours"
    }
}
