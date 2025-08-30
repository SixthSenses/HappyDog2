package com.example.pet_project_frontend.data.remote.interceptors

import com.example.pet_project_frontend.data.local.preferences.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Authorization 헤더가 이미 있으면 그대로 진행
        if (originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest)
        }
        
        // Access Token 가져오기
        val accessToken = runBlocking { tokenManager.getAccessToken() }
        if (accessToken == null) {
            return chain.proceed(originalRequest)
        }
        
        // Authorization 헤더 추가
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        
        return chain.proceed(newRequest)
    }
}