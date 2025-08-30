package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.request.PetRegistrationRequest
import com.example.pet_project_frontend.data.remote.dto.response.PetProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface PetApi {
    @POST("api/pets")
    suspend fun registerPet(@Body createPetRequest: PetRegistrationRequest): Response<PetProfileResponse>
    
    @POST("api/pets/{petId}")
    suspend fun getPetProfile(@retrofit2.http.Path("petId") petId: String): Response<PetProfileResponse>
    
    @PUT("api/pets/{petId}")
    suspend fun updatePetProfile(
        @retrofit2.http.Path("petId") petId: String,
        @Body request: com.example.pet_project_frontend.data.remote.dto.request.PetUpdateRequest
    ): Response<PetProfileResponse>
    
    @POST("api/pets/{petId}/nose-print")
    suspend fun registerNosePrint(
        @retrofit2.http.Path("petId") petId: String,
        @Body request: com.example.pet_project_frontend.data.remote.dto.request.BiometricAnalysisRequest
    ): Response<com.example.pet_project_frontend.data.remote.dto.response.BiometricAnalysisResponse>
    
    @POST("api/pets/{petId}/eye-analysis")
    suspend fun analyzeEye(
        @retrofit2.http.Path("petId") petId: String,
        @Body request: com.example.pet_project_frontend.data.remote.dto.request.BiometricAnalysisRequest
    ): Response<com.example.pet_project_frontend.data.remote.dto.response.EyeAnalysisResponse>
}