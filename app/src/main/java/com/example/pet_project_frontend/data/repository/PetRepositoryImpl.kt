// app/src/main/java/com/example/pet_project_frontend/data/repository/PetRepositoryImpl.kt

package com.example.pet_project_frontend.data.repository

import android.util.Log
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.core.common.SafeApi
import com.example.pet_project_frontend.data.mapper.PetMapper
import com.example.pet_project_frontend.data.remote.api.PetApi
import com.example.pet_project_frontend.data.remote.dto.request.*
import com.example.pet_project_frontend.data.remote.dto.response.*
import com.example.pet_project_frontend.domain.model.Pet
import com.example.pet_project_frontend.domain.repository.PetRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepositoryImpl @Inject constructor(
    private val petApi: PetApi
) : PetRepository {

    companion object {
        private const val TAG = "PetRepositoryImpl"
    }

    override suspend fun registerPet(request: PetRegistrationRequest): AppResult<Pet> {
        Log.d(TAG, "Registering pet: ${request.name}")
        return SafeApi.response { petApi.registerPet(request) }
            .let { res ->
                when (res) {
                    is AppResult.Success -> {
                        Log.d(TAG, "Pet registered successfully: ${res.data.petId}")
                        AppResult.Success(PetMapper.mapToDomainModel(res.data))
                    }
                    is AppResult.Error -> res
                    is AppResult.Exception -> res
                }
            }
    }

    override suspend fun getPetProfile(petId: String): AppResult<Pet> {
        Log.d(TAG, "Getting pet profile: $petId")
        return SafeApi.response { petApi.getPetProfile(petId) }
            .let { res ->
                when (res) {
                    is AppResult.Success -> {
                        Log.d(TAG, "Pet profile fetched successfully: ${res.data.petId}")
                        AppResult.Success(PetMapper.mapToDomainModel(res.data))
                    }
                    is AppResult.Error -> res
                    is AppResult.Exception -> res
                }
            }
    }

    override suspend fun updatePetProfile(petId: String, request: PetUpdateRequest): AppResult<Pet> {
        Log.d(TAG, "Updating pet profile: $petId")
        return SafeApi.response { petApi.updatePetProfile(petId, request) }
            .let { res ->
                when (res) {
                    is AppResult.Success -> {
                        Log.d(TAG, "Pet profile updated successfully: ${res.data.petId}")
                        AppResult.Success(PetMapper.mapToDomainModel(res.data))
                    }
                    is AppResult.Error -> res
                    is AppResult.Exception -> res
                }
            }
    }

    override suspend fun registerNosePrint(petId: String, filePath: String): AppResult<BiometricAnalysisResponse> {
        Log.d(TAG, "Registering nose print for pet: $petId")
        val request = BiometricAnalysisRequest(filePath = filePath)
        return SafeApi.response { petApi.registerNosePrint(petId, request) }
    }

    override suspend fun analyzeEye(petId: String, filePath: String): AppResult<EyeAnalysisResponse> {
        Log.d(TAG, "Analyzing eye for pet: $petId")
        val request = BiometricAnalysisRequest(filePath = filePath)
        return SafeApi.response { petApi.analyzeEye(petId, request) }
    }
}
