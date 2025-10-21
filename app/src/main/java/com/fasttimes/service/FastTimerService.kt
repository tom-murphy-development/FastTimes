package com.fasttimes.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.fasttimes.MainActivity
import com.fasttimes.alarms.AlarmScheduler
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class FastTimerService : LifecycleService() {

    @Inject
    lateinit var fastRepository: FastRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private lateinit var notificationManager: NotificationManager
    private var serviceJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> startService()
            ACTION_STOP -> stopService()
            ACTION_END_FAST -> endFast()
        }
        return START_STICKY
    }

    private fun startService() {
        if (serviceJob?.isActive == true) return

        serviceJob = lifecycleScope.launch {
            // Use collectLatest to ensure only one timer loop runs at a time.
            // When the active fast changes, the previous block is automatically cancelled.
            fastRepository.getAllFasts().map { fasts ->
                fasts.firstOrNull { it.endTime == null }
            }.collectLatest { activeFast ->
                if (activeFast == null) {
                    stopService()
                    return@collectLatest
                }

                startForeground(NOTIFICATION_ID, buildNotification(activeFast))

                while (isActive) {
                    // When the fast is complete, the alarm will fire and show the final notification.
                    // This service can just stop itself.
                    val remainingTime = activeFast.targetDuration?.let { (activeFast.startTime + it) - System.currentTimeMillis() }
                    if (remainingTime != null && remainingTime <= 0) {
                        stopService()
                        return@collectLatest
                    }

                    delay(1000) // Update every second
                    notificationManager.notify(NOTIFICATION_ID, buildNotification(activeFast))
                }
            }
        }
    }

    private fun endFast() {
        lifecycleScope.launch {
            val activeFast = fastRepository.getAllFasts().firstOrNull()?.firstOrNull { it.endTime == null }
            if (activeFast != null) {
                alarmScheduler.cancel(activeFast)
                fastRepository.endFast(activeFast.id, System.currentTimeMillis())
            }
            stopService()
        }
    }

    private fun stopService() {
        serviceJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fast Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the live progress of your current fast."
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(fast: Fast): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val endFastIntent = PendingIntent.getService(
            this, 0, Intent(this, FastTimerService::class.java).apply { action = ACTION_END_FAST },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val now = System.currentTimeMillis()
        val elapsedTime = now - fast.startTime
        val title = "${fast.profile.displayName} in Progress"

        val progress: Int
        val contentText: String
        val isIndeterminate: Boolean

        val totalDuration = fast.targetDuration
        if (totalDuration != null && totalDuration > 0) {
            val targetEndTime = fast.startTime + totalDuration
            val remainingTime = (targetEndTime - now).coerceAtLeast(0)
            progress = (elapsedTime.toDouble() / totalDuration * 100).toInt().coerceIn(0, 100)
            contentText = "Remaining: ${formatDuration(remainingTime)}"
            isIndeterminate = false
        } else {
            progress = 0
            contentText = "Elapsed: ${formatDuration(elapsedTime)}"
            isIndeterminate = true
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Placeholder icon
            .setProgress(100, progress, isIndeterminate)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppIntent)
            .addAction(0, "End Fast", endFastIntent)
            .build()
    }

    private fun formatDuration(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02dh %02dm %02ds", hours, minutes, seconds)
    }

    companion object {
        const val NOTIFICATION_ID = 1234
        const val CHANNEL_ID = "fast_progress_channel"
        const val ACTION_START = "com.fasttimes.service.ACTION_START"
        const val ACTION_STOP = "com.fasttimes.service.ACTION_STOP"
        const val ACTION_END_FAST = "com.fasttimes.service.ACTION_END_FAST"
    }
}
