package com.example.pet_project_frontend.presentation.breed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.data.remote.dto.response.BreedsResponse
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

            breedRepository.searchBreeds(query)
                .onSuccess { breedsResponse ->
                    _uiState.value = BreedSearchUiState(
                        breeds = breedsResponse.breeds,
                        isLoading = false
                    )
                    _searchState.value = BreedSearchState.Success(breedsResponse)
                }
                .onFailure { exception ->
                    _uiState.value = BreedSearchUiState(
                        error = exception.message ?: "품종 검색에 실패했습니다.",
                        isLoading = false
                    )
                    _searchState.value = BreedSearchState.Error(exception.message ?: "품종 검색에 실패했습니다.")
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
        _searchState.value = BreedSearchState.Idle
    }
}
