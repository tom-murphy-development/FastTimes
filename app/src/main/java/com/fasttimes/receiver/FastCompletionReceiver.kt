
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
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FastCompletionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var fastRepository: FastRepository

    companion object {
        const val CHANNEL_ID = "fast_completion_channel"
        const val EXTRA_FAST_ID = "EXTRA_FAST_ID"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val fastId = intent.getLongExtra(EXTRA_FAST_ID, -1)
        if (fastId == -1L) {
            pendingResult.finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fast = fastRepository.getFast(fastId)
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

        // Create notification channel for Android O and above.
        // It's safe to call this repeatedly.
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
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Replace with a proper app icon
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
