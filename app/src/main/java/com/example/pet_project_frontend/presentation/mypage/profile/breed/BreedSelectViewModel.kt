// 변경의도: 초기 견종 값을 SavedStateHandle에서 읽고 저장 콜백이 MyPage 갱신 여부를 판단하도록 확장한다.
package com.example.pet_project_frontend.presentation.mypage.profile.breed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val savedStateHandle: SavedStateHandle
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

    init {
        val initialBreed = savedStateHandle.get<String>("initialBreed").orEmpty()
        _ui.update { it.copy(selectedBreedName = initialBreed.takeIf { it.isNotBlank() }) }
        fetchBreeds("")
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
        _ui.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            // TODO 서버 연동 시 치환: 견종 수정 API를 연동해 실제 응답으로 갱신
            onSuccess(selected, false)
            _ui.update { state -> state.copy(isSaving = false) }
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
                            error = "견종 정보를 불러오는 중 오류가 발생했어요"
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
}
