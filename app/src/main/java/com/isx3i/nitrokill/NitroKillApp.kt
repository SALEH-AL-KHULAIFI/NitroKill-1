package com.isx3i.nitrokill

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import com.isx3i.nitrokill.util.LocaleHelper

class NitroKillApp : Application() {

    companion object {
        const val SPEED_CHANNEL_ID = "nitrokill_speed_channel"
    }

    override fun attachBaseContext(base: Context) {
        /*
         * Do not access DataStore synchronously here.
         *
         * Application.attachBaseContext() is part of the very early
         * application startup sequence. Reading DataStore with runBlocking
         * at this point can cause startup problems.
         *
         * The default language is Arabic. The selected language is applied
         * later by MainActivity.
         */
        super.attachBaseContext(
            LocaleHelper.wrap(base, "ar")
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(
            NotificationManager::class.java
        )

        val channel = NotificationChannel(
            SPEED_CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(
                R.string.notification_channel_desc
            )
            setShowBadge(false)
        }

        manager.createNotificationChannel(channel)
    }
}
