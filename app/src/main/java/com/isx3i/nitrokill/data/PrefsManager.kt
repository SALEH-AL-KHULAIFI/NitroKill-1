package com.isx3i.nitrokill.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "nitrokill_prefs"
)

class PrefsManager(
    private val context: Context
) {

    companion object {

        val KEY_LANGUAGE =
            stringPreferencesKey("language")

        val KEY_MONITOR_ENABLED =
            booleanPreferencesKey("monitor_enabled")
    }

    /**
     * Saved application language.
     *
     * Arabic is the default language.
     */
    val language: Flow<String> =
        context.dataStore.data.map { preferences ->
            val language = preferences[KEY_LANGUAGE]

            if (language == "en") {
                "en"
            } else {
                "ar"
            }
        }

    /**
     * Whether the internet speed monitor is enabled.
     */
    val monitorEnabled: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[KEY_MONITOR_ENABLED] ?: false
        }

    /**
     * Saves the selected application language.
     */
    suspend fun setLanguage(
        language: String
    ) {
        val safeLanguage = if (language == "en") {
            "en"
        } else {
            "ar"
        }

        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = safeLanguage
        }
    }

    /**
     * Saves the monitor enabled state.
     */
    suspend fun setMonitorEnabled(
        enabled: Boolean
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEY_MONITOR_ENABLED] = enabled
        }
    }
}
