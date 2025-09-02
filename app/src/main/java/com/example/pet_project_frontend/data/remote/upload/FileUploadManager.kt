package com.example.pet_project_frontend.data.remote.upload

import com.example.pet_project_frontend.data.remote.api.UploadApi
import com.example.pet_project_frontend.data.remote.dto.request.GetUploadUrlRequest
import com.example.pet_project_frontend.data.remote.dto.response.UploadUrlResponse
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.data.remote.util.SafeApiCall
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
        val request = GetUploadUrlRequest(
            uploadType = uploadType.value,
            filename = filename,
            contentType = contentType
        )
        return SafeApiCall.call { uploadApi.getUploadUrl(request) }
    }
    
    /**
     * 2단계: Firebase Storage에 실제 파일 업로드
     */
    private suspend fun uploadToStorage(
        uploadUrl: String,
        file: File,
        contentType: String
    ): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        // 간단한 재시도 정책: 최대 2회 재시도(총 3회 시도), 타임아웃/일시 오류에 한해 재시도
        val maxAttempts = 3
        var lastError: Throwable? = null
        for (attempt in 1..maxAttempts) {
            try {
                val fileBytes = file.readBytes()
                val requestBody = fileBytes.toRequestBody(contentType.toMediaType())

                val request = Request.Builder()
                    .url(uploadUrl)
                    .put(requestBody)
                    .header("Content-Type", contentType)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                response.use { resp ->
                    if (resp.isSuccessful) {
                        return@withContext NetworkResult.Success(Unit)
                    }
                    // 5xx 또는 429이면 재시도 대상, 그 외는 즉시 실패
                    if (resp.code in 500..599 || resp.code == 429) {
                        // 429인 경우 Retry-After 헤더를 존중
                        if (resp.code == 429) {
                            val retryAfter = resp.header("Retry-After")?.toLongOrNull()
                            if (retryAfter != null && retryAfter > 0) {
                                // Retry-After는 초 단위가 일반적
                                kotlinx.coroutines.delay(retryAfter * 1000)
                            }
                        }
                        // 아래에서 백오프 후 다음 반복으로 이동
                    } else {
                        return@withContext NetworkResult.Error(
                            resp.code,
                            "파일 업로드 실패(${resp.code}): 서버에서 요청을 처리하지 못했습니다."
                        )
                    }
                }

                // 재시도 케이스: 지수 백오프 적용(기본 300ms, 최대 ~2.4s)
                if (attempt < maxAttempts) {
                    val backoffMs = (300L * (1 shl (attempt - 1)).coerceAtMost(8)).coerceAtMost(2400)
                    kotlinx.coroutines.delay(backoffMs)
                    // 다음 attempt로 진행 (루프가 자연스럽게 다음 반복으로 이동)
                }
            } catch (e: java.net.SocketTimeoutException) {
                lastError = e
                if (attempt >= maxAttempts) {
                    return@withContext NetworkResult.Error(
                        408,
                        "파일 업로드 시간이 초과되었습니다. 네트워크 상태를 확인 후 다시 시도해주세요."
                    )
                } else {
                    val backoffMs = (300L * (1 shl (attempt - 1)).coerceAtMost(8)).coerceAtMost(2400)
                    kotlinx.coroutines.delay(backoffMs)
                }
            } catch (e: java.io.IOException) {
                lastError = e
                if (attempt >= maxAttempts) {
                    return@withContext NetworkResult.Error(
                        0,
                        "네트워크 오류로 파일 업로드에 실패했습니다. 연결을 확인한 뒤 다시 시도해주세요."
                    )
                } else {
                    val backoffMs = (300L * (1 shl (attempt - 1)).coerceAtMost(8)).coerceAtMost(2400)
                    kotlinx.coroutines.delay(backoffMs)
                }
            } catch (e: Exception) {
                return@withContext NetworkResult.Exception(e)
            }
        }
        // 재시도 모두 실패
        NetworkResult.Exception(lastError ?: RuntimeException("알 수 없는 업로드 오류"))
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