package com.gorman.ourmemoryapp.data.burials.datasource.remote

import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.firebase.DatabaseNodes
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.models.Burial
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BurialsRemoteDataSourceImpl @Inject constructor(
    @param:MemoryRoot private val root: DatabaseReference
) : BurialsRemoteDataSource {

    override suspend fun getAllBurials(): List<Burial> {
        val snapshot = root.child(DatabaseNodes.BURIALS).get().await()
        return snapshot.children.mapNotNull { it.getValue(Burial::class.java) }
    }
}
