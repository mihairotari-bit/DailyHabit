package com.mihai.dailyhabit

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean(KEY_IS_DARK, false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        val newState = !_isDarkTheme.value
        prefs.edit().putBoolean(KEY_IS_DARK, newState).apply()
        _isDarkTheme.value = newState
    }

    companion object {
        private const val KEY_IS_DARK = "is_dark_theme"
    }
}
