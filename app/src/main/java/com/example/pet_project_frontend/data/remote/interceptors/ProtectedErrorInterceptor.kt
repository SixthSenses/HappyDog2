package com.example.pet_project_frontend.data.remote.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 보호 API 에러를 예외로 변환하지 않고 그대로 전달하는 pass-through.
 * 서버 스펙에 selected_pet_id 개념은 없으므로 관련 동작은 제거.
 */
class ProtectedErrorInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        return response
    }
}
