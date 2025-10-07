package com.example.pet_project_frontend.data.repository

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.core.common.SafeApi
import com.example.pet_project_frontend.data.mapper.CartoonJobMapper
import com.example.pet_project_frontend.data.remote.api.CartoonJobApi
import com.example.pet_project_frontend.data.remote.api.UploadApi
import com.example.pet_project_frontend.data.remote.dto.request.CartoonJobCreateRequest
import com.example.pet_project_frontend.data.remote.dto.request.FinalizeCartoonRequest
import com.example.pet_project_frontend.data.remote.dto.request.UploadType
import com.example.pet_project_frontend.data.remote.dto.request.UploadUrlRequest
import com.example.pet_project_frontend.domain.model.CartoonJob
import com.example.pet_project_frontend.domain.model.CartoonJobHealth
import com.example.pet_project_frontend.domain.repository.CartoonJobRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartoonJobRepositoryImpl @Inject constructor(
    private val cartoonJobApi: CartoonJobApi,
    private val uploadApi: UploadApi,
    private val okHttpClient: OkHttpClient
) : CartoonJobRepository {
    
    override suspend fun uploadFile(
        file: File,
        uploadType: UploadType,
        contentType: String
    ): AppResult<String> {
        // 1. Pre-signed URL 요청
        val urlRequest = UploadUrlRequest(
            uploadType = uploadType,
            filename = file.name,
            contentType = contentType
        )
        
        val urlResult = SafeApi.response {
            uploadApi.requestUploadUrl(urlRequest)
        }
        
        return when (urlResult) {
            is AppResult.Success -> {
                val uploadUrl = urlResult.data.uploadUrl
                val filePath = urlResult.data.filePath
                
                // 2. Pre-signed URL로 파일 업로드 (IO 스레드에서 실행)
                try {
                    withContext(Dispatchers.IO) {
                        val requestBody = file.asRequestBody(contentType.toMediaTypeOrNull())
                        val request = Request.Builder()
                            .url(uploadUrl)
                            .put(requestBody)
                            .build()
                        
                        val response = okHttpClient.newCall(request).execute()
                        
                        if (response.isSuccessful) {
                            AppResult.Success(filePath)
                        } else {
                            AppResult.Error(
                                code = response.code,
                                message = "파일 업로드 실패: ${response.message}"
                            )
                        }
                    }
                } catch (e: Exception) {
                    AppResult.Exception(e)
                }
            }
            is AppResult.Error -> urlResult
            is AppResult.Exception -> urlResult
        }
    }
    
    override suspend fun createCartoonJob(
        filePath: String,
        userText: String?,
        idempotencyKey: String?
    ): AppResult<CartoonJob> {
        val request = CartoonJobCreateRequest(
            filePaths = listOf(filePath),
            userText = userText
        )
        
        val key = idempotencyKey ?: UUID.randomUUID().toString()
        
        val result = SafeApi.response {
            cartoonJobApi.createCartoonJob(request, key)
        }
        
        return when (result) {
            is AppResult.Success -> AppResult.Success(CartoonJobMapper.toDomain(result.data))
            is AppResult.Error -> result
            is AppResult.Exception -> result
        }
    }
    
    override suspend fun getCartoonJob(jobId: String): AppResult<CartoonJob> {
        val result = SafeApi.response {
            cartoonJobApi.getCartoonJob(jobId)
        }
        
        return when (result) {
            is AppResult.Success -> AppResult.Success(CartoonJobMapper.toDomain(result.data))
            is AppResult.Error -> result
            is AppResult.Exception -> result
        }
    }
    
    override suspend fun cancelCartoonJob(
        jobId: String,
        idempotencyKey: String?
    ): AppResult<CartoonJob> {
        val key = idempotencyKey ?: UUID.randomUUID().toString()
        
        val result = SafeApi.response {
            cartoonJobApi.cancelCartoonJob(jobId, idempotencyKey = key)
        }
        
        return when (result) {
            is AppResult.Success -> AppResult.Success(CartoonJobMapper.toDomain(result.data))
            is AppResult.Error -> result
            is AppResult.Exception -> result
        }
    }
    
    override suspend fun getCartoonJobHealth(): AppResult<CartoonJobHealth> {
        val result = SafeApi.response {
            cartoonJobApi.getCartoonJobHealth()
        }
        
        return when (result) {
            is AppResult.Success -> AppResult.Success(CartoonJobMapper.toDomain(result.data))
            is AppResult.Error -> result
            is AppResult.Exception -> result
        }
    }
    
    override suspend fun finalizeCartoonImage(filePath: String): AppResult<String> {
        val request = FinalizeCartoonRequest(filePath)
        
        val result = SafeApi.response {
            uploadApi.finalizeCartoon(request)
        }
        
        return when (result) {
            is AppResult.Success -> AppResult.Success(result.data.publicUrl)
            is AppResult.Error -> result
            is AppResult.Exception -> result
        }
    }
}
