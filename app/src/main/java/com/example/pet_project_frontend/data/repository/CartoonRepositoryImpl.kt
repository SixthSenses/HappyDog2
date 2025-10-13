package com.example.pet_project_frontend.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.pet_project_frontend.data.remote.api.CartoonJobApi
import com.example.pet_project_frontend.data.remote.api.UploadApi
import com.example.pet_project_frontend.data.remote.dto.UploadUrlRequestDto
import com.example.pet_project_frontend.data.remote.dto.request.CartoonJobCreateRequest
import com.example.pet_project_frontend.domain.repository.CartoonJobStatus
import com.example.pet_project_frontend.domain.repository.CartoonRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartoonRepositoryImpl @Inject constructor(
    private val cartoonJobApi: CartoonJobApi,
    private val uploadApi: UploadApi,
    @ApplicationContext private val context: Context
) : CartoonRepository {

    companion object {
        private const val TAG = "CartoonRepository"
    }

    override suspend fun createCartoonJob(userText: String?, imageUri: Uri): Result<String> {
        return try {
            Log.d(TAG, "createCartoonJob: Starting, userText=$userText, imageUri=$imageUri")
            
            // 1. 업로드 URL 요청
            val filename = "cartoon_${System.currentTimeMillis()}.jpg"
            val contentType = "image/jpeg"
            val uploadType = "cartoon_source_image" // API 스펙: cartoon_source_image
            
            val uploadUrlResponse = uploadApi.getUploadUrl(
                UploadUrlRequestDto(
                    contentType = contentType,
                    uploadType = uploadType,
                    filename = filename
                )
            )
            
            if (!uploadUrlResponse.isSuccessful || uploadUrlResponse.body() == null) {
                Log.e(TAG, "createCartoonJob: Failed to get upload URL: ${uploadUrlResponse.code()}")
                return Result.failure(Exception("업로드 URL 생성 실패"))
            }
            
            val uploadUrlData = uploadUrlResponse.body()!!
            Log.d(TAG, "createCartoonJob: Got uploadUrl=${uploadUrlData.uploadUrl}")
            Log.d(TAG, "createCartoonJob: Got filePath=${uploadUrlData.filePath}")
            Log.d(TAG, "createCartoonJob: Got publicUrl=${uploadUrlData.publicUrl}")
            
            // fileUrl 구성: publicUrl > 완전한 URL인 filePath > uploadUrl에서 쿼리 제거
            val fileUrl = when {
                // publicUrl이 있으면 우선 사용
                uploadUrlData.publicUrl != null -> {
                    Log.d(TAG, "createCartoonJob: Using publicUrl")
                    uploadUrlData.publicUrl
                }
                // filePath가 이미 완전한 URL인 경우
                uploadUrlData.filePath.startsWith("http") -> {
                    Log.d(TAG, "createCartoonJob: filePath is complete URL")
                    uploadUrlData.filePath
                }
                // uploadUrl에 이미 전체 경로가 포함되어 있으므로 쿼리 파라미터만 제거
                else -> {
                    Log.d(TAG, "createCartoonJob: Using uploadUrl without query params")
                    uploadUrlData.uploadUrl.substringBefore("?")
                }
            }
            
            Log.d(TAG, "createCartoonJob: Using fileUrl=$fileUrl")
            
            // 2. 이미지 업로드
            val uploadResult = uploadImageToS3(uploadUrlData.uploadUrl, imageUri, contentType)
            if (uploadResult.isFailure) {
                Log.e(TAG, "createCartoonJob: Image upload failed")
                return Result.failure(uploadResult.exceptionOrNull() ?: Exception("이미지 업로드 실패"))
            }
            
            Log.d(TAG, "createCartoonJob: Image uploaded successfully")
            
            // 3. Cartoon Job 생성
            val request = CartoonJobCreateRequest(
                userText = userText,
                filePaths = listOf(fileUrl) // 완전한 URL 사용
            )
            
            Log.d(TAG, "createCartoonJob: Creating job with userText='$userText', fileUrl='$fileUrl'")
            val response = cartoonJobApi.createCartoonJob(request)
            
            if (response.isSuccessful && response.body() != null) {
                val jobId = response.body()!!.jobId
                Log.d(TAG, "createCartoonJob: Success, jobId=$jobId")
                Result.success(jobId)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "createCartoonJob: API failed: ${response.code()}")
                Log.e(TAG, "createCartoonJob: Request - userText=$userText, fileUrl=$fileUrl")
                Log.e(TAG, "createCartoonJob: Error body - $errorBody")
                Result.failure(Exception("만화 작업 생성 실패: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "createCartoonJob: Exception", e)
            Result.failure(e)
        }
    }

    override suspend fun getCartoonJobStatus(jobId: String): Result<CartoonJobStatus> {
        return try {
            Log.d(TAG, "getCartoonJobStatus: jobId=$jobId")
            
            val response = cartoonJobApi.getCartoonJobStatus(jobId)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val status = CartoonJobStatus(
                    jobId = body.jobId,
                    status = body.status,
                    resultImageUrl = body.resultImageUrl,
                    errorMessage = body.errorMessage
                )
                Log.d(TAG, "getCartoonJobStatus: status=${status.status}")
                Result.success(status)
            } else {
                Log.e(TAG, "getCartoonJobStatus: Failed: ${response.code()}")
                Result.failure(Exception("작업 상태 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCartoonJobStatus: Exception", e)
            Result.failure(e)
        }
    }

    override suspend fun cancelCartoonJob(jobId: String): Result<Unit> {
        return try {
            Log.d(TAG, "cancelCartoonJob: jobId=$jobId")
            
            val response = cartoonJobApi.cancelCartoonJob(jobId)
            
            if (response.isSuccessful) {
                Log.d(TAG, "cancelCartoonJob: Success")
                Result.success(Unit)
            } else {
                Log.e(TAG, "cancelCartoonJob: Failed: ${response.code()}")
                Result.failure(Exception("작업 취소 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "cancelCartoonJob: Exception", e)
            Result.failure(e)
        }
    }

    private suspend fun uploadImageToS3(uploadUrl: String, fileUri: Uri, contentType: String): Result<Unit> {
        return try {
            Log.d(TAG, "uploadImageToS3: Starting upload to $uploadUrl")
            
            val inputStream = context.contentResolver.openInputStream(fileUri)
            if (inputStream == null) {
                Log.e(TAG, "uploadImageToS3: Cannot open input stream")
                return Result.failure(Exception("파일을 열 수 없습니다"))
            }
            
            val tempFile = File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}")
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            val requestBody = tempFile.asRequestBody(contentType.toMediaTypeOrNull())
            
            val response = uploadApi.uploadFile(
                url = uploadUrl,
                contentType = contentType,
                file = requestBody
            )

            // 임시 파일 삭제
            tempFile.delete()

            if (response.isSuccessful) {
                Log.d(TAG, "uploadImageToS3: Upload successful")
                Result.success(Unit)
            } else {
                Log.e(TAG, "uploadImageToS3: Upload failed: ${response.code()}")
                Result.failure(Exception("S3 업로드 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadImageToS3: Exception", e)
            Result.failure(e)
        }
    }
}
