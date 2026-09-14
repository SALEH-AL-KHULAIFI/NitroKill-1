package com.isx3i.nitrokill.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    /**
     * Wraps the supplied Context with the requested language.
     *
     * This method does not access DataStore and is safe to use
     * during application startup.
     */
    fun wrap(
        context: Context,
        languageCode: String
    ): Context {

        val language = if (languageCode == "en") {
            "en"
        } else {
            "ar"
        }

        val locale = Locale(language)

        Locale.setDefault(locale)

        val configuration = Configuration(
            context.resources.configuration
        )

        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(
            configuration
        )
    }

    /**
     * Applies the selected language to the current resources.
     *
     * MainActivity calls this after the language preference
     * has been changed.
     */
    fun applyToResources(
        context: Context,
        languageCode: String
    ) {
        val language = if (languageCode == "en") {
            "en"
        } else {
            "ar"
        }

        val locale = Locale(language)

        Locale.setDefault(locale)

        val configuration = Configuration(
            context.resources.configuration
        )

        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(
            configuration,
            context.resources.displayMetrics
        )
    }
}
