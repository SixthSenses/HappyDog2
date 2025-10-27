package com.example.pet_project_frontend.presentation.mungstar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.domain.model.Post
import com.example.pet_project_frontend.domain.repository.PostRepository
import com.example.pet_project_frontend.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MungStarUiState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val currentUserId: String? = null
)

@HiltViewModel
class MungStarViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MungStarUiState())
    val uiState: StateFlow<MungStarUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadPosts()
    }

    // 현재 사용자 정보 로드
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val result = userRepository.getUserInfo()
                if (result is com.example.pet_project_frontend.core.common.AppResult.Success) {
                    _uiState.value = _uiState.value.copy(currentUserId = result.data.id)
                }
            } catch (e: Exception) {
                // 에러 무시 (person 아이콘만 비활성화)
            }
        }
    }

    // 게시글 목록 로드
    fun loadPosts(refresh: Boolean = false) {
        if (refresh) {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
        } else {
            if (_uiState.value.isLoading) return
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        }

        viewModelScope.launch {
            try {
                val cursor = if (refresh) null else _uiState.value.nextCursor
                val result = postRepository.getPostsFeed(limit = 20, cursor = cursor)

                if (result.isSuccess) {
                    val feed = result.getOrNull()!!
                    
                    // 디버깅: is_verified 값 로깅
                    feed.posts.forEach { post ->
                        Log.d("MungStarViewModel", "Post ${post.postId}: pet=${post.pet?.name}, isVerified=${post.pet?.isVerified}")
                    }
                    
                    _uiState.value = if (refresh) {
                        _uiState.value.copy(
                            isLoading = false,
                            isRefreshing = false,
                            posts = feed.posts,
                            nextCursor = feed.nextCursor,
                            hasMore = feed.hasMore
                        )
                    } else {
                        _uiState.value.copy(
                            isLoading = false,
                            posts = _uiState.value.posts + feed.posts,
                            nextCursor = feed.nextCursor,
                            hasMore = feed.hasMore
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "피드 로드 실패: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "오류 발생: ${e.message}"
                )
            }
        }
    }

    // 좋아요 토글
    fun toggleLike(postId: String) {
        viewModelScope.launch {
            try {
                val result = postRepository.toggleLike(postId)

                if (result.isSuccess) {
                    // UI에서 즉시 반영
                    _uiState.value = _uiState.value.copy(
                        posts = _uiState.value.posts.map { post ->
                            if (post.postId == postId) {
                                post.copy(
                                    isLiked = !post.isLiked,
                                    likesCount = if (post.isLiked) post.likesCount - 1 else post.likesCount + 1
                                )
                            } else {
                                post
                            }
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "좋아요 처리 실패: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "오류 발생: ${e.message}"
                )
            }
        }
    }

    // 새로고침
    fun refresh() {
        loadPosts(refresh = true)
    }

    // 더 로드
    fun loadMore() {
        if (_uiState.value.hasMore && !_uiState.value.isLoading) {
            loadPosts(refresh = false)
        }
    }

    // 에러 초기화
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
