// app/src/main/java/com/example/pet_project_frontend/data/repository/AuthRepositoryImpl.kt

package com.example.pet_project_frontend.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.pet_project_frontend.data.local.preferences.TokenManager
import com.example.pet_project_frontend.data.remote.api.AuthApi
import com.example.pet_project_frontend.data.remote.dto.request.*
import com.example.pet_project_frontend.data.remote.dto.response.*
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val dataStore: DataStore<Preferences>
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NICKNAME_KEY = stringPreferencesKey("user_nickname")
        private val USER_PROFILE_IMAGE_URL_KEY = stringPreferencesKey("user_profile_image_url")
    }

    override suspend fun socialLogin(authCode: String): NetworkResult<SocialLoginResponse> {
        return try {
            Log.d(TAG, "Attempting social login with auth code")

            val request = SocialLoginRequest(
                provider = "google",
                authCode = authCode
            )

            Log.d(TAG, "Sending request to: /api/auth/social")
            val response = authApi.socialLogin(request)

            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    Log.d(TAG, "Login successful. User ID: ${loginResponse.userId}, Is new: ${loginResponse.isNewUser}")
                    NetworkResult.Success(loginResponse)
                } ?: run {
                    Log.e(TAG, "Response body is null")
                    NetworkResult.Error(response.code(), "응답이 비어있습니다.")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Login failed. Code: ${response.code()}, Error: $errorBody")
                NetworkResult.Error(
                    response.code(),
                    errorBody ?: "로그인에 실패했습니다: ${response.code()}"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during social login", e)
            NetworkResult.Exception(e)
        }
    }

    override suspend fun refreshToken(refreshToken: String): NetworkResult<TokenRefreshResponse> {
        return try {
            Log.d(TAG, "Attempting to refresh token")

            val response = authApi.refreshToken("Bearer $refreshToken")

            if (response.isSuccessful) {
                response.body()?.let { tokenResponse ->
                    Log.d(TAG, "Token refresh successful")
                    // 새 토큰 자동 저장
                    saveTokens(tokenResponse.accessToken, tokenResponse.refreshToken)
                    NetworkResult.Success(tokenResponse)
                } ?: run {
                    Log.e(TAG, "Token refresh response body is null")
                    NetworkResult.Error(response.code(), "응답이 비어있습니다.")
                }
            } else {
                Log.e(TAG, "Token refresh failed. Code: ${response.code()}")
                NetworkResult.Error(response.code(), "토큰 갱신에 실패했습니다: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during token refresh", e)
            NetworkResult.Exception(e)
        }
    }

    override suspend fun logout(accessToken: String, refreshToken: String): NetworkResult<Unit> {
        return try {
            Log.d(TAG, "Attempting logout")

            val request = LogoutRequest(
                accessToken = accessToken,
                refreshToken = refreshToken
            )

            val response = authApi.logout(request)

            if (response.isSuccessful) {
                Log.d(TAG, "Logout successful")
                clearTokens()
                clearUserInfo()
                NetworkResult.Success(Unit)
            } else {
                Log.e(TAG, "Logout failed. Code: ${response.code()}")
                // 서버 로그아웃 실패해도 로컬 토큰은 삭제
                clearTokens()
                clearUserInfo()
                NetworkResult.Error(response.code(), "로그아웃에 실패했습니다: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during logout", e)
            // 예외 발생해도 로컬 토큰은 삭제
            clearTokens()
            clearUserInfo()
            NetworkResult.Exception(e)
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        Log.d(TAG, "Saving tokens")
        tokenManager.saveTokens(accessToken, refreshToken)
    }

    override suspend fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }

    override suspend fun getRefreshToken(): String? {
        return tokenManager.getRefreshToken()
    }

    override suspend fun clearTokens() {
        Log.d(TAG, "Clearing tokens")
        tokenManager.clearTokens()
    }

    override suspend fun saveUserInfo(userInfo: UserInfo) {
        Log.d(TAG, "Saving user info: ${userInfo.userId}")
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userInfo.userId
            preferences[USER_EMAIL_KEY] = userInfo.email
            preferences[USER_NICKNAME_KEY] = userInfo.nickname
            preferences[USER_PROFILE_IMAGE_URL_KEY] = userInfo.profileImageUrl ?: ""
        }
    }

    override suspend fun getUserInfo(): UserInfo? {
        return try {
            val preferences = dataStore.data.first()
            val userId = preferences[USER_ID_KEY] ?: return null
            val email = preferences[USER_EMAIL_KEY] ?: return null
            val nickname = preferences[USER_NICKNAME_KEY] ?: return null
            val profileImageUrl = preferences[USER_PROFILE_IMAGE_URL_KEY]?.takeIf { it.isNotBlank() }

            UserInfo(
                userId = userId,
                email = email,
                nickname = nickname,
                profileImageUrl = profileImageUrl
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user info", e)
            null
        }
    }

    override suspend fun clearUserInfo() {
        Log.d(TAG, "Clearing user info")
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_NICKNAME_KEY)
            preferences.remove(USER_PROFILE_IMAGE_URL_KEY)
        }
    }
}