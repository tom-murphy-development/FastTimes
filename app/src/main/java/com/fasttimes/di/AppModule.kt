package com.fasttimes.di

import android.content.Context
import androidx.room.Room
import com.fasttimes.data.AppDatabase
import com.fasttimes.data.fast.FastDao
import com.fasttimes.data.fast.FastRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fasttimes-db"
        ).build()

    @Provides
    fun provideFastDao(db: AppDatabase): FastDao = db.fastDao()

    @Provides
    @Singleton
    fun provideFastRepository(fastDao: FastDao): FastRepository = FastRepository(fastDao)
}

