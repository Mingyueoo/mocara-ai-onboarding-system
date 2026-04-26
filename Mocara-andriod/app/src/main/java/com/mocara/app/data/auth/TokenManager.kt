package com.mocara.app.data.auth

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)

class TokenManager(context: Context) {
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(PREFS_NAME) }
    )

    val accessTokenFlow: Flow<String?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[ACCESS_TOKEN] }

    val refreshTokenFlow: Flow<String?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[REFRESH_TOKEN] }

    suspend fun getAccessToken(): String? = accessTokenFlow.firstOrNull()

    suspend fun getRefreshToken(): String? = refreshTokenFlow.firstOrNull()

    suspend fun saveTokens(tokens: AuthTokens) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = tokens.accessToken
            preferences[REFRESH_TOKEN] = tokens.refreshToken
        }
    }

    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
        }
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs.preferences_pb"
        private val ACCESS_TOKEN: Preferences.Key<String> = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN: Preferences.Key<String> = stringPreferencesKey("refresh_token")
    }
}
