package com.gorman.ourmemoryapp.reminders

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime

object VictoryDayReminderScheduler {

    private const val WORK_NAME = "victory_day_reminder"

    fun schedule(context: Context, clock: Clock, policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP) {
        val now = ZonedDateTime.now(clock)
        val request = OneTimeWorkRequestBuilder<VictoryDayReminderWorker>()
            .setInitialDelay(Duration.between(now, nextVictoryDayReminder(now)))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, policy, request)
    }
}
