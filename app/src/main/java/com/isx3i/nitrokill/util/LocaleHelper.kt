package com.isx3i.nitrokill.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.isx3i.nitrokill.data.PrefsManager
import java.util.Locale

/**
 * NitroKill's language switch is an in-app override (not tied to the phone's
 * system language), so we wrap the base Context with the chosen Locale
 * ourselves rather than relying on AppCompat's per-app-language APIs —
 * this keeps the app usable even on the minimal, non-AppCompat theme.
 */
object LocaleHelper {

    fun readSavedLanguageBlocking(context: Context): String =
        PrefsManager.readLanguageBlocking(context)

    fun wrap(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    /** Call after changing the language so open screens pick it up immediately. */
    fun applyToResources(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }
}
