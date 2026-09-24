package com.gorman.ourmemoryapp.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object FavoriteAnniversaryScheduler {

    private const val WORK_NAME = "favorite_anniversaries"
    private const val REMINDER_HOUR = 10
    private const val REPEAT_DAYS = 1L

    fun schedule(context: Context, clock: Clock) {
        val now = ZonedDateTime.now(clock)
        val todayAtHour = now.with(LocalTime.of(REMINDER_HOUR, 0))
        val nextRun = if (now.isBefore(todayAtHour)) todayAtHour else todayAtHour.plusDays(REPEAT_DAYS)
        val request = PeriodicWorkRequestBuilder<FavoriteAnniversaryWorker>(REPEAT_DAYS, TimeUnit.DAYS)
            .setInitialDelay(Duration.between(now, nextRun))
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
