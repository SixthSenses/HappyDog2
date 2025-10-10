// 변경 의도: hasPet 초기 null 상태를 외부로 전달해 첫 화면 라우팅 튐 현상을 방지
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
    
    // �체 �증
    suspend fun registerNosePrint(petId: String, filePath: String): AppResult<BiometricAnalysisResponse>
    suspend fun analyzeEye(petId: String, filePath: String): AppResult<EyeAnalysisResponse>

    // �일 ��책�기반�보조 API: 존재 �인
    fun hasPet(): Flow<Boolean?>
}
