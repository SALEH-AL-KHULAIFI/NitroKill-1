package com.isx3i.nitrokill

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import com.isx3i.nitrokill.data.PrefsManager
import com.isx3i.nitrokill.util.LocaleHelper

class NitroKillApp : Application() {

    companion object {
        const val SPEED_CHANNEL_ID = "nitrokill_speed_channel"
    }

    override fun attachBaseContext(base: Context) {
        // Apply the user's saved language choice before any resources are resolved.
        val savedLang = LocaleHelper.readSavedLanguageBlocking(base)
        super.attachBaseContext(LocaleHelper.wrap(base, savedLang))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            SPEED_CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW // low = no sound, stays visible & silent
        ).apply {
            description = getString(R.string.notification_channel_desc)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }
}
