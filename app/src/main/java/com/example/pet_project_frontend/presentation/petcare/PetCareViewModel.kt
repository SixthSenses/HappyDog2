package com.example.pet_project_frontend.presentation.petcare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordsResponse
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import com.example.pet_project_frontend.data.remote.dto.response.PetCareSettings
import com.example.pet_project_frontend.data.remote.dto.request.CareRecordUpdateRequest
import com.example.pet_project_frontend.domain.repository.PetCareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetCareViewModel @Inject constructor(
    private val petCareRepository: PetCareRepository
) : ViewModel() {

    private val _careRecordsState = MutableStateFlow<CareRecordsState>(CareRecordsState.Loading)
    val careRecordsState: StateFlow<CareRecordsState> = _careRecordsState.asStateFlow()
    
    private val _createRecordState = MutableStateFlow<CreateRecordState>(CreateRecordState.Idle)
    val createRecordState: StateFlow<CreateRecordState> = _createRecordState.asStateFlow()

    private val _deleteRecordState = MutableStateFlow<DeleteRecordState>(DeleteRecordState.Idle)
    val deleteRecordState: StateFlow<DeleteRecordState> = _deleteRecordState.asStateFlow()

    private val _settingsState = MutableStateFlow<PetCareSettingsState>(PetCareSettingsState.Loading)
    val settingsState: StateFlow<PetCareSettingsState> = _settingsState.asStateFlow()

    fun getCareRecords(
        petId: String,
        date: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        recordTypes: List<String>? = null,
        grouped: Boolean = false,
        limit: Int = 10,
        cursor: String? = null,
    sort: String = "timestamp_desc"
    ) {
        viewModelScope.launch {
            _careRecordsState.value = CareRecordsState.Loading

            when (val res = petCareRepository.getCareRecords(
                petId = petId,
                date = date,
                startDate = startDate,
                endDate = endDate,
                recordTypes = recordTypes,
                grouped = grouped,
                limit = limit,
                cursor = cursor,
                sort = sort
            )) {
                is AppResult.Success -> _careRecordsState.value = CareRecordsState.Success(res.data)
                is AppResult.Error -> _careRecordsState.value = CareRecordsState.Error(res.message ?: "오류가 발생했습니다")
                is AppResult.Exception -> _careRecordsState.value = CareRecordsState.Error(res.throwable.message ?: "예외가 발생했습니다")
            }
        }
    }

    fun createCareRecord(
        petId: String,
        recordType: String,
        timestamp: Long,
        data: Any,
        notes: String? = null,
        requestId: String? = null
    ) {
        viewModelScope.launch {
            _createRecordState.value = CreateRecordState.Loading
            
            when (val res = petCareRepository.createCareRecord(
                petId = petId,
                recordType = recordType,
                timestamp = timestamp,
                data = data,
                notes = notes,
                requestId = requestId
            )) {
                is AppResult.Success -> {
                    _createRecordState.value = CreateRecordState.Success(res.data)
                    getCareRecords(petId)
                }
                is AppResult.Error -> _createRecordState.value = CreateRecordState.Error(res.message ?: "생성 실패")
                is AppResult.Exception -> _createRecordState.value = CreateRecordState.Error(res.throwable.message ?: "예외 발생")
            }
        }
    }

    fun loadPetCareSettings() {
        viewModelScope.launch {
            _settingsState.value = PetCareSettingsState.Loading
            when (val res = petCareRepository.getPetCareSettings()) {
                is AppResult.Success -> _settingsState.value = PetCareSettingsState.Success(res.data)
                is AppResult.Error -> _settingsState.value = PetCareSettingsState.Error(res.message ?: "설정 조회 실패")
                is AppResult.Exception -> _settingsState.value = PetCareSettingsState.Error(res.throwable.message ?: "예외 발생")
            }
        }
    }

    fun updatePetCareSettings(settings: PetCareSettings) {
        viewModelScope.launch {
            _settingsState.value = PetCareSettingsState.Loading
            when (val res = petCareRepository.updatePetCareSettings(settings)) {
                is AppResult.Success -> _settingsState.value = PetCareSettingsState.Success(res.data)
                is AppResult.Error -> _settingsState.value = PetCareSettingsState.Error(res.message ?: "설정 저장 실패")
                is AppResult.Exception -> _settingsState.value = PetCareSettingsState.Error(res.throwable.message ?: "예외 발생")
            }
        }
    }

    fun updateCareRecord(petId: String, logId: String, update: CareRecordUpdateRequest) {
        viewModelScope.launch {
            // 재사용: 생성과 동일한 로딩/완료 UX는 화면에서 처리
            when (val res = petCareRepository.updateCareRecord(petId, logId, update)) {
                is AppResult.Success -> getCareRecords(petId)
                is AppResult.Error -> { /* TODO: expose error */ }
                is AppResult.Exception -> { /* TODO: expose error */ }
            }
        }
    }

    fun deleteCareRecord(petId: String, logId: String) {
        viewModelScope.launch {
            _deleteRecordState.value = DeleteRecordState.Loading
            when (val res = petCareRepository.deleteCareRecord(petId, logId)) {
                is AppResult.Success -> {
                    _deleteRecordState.value = DeleteRecordState.Success
                    getCareRecords(petId)
                }
                is AppResult.Error -> _deleteRecordState.value = DeleteRecordState.Error(res.message ?: "삭제 실패")
                is AppResult.Exception -> _deleteRecordState.value = DeleteRecordState.Error(res.throwable.message ?: "예외 발생")
            }
        }
    }
}

sealed class CareRecordsState {
    object Loading : CareRecordsState()
    data class Success(val response: CareRecordsResponse) : CareRecordsState()
    data class Error(val message: String) : CareRecordsState()
}

sealed class CreateRecordState {
    object Idle : CreateRecordState()
    object Loading : CreateRecordState()
    data class Success(val record: CareRecordResponse) : CreateRecordState()
    data class Error(val message: String) : CreateRecordState()
}

sealed class PetCareSettingsState {
    object Loading : PetCareSettingsState()
    data class Success(val settings: PetCareSettings) : PetCareSettingsState()
    data class Error(val message: String) : PetCareSettingsState()
}

sealed class DeleteRecordState {
    object Idle : DeleteRecordState()
    object Loading : DeleteRecordState()
    object Success : DeleteRecordState()
    data class Error(val message: String) : DeleteRecordState()
}
