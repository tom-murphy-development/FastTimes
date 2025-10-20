
package com.fasttimes.di

import com.fasttimes.alarms.AlarmScheduler
import com.fasttimes.alarms.FastAlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmSchedulerModule {

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(impl: FastAlarmScheduler): AlarmScheduler
}
