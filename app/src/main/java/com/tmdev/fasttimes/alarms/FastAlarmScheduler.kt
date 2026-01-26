/*
 * Copyright (C) 2025 tom-murphy-development
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.tmdev.fasttimes.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tmdev.fasttimes.data.fast.Fast
import com.tmdev.fasttimes.receiver.FastCompletionReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface AlarmScheduler {
    fun schedule(fast: Fast)
    fun cancel(fast: Fast)
}

class FastAlarmScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
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
            val canSchedule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true // No special permission needed before S
            }

            if (canSchedule) {
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
