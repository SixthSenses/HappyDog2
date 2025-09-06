package com.example.pet_project_frontend.presentation.petcare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordsResponse
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import com.example.pet_project_frontend.data.remote.dto.response.PetCareSettings
import com.example.pet_project_frontend.data.remote.dto.request.CareRecordUpdateRequest
import com.example.pet_project_frontend.domain.repository.PetCareRepository
import com.example.pet_project_frontend.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import com.example.pet_project_frontend.data.local.preferences.TokenManager
import com.example.pet_project_frontend.domain.model.Pet
import java.util.UUID
import com.example.pet_project_frontend.core.utils.DateFormatter
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import com.example.pet_project_frontend.core.remoteconfig.CardType
import com.example.pet_project_frontend.core.remoteconfig.FeatureToggles

@HiltViewModel
class PetCareViewModel @Inject constructor(
    private val petCareRepository: PetCareRepository,
    private val petRepository: PetRepository,
    private val tokenManager: TokenManager,
    private val featureToggles: FeatureToggles
) : ViewModel() {

    private val _careRecordsState = MutableStateFlow<CareRecordsState>(CareRecordsState.Loading)
    val careRecordsState: StateFlow<CareRecordsState> = _careRecordsState.asStateFlow()
    
    private val _createRecordState = MutableStateFlow<CreateRecordState>(CreateRecordState.Idle)
    val createRecordState: StateFlow<CreateRecordState> = _createRecordState.asStateFlow()

    private val _deleteRecordState = MutableStateFlow<DeleteRecordState>(DeleteRecordState.Idle)
    val deleteRecordState: StateFlow<DeleteRecordState> = _deleteRecordState.asStateFlow()

    private val _settingsState = MutableStateFlow<PetCareSettingsState>(PetCareSettingsState.Loading)
    val settingsState: StateFlow<PetCareSettingsState> = _settingsState.asStateFlow()

    // 페이징/메타 상태
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    private val _loadMoreError = MutableStateFlow<String?>(null)
    val loadMoreError: StateFlow<String?> = _loadMoreError.asStateFlow()

    // 타입별 개별 조회 상태(카드 단위 재시도용)
    private val _typeStates = MutableStateFlow<Map<String, TypeState>>(emptyMap())
    val typeStates: StateFlow<Map<String, TypeState>> = _typeStates.asStateFlow()

    // 마지막 조회 파라미터 저장(append 시 재사용)
    private var lastQuery: QueryParams? = null

    // 현재 사용할 petId (딥링크/인자 우선, 없으면 DataStore의 selected_pet_id)
    private val _activePetId = MutableStateFlow<String?>(null)
    val activePetId: StateFlow<String?> = _activePetId.asStateFlow()

    // 프로필 헤더용 펫 프로필 상태
    private val _petProfileState = MutableStateFlow<PetProfileState>(PetProfileState.Loading)
    val petProfileState: StateFlow<PetProfileState> = _petProfileState.asStateFlow()

    // UI 단발 이벤트 (Snackbar 등)
    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 4, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<UiEvent> = _events

    // Remote Config 기반 카드 순서/노출 State
    private val _cardOrder = MutableStateFlow<List<CardType>>(featureToggles.visibleCards())
    val cardOrder: StateFlow<List<CardType>> = _cardOrder.asStateFlow()

    fun setActivePetId(petIdOrNull: String?) {
        viewModelScope.launch {
            _activePetId.value = petIdOrNull ?: tokenManager.getSelectedPetId()
        }
    }

    // 선택 펫 변경을 관찰하여 자동으로 대시보드 데이터를 새로고침
    fun observeSelectedPetAndRefresh(defaultLimit: Int = 10, defaultSort: String = "timestamp_desc") {
        viewModelScope.launch {
            tokenManager.getSelectedPetIdFlow().collectLatest { currentId ->
                _activePetId.value = currentId
                if (currentId.isNullOrBlank()) {
                    _careRecordsState.value = CareRecordsState.Error("선택된 반려동물이 없습니다. 반려견을 등록하거나 선택해주세요.")
                    return@collectLatest
                }
                // 기본 파라미터로 최신 데이터 조회
                getCareRecords(
                    petId = currentId,
                    limit = defaultLimit,
                    sort = defaultSort
                )
            }
        }
    }

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
            lastQuery = QueryParams(
                petId = petId,
                date = date,
                startDate = startDate,
                endDate = endDate,
                recordTypes = recordTypes,
                grouped = grouped,
                limit = limit,
                sort = sort
            )

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

    fun appendCareRecords() {
        val current = _careRecordsState.value as? CareRecordsState.Success ?: return
        val params = lastQuery ?: return
        val nextCursor = current.response.meta.nextCursor ?: return
        if (_isLoadingMore.value) return
        _isLoadingMore.value = true
        _loadMoreError.value = null

        viewModelScope.launch {
            when (val res = petCareRepository.getCareRecords(
                petId = params.petId,
                date = params.date,
                startDate = params.startDate,
                endDate = params.endDate,
                recordTypes = params.recordTypes,
                grouped = params.grouped,
                limit = params.limit,
                cursor = nextCursor,
                sort = params.sort
            )) {
                is AppResult.Success -> {
                    val merged = mergeResponses(current.response, res.data)
                    _careRecordsState.value = CareRecordsState.Success(merged)
                    _isLoadingMore.value = false
                }
                is AppResult.Error -> {
                    _loadMoreError.value = res.message ?: "더보기 실패"
                    _isLoadingMore.value = false
                }
                is AppResult.Exception -> {
                    _loadMoreError.value = res.throwable.message ?: "더보기 예외"
                    _isLoadingMore.value = false
                }
            }
        }
    }

    fun refreshType(petId: String, type: String) {
        val params = lastQuery
        val date = params?.date
        val startDate = params?.startDate
        val endDate = params?.endDate
        _typeStates.value = _typeStates.value.toMutableMap().also { it[type] = TypeState.Loading }

        viewModelScope.launch {
            when (val res = petCareRepository.getRecordsByType(
                petId = petId,
                recordType = type,
                date = date,
                startDate = startDate,
                endDate = endDate,
                limit = params?.limit ?: 50,
                cursor = null
            )) {
                is AppResult.Success -> {
                    _typeStates.value = _typeStates.value.toMutableMap().also {
                        it[type] = TypeState.Success(res.data.records)
                    }
                }
                is AppResult.Error -> {
                    _typeStates.value = _typeStates.value.toMutableMap().also {
                        it[type] = TypeState.Error(res.message ?: "조회 실패")
                    }
                }
                is AppResult.Exception -> {
                    _typeStates.value = _typeStates.value.toMutableMap().also {
                        it[type] = TypeState.Error(res.throwable.message ?: "예외 발생")
                    }
                }
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
            val rid = requestId ?: UUID.randomUUID().toString()

            // 낙관적 업데이트: 현재 화면 데이터에 임시 레코드 추가
            val before = _careRecordsState.value
            val optimisticRecord = CareRecordResponse(
                logId = "local-$rid",
                petId = petId,
                recordType = recordType,
                timestamp = timestamp,
                data = data,
                notes = notes,
                searchDate = DateFormatter.todayUtcDate()
            )
            if (before is CareRecordsState.Success) {
                val base = before.response
                val newGrouped = base.grouped?.toMutableMap() ?: mutableMapOf()
                val updatedList = (newGrouped[recordType]?.toMutableList() ?: mutableListOf()).apply { add(0, optimisticRecord) }
                newGrouped[recordType] = updatedList
                val newRecords = (listOf(optimisticRecord) + base.records)
                _careRecordsState.value = CareRecordsState.Success(
                    base.copy(
                        records = newRecords,
                        grouped = newGrouped
                    )
                )
            }

            when (val res = petCareRepository.createCareRecord(
                petId = petId,
                recordType = recordType,
                timestamp = timestamp,
                data = data,
                notes = notes,
                requestId = rid
            )) {
                is AppResult.Success -> {
                    _createRecordState.value = CreateRecordState.Success(res.data)
                    getCareRecords(petId)
                    _events.tryEmit(UiEvent.Snack("기록이 추가되었습니다"))
                }
                is AppResult.Error -> {
                    _createRecordState.value = CreateRecordState.Error(res.message ?: "생성 실패")
                    // 실패 시 서버 데이터로 롤백
                    getCareRecords(petId)
                    _events.tryEmit(UiEvent.Snack(res.message ?: "기록 생성 실패"))
                }
                is AppResult.Exception -> {
                    _createRecordState.value = CreateRecordState.Error(res.throwable.message ?: "예외 발생")
                    // 실패 시 서버 데이터로 롤백
                    getCareRecords(petId)
                    _events.tryEmit(UiEvent.Snack("기록 생성 중 예외가 발생했습니다"))
                }
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
                is AppResult.Success -> {
                    _settingsState.value = PetCareSettingsState.Success(res.data)
                    _events.tryEmit(UiEvent.Snack("설정이 저장되었습니다"))
                }
                is AppResult.Error -> _settingsState.value = PetCareSettingsState.Error(res.message ?: "설정 저장 실패")
                is AppResult.Exception -> _settingsState.value = PetCareSettingsState.Error(res.throwable.message ?: "예외 발생")
            }
        }
    }

    fun updateCareRecord(petId: String, logId: String, update: CareRecordUpdateRequest) {
        viewModelScope.launch {
            // 재사용: 생성과 동일한 로딩/완료 UX는 화면에서 처리
            when (val res = petCareRepository.updateCareRecord(petId, logId, update)) {
                is AppResult.Success -> {
                    getCareRecords(petId)
                    _events.tryEmit(UiEvent.Snack("기록이 수정되었습니다"))
                }
                is AppResult.Error -> {
                    _events.tryEmit(UiEvent.Snack(res.message ?: "수정 실패"))
                }
                is AppResult.Exception -> {
                    _events.tryEmit(UiEvent.Snack("수정 중 예외가 발생했습니다"))
                }
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
                    _events.tryEmit(UiEvent.Snack("기록이 삭제되었습니다"))
                }
                is AppResult.Error -> {
                    _deleteRecordState.value = DeleteRecordState.Error(res.message ?: "삭제 실패")
                    _events.tryEmit(UiEvent.Snack(res.message ?: "삭제 실패"))
                }
                is AppResult.Exception -> {
                    _deleteRecordState.value = DeleteRecordState.Error(res.throwable.message ?: "예외 발생")
                    _events.tryEmit(UiEvent.Snack("삭제 중 예외가 발생했습니다"))
                }
            }
        }
    }

    fun refreshToggles() {
        _cardOrder.value = featureToggles.visibleCards()
    }

    fun loadPetProfile(petId: String) {
        viewModelScope.launch {
            _petProfileState.value = PetProfileState.Loading
            when (val res = petRepository.getPetProfile(petId)) {
                is AppResult.Success -> _petProfileState.value = PetProfileState.Success(res.data)
                is AppResult.Error -> _petProfileState.value = PetProfileState.Error(res.message ?: "프로필 조회 실패")
                is AppResult.Exception -> _petProfileState.value = PetProfileState.Error(res.throwable.message ?: "프로필 조회 예외")
            }
        }
    }

    // Remote Config 기반 카드 순서/노출 제어를 ViewModel로 노출
    fun visibleCards(): List<CardType> = featureToggles.visibleCards()
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

// 타입별 카드 상태
sealed class TypeState {
    object Loading : TypeState()
    data class Success(val records: List<CareRecordResponse>) : TypeState()
    data class Error(val message: String) : TypeState()
}

sealed class PetProfileState {
    object Loading : PetProfileState()
    data class Success(val pet: Pet) : PetProfileState()
    data class Error(val message: String) : PetProfileState()
}

sealed class UiEvent {
    data class Snack(val message: String) : UiEvent()
}

data class QueryParams(
    val petId: String,
    val date: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val recordTypes: List<String>? = null,
    val grouped: Boolean = false,
    val limit: Int = 10,
    val sort: String = "timestamp_desc"
)

private fun mergeResponses(base: CareRecordsResponse, next: CareRecordsResponse): CareRecordsResponse {
    val mergedGrouped = if (base.grouped != null || next.grouped != null) {
        val map = mutableMapOf<String, MutableList<CareRecordResponse>>()
        base.grouped?.forEach { (k, v) -> map.getOrPut(k) { mutableListOf() }.addAll(v) }
        next.grouped?.forEach { (k, v) -> map.getOrPut(k) { mutableListOf() }.addAll(v) }
        // dedupe by logId and sort by timestamp desc (default)
        map.mapValues { (_, list) ->
            list.distinctBy { it.logId }.sortedByDescending { it.timestamp }
        }
    } else null

    val mergedRecords = (base.records + next.records)
        .distinctBy { it.logId }
        .sortedByDescending { it.timestamp }

    return CareRecordsResponse(
        records = mergedRecords,
        grouped = mergedGrouped,
        meta = next.meta // carry over latest meta (has_more/next_cursor)
    )
}
