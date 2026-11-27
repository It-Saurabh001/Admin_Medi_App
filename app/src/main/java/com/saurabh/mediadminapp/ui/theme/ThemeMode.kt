package com.saurabh.mediadminapp.ui.theme

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val label: String) { SYSTEM("System default"), LIGHT("Light"), DARK("Dark") }

/** The admin's theme choice, kept across app restarts. */
class ThemeController(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE)
    private val _mode = MutableStateFlow(
        runCatching { ThemeMode.valueOf(prefs.getString(KEY, null) ?: "") }.getOrDefault(ThemeMode.SYSTEM)
    )
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun set(mode: ThemeMode) {
        _mode.value = mode
        prefs.edit { putString(KEY, mode.name) }
    }

    private companion object { const val KEY = "theme_mode" }
}

val LocalThemeController = staticCompositionLocalOf<ThemeController?> { null }
