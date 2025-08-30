package com.example.pet_project_frontend.data.remote.result

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>()
    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>()
    
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isException(): Boolean = this is Exception
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw RuntimeException("API Error: $code - $message")
        is Exception -> throw throwable
    }
    
    fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }
    
    fun onError(action: (Int, String) -> Unit): NetworkResult<T> {
        if (this is Error) {
            action(code, message)
        }
        return this
    }
    
    fun onException(action: (Throwable) -> Unit): NetworkResult<T> {
        if (this is Exception) {
            action(throwable)
        }
        return this
    }
}
