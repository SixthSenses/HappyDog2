package com.example.pet_project_frontend.presentation.mypage.profile.breed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.UpdatePetRequest
import com.example.pet_project_frontend.domain.repository.PetRepository
import com.example.pet_project_frontend.domain.usecase.breed.SearchBreedsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BreedSelectViewModel @Inject constructor(
    private val searchBreedsUseCase: SearchBreedsUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val petRepository: PetRepository
) : ViewModel() {

    data class UiState(
        val query: String = "",
        val breeds: List<String> = emptyList(),
        val selectedBreedName: String? = null,
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private var searchJob: Job? = null

    private var petId: String? = savedStateHandle
        .get<String>("petId")
        ?.takeUnless { it.isNullOrBlank() || it == "null" }

    init {
        val initialBreed = savedStateHandle.get<String>("initialBreed").orEmpty()
        _ui.update { it.copy(selectedBreedName = initialBreed.takeIf { it.isNotBlank() }) }
        fetchBreeds("")
    }

    fun initializePetId(id: String?) {
        if (!id.isNullOrBlank() && id != "null" && petId == null) {
            petId = id
            savedStateHandle["petId"] = id
        }
    }

    fun onQueryChange(value: String) {
        _ui.update { it.copy(query = value) }
        fetchBreeds(value)
    }

    fun clearQuery() {
        if (_ui.value.query.isEmpty()) return
        _ui.update { it.copy(query = "") }
        fetchBreeds("")
    }

    fun onBreedSelected(name: String) {
        _ui.update { it.copy(selectedBreedName = name) }
    }

    fun confirmSelection(onSuccess: (String, Boolean) -> Unit) {
        val selected = _ui.value.selectedBreedName ?: return
        if (_ui.value.isSaving) return
        _ui.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val targetPetId = petId ?: run {
                val profile = when (val result = petRepository.getMyPetProfile()) {
                    is AppResult.Success -> result.data
                    is AppResult.Error -> {
                        val message = result.message ?: result.validation?.generalMessage ?: GENERIC_SAVE_ERROR_MESSAGE
                        _ui.update { it.copy(isSaving = false, error = message) }
                        return@launch
                    }
                    is AppResult.Exception -> {
                        val message = result.throwable.message ?: GENERIC_SAVE_ERROR_MESSAGE
                        _ui.update { it.copy(isSaving = false, error = message) }
                        return@launch
                    }
                }

                petId = profile.id
                savedStateHandle["petId"] = profile.id
                profile.id
            }

            val request = UpdatePetRequest(
                breed = selected
            )

            when (val update = petRepository.updatePetProfile(targetPetId, request)) {
                is AppResult.Success -> {
                    val appliedBreed = update.data.breed.ifBlank { selected }
                    _ui.update {
                        it.copy(
                            isSaving = false,
                            selectedBreedName = appliedBreed,
                            error = null
                        )
                    }
                    onSuccess(appliedBreed, true)
                }
                is AppResult.Error -> {
                    val message = update.message ?: update.validation?.generalMessage ?: GENERIC_SAVE_ERROR_MESSAGE
                    _ui.update { it.copy(isSaving = false, error = message) }
                }
                is AppResult.Exception -> {
                    val message = update.throwable.message ?: GENERIC_SAVE_ERROR_MESSAGE
                    _ui.update { it.copy(isSaving = false, error = message) }
                }
            }
        }
    }

    private fun fetchBreeds(rawQuery: String) {
        val query = rawQuery.trim()
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            delay(200)
            searchBreedsUseCase(query)
                .catch {
                    _ui.update {
                        it.copy(
                            breeds = emptyList(),
                            isLoading = false,
                            error = "견종 정보를 불러오는 동안 오류가 발생했어요."
                        )
                    }
                }
                .collectLatest { responses ->
                    val names = responses.map { it.breedName }
                    _ui.update { state ->
                        val adjustedSelection = state.selectedBreedName?.takeIf { selected ->
                            names.any { it == selected }
                        }
                        state.copy(
                            breeds = names,
                            isLoading = false,
                            error = null,
                            selectedBreedName = adjustedSelection
                        )
                    }
                }
        }
    }

    companion object {
        private const val GENERIC_SAVE_ERROR_MESSAGE = "견종을 저장하는 중 문제가 발생했어요. 잠시 후 다시 시도해주세요."
    }
}
