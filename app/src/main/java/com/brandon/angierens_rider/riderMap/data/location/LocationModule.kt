package com.brandon.angierens_rider.riderMap.data.location

import android.content.Context
import com.brandon.angierens_rider.riderMap.domain.location.LocationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideLocationService(
        @ApplicationContext context: Context
    ): LocationServiceImpl {
        return LocationServiceImpl(context)
    }

    @Provides
    @Singleton
    fun provideLocationRepository(
        service: LocationServiceImpl
    ): LocationRepository {
        return LocationRepositoryImpl(service)
    }

}