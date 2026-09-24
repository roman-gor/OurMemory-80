package com.gorman.ourmemoryapp.data.content.datasource.remote

import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.firebase.DatabaseNodes
import com.gorman.ourmemoryapp.data.firebase.VeteranKeys
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.models.Veteran
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ContentRemoteDataSourceImpl @Inject constructor(
    @param:MemoryRoot private val root: DatabaseReference
) : ContentRemoteDataSource {

    override suspend fun saveVeteran(veteran: Veteran) {
        veteranReference(veteran.id).setValue(veteran).await()
    }

    override suspend fun deleteVeteran(veteranId: String) {
        veteranReference(veteranId).removeValue().await()
    }

    private fun veteranReference(veteranId: String) =
        root.child(DatabaseNodes.VETERANS).child(VeteranKeys.forId(veteranId))
}
