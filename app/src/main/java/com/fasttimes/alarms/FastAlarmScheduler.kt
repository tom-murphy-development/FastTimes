
package com.fasttimes.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.fasttimes.data.fast.Fast
import com.fasttimes.receiver.FastCompletionReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface AlarmScheduler {
    fun schedule(fast: Fast)
    fun cancel(fast: Fast)
}

class FastAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmScheduler {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(fast: Fast) {
        val intent = Intent(context, FastCompletionReceiver::class.java).apply {
            putExtra("EXTRA_FAST_ID", fast.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            fast.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val completionTime = fast.startTime + (fast.targetDuration ?: 0)

        // Ensure we only schedule for future fasts with a valid target duration
        if (completionTime > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        completionTime,
                        pendingIntent
                    )
                } else {
                    // Cannot schedule exact alarms, so we don't do anything to prevent a crash
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    completionTime,
                    pendingIntent
                )
            }
        }
    }

    override fun cancel(fast: Fast) {
        val intent = Intent(context, FastCompletionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            fast.id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        // If the pending intent exists, cancel it
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
