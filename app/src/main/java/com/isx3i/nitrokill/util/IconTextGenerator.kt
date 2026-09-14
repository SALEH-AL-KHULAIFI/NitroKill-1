package com.isx3i.nitrokill.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat

/**
 * Status-bar notification icons are tiny (roughly 24dp) and Android forces
 * them to render as a plain white silhouette — a static "speed" glyph would
 * look identical whether you're at 0 or 50 Mbps, which was the root cause of
 * the "icon too small / not clear" complaint. The standard fix (used by every
 * speed-meter app on the Play Store) is to draw the number itself as a bitmap
 * and hand that bitmap to the notification as its small icon, so what the
 * user sees next to the clock/battery IS the number.
 */
object IconTextGenerator {

    private const val SIZE = 96 // px, matches the largest density notification icons render at

    fun forSpeed(bytesPerSecond: Long): IconCompat {
        val (value, unit) = shortLabel(bytesPerSecond)
        return render(value, unit)
    }

    /** Splits "12.4 MB/s" into a big number line and a small unit line so both stay legible. */
    private fun shortLabel(bytesPerSecond: Long): Pair<String, String> {
        val kbps = bytesPerSecond / 1024.0
        return when {
            kbps < 1.0 -> "0" to "K"
            kbps < 1000.0 -> String.format("%.0f", kbps) to "K"
            else -> String.format("%.1f", kbps / 1024.0) to "M"
        }
    }

    private fun render(value: String, unit: String): IconCompat {
        val bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = if (value.length > 2) SIZE * 0.42f else SIZE * 0.52f
        }
        val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = SIZE * 0.24f
        }

        val numberY = SIZE * 0.52f
        val unitY = SIZE * 0.86f
        canvas.drawText(value, SIZE / 2f, numberY, numberPaint)
        canvas.drawText(unit, SIZE / 2f, unitY, unitPaint)

        return IconCompat.createWithBitmap(bitmap)
    }
}
