package com.example.pet_project_frontend.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.pet_project_frontend.data.remote.api.AuthApi
import com.example.pet_project_frontend.data.remote.dto.request.*
import com.example.pet_project_frontend.data.remote.dto.response.*
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    @ApplicationContext private val context: Context
) : AuthRepository {

    private val dataStore = context.dataStore

    override suspend fun socialLogin(authCode: String): NetworkResult<SocialLoginResponse> {
        return try {
            val request = SocialLoginRequest(authCode = authCode)
            val response = authApi.socialLogin(request)
            
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    NetworkResult.Success(loginResponse)
                } ?: NetworkResult.Error(response.code(), "응답이 비어있습니다.")
            } else {
                NetworkResult.Error(response.code(), "로그인에 실패했습니다: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    override suspend fun refreshToken(refreshToken: String): NetworkResult<TokenRefreshResponse> {
        return try {
            val response = authApi.refreshToken("Bearer $refreshToken")
            
            if (response.isSuccessful) {
                response.body()?.let { tokenResponse ->
                    NetworkResult.Success(tokenResponse)
                } ?: NetworkResult.Error(response.code(), "응답이 비어있습니다.")
            } else {
                NetworkResult.Error(response.code(), "토큰 갱신에 실패했습니다: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    override suspend fun logout(accessToken: String, refreshToken: String): NetworkResult<Unit> {
        return try {
            val request = LogoutRequest(accessToken, refreshToken)
            val response = authApi.logout(request)
            
            if (response.isSuccessful) {
                clearTokens()
                clearUserInfo()
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.code(), "로그아웃에 실패했습니다: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    override suspend fun getAccessToken(): String? {
        var accessToken: String? = null
        dataStore.data.collect { preferences ->
            accessToken = preferences[ACCESS_TOKEN_KEY]
        }
        return accessToken
    }

    override suspend fun getRefreshToken(): String? {
        var refreshToken: String? = null
        dataStore.data.collect { preferences ->
            refreshToken = preferences[REFRESH_TOKEN_KEY]
        }
        return refreshToken
    }

    override suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }

    override suspend fun saveUserInfo(userInfo: UserInfo) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userInfo.userId
            preferences[USER_EMAIL_KEY] = userInfo.email
            preferences[USER_NICKNAME_KEY] = userInfo.nickname
            preferences[USER_PROFILE_IMAGE_URL_KEY] = userInfo.profileImageUrl ?: ""
        }
    }

    override suspend fun getUserInfo(): UserInfo? {
        var userInfo: UserInfo? = null
        dataStore.data.collect { preferences ->
            val userId = preferences[USER_ID_KEY] ?: return@collect
            val email = preferences[USER_EMAIL_KEY] ?: return@collect
            val nickname = preferences[USER_NICKNAME_KEY] ?: return@collect
            val profileImageUrl = preferences[USER_PROFILE_IMAGE_URL_KEY]?.takeIf { url -> url.isNotBlank() }
            
            userInfo = UserInfo(
                userId = userId,
                email = email,
                nickname = nickname,
                profileImageUrl = profileImageUrl
            )
        }
        return userInfo
    }

    override suspend fun clearUserInfo() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_NICKNAME_KEY)
            preferences.remove(USER_PROFILE_IMAGE_URL_KEY)
        }
    }

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NICKNAME_KEY = stringPreferencesKey("user_nickname")
        private val USER_PROFILE_IMAGE_URL_KEY = stringPreferencesKey("user_profile_image_url")
    }
}