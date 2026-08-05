package com.example

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _useSystemTheme = MutableStateFlow(prefs.getBoolean("use_system_theme", true))
    val useSystemTheme: StateFlow<Boolean> = _useSystemTheme.asStateFlow()

    private val _useDarkTheme = MutableStateFlow(prefs.getBoolean("use_dark_theme", false))
    val useDarkTheme: StateFlow<Boolean> = _useDarkTheme.asStateFlow()

    private val _useDynamicColor = MutableStateFlow(prefs.getBoolean("use_dynamic_color", true))
    val useDynamicColor: StateFlow<Boolean> = _useDynamicColor.asStateFlow()

    fun setUseSystemTheme(value: Boolean) {
        prefs.edit().putBoolean("use_system_theme", value).apply()
        _useSystemTheme.value = value
    }

    fun setUseDarkTheme(value: Boolean) {
        prefs.edit().putBoolean("use_dark_theme", value).apply()
        _useDarkTheme.value = value
    }

    fun setUseDynamicColor(value: Boolean) {
        prefs.edit().putBoolean("use_dynamic_color", value).apply()
        _useDynamicColor.value = value
    }
}
