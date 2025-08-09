package com.gorman.ourmemoryapp.data

import retrofit2.http.GET
import retrofit2.http.Query

interface YandexApiService {
    @GET("v1/disk/public/resources/download")
    suspend fun getHrefFromLink(
        @Query("public_key") publicKey: String
    ): YandexImageResponse
}