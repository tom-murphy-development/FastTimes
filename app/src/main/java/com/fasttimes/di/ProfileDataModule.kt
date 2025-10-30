package com.fasttimes.di

import com.fasttimes.data.AppDatabase
import com.fasttimes.data.profile.DefaultFastingProfileRepository
import com.fasttimes.data.profile.FastingProfileDao
import com.fasttimes.data.profile.FastingProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileDataModule {

    @Binds
    @Singleton
    abstract fun bindFastingProfileRepository(
        defaultFastingProfileRepository: DefaultFastingProfileRepository
    ): FastingProfileRepository

    companion object {
        @Provides
        @Singleton
        fun provideFastingProfileDao(appDatabase: AppDatabase): FastingProfileDao {
            return appDatabase.fastingProfileDao()
        }
    }
}
