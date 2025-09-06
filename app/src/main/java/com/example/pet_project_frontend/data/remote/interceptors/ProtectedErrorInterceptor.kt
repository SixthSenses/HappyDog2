package com.example.pet_project_frontend.data.remote.interceptors

import com.example.pet_project_frontend.data.local.preferences.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 보호 API(Authorization 헤더 존재)에서 403/404를 수신하면
 * selected_pet_id를 전역적으로 무효화한다.
 * 예외는 던지지 않고 응답을 그대로 반환한다.
 */
class ProtectedErrorInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val hasAuthHeader = request.header("Authorization")?.isNotBlank() == true

        val response = chain.proceed(request)

        if (hasAuthHeader && (response.code == 403 || response.code == 404)) {
            // 비동기 저장소 접근을 위해 runBlocking 최소화
            runBlocking {
                tokenManager.clearSelectedPetId()
            }
        }

        return response
    }
}
