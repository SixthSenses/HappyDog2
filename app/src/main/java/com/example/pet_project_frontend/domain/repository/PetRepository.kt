package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.PetRegistrationRequest
import com.example.pet_project_frontend.data.remote.dto.request.PetUpdateRequest
import com.example.pet_project_frontend.data.remote.dto.response.BiometricAnalysisResponse
import com.example.pet_project_frontend.data.remote.dto.response.EyeAnalysisResponse
import com.example.pet_project_frontend.data.remote.dto.response.PetViewBasedResponse
import com.example.pet_project_frontend.domain.model.Pet
import com.example.pet_project_frontend.domain.model.PetStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PetRepository {
    suspend fun registerPet(request: PetRegistrationRequest): AppResult<Pet>
    suspend fun getPetProfile(petId: String): AppResult<Pet>
    suspend fun getMyPetProfile(): AppResult<Pet>
    suspend fun getPetProfileForPetCare(): AppResult<PetViewBasedResponse>
    suspend fun updatePetProfile(petId: String, request: PetUpdateRequest): AppResult<Pet>
    
    // 생체 인증
    suspend fun registerNosePrint(petId: String, filePath: String): AppResult<BiometricAnalysisResponse>
    suspend fun analyzeEye(petId: String, filePath: String): AppResult<EyeAnalysisResponse>

    /**
     * 반려동물 존재 여부 상태 (Loading/HasPet/NoPet)
     * 앱 시작 시 자동으로 서버에서 상태를 확인합니다.
     */
    fun getPetStatus(): StateFlow<PetStatus>
    
    /**
     * 명시적으로 펫 상태를 새로고침 (로그인 후, 등록 후 등)
     */
    suspend fun refreshPetStatusManually()
    
    /**
     * @Deprecated 하위 호환성을 위해 유지. getPetStatus() 사용 권장
     */
    @Deprecated(
        message = "Use getPetStatus() instead",
        replaceWith = ReplaceWith("getPetStatus().map { it.hasPet }")
    )
    fun hasPet(): Flow<Boolean>
}