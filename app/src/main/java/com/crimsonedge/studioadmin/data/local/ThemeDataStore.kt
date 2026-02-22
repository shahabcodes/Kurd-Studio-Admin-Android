package com.crimsonedge.studioadmin.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

@Singleton
class ThemeDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("themeMode")
        val APP_THEME = stringPreferencesKey("appTheme")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometricEnabled")
    }

    val themeMode: Flow<String> = context.themeDataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: "SYSTEM"
    }

    val appTheme: Flow<String> = context.themeDataStore.data.map { prefs ->
        prefs[Keys.APP_THEME] ?: "ROSE"
    }

    suspend fun setThemeMode(mode: String) {
        context.themeDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }

    suspend fun setAppTheme(theme: String) {
        context.themeDataStore.edit { prefs ->
            prefs[Keys.APP_THEME] = theme
        }
    }

    val biometricEnabled: Flow<Boolean> = context.themeDataStore.data.map { prefs ->
        prefs[Keys.BIOMETRIC_ENABLED] ?: true
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[Keys.BIOMETRIC_ENABLED] = enabled
        }
    }
}
