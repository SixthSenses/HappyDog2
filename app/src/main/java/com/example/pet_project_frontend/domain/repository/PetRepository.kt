package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.PetRegistrationRequest
import com.example.pet_project_frontend.data.remote.dto.request.PetUpdateRequest
import com.example.pet_project_frontend.data.remote.dto.response.BiometricAnalysisResponse
import com.example.pet_project_frontend.data.remote.dto.response.EyeAnalysisResponse
import com.example.pet_project_frontend.domain.model.Pet
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    suspend fun registerPet(request: PetRegistrationRequest): AppResult<Pet>
    suspend fun getPetProfile(petId: String): AppResult<Pet>
    suspend fun getMyPetProfile(): AppResult<Pet>
    suspend fun updatePetProfile(petId: String, request: PetUpdateRequest): AppResult<Pet>
    
    // 생체 인증
    suspend fun registerNosePrint(petId: String, filePath: String): AppResult<BiometricAnalysisResponse>
    suspend fun analyzeEye(petId: String, filePath: String): AppResult<EyeAnalysisResponse>

    // 단일 펫 정책에 기반한 보조 API: 존재 여부 확인
    fun hasPet(): kotlinx.coroutines.flow.Flow<Boolean>
}