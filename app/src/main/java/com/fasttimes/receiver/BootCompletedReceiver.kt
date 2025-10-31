package com.fasttimes.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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

            CoroutineScope(Dispatchers.IO).launch {
                // Find the current active fast (if any)
                val activeFast = fastsRepository.getActiveFast().first()
                activeFast?.let {
                    // Reschedule the alarm for the active fast
                    alarmScheduler.schedule(it)
                }
            }
        }
    }
}
