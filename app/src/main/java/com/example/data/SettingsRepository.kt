package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    companion object {
        val WEB_APP_URL = stringPreferencesKey("web_app_url")
        val ACADEMIC_YEAR = stringPreferencesKey("academic_year")
    }

    val webAppUrlFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[WEB_APP_URL] ?: ""
    }

    val academicYearFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ACADEMIC_YEAR] ?: ""
    }

    suspend fun updateWebAppUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[WEB_APP_URL] = url
        }
    }

    suspend fun updateAcademicYear(year: String) {
        context.dataStore.edit { preferences ->
            preferences[ACADEMIC_YEAR] = year
        }
    }
}
