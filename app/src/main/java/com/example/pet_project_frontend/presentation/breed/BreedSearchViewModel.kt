package com.example.pet_project_frontend.presentation.breed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.model.Breed
import com.example.pet_project_frontend.domain.repository.BreedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BreedSearchViewModel @Inject constructor(
    private val breedRepository: BreedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BreedSearchUiState())
    val uiState: StateFlow<BreedSearchUiState> = _uiState.asStateFlow()
    
    private val _searchState = MutableStateFlow<BreedSearchState>(BreedSearchState.Idle)
    val searchState: StateFlow<BreedSearchState> = _searchState.asStateFlow()

    fun searchBreeds(query: String) {
        if (query.length < 2) {
            _uiState.value = BreedSearchUiState(breeds = emptyList())
            _searchState.value = BreedSearchState.Idle
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            _searchState.value = BreedSearchState.Loading

            when (val result = breedRepository.searchBreeds(query)) {
                is AppResult.Success -> {
                    _uiState.value = BreedSearchUiState(
                        breeds = result.data,
                        isLoading = false
                    )
                    _searchState.value = BreedSearchState.Success(result.data)
                }
                is AppResult.Error -> {
                    val errorMessage = "품종 검색에 실패했습니다: ${result.message}"
                    _uiState.value = BreedSearchUiState(
                        error = errorMessage,
                        isLoading = false
                    )
                    _searchState.value = BreedSearchState.Error(errorMessage)
                }
                is AppResult.Exception -> {
                    val errorMessage = result.throwable.message ?: "품종 검색에 실패했습니다."
                    _uiState.value = BreedSearchUiState(
                        error = errorMessage,
                        isLoading = false
                    )
                    _searchState.value = BreedSearchState.Error(errorMessage)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
        _searchState.value = BreedSearchState.Idle
    }
}
