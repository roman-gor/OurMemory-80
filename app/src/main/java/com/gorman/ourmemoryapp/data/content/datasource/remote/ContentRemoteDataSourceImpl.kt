package com.gorman.ourmemoryapp.data.content.datasource.remote

import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.firebase.DatabaseNodes
import com.gorman.ourmemoryapp.data.firebase.VeteranKeys
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.models.Tour
import com.gorman.ourmemoryapp.domain.models.Veteran
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ContentRemoteDataSourceImpl @Inject constructor(
    @param:MemoryRoot private val root: DatabaseReference
) : ContentRemoteDataSource {

    private val veterans = root.child(DatabaseNodes.VETERANS)
    private val burials = root.child(DatabaseNodes.BURIALS)
    private val tours = root.child(DatabaseNodes.TOURS)

    override suspend fun saveVeteran(veteran: Veteran) {
        veterans.child(VeteranKeys.forId(veteran.id)).setValue(veteran).await()
    }

    override suspend fun deleteVeteran(veteranId: String) {
        veterans.child(VeteranKeys.forId(veteranId)).removeValue().await()
    }

    override fun newBurialId() = requireNotNull(burials.push().key)

    override suspend fun saveBurial(burial: Burial) {
        burials.child(burial.id).setValue(burial).await()
    }

    override fun newTourId() = requireNotNull(tours.push().key)

    override suspend fun saveTour(tour: Tour) {
        tours.child(tour.id).setValue(tour).await()
    }

    override suspend fun deleteTour(tourId: String) {
        tours.child(tourId).removeValue().await()
    }
}
