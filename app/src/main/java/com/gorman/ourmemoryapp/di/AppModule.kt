package com.gorman.ourmemoryapp.di

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.gorman.ourmemoryapp.data.burials.datasource.remote.BurialsRemoteDataSource
import com.gorman.ourmemoryapp.data.burials.datasource.remote.BurialsRemoteDataSourceImpl
import com.gorman.ourmemoryapp.data.burials.repository.BurialsRepositoryImpl
import com.gorman.ourmemoryapp.data.datasource.FirebaseDB
import com.gorman.ourmemoryapp.data.datasource.FirebaseDBImpl
import com.gorman.ourmemoryapp.data.datasource.YandexApiService
import com.gorman.ourmemoryapp.data.firebase.DatabaseNodes
import com.gorman.ourmemoryapp.data.repository.VeteransRepositoryImpl
import com.gorman.ourmemoryapp.data.tours.datasource.remote.ToursRemoteDataSource
import com.gorman.ourmemoryapp.data.tours.datasource.remote.ToursRemoteDataSourceImpl
import com.gorman.ourmemoryapp.data.tours.repository.ToursRepositoryImpl
import com.gorman.ourmemoryapp.di.annotation.IoDispatcher
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.repository.BurialsRepository
import com.gorman.ourmemoryapp.domain.repository.ToursRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private const val BASE_URL = "https://cloud-api.yandex.net/"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance().apply { setPersistenceEnabled(true) }
    }

    @Provides
    @Singleton
    @MemoryRoot
    fun provideMemoryRoot(database: FirebaseDatabase): DatabaseReference {
        return database.getReference(DatabaseNodes.ROOT)
    }

    @Provides
    @Singleton
    fun provideFirebaseDBImpl(@MemoryRoot root: DatabaseReference): FirebaseDB =
        FirebaseDBImpl(root.child(DatabaseNodes.VETERANS))

    @Provides
    @Singleton
    fun provideVeteransRepositoryImpl(firebaseDB: FirebaseDB, apiService: YandexApiService): VeteransRepository {
        return VeteransRepositoryImpl(firebaseDB, apiService)
    }

    @Provides
    @Singleton
    fun provideBurialsRemoteDataSource(@MemoryRoot root: DatabaseReference): BurialsRemoteDataSource =
        BurialsRemoteDataSourceImpl(root)

    @Provides
    @Singleton
    fun provideBurialsRepository(dataSource: BurialsRemoteDataSource): BurialsRepository =
        BurialsRepositoryImpl(dataSource)

    @Provides
    @Singleton
    fun provideToursRemoteDataSource(@MemoryRoot root: DatabaseReference): ToursRemoteDataSource =
        ToursRemoteDataSourceImpl(root)

    @Provides
    @Singleton
    fun provideToursRepository(dataSource: ToursRemoteDataSource): ToursRepository =
        ToursRepositoryImpl(dataSource)

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
