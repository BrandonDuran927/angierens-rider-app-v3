package com.brandon.angierens_rider.riderMap.di

import com.brandon.angierens_rider.riderMap.data.remote.RiderMapRepositoryImpl
import com.brandon.angierens_rider.riderMap.domain.RiderMapRepository
import com.brandon.angierens_rider.task.data.repository.TaskRepositoryImpl
import com.brandon.angierens_rider.task.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        riderMapRepositoryImpl: RiderMapRepositoryImpl,
    ): RiderMapRepository
}