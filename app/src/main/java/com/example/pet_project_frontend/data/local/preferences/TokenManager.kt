// app/src/main/java/com/example/pet_project_frontend/data/local/preferences/TokenManager.kt

package com.example.pet_project_frontend.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }

    /**
     * 액세스 토큰 저장
     */
    suspend fun saveAccessToken(token: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
        }
    }

    /**
     * 리프레시 토큰 저장
     */
    suspend fun saveRefreshToken(token: String) {
        dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN_KEY] = token
        }
    }

    /**
     * 두 토큰 모두 저장
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    /**
     * 액세스 토큰 가져오기 (동기)
     */
    suspend fun getAccessToken(): String? {
        return dataStore.data.first()[ACCESS_TOKEN_KEY]
    }

    /**
     * 리프레시 토큰 가져오기 (동기)
     */
    suspend fun getRefreshToken(): String? {
        return dataStore.data.first()[REFRESH_TOKEN_KEY]
    }

    /**
     * 액세스 토큰 Flow 반환 (비동기)
     */
    fun getAccessTokenFlow(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }
    }

    /**
     * 리프레시 토큰 Flow 반환 (비동기)
     */
    fun getRefreshTokenFlow(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }
    }

    /**
     * 모든 토큰 삭제
     */
    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }

    /**
     * 토큰 존재 여부 확인
     */
    suspend fun hasTokens(): Boolean {
        val preferences = dataStore.data.first()
        return preferences[ACCESS_TOKEN_KEY] != null && preferences[REFRESH_TOKEN_KEY] != null
    }

    /**
     * 액세스 토큰 존재 여부 확인 (Flow)
     */
    fun hasAccessToken(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            !preferences[ACCESS_TOKEN_KEY].isNullOrBlank()
        }
    }
}