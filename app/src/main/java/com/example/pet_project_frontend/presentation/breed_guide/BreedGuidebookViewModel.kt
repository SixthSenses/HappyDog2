package com.example.pet_project_frontend.presentation.breed_guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.data.remote.dto.response.BreedGuidebookResponse
import com.example.pet_project_frontend.domain.usecase.breed.GetBreedGuidebookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 품종 가이드북 상세 ViewModel
 */
@HiltViewModel
class BreedGuidebookViewModel @Inject constructor(
    private val getBreedGuidebookUseCase: GetBreedGuidebookUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<BreedGuidebookUiState>(BreedGuidebookUiState.Loading)
    val uiState: StateFlow<BreedGuidebookUiState> = _uiState.asStateFlow()
    
    fun loadGuidebook(breedName: String) {
        viewModelScope.launch {
            _uiState.value = BreedGuidebookUiState.Loading
            
            getBreedGuidebookUseCase(breedName)
                .onSuccess { guidebook ->
                    _uiState.value = BreedGuidebookUiState.Success(guidebook)
                }
                .onFailure { error ->
                    _uiState.value = BreedGuidebookUiState.Error(
                        error.message ?: "품종 가이드북을 불러오는 중 오류가 발생했습니다"
                    )
                }
        }
    }
}

/**
 * 품종 가이드북 UI 상태
 */
sealed class BreedGuidebookUiState {
    object Loading : BreedGuidebookUiState()
    data class Success(val guidebook: BreedGuidebookResponse) : BreedGuidebookUiState()
    data class Error(val message: String) : BreedGuidebookUiState()
}