package com.mihai.dailyhabit

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

val KEY_THEME_MODE = stringPreferencesKey("theme_mode")

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

class ThemePreferences(private val context: Context) {
    
    init {
        // Idempotent migration
        val sharedPrefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        if (sharedPrefs.contains("is_dark_theme")) {
            val isDark = sharedPrefs.getBoolean("is_dark_theme", false)
            val newMode = if (isDark) ThemeMode.DARK else ThemeMode.LIGHT
            runBlocking {
                context.dataStore.edit { prefs ->
                    if (prefs[KEY_THEME_MODE] == null) {
                        prefs[KEY_THEME_MODE] = newMode.name
                    }
                }
            }
            sharedPrefs.edit().remove("is_dark_theme").apply()
        }
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val modeString = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeString)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

}
