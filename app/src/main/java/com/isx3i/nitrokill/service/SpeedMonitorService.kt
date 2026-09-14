package com.isx3i.nitrokill.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.isx3i.nitrokill.MainActivity
import com.isx3i.nitrokill.NitroKillApp
import com.isx3i.nitrokill.R
import com.isx3i.nitrokill.data.UsageRepository
import com.isx3i.nitrokill.util.IconTextGenerator

class SpeedMonitorService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val TICK_MS = 1000L

        fun start(context: Context) {
            val intent = Intent(context, SpeedMonitorService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, SpeedMonitorService::class.java))
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var lastBytes = 0L
    private lateinit var usageRepository: UsageRepository

    private val tick = object : Runnable {
        override fun run() {
            updateSpeed()
            usageRepository.ensureBaselineForToday()
            handler.postDelayed(this, TICK_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        usageRepository = UsageRepository(this)
        lastBytes = totalBytesNow()
        startForeground(NOTIFICATION_ID, buildNotification(0))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.removeCallbacks(tick)
        handler.post(tick)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun totalBytesNow(): Long {
        val rx = TrafficStats.getTotalRxBytes()
        val tx = TrafficStats.getTotalTxBytes()
        return if (rx == TrafficStats.UNSUPPORTED.toLong() || tx == TrafficStats.UNSUPPORTED.toLong()) 0L else rx + tx
    }

    private fun updateSpeed() {
        val now = totalBytesNow()
        val bytesPerSecond = ((now - lastBytes).coerceAtLeast(0) * 1000L) / TICK_MS
        lastBytes = now

        val notification = buildNotification(bytesPerSecond)
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(bytesPerSecond: Long): android.app.Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val contentIntent = android.app.PendingIntent.getActivity(
            this, 0, openAppIntent,
            android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val speedText = UsageRepository.formatBytes(bytesPerSecond) + "/s"

        return NotificationCompat.Builder(this, NitroKillApp.SPEED_CHANNEL_ID)
            .setSmallIcon(IconTextGenerator.forSpeed(bytesPerSecond))
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(speedText)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
