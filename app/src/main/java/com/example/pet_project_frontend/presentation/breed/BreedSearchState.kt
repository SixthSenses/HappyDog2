package com.example.pet_project_frontend.presentation.breed

import com.example.pet_project_frontend.domain.model.Breed

sealed class BreedSearchState {
    object Idle : BreedSearchState()
    object Loading : BreedSearchState()
    data class Success(val breeds: List<Breed>) : BreedSearchState()
    data class Error(val message: String) : BreedSearchState()
}

data class BreedSearchUiState(
    val breeds: List<Breed> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
