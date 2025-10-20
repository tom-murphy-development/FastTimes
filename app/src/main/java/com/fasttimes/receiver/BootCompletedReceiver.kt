package com.fasttimes.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fasttimes.alarms.AlarmScheduler
import com.fasttimes.data.fast.FastRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var fastRepository: FastRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                // Find the current active fast (if any)
                val activeFast = fastRepository.getCurrentFast().first()
                activeFast?.let {
                    // Reschedule the alarm for the active fast
                    alarmScheduler.schedule(it)
                }
            }
        }
    }
}
