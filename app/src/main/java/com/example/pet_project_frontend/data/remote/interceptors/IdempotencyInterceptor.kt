package com.example.pet_project_frontend.data.remote.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

/**
 * 서버 가이드에 따라 쓰기 메서드(POST/PUT/PATCH)에 X-Idempotency-Key를 자동 주입한다.
 * - 동일 호스트(우리 API 베이스 호스트)로 향하는 요청에만 주입한다.
 * - 이미 헤더가 있으면 보존한다(재시도 등에서 동일 키 유지).
 * - 외부 호스트(예: Firebase Storage 업로드)는 제외한다.
 */
class IdempotencyInterceptor @Inject constructor(
    @Named("API_BASE_HOST") private val apiBaseHost: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
    val method = request.method.uppercase()

    // 쓰기 메서드 + 일부 DELETE 화이트리스트만 대상
    val isWrite = method == "POST" || method == "PUT" || method == "PATCH"
    val isWhitelistedDelete = method == "DELETE" && request.url.encodedPath.startsWith("/api/cartoon-jobs/")
    if (!isWrite && !isWhitelistedDelete) return chain.proceed(request)

        // 동일 호스트로 향하는 API 요청만 대상
        if (request.url.host != apiBaseHost) {
            return chain.proceed(request)
        }

        // 이미 키가 있으면 그대로 진행 (재시도 시 동일 키 유지)
        if (request.header("X-Idempotency-Key") != null) {
            return chain.proceed(request)
        }

        val key = UUID.randomUUID().toString()
        val newRequest = request.newBuilder()
            .header("X-Idempotency-Key", key)
            .build()
        return chain.proceed(newRequest)
    }
}
