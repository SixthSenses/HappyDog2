package com.example.pet_project_frontend.presentation.breed_guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import com.example.pet_project_frontend.domain.model.BreedGuideLocal
import com.example.pet_project_frontend.domain.model.BreedGuideData
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 견종 가이드 ViewModel
 * 로컬 데이터로 26개 품종 목록 관리
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class BreedGuideViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow<BreedGuideUiState>(BreedGuideUiState.Loading)
    val uiState: StateFlow<BreedGuideUiState> = _uiState.asStateFlow()
    
    // 검색어 상태
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 모든 견종 데이터 (필터링용) - 로컬 데이터 사용
    private val _allBreeds = MutableStateFlow<List<BreedGuideLocal>>(emptyList())
    
    init {
        loadBreeds()
        
        // 검색어 변경 감지 및 필터링
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // 300ms 딜레이
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isEmpty()) {
                        // 검색어가 비어있으면 모든 가이드북 품종 표시
                        _uiState.value = BreedGuideUiState.Success(_allBreeds.value)
                    } else {
                        // 검색어가 있으면 필터링
                        val filteredBreeds = _allBreeds.value.filter { breed ->
                            breed.breedName.contains(query, ignoreCase = true)
                        }
                        _uiState.value = BreedGuideUiState.Success(filteredBreeds)
                    }
                }
        }
    }
    
    fun loadBreeds() {
        viewModelScope.launch {
            _uiState.value = BreedGuideUiState.Loading
            
            try {
                // 로컬 데이터에서 26개 품종 가져오기
                val breeds = BreedGuideData.guidebookBreeds
                
                println("BreedGuideViewModel: Loaded ${breeds.size} breeds from local data")
                
                _allBreeds.value = breeds
                _uiState.value = BreedGuideUiState.Success(breeds)
                println("BreedGuideViewModel: UI state updated successfully with ${breeds.size} breeds")
            } catch (e: Exception) {
                println("BreedGuideViewModel: Error loading breeds - ${e.message}")
                _uiState.value = BreedGuideUiState.Error(
                    "견종 정보를 불러오는 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 검색어 업데이트
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * 검색어 초기화
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }
}

/**
 * 견종 가이드 UI 상태
 */
sealed class BreedGuideUiState {
    object Loading : BreedGuideUiState()
    data class Success(val breeds: List<BreedGuideLocal>) : BreedGuideUiState()
    data class Error(val message: String) : BreedGuideUiState()
}