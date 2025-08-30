package com.example.pet_project_frontend.data.remote.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import java.io.IOException

class ErrorInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        
        if (!response.isSuccessful) {
            val errorBody = response.body?.string()
            
            // 에러 메시지 파싱
            val errorMessage = try {
                errorBody?.let {
                    JSONObject(it).optString("message", "Unknown error")
                } ?: "Unknown error"
            } catch (e: Exception) {
                "Unknown error"
            }
            
            // 특정 에러 코드에 대한 처리
            when (response.code) {
                400 -> throw BadRequestException(errorMessage)
                401 -> {
                    // TokenAuthenticator에서 처리하므로 여기서는 응답만 반환
                    return response.newBuilder()
                        .body(errorBody?.toResponseBody(response.body?.contentType()))
                        .build()
                }
                403 -> throw ForbiddenException(errorMessage)
                404 -> throw NotFoundException(errorMessage)
                422 -> throw UnprocessableEntityException(errorMessage)
                500, 502, 503, 504 -> throw ServerException(errorMessage)
                else -> throw ApiException(response.code, errorMessage)
            }
        }
        
        return response
    }
}

// Custom Exceptions
class ApiException(val code: Int, message: String) : IOException(message)
class BadRequestException(message: String) : IOException(message)
class ForbiddenException(message: String) : IOException(message)
class NotFoundException(message: String) : IOException(message)
class UnprocessableEntityException(message: String) : IOException(message)
class ServerException(message: String) : IOException(message)