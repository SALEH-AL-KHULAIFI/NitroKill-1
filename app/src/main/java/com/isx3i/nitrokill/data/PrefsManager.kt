package com.isx3i.nitrokill.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "nitrokill_prefs")

/**
 * Small wrapper around DataStore for the few settings NitroKill needs.
 * Nothing here is sensitive: a language code and two on/off flags.
 */
class PrefsManager(private val context: Context) {

    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("language") // "ar" or "en"
        val KEY_MONITOR_ENABLED = booleanPreferencesKey("monitor_enabled")

        /** Reads the saved language synchronously — only used once, from attachBaseContext. */
        fun readLanguageBlocking(context: Context): String = runBlocking {
            context.dataStore.data.map { it[KEY_LANGUAGE] ?: "ar" }.first()
        }
    }

    val language: Flow<String> = context.dataStore.data.map { it[KEY_LANGUAGE] ?: "ar" }
    val monitorEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_MONITOR_ENABLED] ?: false }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun setMonitorEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_MONITOR_ENABLED] = enabled }
    }
}
