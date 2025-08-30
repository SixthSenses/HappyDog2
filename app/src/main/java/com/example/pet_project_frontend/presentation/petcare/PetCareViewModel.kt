package com.example.pet_project_frontend.presentation.petcare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordsResponse
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
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

    fun getCareRecords(
        petId: String,
        date: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        recordTypes: List<String>? = null,
        grouped: Boolean = false,
        limit: Int = 10,
        cursor: String? = null,
        sort: String = "desc"
    ) {
        viewModelScope.launch {
            _careRecordsState.value = CareRecordsState.Loading

            petCareRepository.getCareRecords(
                petId = petId,
                date = date,
                startDate = startDate,
                endDate = endDate,
                recordTypes = recordTypes,
                grouped = grouped,
                limit = limit,
                cursor = cursor,
                sort = sort
            )
                .onSuccess { response ->
                    _careRecordsState.value = CareRecordsState.Success(response)
                }
                .onFailure { exception ->
                    _careRecordsState.value = CareRecordsState.Error(
                        exception.message ?: "케어 기록을 불러오는데 실패했습니다."
                    )
                }
        }
    }

    fun createCareRecord(
        petId: String,
        recordType: String,
        timestamp: Long,
        data: Any,
        notes: String? = null
    ) {
        viewModelScope.launch {
            _createRecordState.value = CreateRecordState.Loading
            
            petCareRepository.createCareRecord(
                petId = petId,
                recordType = recordType,
                timestamp = timestamp,
                data = data,
                notes = notes
            )
                .onSuccess { record ->
                    _createRecordState.value = CreateRecordState.Success(record)
                    // 기록 생성 후 목록 새로고침
                    getCareRecords(petId)
                }
                .onFailure { exception ->
                    _createRecordState.value = CreateRecordState.Error(
                        exception.message ?: "케어 기록 생성에 실패했습니다."
                    )
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
