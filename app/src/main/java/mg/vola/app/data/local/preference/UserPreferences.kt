package com.vola.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vola_preferences")

class UserPreferences(context: Context) {
    private val dataStore = context.dataStore
    
    // Keys
    private object Keys {
        val IS_ONBOARDING_COMPLETE = booleanPreferencesKey("is_onboarding_complete")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val SELECTED_CURRENCY = stringPreferencesKey("selected_currency")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FIRST_LAUNCH_DATE = longPreferencesKey("first_launch_date")
        val LAST_SYNC_DATE = longPreferencesKey("last_sync_date")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val MONTHLY_INCOME = doublePreferencesKey("monthly_income")
        val SAVINGS_TARGET = doublePreferencesKey("savings_target")
        val CURRENCY_FORMAT = stringPreferencesKey("currency_format")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val BACKUP_ENABLED = booleanPreferencesKey("backup_enabled")
        val BACKUP_FREQUENCY = intPreferencesKey("backup_frequency")
        val LAST_BACKUP_DATE = longPreferencesKey("last_backup_date")
    }
    
    // Flows
    val isOnboardingComplete: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[Keys.IS_ONBOARDING_COMPLETE] ?: false }
    
    val selectedLanguage: Flow<String> = dataStore.data
        .map { preferences -> preferences[Keys.SELECTED_LANGUAGE] ?: "mg" }
    
    val selectedCurrency: Flow<String> = dataStore.data
        .map { preferences -> preferences[Keys.SELECTED_CURRENCY] ?: "MGA" }
    
    val isBiometricEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[Keys.BIOMETRIC_ENABLED] ?: false }
    
    val isNotificationsEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[Keys.NOTIFICATIONS_ENABLED] ?: true }
    
    val isDarkMode: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[Keys.DARK_MODE] ?: false }
    
    val monthlyIncome: Flow<Double> = dataStore.data
        .map { preferences -> preferences[Keys.MONTHLY_INCOME] ?: 0.0 }
    
    // Setters
    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.IS_ONBOARDING_COMPLETE] = complete
        }
    }
    
    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[Keys.SELECTED_LANGUAGE] = language
        }
    }
    
    suspend fun setCurrency(currency: String) {
        dataStore.edit { preferences ->
            preferences[Keys.SELECTED_CURRENCY] = currency
        }
    }
    
    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.BIOMETRIC_ENABLED] = enabled
        }
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.DARK_MODE] = enabled
        }
    }
    
    suspend fun setMonthlyIncome(income: Double) {
        dataStore.edit { preferences ->
            preferences[Keys.MONTHLY_INCOME] = income
        }
    }
    
    suspend fun setSavingsTarget(target: Double) {
        dataStore.edit { preferences ->
            preferences[Keys.SAVINGS_TARGET] = target
        }
    }
    
    suspend fun setUserInfo(name: String, email: String) {
        dataStore.edit { preferences ->
            preferences[Keys.USER_NAME] = name
            preferences[Keys.USER_EMAIL] = email
            if (preferences[Keys.USER_ID] == null) {
                preferences[Keys.USER_ID] = generateUserId()
            }
            if (preferences[Keys.FIRST_LAUNCH_DATE] == null) {
                preferences[Keys.FIRST_LAUNCH_DATE] = System.currentTimeMillis()
            }
        }
    }
    
    suspend fun updateLastSyncDate() {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_SYNC_DATE] = System.currentTimeMillis()
        }
    }
    
    suspend fun updateLastBackupDate() {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_BACKUP_DATE] = System.currentTimeMillis()
        }
    }
    
    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    // Helper methods
    suspend fun getUserId(): String? {
        return dataStore.data.map { it[Keys.USER_ID] }.firstOrNull()
    }
    
    suspend fun getUserName(): String? {
        return dataStore.data.map { it[Keys.USER_NAME] }.firstOrNull()
    }
    
    suspend fun getFirstLaunchDate(): Long? {
        return dataStore.data.map { it[Keys.FIRST_LAUNCH_DATE] }.firstOrNull()
    }
    
    suspend fun getDaysSinceFirstLaunch(): Int {
        val firstLaunch = getFirstLaunchDate() ?: return 0
        val firstLaunchDate = Instant.fromEpochMilliseconds(firstLaunch)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        val today = kotlinx.datetime.Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        return firstLaunchDate.daysUntil(today)
    }
    
    private fun generateUserId(): String {
        return "user_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    companion object {
        const val DEFAULT_CURRENCY = "MGA"
        const val DEFAULT_LANGUAGE = "mg"
        const val DEFAULT_DATE_FORMAT = "dd/MM/yyyy"
        const val DEFAULT_CURRENCY_FORMAT = "#,##0"
    }
}