package com.tensiorr.budgetapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tensiorr.budgetapp.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * User preferences storage using DataStore.
 *
 * Stores:
 * - Theme mode (LIGHT, DARK, SYSTEM)
 * - Language preference
 * - Default currency
 * - Date format preference
 */
class UserPreferences(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val DEFAULT_CURRENCY_KEY = stringPreferencesKey("default_currency")
        private val DATE_FORMAT_KEY = stringPreferencesKey("date_format")
        private val IS_GUEST_MODE = booleanPreferencesKey("is_guest_mode")
        private val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        private val WAS_GUEST_BEFORE_LOGIN = booleanPreferencesKey("was_guest_before_login")

    }

    /**
     * Get theme mode flow.
     * Returns: "LIGHT", "DARK", or "SYSTEM"
     */
    val themeModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: "SYSTEM"
    }

    /**
     * Set theme mode.
     */
    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    /**
     * Get language flow.
     * Returns: "pl" or "en"
     */
    val languageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "pl"
    }

    /**
     * Set language.
     */
    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    /**
     * Get default currency flow.
     * Returns: "PLN", "EUR", "USD", etc.
     */
    val defaultCurrencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_CURRENCY_KEY] ?: "PLN"
    }

    /**
     * Set default currency.
     */
    suspend fun setDefaultCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_CURRENCY_KEY] = currency
        }
    }

    /**
     * Get theme mode as enum flow.
     */
    val themeModeEnumFlow: Flow<ThemeMode> = themeModeFlow.map { mode ->
        try {
            ThemeMode.valueOf(mode)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    /**
     * Get date format flow.
     * Returns: "DD.MM.YYYY", "MM.DD.YYYY", "YYYY-MM-DD", etc.
     */
    val dateFormatFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DATE_FORMAT_KEY] ?: "DD.MM.YYYY"
    }

    /**
     * Set date format.
     */
    suspend fun setDateFormat(format: String) {
        context.dataStore.edit { preferences ->
            preferences[DATE_FORMAT_KEY] = format
        }
    }

    val isGuestModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_GUEST_MODE] ?: false
        }

    suspend fun setGuestMode(isGuest: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_GUEST_MODE] = isGuest
        }
    }

    val lastSyncTimestampFlow: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_SYNC_TIMESTAMP]
        }

    suspend fun setLastSyncTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIMESTAMP] = timestamp
        }
    }

    suspend fun markGuestToLogin() {
        context.dataStore.edit { preferences ->
            preferences[WAS_GUEST_BEFORE_LOGIN] = true
        }
    }

    suspend fun clearGuestToLoginFlag() {
        context.dataStore.edit { preferences ->
            preferences[WAS_GUEST_BEFORE_LOGIN] = false
        }
    }

    suspend fun wasGuestBeforeLogin(): Boolean {
        return context.dataStore.data.first()[WAS_GUEST_BEFORE_LOGIN] ?: false
    }
}

