package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.domain.repository.MediaRepository

class FakeMediaRepository : MediaRepository {

    val uploads = mutableListOf<Pair<String, String>>()

    override suspend fun uploadPhoto(uri: String, folder: String): String {
        uploads += uri to folder
        return "https://media/$folder/photo"
    }

    override suspend fun uploadAudio(uri: String, folder: String): String {
        uploads += uri to folder
        return "https://media/$folder/audio"
    }
}
