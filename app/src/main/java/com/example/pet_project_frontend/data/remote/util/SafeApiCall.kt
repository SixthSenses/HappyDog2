package com.example.pet_project_frontend.data.remote.util

import com.example.pet_project_frontend.data.remote.dto.response.ErrorResponse
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.google.gson.Gson
import retrofit2.Response

/**
 * Retrofit Response<T>를 표준 NetworkResult로 변환하는 유틸.
 */
object SafeApiCall {
    private val gson = Gson()

    fun <T> fromResponse(response: Response<T>): NetworkResult<T> {
        return try {
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    NetworkResult.Success(body)
                } else {
                    NetworkResult.Error(response.code(), "Empty response body")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val parsed = try {
                    errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
                } catch (_: Exception) { null }

                val message = parsed?.message ?: errorBody ?: "HTTP ${response.code()}"
                NetworkResult.Error(response.code(), message, parsed)
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun <T> call(block: suspend () -> Response<T>): NetworkResult<T> = try {
        fromResponse(block())
    } catch (e: Exception) {
        NetworkResult.Exception(e)
    }
}

inline fun <reified T> String.parseErrorOrNull(): ErrorResponse? = try {
    com.google.gson.Gson().fromJson(this, ErrorResponse::class.java)
} catch (_: Exception) { null }
