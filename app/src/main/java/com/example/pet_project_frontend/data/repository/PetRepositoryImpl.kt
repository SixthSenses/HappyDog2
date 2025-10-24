// 변경 의도: hasPet 초기 null 상태를 유지해 첫 화면 라우팅 튐 현상을 방지

package com.example.pet_project_frontend.data.repository

import android.util.Log
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.core.common.SafeApi
import com.example.pet_project_frontend.data.mapper.PetMapper
import com.example.pet_project_frontend.data.remote.api.PetApi
import com.example.pet_project_frontend.data.remote.dto.request.BiometricAnalysisRequest
import com.example.pet_project_frontend.data.remote.dto.request.PetRegistrationRequest
import com.example.pet_project_frontend.data.remote.dto.request.PetUpdateRequest
import com.example.pet_project_frontend.data.remote.dto.response.BiometricAnalysisResponse
import com.example.pet_project_frontend.data.remote.dto.response.EyeAnalysisResponse
import com.example.pet_project_frontend.domain.model.Pet
import com.example.pet_project_frontend.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepositoryImpl @Inject constructor(
    private val petApi: PetApi
) : PetRepository {

    companion object {
        private const val TAG = "PetRepositoryImpl"
    }

    // StateFlow는 반려동물 보유 여부를 캐싱하기 위한 컨테이너
    // null = 미확인, true = 보유, false = 미보유
    private val _hasPet = MutableStateFlow<Boolean?>(null)

    // 서버에서 반려동물 상태를 확인하고 캐시를 갱신
    private suspend fun refreshPetStatus() {
        Log.d(TAG, "Refreshing pet status from server")
        when (val res = SafeApi.response { petApi.getMyPetProfile() }) {
            is AppResult.Success -> {
                Log.d(TAG, "Pet found, setting hasPet = true")
                _hasPet.value = true
            }
            is AppResult.Error -> {
                Log.d(TAG, "Pet not found (${res.code}), setting hasPet = false")
                _hasPet.value = false
            }
            is AppResult.Exception -> {
                Log.e(TAG, "Exception checking pet status", res.throwable)
                _hasPet.value = false
            }
        }
    }

    override suspend fun registerPet(request: PetRegistrationRequest): AppResult<Pet> {
        Log.d(TAG, "Registering pet: ${request.name}")
        return SafeApi.response { petApi.registerPet(request) }
            .let { res ->
                when (res) {
                    is AppResult.Success -> {
                        Log.d(TAG, "Pet registered successfully: ${res.data.petId}")
                        // 등록 성공 즉시 상태 캐시 갱신
                        _hasPet.value = true
                        Log.d(TAG, "Updated hasPet state to true after registration")
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

    override suspend fun getMyPetProfile(): AppResult<Pet> {
        Log.d(TAG, "Getting my pet profile (single-pet policy)")
        return when (val viewRes = SafeApi.response { petApi.getMyPetProfile() }) {
            is AppResult.Success -> {
                val petId = viewRes.data.petId
                when (val profRes = SafeApi.response { petApi.getPetProfile(petId) }) {
                    is AppResult.Success -> {
                        Log.d(TAG, "My pet profile fetched successfully: ${profRes.data.petId}")
                        AppResult.Success(PetMapper.mapToDomainModel(profRes.data))
                    }
                    is AppResult.Error -> profRes
                    is AppResult.Exception -> profRes
                }
            }
            is AppResult.Error -> viewRes
            is AppResult.Exception -> viewRes
        }
    }

    override suspend fun getPetProfileForPetCare(): AppResult<PetViewBasedResponse> {
        Log.d(TAG, "Getting pet profile for petcare view")
        return SafeApi.response { petApi.getPetProfileForPetCare() }
            .let { res ->
                when (res) {
                    is AppResult.Success -> {
                        Log.d(TAG, "PetCare profile fetched successfully: ${res.data.petId}")
                        res
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

    override fun hasPet(): Flow<Boolean?> =
        _hasPet
            .asStateFlow()
            .onStart {
                if (_hasPet.value == null) {
                    Log.d(TAG, "Pet status not cached, checking server")
                    refreshPetStatus()
                }
            }
}
