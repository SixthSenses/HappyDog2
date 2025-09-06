package com.example.pet_project_frontend.core.common

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.HttpException
import retrofit2.Response

object SafeApi {
    private val gson by lazy { Gson() }

    suspend fun <T> body(block: suspend () -> T): AppResult<T> {
        return try {
            AppResult.Success(block())
        } catch (e: HttpException) {
            mapHttpException(e)
        } catch (t: Throwable) {
            AppResult.Exception(t)
        }
    }

    suspend fun <T> response(block: suspend () -> Response<T>): AppResult<T> {
        return try {
            val res = block()
            if (res.isSuccessful) {
                val body = res.body()
                if (body != null) AppResult.Success(body)
                else AppResult.Error(code = res.code(), message = "Empty response body")
            } else {
                mapHttpResponse(res)
            }
        } catch (e: HttpException) {
            mapHttpException(e)
        } catch (t: Throwable) {
            AppResult.Exception(t)
        }
    }

    // Like response(), but allows null body on success (e.g., 204 No Content)
    suspend fun <T> responseNullable(block: suspend () -> Response<T>): AppResult<T?> {
        return try {
            val res = block()
            if (res.isSuccessful) {
                AppResult.Success(res.body())
            } else {
                mapHttpResponse(res)
            }
        } catch (e: HttpException) {
            mapHttpException(e)
        } catch (t: Throwable) {
            AppResult.Exception(t)
        }
    }

    private fun mapHttpException(e: HttpException): AppResult.Error {
        val code = e.code()
        val raw = e.response()?.errorBody()?.string()
        val ve = parseValidationError(raw)
        val message = e.message() ?: ve?.generalMessage ?: "HTTP $code"
        return AppResult.Error(code = code, message = message, validation = ve, cause = e)
    }

    private fun mapHttpResponse(res: Response<*>): AppResult.Error {
        val code = res.code()
        val raw = res.errorBody()?.string()
        val ve = parseValidationError(raw)
        val message = res.message() ?: ve?.generalMessage ?: "HTTP $code"
        return AppResult.Error(code = code, message = message, validation = ve)
    }

    // Try to parse backend validation error shape: { message?: string, errors?: { field: message } }
    private fun parseValidationError(raw: String?): ValidationError? {
        return try {
            if (raw.isNullOrBlank()) null else run {
                val json = gson.fromJson(raw, JsonObject::class.java)
                val msg = if (json.has("message")) json.get("message").asString else null
                val fields = mutableMapOf<String, String>()
                if (json.has("errors") && json.get("errors").isJsonObject) {
                    val obj = json.getAsJsonObject("errors")
                    for ((k, v) in obj.entrySet()) {
                        fields[k] = v.asString
                    }
                }
                if (fields.isEmpty() && msg == null) null else ValidationError(fields = fields, generalMessage = msg)
            }
        } catch (_: Throwable) {
            null
        }
    }

    // For endpoints returning Response<Unit> (e.g., DELETE 204 No Content)
    suspend fun responseUnit(block: suspend () -> Response<Unit>): AppResult<Unit> {
        return try {
            val res = block()
            if (res.isSuccessful) {
                AppResult.Success(Unit)
            } else {
                mapHttpResponse(res)
            }
        } catch (e: HttpException) {
            mapHttpException(e)
        } catch (t: Throwable) {
            AppResult.Exception(t)
        }
    }
}
