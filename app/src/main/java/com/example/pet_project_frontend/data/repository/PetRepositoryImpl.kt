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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepositoryImpl @Inject constructor(
    private val petApi: PetApi
) : PetRepository {

    companion object {
        private const val TAG = "PetRepositoryImpl"
    }

    // StateFlow로 반려동물 보유 상태 관리
    // null = 아직 확인 안됨, true = 보유, false = 미보유
    private val _hasPet = MutableStateFlow<Boolean?>(null)
    
    // 서버에서 반려동물 상태를 확인하고 StateFlow 업데이트
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
                        // 🔥 핵심: 반려동물 등록 성공 시 즉시 상태 업데이트
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
        // OpenAPI: /api/pets/profile returns PetViewBasedResponse (요약 뷰)
        // 도메인 Pet에는 상세 필드가 필요하므로 petId로 상세 조회를 한 번 더 수행합니다.
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

    override suspend fun updatePetProfile(petId: String, request: PetUpdateRequest): AppResult<Pet> {
        Log.d(TAG, "Updating pet profile: $petId, request: $request")
        return SafeApi.response { petApi.updatePetProfile(petId, request) }
            .let { res ->
                when (res) {
                    is AppResult.Success -> {
                        Log.d(TAG, "Pet profile updated successfully: ${res.data.petId}")
                        AppResult.Success(PetMapper.mapToDomainModel(res.data))
                    }
                    is AppResult.Error -> {
                        Log.e(TAG, "Pet profile update failed: code=${res.code}, message=${res.message}, validation=${res.validation}")
                        res
                    }
                    is AppResult.Exception -> {
                        Log.e(TAG, "Pet profile update exception: ${res.throwable.message}", res.throwable)
                        res
                    }
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

    override fun hasPet(): kotlinx.coroutines.flow.Flow<Boolean> =
        kotlinx.coroutines.flow.flow {
            // 🔥 핵심: 캐시된 상태가 있으면 먼저 emit
            _hasPet.value?.let { 
                Log.d(TAG, "Emitting cached hasPet status: $it")
                emit(it) 
            }
            
            // 아직 확인하지 않았으면 서버에서 확인
            if (_hasPet.value == null) {
                Log.d(TAG, "Pet status not cached, checking server")
                refreshPetStatus()
                _hasPet.value?.let { 
                    Log.d(TAG, "Emitting refreshed hasPet status: $it")
                    emit(it) 
                }
            }
            
            // 🔥 핵심: StateFlow의 변화를 계속 감지하여 실시간 업데이트
            _hasPet.asStateFlow().collect { hasPet ->
                if (hasPet != null) {
                    Log.d(TAG, "StateFlow changed, emitting new hasPet status: $hasPet")
                    emit(hasPet)
                }
            }
        }
}
