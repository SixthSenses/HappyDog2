package com.example.pet_project_frontend.presentation.petcare.history

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import com.example.pet_project_frontend.domain.repository.PetCareRepository
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class CareHistoryPagingSource(
    private val repo: PetCareRepository,
    private val petId: String,
    private val type: String?
) : PagingSource<String, CareRecordResponse>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, CareRecordResponse> {
        val cursor = params.key
        return when (val res = repo.getCareRecords(
            petId = petId,
            date = null,
            startDate = null,
            endDate = null,
            recordTypes = type?.let { listOf(it) },
            grouped = false,
            limit = params.loadSize,
            cursor = cursor,
            sort = "timestamp_desc"
        )) {
            is AppResult.Success -> {
                val data = res.data.records
                val next = res.data.meta.nextCursor
                LoadResult.Page(data = data, prevKey = null, nextKey = next)
            }
            is AppResult.Error -> LoadResult.Error(Exception(res.message))
            is AppResult.Exception -> LoadResult.Error(res.throwable)
        }
    }

    override fun getRefreshKey(state: PagingState<String, CareRecordResponse>): String? = null
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repo: PetCareRepository
) : ViewModel() {
    fun pager(petId: String, type: String?) = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 20),
        pagingSourceFactory = { CareHistoryPagingSource(repo, petId, type) }
    ).flow.cachedIn(viewModelScope)
}
