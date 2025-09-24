// app/src/main/java/com/example/pet_project_frontend/data/repository/AuthRepositoryImpl.kt

package com.example.pet_project_frontend.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.pet_project_frontend.data.local.preferences.TokenManager
import com.example.pet_project_frontend.data.remote.api.AuthApi
import com.example.pet_project_frontend.data.remote.dto.request.*
import com.example.pet_project_frontend.data.remote.dto.response.*
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.core.common.SafeApi
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

    override suspend fun socialLogin(authCode: String): AppResult<SocialLoginResponse> {
        Log.d(TAG, "Attempting social login with auth code")
        val request = SocialLoginRequest(
            provider = "google",
            authCode = authCode
        )
        val result: AppResult<SocialLoginResponse> = SafeApi.response { authApi.socialLogin(request) }
        when (result) {
            is AppResult.Success -> Log.d(TAG, "Login successful. User ID: ${result.data.userId}, Is new: ${result.data.isNewUser}")
            is AppResult.Error -> Log.e(TAG, "Login failed. Code: ${result.code}, Error: ${result.message}")
            is AppResult.Exception -> Log.e(TAG, "Login exception", result.throwable)
        }
        return result
    }

    override suspend fun refreshToken(refreshToken: String): AppResult<TokenRefreshResponse> {
        Log.d(TAG, "Attempting to refresh token")
        val result: AppResult<TokenRefreshResponse> = SafeApi.response { authApi.refreshToken("Bearer $refreshToken", EmptyRequest()) }
        when (result) {
            is AppResult.Success -> {
                Log.d(TAG, "Token refresh successful")
                // 리프레시 토큰은 서버에서 재발급하지 않으므로 기존 값을 유지하고 액세스 토큰만 갱신
                tokenManager.saveAccessToken(result.data.accessToken)
            }
            is AppResult.Error -> Log.e(TAG, "Token refresh failed. Code: ${result.code}, Error: ${result.message}")
            is AppResult.Exception -> Log.e(TAG, "Token refresh exception", result.throwable)
        }
        return result
    }

    override suspend fun logout(accessToken: String, refreshToken: String): AppResult<Unit> {
        Log.d(TAG, "Attempting logout")
        val request = LogoutRequest(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
        val apiResult: AppResult<LogoutResponse> = SafeApi.response { authApi.logout(request) }
        when (apiResult) {
            is AppResult.Success -> Log.d(TAG, "Logout successful")
            is AppResult.Error -> Log.e(TAG, "Logout failed. Code: ${apiResult.code}, Error: ${apiResult.message}")
            is AppResult.Exception -> Log.e(TAG, "Exception during logout", apiResult.throwable)
        }
        // 서버 성공/실패와 무관하게 로컬 정리 수행
        clearTokens()
        clearUserInfo()
        // 도메인 계층에는 DTO를 숨기고 Unit으로 결과 매핑
        return when (apiResult) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Error -> AppResult.Error(
                code = apiResult.code,
                message = apiResult.message,
                validation = apiResult.validation,
                cause = apiResult.cause
            )
            is AppResult.Exception -> AppResult.Exception(apiResult.throwable)
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

    override suspend fun saveUser(user: com.example.pet_project_frontend.domain.model.User) {
        Log.d(TAG, "Saving user (domain): ${user.id}")
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.id
            preferences[USER_EMAIL_KEY] = user.email
            preferences[USER_NICKNAME_KEY] = user.name
            preferences[USER_PROFILE_IMAGE_URL_KEY] = user.profileImageUrl ?: ""
        }
    }
}