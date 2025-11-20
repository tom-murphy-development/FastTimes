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
package com.fasttimes.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.fasttimes.MainActivity
import com.fasttimes.R
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastsRepository
import com.fasttimes.data.settings.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FastCompletionReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FastCompletionReceiverEntryPoint {
        fun fastsRepository(): FastsRepository
        fun settingsRepository(): SettingsRepository
    }

    companion object {
        const val CHANNEL_ID = "fast_completion_channel"
        const val EXTRA_FAST_ID = "EXTRA_FAST_ID"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context,
            FastCompletionReceiverEntryPoint::class.java
        )
        val fastsRepository = hiltEntryPoint.fastsRepository()
        val settingsRepository = hiltEntryPoint.settingsRepository()

        val pendingResult = goAsync()
        val fastId = intent.getLongExtra(EXTRA_FAST_ID, -1)
        if (fastId == -1L) {
            pendingResult.finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val showNotification = settingsRepository.showGoalReachedNotification.first()
                if (!showNotification) {
                    return@launch // Setting is off, do nothing.
                }

                val fast = fastsRepository.getFast(fastId).first()
                fast?.let {
                    // Ensure notification is not shown for manually ended fasts
                    if (it.endTime == null) {
                        sendNotification(context, it)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun sendNotification(context: Context, fast: Fast) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fast Completion",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for completed fasts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val durationInHours = fast.targetDuration?.let { it / (1000 * 60 * 60) } ?: 0

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle("Goal Reached!")
            .setContentText("You've completed your ${durationInHours}-hour fast. Well done!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(fast.id.hashCode(), notification)
    }
}
