package com.gorman.ourmemoryapp.data.tours.datasource.remote

import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.firebase.DatabaseNodes
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.models.Tour
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ToursRemoteDataSourceImpl @Inject constructor(
    @param:MemoryRoot private val root: DatabaseReference
) : ToursRemoteDataSource {

    override suspend fun getAllTours(): List<Tour> {
        val snapshot = root.child(DatabaseNodes.TOURS).get().await()
        return snapshot.children.mapNotNull { it.getValue(Tour::class.java) }
    }
}
