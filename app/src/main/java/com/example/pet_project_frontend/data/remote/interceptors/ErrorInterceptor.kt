package com.example.pet_project_frontend.data.remote.interceptors

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 에러를 예외로 변환하지 않고 그대로 내려보내는 pass-through 인터셉터.
 * 401은 TokenAuthenticator가 처리하므로 여기서는 어떠한 예외도 던지지 않습니다.
 */
class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        // 모든 상태코드는 그대로 반환하여 Retrofit 레벨에서 처리(SafeApiCall 등)
        return response
    }
}