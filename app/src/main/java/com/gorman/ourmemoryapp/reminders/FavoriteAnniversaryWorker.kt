package com.gorman.ourmemoryapp.reminders

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gorman.ourmemoryapp.MainActivity
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.Anniversary
import com.gorman.ourmemoryapp.domain.models.AnniversaryKind
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.domain.models.anniversariesOn
import com.gorman.ourmemoryapp.ui.common.models.VeteranLink
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class FavoriteAnniversaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(applicationContext, ReminderEntryPoint::class.java)
        return runCatching { todayAnniversaries(entryPoint) }
            .onSuccess { reminders -> reminders.forEach { (veteran, anniversary) -> notify(veteran, anniversary) } }
            .fold(onSuccess = { Result.success() }, onFailure = { Result.retry() })
    }

    private suspend fun todayAnniversaries(entryPoint: ReminderEntryPoint): List<Pair<Veteran, Anniversary>> {
        val favorites = entryPoint.favoritesRepository().observeFavorites().first()
        val isEnabled = entryPoint.settingsRepository().observeSettings().first().favoriteReminders
        if (!isEnabled || favorites.isEmpty()) return emptyList()
        val today = LocalDate.now(entryPoint.clock())
        return entryPoint.veteransRepository().getAllVeterans()
            .filter { it.id in favorites }
            .flatMap { veteran -> veteran.anniversariesOn(today).map { veteran to it } }
    }

    private fun notify(veteran: Veteran, anniversary: Anniversary) {
        val context = applicationContext
        val textRes = if (anniversary.kind == AnniversaryKind.BIRTHDAY) {
            R.string.birthday_in_year
        } else {
            R.string.day_of_memory_in_year
        }
        ReminderNotifications.show(
            context = context,
            notificationId = NOTIFICATION_ID_BASE + veteran.id.hashCode() + anniversary.kind.ordinal,
            title = veteran.name,
            text = context.getString(textRes, anniversary.year),
            intent = Intent(Intent.ACTION_VIEW, "${VeteranLink.BASE_URL}/${veteran.id}".toUri())
                .setClass(context, MainActivity::class.java)
        )
    }

    companion object {
        private const val NOTIFICATION_ID_BASE = 1000
    }
}
