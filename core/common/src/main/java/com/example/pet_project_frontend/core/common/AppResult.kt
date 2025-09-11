package com.example.pet_project_frontend.core.common

// 표준 Result/Either 타입 + ValidationError
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(
        val code: Int? = null,
        val message: String? = null,
        val validation: ValidationError? = null,
        val cause: Throwable? = null
    ) : AppResult<Nothing>()
    data class Exception(val throwable: Throwable) : AppResult<Nothing>()

    inline fun onSuccess(block: (T) -> Unit): AppResult<T> { if (this is Success) block(data); return this }
    inline fun onError(block: (Error) -> Unit): AppResult<T> { if (this is Error) block(this); return this }
    inline fun onException(block: (Throwable) -> Unit): AppResult<T> { if (this is Exception) block(throwable); return this }
}

data class ValidationError(
    val fields: Map<String, String> = emptyMap(), // field -> message
    val generalMessage: String? = null
)
