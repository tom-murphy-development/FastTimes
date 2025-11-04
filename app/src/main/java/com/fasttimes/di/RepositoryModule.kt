package com.fasttimes.di

import com.fasttimes.data.fast.FastsRepository
import com.fasttimes.data.fast.FastsRepositoryImpl
import com.fasttimes.data.profile.DefaultFastingProfileRepositoryImpl
import com.fasttimes.data.profile.FastingProfileRepository
import com.fasttimes.data.settings.DefaultSettingsRepository
import com.fasttimes.data.settings.SettingsRepository
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
    abstract fun bindFastsRepository(impl: FastsRepositoryImpl): FastsRepository

    @Binds
    @Singleton
    abstract fun bindFastingProfileRepository(impl: DefaultFastingProfileRepositoryImpl): FastingProfileRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: DefaultSettingsRepository): SettingsRepository
}
