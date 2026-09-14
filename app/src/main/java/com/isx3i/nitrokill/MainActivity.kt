package com.isx3i.nitrokill

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.isx3i.nitrokill.data.PrefsManager
import com.isx3i.nitrokill.service.CleanerAccessibilityService
import com.isx3i.nitrokill.service.SpeedMonitorService
import com.isx3i.nitrokill.ui.MainScreen
import com.isx3i.nitrokill.ui.NitroKillTheme
import com.isx3i.nitrokill.ui.OptionsScreen
import com.isx3i.nitrokill.util.LocaleHelper
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class MainActivity : ComponentActivity() {

    private lateinit var prefs: PrefsManager

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* if denied, the ongoing notification simply won't show — service still runs */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PrefsManager(this)
        requestNotificationPermissionIfNeeded()

        setContent {
            var screen by remember { mutableStateOf(Screen.MAIN) }

            NitroKillTheme {
                when (screen) {
                    Screen.MAIN -> MainScreen(
                        onOpenOptions = { screen = Screen.OPTIONS },
                        onReenable = { handleReenable() },
                        onHideAndExit = { moveTaskToBack(true) },
                        onToggleMonitor = { enabled -> toggleMonitoring(enabled) }
                    )
                    Screen.OPTIONS -> OptionsScreen(
                        prefs = prefs,
                        onBack = { screen = Screen.MAIN },
                        onLanguageChanged = { lang -> applyLanguageAndRestart(lang) },
                        onOpenAccessibilitySettings = { openAccessibilitySettings() },
                        isAccessibilityEnabled = { CleanerAccessibilityService.isEnabled() }
                    )
                }
            }
        }
    }

    private enum class Screen { MAIN, OPTIONS }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun toggleMonitoring(enabled: Boolean) {
        lifecycleScope.launch { prefs.setMonitorEnabled(enabled) }
        if (enabled) SpeedMonitorService.start(this) else SpeedMonitorService.stop(this)
    }

    /** "Re-enable": restarts the speed-monitor service if it died, and runs the cleaner. */
    private fun handleReenable() {
        SpeedMonitorService.start(this)
        val dispatched = CleanerAccessibilityService.requestClose()
        if (!dispatched) {
            Toast.makeText(this, getString(R.string.cleaner_toast_enable_first), Toast.LENGTH_LONG).show()
            openAccessibilitySettings()
        }
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun applyLanguageAndRestart(lang: String) {
        lifecycleScope.launch {
            prefs.setLanguage(lang)
            LocaleHelper.applyToResources(this@MainActivity, lang)
            recreate()
        }
    }
}
