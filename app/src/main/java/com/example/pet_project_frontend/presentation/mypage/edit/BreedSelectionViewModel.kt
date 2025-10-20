package com.example.pet_project_frontend.presentation.mypage.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.model.Breed
import com.example.pet_project_frontend.domain.repository.BreedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BreedSelectionViewModel @Inject constructor(
    private val breedRepository: BreedRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BreedSearchUiState())
    val uiState: StateFlow<BreedSearchUiState> = _uiState.asStateFlow()
    
    fun loadAllBreeds() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = breedRepository.getAllBreeds()) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            breeds = result.data,
                            isLoading = false
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "품종 목록을 불러오는데 실패했습니다"
                        )
                    }
                }
                is AppResult.Exception -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "품종 목록을 불러오는 중 오류가 발생했습니다"
                        )
                    }
                }
            }
        }
    }
    
    fun searchBreeds(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = breedRepository.searchBreeds(query)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            breeds = result.data,
                            isLoading = false
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "품종 검색에 실패했습니다"
                        )
                    }
                }
                is AppResult.Exception -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "품종 검색 중 오류가 발생했습니다"
                        )
                    }
                }
            }
        }
    }
}

data class BreedSearchUiState(
    val breeds: List<Breed> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
