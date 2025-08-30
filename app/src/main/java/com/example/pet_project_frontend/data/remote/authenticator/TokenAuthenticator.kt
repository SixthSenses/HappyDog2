package com.example.pet_project_frontend.data.remote.authenticator

import com.example.pet_project_frontend.data.local.preferences.TokenManager
import com.example.pet_project_frontend.data.remote.api.AuthApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRetrofit: Retrofit
) : Authenticator {
    
    private val mutex = Mutex()
    
    override fun authenticate(route: Route?, response: Response): Request? {
        // 401 에러가 아닌 경우 null 반환
        if (response.code != 401) {
            return null
        }
        
        // 이미 재시도한 경우 null 반환 (무한 루프 방지)
        if (response.request.header("Retry-After-Refresh") != null) {
            return null
        }
        
        return runBlocking {
            mutex.withLock {
                // 현재 저장된 토큰 확인
                val currentAccessToken = tokenManager.getAccessToken()
                val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                
                // 토큰이 이미 갱신되었는지 확인
                if (currentAccessToken != null && currentAccessToken != requestToken) {
                    // 이미 다른 스레드에서 토큰을 갱신했으므로 새 토큰으로 재시도
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer $currentAccessToken")
                        .header("Retry-After-Refresh", "true")
                        .build()
                }
                
                // 리프레시 토큰으로 새 액세스 토큰 요청
                val refreshToken = tokenManager.getRefreshToken()
                if (refreshToken == null) {
                    // 리프레시 토큰이 없으면 로그아웃 처리
                    tokenManager.clearTokens()
                    return@withLock null
                }
                
                try {
                    val authApi = authRetrofit.create(AuthApi::class.java)
                    val refreshResponse = authApi.refreshToken("Bearer $refreshToken")
                    
                    if (refreshResponse.isSuccessful) {
                        refreshResponse.body()?.let { tokenResponse ->
                            // 새로운 토큰 저장
                            tokenManager.saveAccessToken(tokenResponse.accessToken)
                            tokenManager.saveRefreshToken(tokenResponse.refreshToken)
                            
                            // 새로운 액세스 토큰으로 원래 요청 재시도
                            return@withLock response.request.newBuilder()
                                .header("Authorization", "Bearer ${tokenResponse.accessToken}")
                                .header("Retry-After-Refresh", "true")
                                .build()
                        }
                    }
                    
                    // 토큰 갱신 실패
                    tokenManager.clearTokens()
                    null
                } catch (e: Exception) {
                    // 네트워크 오류 등으로 토큰 갱신 실패
                    tokenManager.clearTokens()
                    null
                }
            }
        }
    }
}