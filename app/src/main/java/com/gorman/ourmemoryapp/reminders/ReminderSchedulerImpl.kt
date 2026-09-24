package com.gorman.ourmemoryapp.reminders

import android.content.Context
import com.gorman.ourmemoryapp.domain.repository.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import javax.inject.Inject

class ReminderSchedulerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val clock: Clock
) : ReminderScheduler {

    override fun setVictoryDayReminder(isEnabled: Boolean) {
        if (isEnabled) {
            VictoryDayReminderScheduler.schedule(context, clock)
        } else {
            VictoryDayReminderScheduler.cancel(context)
        }
    }
}
