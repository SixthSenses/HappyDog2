package com.example.pet_project_frontend.presentation.breed

import com.example.pet_project_frontend.data.remote.dto.response.BreedsResponse
import com.example.pet_project_frontend.data.remote.dto.response.BreedResponse

sealed class BreedSearchState {
    object Idle : BreedSearchState()
    object Loading : BreedSearchState()
    data class Success(val response: BreedsResponse) : BreedSearchState()
    data class Error(val message: String) : BreedSearchState()
}

data class BreedSearchUiState(
    val breeds: List<BreedResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
