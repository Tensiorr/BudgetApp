package com.tensiorr.budgetapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages update-related preferences using DataStore.
 */
class UpdatePreferences(private val context: Context) {

    companion object {
        private val Context.updateDataStore: DataStore<Preferences> by preferencesDataStore(
            name = "update_preferences"
        )

        private val AUTO_CHECK_ENABLED = booleanPreferencesKey("auto_check_enabled")
        private val LAST_CHECK_TIMESTAMP = longPreferencesKey("last_check_timestamp")
        private val DISMISSED_VERSION = longPreferencesKey("dismissed_version")
    }

    /**
     * Whether automatic update checks are enabled.
     */
    val autoCheckEnabled: Flow<Boolean> = context.updateDataStore.data.map { preferences ->
        preferences[AUTO_CHECK_ENABLED] ?: true
    }

    /**
     * Sets auto-check enabled state.
     */
    suspend fun setAutoCheckEnabled(enabled: Boolean) {
        context.updateDataStore.edit { preferences ->
            preferences[AUTO_CHECK_ENABLED] = enabled
        }
    }

    /**
     * Timestamp of last update check.
     */
    val lastCheckTimestamp: Flow<Long> = context.updateDataStore.data.map { preferences ->
        preferences[LAST_CHECK_TIMESTAMP] ?: 0L
    }

    /**
     * Updates last check timestamp to current time.
     */
    suspend fun updateLastCheckTimestamp() {
        context.updateDataStore.edit { preferences ->
            preferences[LAST_CHECK_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    /**
     * Version code that user dismissed (to not show again).
     */
    val dismissedVersion: Flow<Long> = context.updateDataStore.data.map { preferences ->
        preferences[DISMISSED_VERSION] ?: 0L
    }

    /**
     * Marks a version as dismissed.
     */
    suspend fun dismissVersion(versionCode: Long) {
        context.updateDataStore.edit { preferences ->
            preferences[DISMISSED_VERSION] = versionCode
        }
    }
}