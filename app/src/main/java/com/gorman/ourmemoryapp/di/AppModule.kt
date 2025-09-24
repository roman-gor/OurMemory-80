package com.gorman.ourmemoryapp.di

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.gorman.ourmemoryapp.data.datasource.FirebaseDB
import com.gorman.ourmemoryapp.data.datasource.YandexApiService
import com.gorman.ourmemoryapp.data.datasource.FirebaseDBImpl
import com.gorman.ourmemoryapp.data.repository.VeteransRepositoryImpl
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private const val BASE_URL = "https://cloud-api.yandex.net/"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

    @Provides
    @Singleton
    fun provideDatabaseReference(database: FirebaseDatabase): DatabaseReference {
        return database.getReference("Veterans")
    }

    @Provides
    @Singleton
    fun provideFirebaseDBImpl(databaseReference: DatabaseReference): FirebaseDB =
        FirebaseDBImpl(databaseReference)

    @Provides
    @Singleton
    fun provideVeteransRepositoryImpl(firebaseDB: FirebaseDB, apiService: YandexApiService): VeteransRepository {
        return VeteransRepositoryImpl(firebaseDB, apiService)
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): YandexApiService =
        retrofit.create(YandexApiService::class.java)
}