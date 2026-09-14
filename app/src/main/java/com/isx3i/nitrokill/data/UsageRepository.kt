package com.isx3i.nitrokill.data

import android.content.Context
import android.net.TrafficStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Rough daily data-usage counter for the on-screen table (date / mobile /
 * transfer / total). This is a lightweight approximation using TrafficStats
 * baselines reset at midnight — it is NOT a metered, carrier-accurate
 * counter, which is exactly why the app's disclaimer mentions the numbers
 * can be inaccurate if the app itself gets closed or the phone reboots
 * between readings (the baseline is only saved when NitroKill is running).
 */
class UsageRepository(context: Context) {

    private val prefs = context.getSharedPreferences("nitrokill_usage", Context.MODE_PRIVATE)
    private val dayFormat = SimpleDateFormat("d/M/yyyy", Locale.US)

    data class DailyUsage(val date: String, val mobileBytes: Long, val transferBytes: Long) {
        val totalBytes: Long get() = mobileBytes + transferBytes
    }

    /** Call periodically (e.g. from the monitor service) to roll baselines at day change. */
    fun ensureBaselineForToday() {
        val today = dayFormat.format(Date())
        if (prefs.getString(KEY_BASELINE_DAY, null) != today) {
            prefs.edit()
                .putString(KEY_BASELINE_DAY, today)
                .putLong(KEY_BASELINE_MOBILE, currentMobileBytes())
                .putLong(KEY_BASELINE_TOTAL, currentTotalBytes())
                .apply()
        }
    }

    fun getTodayUsage(): DailyUsage {
        ensureBaselineForToday()
        val today = prefs.getString(KEY_BASELINE_DAY, dayFormat.format(Date())) ?: dayFormat.format(Date())
        val baseMobile = prefs.getLong(KEY_BASELINE_MOBILE, currentMobileBytes())
        val baseTotal = prefs.getLong(KEY_BASELINE_TOTAL, currentTotalBytes())

        val mobile = (currentMobileBytes() - baseMobile).coerceAtLeast(0)
        val total = (currentTotalBytes() - baseTotal).coerceAtLeast(0)
        val transfer = (total - mobile).coerceAtLeast(0)

        return DailyUsage(today, mobile, transfer)
    }

    private fun currentMobileBytes(): Long {
        val rx = TrafficStats.getMobileRxBytes()
        val tx = TrafficStats.getMobileTxBytes()
        return if (rx == TrafficStats.UNSUPPORTED.toLong() || tx == TrafficStats.UNSUPPORTED.toLong()) 0L else rx + tx
    }

    private fun currentTotalBytes(): Long {
        val rx = TrafficStats.getTotalRxBytes()
        val tx = TrafficStats.getTotalTxBytes()
        return if (rx == TrafficStats.UNSUPPORTED.toLong() || tx == TrafficStats.UNSUPPORTED.toLong()) 0L else rx + tx
    }

    companion object {
        private const val KEY_BASELINE_DAY = "baseline_day"
        private const val KEY_BASELINE_MOBILE = "baseline_mobile"
        private const val KEY_BASELINE_TOTAL = "baseline_total"

        /** Formats bytes the way the notes did: whole megabytes with a "م" suffix, or GB above 1024MB. */
        fun formatBytes(bytes: Long): String {
            val mb = bytes / (1024.0 * 1024.0)
            return if (mb >= 1024.0) {
                String.format(Locale.US, "%.2f GB", mb / 1024.0)
            } else {
                String.format(Locale.US, "%.0f MB", mb)
            }
        }
    }
}
