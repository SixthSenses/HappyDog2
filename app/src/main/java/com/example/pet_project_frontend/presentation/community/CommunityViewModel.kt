package com.example.pet_project_frontend.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.model.Post
import com.example.pet_project_frontend.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 커뮤니티(멍스타그램) 피드 화면 ViewModel
 * MVVM + UDF 패턴: 단일 불변 StateFlow<UiState> 노출
 */
@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CommunityUiState>(CommunityUiState.Loading)
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        loadPostsFeed()
    }

    fun loadPostsFeed() {
        viewModelScope.launch {
            _uiState.update { CommunityUiState.Loading }
            
            when (val result = communityRepository.getPostsFeed(limit = 20, cursor = null)) {
                is AppResult.Success -> {
                    val page = result.data
                    if (page.posts.isEmpty()) {
                        _uiState.update { CommunityUiState.Empty }
                    } else {
                        _uiState.update {
                            CommunityUiState.Success(
                                posts = page.posts,
                                nextCursor = page.nextCursor,
                                isLoadingMore = false
                            )
                        }
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        CommunityUiState.Error("게시글을 불러올 수 없습니다: ${result.message}")
                    }
                }
                is AppResult.Exception -> {
                    _uiState.update {
                        CommunityUiState.Error("네트워크 오류가 발생했습니다: ${result.throwable.message}")
                    }
                }
            }
        }
    }

    fun loadMorePosts() {
        val currentState = _uiState.value as? CommunityUiState.Success ?: return
        if (currentState.isLoadingMore || currentState.nextCursor == null) return

        viewModelScope.launch {
            _uiState.update { currentState.copy(isLoadingMore = true) }
            
            when (val result = communityRepository.getPostsFeed(
                limit = 20,
                cursor = currentState.nextCursor
            )) {
                is AppResult.Success -> {
                    val page = result.data
                    _uiState.update {
                        currentState.copy(
                            posts = currentState.posts + page.posts,
                            nextCursor = page.nextCursor,
                            isLoadingMore = false
                        )
                    }
                }
                is AppResult.Error, is AppResult.Exception -> {
                    // 더 불러오기 실패시 isLoadingMore만 false로 변경
                    _uiState.update { currentState.copy(isLoadingMore = false) }
                }
            }
        }
    }

    fun togglePostLike(postId: String) {
        viewModelScope.launch {
            when (communityRepository.togglePostLike(postId)) {
                is AppResult.Success -> {
                    // 로컬에서 UI 즉시 업데이트
                    val currentState = _uiState.value as? CommunityUiState.Success ?: return@launch
                    val updatedPosts = currentState.posts.map { post ->
                        if (post.postId == postId) {
                            post.copy(
                                isLiked = !post.isLiked,
                                likeCount = if (post.isLiked) post.likeCount - 1 else post.likeCount + 1
                            )
                        } else {
                            post
                        }
                    }
                    _uiState.update { currentState.copy(posts = updatedPosts) }
                }
                is AppResult.Error, is AppResult.Exception -> {
                    // 좋아요 실패시 에러 메시지 표시할 수 있음 (선택적)
                }
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            when (communityRepository.deletePost(postId)) {
                is AppResult.Success -> {
                    // 로컬 리스트에서 제거
                    val currentState = _uiState.value as? CommunityUiState.Success ?: return@launch
                    val updatedPosts = currentState.posts.filter { it.postId != postId }
                    
                    if (updatedPosts.isEmpty()) {
                        _uiState.update { CommunityUiState.Empty }
                    } else {
                        _uiState.update { currentState.copy(posts = updatedPosts) }
                    }
                }
                is AppResult.Error -> {
                    // 삭제 실패시 에러 처리 (선택적)
                }
                is AppResult.Exception -> {
                    // 예외 처리 (선택적)
                }
            }
        }
    }

    fun refresh() {
        loadPostsFeed()
    }
}

/**
 * 커뮤니티 화면 UI 상태
 */
sealed interface CommunityUiState {
    data object Loading : CommunityUiState
    data object Empty : CommunityUiState
    
    data class Success(
        val posts: List<Post>,
        val nextCursor: String?,
        val isLoadingMore: Boolean
    ) : CommunityUiState
    
    data class Error(val message: String) : CommunityUiState
}
