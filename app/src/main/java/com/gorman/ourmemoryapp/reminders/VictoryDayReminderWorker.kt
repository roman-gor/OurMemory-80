package com.gorman.ourmemoryapp.reminders

import android.content.Context
import android.content.Intent
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
        val context = applicationContext
        ReminderNotifications.show(
            context = context,
            notificationId = NOTIFICATION_ID,
            title = context.getString(R.string.victory_day),
            text = context.getString(R.string.remember_heroes_msg),
            intent = Intent(context, MainActivity::class.java)
        )
        VictoryDayReminderScheduler.schedule(
            context = context,
            clock = Clock.systemDefaultZone(),
            policy = ExistingWorkPolicy.APPEND_OR_REPLACE
        )
        return Result.success()
    }

    companion object {
        private const val NOTIFICATION_ID = 509
    }
}
