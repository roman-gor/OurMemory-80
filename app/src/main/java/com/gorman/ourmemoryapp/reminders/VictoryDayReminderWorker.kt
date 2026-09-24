package com.gorman.ourmemoryapp.reminders

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkerParameters
import com.gorman.ourmemoryapp.MainActivity
import com.gorman.ourmemoryapp.R
import java.time.Clock

class VictoryDayReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        showNotification()
        VictoryDayReminderScheduler.schedule(
            context = applicationContext,
            clock = Clock.systemDefaultZone(),
            policy = ExistingWorkPolicy.APPEND_OR_REPLACE
        )
        return Result.success()
    }

    private fun showNotification() {
        val context = applicationContext
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        val manager = NotificationManagerCompat.from(context)
        if (!hasPermission || !manager.areNotificationsEnabled()) return

        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.reminders))
                .build()
        )
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.flame)
            .setContentTitle(context.getString(R.string.victory_day))
            .setContentText(context.getString(R.string.remember_heroes_msg))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.remember_heroes_msg)))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "reminders"
        private const val NOTIFICATION_ID = 509
    }
}
