package com.gorman.ourmemoryapp

import android.app.Application
import com.gorman.ourmemoryapp.reminders.VictoryDayReminderScheduler
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import java.time.Clock

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        VictoryDayReminderScheduler.schedule(this, Clock.systemDefaultZone())
    }
}
