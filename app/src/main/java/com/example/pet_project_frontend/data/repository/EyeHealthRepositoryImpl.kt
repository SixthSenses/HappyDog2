package com.example.pet_project_frontend.data.repository

import android.content.Context
import android.net.Uri
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.core.common.SafeApi
import com.example.pet_project_frontend.data.mapper.EyeAnalysisMapper
import com.example.pet_project_frontend.data.mapper.EyeHealthMapper
import com.example.pet_project_frontend.data.remote.api.EyeHealthApi
import com.example.pet_project_frontend.data.remote.dto.BiometricAnalysisRequestDto
import com.example.pet_project_frontend.data.remote.upload.FileUploadManager
import com.example.pet_project_frontend.data.remote.upload.UploadType
import com.example.pet_project_frontend.domain.model.EyeAnalysis
import com.example.pet_project_frontend.domain.model.EyeAnalysisHistory
import com.example.pet_project_frontend.domain.repository.EyeHealthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 안구 건강 분석 Repository Implementation (Data Layer)
 * Domain Repository Contract 구현
 */
@Singleton
class EyeHealthRepositoryImpl @Inject constructor(
    private val eyeHealthApi: EyeHealthApi,
    private val fileUploadManager: FileUploadManager,
    @ApplicationContext private val context: Context
) : EyeHealthRepository {

    override suspend fun analyzeEyeHealth(petId: String, imageUri: Uri): Result<EyeAnalysis> {
        return try {
            // Step 1: Uri를 File로 변환
            val imageFile = uriToFile(imageUri) ?: return Result.failure(
                Exception("이미지 파일을 처리할 수 없습니다")
            )

            // Step 2: 파일 업로드 (FileUploadManager 사용)
            when (val uploadResult = fileUploadManager.uploadFile(imageFile, UploadType.EYE_ANALYSIS)) {
                is AppResult.Success -> {
                    val filePath = uploadResult.data

                    // Step 3: 안구 분석 요청
                    val analysisRequest = BiometricAnalysisRequestDto(filePath = filePath)
                    when (val analysisResult = SafeApi.response { 
                        eyeHealthApi.analyzeEyeHealth(petId, analysisRequest) 
                    }) {
                        is AppResult.Success -> {
                            val domainModel = with(EyeAnalysisMapper) { analysisResult.data.toDomain() }
                            Result.success(domainModel)
                        }
                        is AppResult.Error -> {
                            Result.failure(Exception("분석 실패: ${analysisResult.message}"))
                        }
                        is AppResult.Exception -> {
                            Result.failure(analysisResult.throwable)
                        }
                    }
                }
                is AppResult.Error -> {
                    Result.failure(Exception("파일 업로드 실패: ${uploadResult.message}"))
                }
                is AppResult.Exception -> {
                    Result.failure(uploadResult.throwable)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEyeAnalysisHistory(
        petId: String?,
        limit: Int,
        cursor: String?
    ): Result<EyeAnalysisHistory> {
        return try {
            when (val result = SafeApi.response { 
                eyeHealthApi.getEyeAnalysisHistory(petId, limit, cursor) 
            }) {
                is AppResult.Success -> {
                    val domainModel = with(EyeHealthMapper) { result.data.toDomain() }
                    Result.success(domainModel)
                }
                is AppResult.Error -> {
                    Result.failure(Exception("히스토리 조회 실패: ${result.message}"))
                }
                is AppResult.Exception -> {
                    Result.failure(result.throwable)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uri를 File로 변환하는 헬퍼 메서드
     */
    private suspend fun uriToFile(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val fileName = "eye_analysis_${System.currentTimeMillis()}.jpg"
            val cacheFile = File(context.cacheDir, fileName)
            
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }
            cacheFile
        } catch (e: Exception) {
            null
        }
    }
}