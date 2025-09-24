package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.request.PetRegistrationRequest
import com.example.pet_project_frontend.data.remote.dto.response.PetProfileResponse
import com.example.pet_project_frontend.data.remote.dto.response.PetViewBasedResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface PetApi {
    // OpenAPI defines POST path as "/api/pets/" (with trailing slash)
    @POST("api/pets/")
    suspend fun registerPet(@Body createPetRequest: PetRegistrationRequest): Response<PetProfileResponse>
    
    @GET("api/pets/{petId}")
    suspend fun getPetProfile(@retrofit2.http.Path("petId") petId: String): Response<PetProfileResponse>

    // 사용자 본인의 단일 펫 프로필 조회 (백엔드 정책: 사용자 1명당 1펫)
    @GET("api/pets/profile")
    suspend fun getMyPetProfile(): Response<PetViewBasedResponse>
    
    // 펫케어 홈 화면용 프로필 조회 (view=petcare)
    @GET("api/pets/profile")
    suspend fun getPetProfileForPetCare(@retrofit2.http.Query("view") view: String = "petcare"): Response<PetViewBasedResponse>
    
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