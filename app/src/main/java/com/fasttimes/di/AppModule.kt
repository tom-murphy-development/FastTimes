package com.fasttimes.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.fasttimes.data.AppDatabase
import com.fasttimes.data.AppDatabase.Companion.MIGRATION_4_5
import com.fasttimes.data.AppDatabaseCallback
import com.fasttimes.data.fast.FastDao
import com.fasttimes.data.profile.FastingProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @ApplicationScope
    @Singleton
    @Provides
    fun providesApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        callback: AppDatabaseCallback
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "fasttimes-db"
    ).addCallback(callback).addMigrations(MIGRATION_4_5).build()

    @Provides
    fun provideFastDao(db: AppDatabase): FastDao = db.fastDao()

    @Provides
    fun provideFastingProfileDao(db: AppDatabase): FastingProfileDao = db.fastingProfileDao()

    @Provides
    @Singleton
    fun provideAppDatabaseCallback(
        fastingProfileDao: Provider<FastingProfileDao>,
        @ApplicationScope applicationScope: CoroutineScope
    ): AppDatabaseCallback = AppDatabaseCallback(fastingProfileDao, applicationScope)
}
