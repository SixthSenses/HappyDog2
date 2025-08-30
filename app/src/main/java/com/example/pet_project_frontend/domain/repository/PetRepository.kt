package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.data.remote.dto.request.PetRegistrationRequest
import com.example.pet_project_frontend.data.remote.dto.request.PetUpdateRequest
import com.example.pet_project_frontend.data.remote.dto.response.BiometricAnalysisResponse
import com.example.pet_project_frontend.data.remote.dto.response.EyeAnalysisResponse
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.domain.model.Pet
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    suspend fun registerPet(request: PetRegistrationRequest): NetworkResult<Pet>
    suspend fun getPetProfile(petId: String): NetworkResult<Pet>
    suspend fun updatePetProfile(petId: String, request: PetUpdateRequest): NetworkResult<Pet>
    
    // 생체 인증
    suspend fun registerNosePrint(petId: String, filePath: String): NetworkResult<BiometricAnalysisResponse>
    suspend fun analyzeEye(petId: String, filePath: String): NetworkResult<EyeAnalysisResponse>
}