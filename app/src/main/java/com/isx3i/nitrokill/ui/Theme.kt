package com.isx3i.nitrokill.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NitroKillColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    secondary = Color(0xFFFF6D00),
    background = Color(0xFFF5F7F5),
    surface = Color.White,
    onSurface = Color(0xFF1A1C19),
)

@Composable
fun NitroKillTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NitroKillColors,
        content = content
    )
}
