package com.gorman.ourmemoryapp.data.favorites.datasource.remote

import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.firebase.DatabaseNodes
import com.gorman.ourmemoryapp.data.firebase.observeValue
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FavoritesRemoteDataSourceImpl @Inject constructor(
    @param:MemoryRoot private val root: DatabaseReference
) : FavoritesRemoteDataSource {

    override fun observe(uid: String) = favorites(uid).observeValue().map { snapshot ->
        snapshot.children.mapNotNull { it.key }.toSet()
    }

    override suspend fun set(uid: String, veteranId: String, isFavorite: Boolean) {
        favorites(uid).child(veteranId).setValue(if (isFavorite) true else null).await()
    }

    override suspend fun addAll(uid: String, veteranIds: Set<String>) {
        if (veteranIds.isEmpty()) return
        favorites(uid).updateChildren(veteranIds.associateWith { true }).await()
    }

    private fun favorites(uid: String) = root.child(DatabaseNodes.USERS).child(uid).child(DatabaseNodes.FAVORITES)
}
