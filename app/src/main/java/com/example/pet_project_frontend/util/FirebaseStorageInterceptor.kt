package com.example.pet_project_frontend.util

import android.util.Log
import coil.intercept.Interceptor
import coil.request.ImageResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap

/**
 * Coil Interceptor for Firebase Storage URLs
 * 
 * 만료된 Firebase Storage URL을 자동으로 갱신합니다.
 * URL 캐싱을 통해 반복 호출을 최소화합니다.
 */
class FirebaseStorageInterceptor : Interceptor {
    private val TAG = "FirebaseStorageInterceptor"
    
    // URL 캐시: 원본 URL -> 갱신된 URL
    private val urlCache = ConcurrentHashMap<String, String>()
    
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = chain.request
        val data = request.data
        
        // URL 문자열인 경우에만 처리
        if (data is String && isFirebaseStorageUrl(data)) {
            try {
                // 캐시 확인
                val cachedUrl = urlCache[data]
                if (cachedUrl != null) {
                    // 캐시된 URL 사용 (로그 생략하여 노이즈 감소)
                    val newRequest = request.newBuilder()
                        .data(cachedUrl)
                        .build()
                    return chain.proceed(newRequest)
                }
                
                Log.d(TAG, "Refreshing Firebase Storage URL")
                
                // 타임아웃 5초로 제한
                val validUrl = withTimeout(5000L) {
                    FirebaseUrlHelper.getValidDownloadUrl(data)
                }
                
                if (validUrl != data) {
                    Log.d(TAG, "URL refreshed successfully")
                    // 캐시에 저장
                    urlCache[data] = validUrl
                    
                    // 새 URL로 요청 재생성
                    val newRequest = request.newBuilder()
                        .data(validUrl)
                        .build()
                    return chain.proceed(newRequest)
                } else {
                    // URL이 갱신되지 않음 (이미 유효하거나 갱신 실패)
                    Log.d(TAG, "Using original URL (refresh not needed or failed)")
                }
            } catch (e: CancellationException) {
                // 코루틴 취소: 정상적인 흐름, 조용히 처리
                // 빠른 스크롤 시 발생 가능
                Log.v(TAG, "URL refresh cancelled (normal during fast scroll)")
                // 취소 예외는 다시 던져서 상위에서 처리하도록 함
                throw e
            } catch (e: Exception) {
                // 기타 에러: 원본 URL로 폴백
                Log.w(TAG, "Failed to refresh URL: ${e.javaClass.simpleName}: ${e.message}")
            }
        }
        
        return chain.proceed(request)
    }
    
    private fun isFirebaseStorageUrl(url: String): Boolean {
        return url.contains("firebasestorage.googleapis.com") ||
               url.contains("storage.googleapis.com")
    }
    
    /**
     * 캐시 초기화 (필요시 호출)
     */
    fun clearCache() {
        urlCache.clear()
        Log.d(TAG, "URL cache cleared")
    }
}
