package com.example.pet_project_frontend.data.remote.upload

import com.example.pet_project_frontend.data.remote.api.UploadApi
import com.example.pet_project_frontend.data.remote.dto.request.GetUploadUrlRequest
import com.example.pet_project_frontend.data.remote.dto.response.UploadUrlResponse
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 파일 업로드 타입 enum
 * 백엔드 API 명세서에 정의된 upload_type 값들
 */
enum class UploadType(val value: String) {
    USER_PROFILE("user_profile"),
    PET_NOSE_PRINT("pet_nose_print"),
    EYE_ANALYSIS("eye_analysis"),
    POST_IMAGE("post_image"),
    CARTOON_SOURCE_IMAGE("cartoon_source_image")
}

/**
 * 파일 업로드 매니저
 * 2단계 파일 업로드 워크플로우를 처리
 */
@Singleton
class FileUploadManager @Inject constructor(
    private val uploadApi: UploadApi,
    private val okHttpClient: OkHttpClient
) {
    
    /**
     * 파일 업로드 수행
     * 
     * @param file 업로드할 파일
     * @param uploadType 업로드 타입
     * @return 업로드된 파일의 경로 (file_path)
     */
    suspend fun uploadFile(
        file: File,
        uploadType: UploadType
    ): NetworkResult<String> = withContext(Dispatchers.IO) {
        try {
            // 1단계: Pre-signed URL 요청
            val urlResult = getUploadUrl(
                uploadType = uploadType,
                filename = file.name,
                contentType = getMimeType(file.extension)
            )
            
            when (urlResult) {
                is NetworkResult.Success -> {
                    val uploadInfo = urlResult.data
                    
                    // 2단계: 실제 파일 업로드
                    val uploadResult = uploadToStorage(
                        uploadUrl = uploadInfo.uploadUrl,
                        file = file,
                        contentType = getMimeType(file.extension)
                    )
                    
                    when (uploadResult) {
                        is NetworkResult.Success -> {
                            // 업로드 성공 시 file_path 반환
                            NetworkResult.Success(uploadInfo.filePath)
                        }
                        is NetworkResult.Error -> uploadResult
                        is NetworkResult.Exception -> uploadResult
                    }
                }
                is NetworkResult.Error -> urlResult
                is NetworkResult.Exception -> urlResult
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
    
    /**
     * 1단계: Pre-signed URL 요청
     */
    private suspend fun getUploadUrl(
        uploadType: UploadType,
        filename: String,
        contentType: String
    ): NetworkResult<UploadUrlResponse> {
        return try {
            val request = GetUploadUrlRequest(
                uploadType = uploadType.value,
                filename = filename,
                contentType = contentType
            )
            
            val response = uploadApi.getUploadUrl(request)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    NetworkResult.Success(it)
                } ?: NetworkResult.Error(response.code(), "Empty response body")
            } else {
                NetworkResult.Error(response.code(), "Failed to get upload URL: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
    
    /**
     * 2단계: Firebase Storage에 실제 파일 업로드
     */
    private suspend fun uploadToStorage(
        uploadUrl: String,
        file: File,
        contentType: String
    ): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val fileBytes = file.readBytes()
            val requestBody = fileBytes.toRequestBody(contentType.toMediaType())
            
            val request = Request.Builder()
                .url(uploadUrl)
                .put(requestBody)
                .header("Content-Type", contentType)
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.code, "Upload failed: ${response.code}")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
    
    /**
     * 파일 확장자로부터 MIME 타입 추출
     */
    private fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }
    }
}