package com.example.pet_project_frontend.data.remote.authenticator

import com.example.pet_project_frontend.data.local.preferences.TokenManager
import com.example.pet_project_frontend.data.remote.api.AuthApi
import com.example.pet_project_frontend.data.remote.dto.response.TokenRefreshResponse
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val retrofit: Retrofit
) : Authenticator {
    
    override fun authenticate(route: Route?, response: Response): Request? {
        // 401 에러가 아닌 경우 null 반환
        if (response.code != 401) {
            return null
        }
        
        // 리프레시 토큰 가져오기
        val refreshToken = runBlocking { tokenManager.getRefreshToken() }
        if (refreshToken == null) {
            // 리프레시 토큰이 없으면 로그아웃 처리
            runBlocking { tokenManager.clearTokens() }
            return null
        }
        
        return try {
            // 원래 요청 저장
            val originalRequest = response.request
            
            // 토큰 리프레시 API 호출
            val authApi = retrofit.create(AuthApi::class.java)
            val refreshResponse = runBlocking { 
                authApi.refreshToken("Bearer $refreshToken")
            }
            
            if (refreshResponse.isSuccessful) {
                refreshResponse.body()?.let { tokenResponse ->
                    // 새로운 토큰 저장
                    runBlocking {
                        tokenManager.saveAccessToken(tokenResponse.accessToken)
                        tokenManager.saveRefreshToken(tokenResponse.refreshToken)
                    }
                    
                    // 새로운 액세스 토큰으로 원래 요청 재시도
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer ${tokenResponse.accessToken}")
                        .build()
                } ?: null
            } else {
                null
            }
                
        } catch (e: Exception) {
            // 토큰 리프레시 실패 시 토큰 클리어
            runBlocking { tokenManager.clearTokens() }
            null
        }
    }
}
