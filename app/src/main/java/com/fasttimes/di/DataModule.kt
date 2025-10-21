package com.fasttimes.di

import com.fasttimes.data.fast.FastsRepository
import com.fasttimes.data.fast.FastsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
abstract class DataModule {

    @Binds
    abstract fun bindFastsRepository(impl: FastsRepositoryImpl): FastsRepository
}
