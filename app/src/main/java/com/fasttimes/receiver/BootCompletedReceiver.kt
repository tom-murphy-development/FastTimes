package com.fasttimes.receiver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.fasttimes.alarms.AlarmScheduler
import com.fasttimes.data.fast.FastsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootCompletedReceiverEntryPoint {
        fun fastsRepository(): FastsRepository
        fun alarmScheduler(): AlarmScheduler
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val hiltEntryPoint = EntryPointAccessors.fromApplication(
                context,
                BootCompletedReceiverEntryPoint::class.java
            )
            val fastsRepository = hiltEntryPoint.fastsRepository()
            val alarmScheduler = hiltEntryPoint.alarmScheduler()
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            CoroutineScope(Dispatchers.IO).launch {
                val activeFast = fastsRepository.getActiveFast().first()
                activeFast?.let {
                    val canSchedule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        alarmManager.canScheduleExactAlarms()
                    } else {
                        true // No special permission needed before S
                    }

                    if (canSchedule) {
                        alarmScheduler.schedule(it)
                    }
                }
            }
        }
    }
}
