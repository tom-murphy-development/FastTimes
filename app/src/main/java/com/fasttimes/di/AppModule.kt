package com.fasttimes.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.fasttimes.data.AppDatabase
import com.fasttimes.data.fast.FastDao
import com.fasttimes.data.fast.FastRepository
import com.fasttimes.data.settings.DefaultSettingsRepository
import com.fasttimes.data.settings.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(dataStore: DataStore<Preferences>): SettingsRepository = DefaultSettingsRepository(dataStore)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase = 
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fasttimes-db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()

    @Provides
    fun provideFastDao(db: AppDatabase): FastDao = db.fastDao()

    @Provides
    @Singleton
    fun provideFastRepository(fastDao: FastDao): FastRepository = FastRepository(fastDao)
}
