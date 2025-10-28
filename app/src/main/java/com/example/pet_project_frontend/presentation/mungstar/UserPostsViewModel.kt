package com.example.pet_project_frontend.presentation.mungstar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.domain.model.PetInfo
import com.example.pet_project_frontend.domain.model.Post
import com.example.pet_project_frontend.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserPostsUiState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val profilePet: PetInfo? = null
)

@HiltViewModel
class UserPostsViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "UserPostsViewModel"
    }
    
    private val _uiState = MutableStateFlow(UserPostsUiState())
    val uiState: StateFlow<UserPostsUiState> = _uiState.asStateFlow()
    
    private var currentAuthorId: String? = null
    
    fun loadUserPosts(authorId: String, refresh: Boolean = false) {
        currentAuthorId = authorId
        
        viewModelScope.launch {
            if (refresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }
            
            val result = postRepository.getUserPosts(
                authorId = authorId,
                limit = 20,
                cursor = if (refresh) null else _uiState.value.nextCursor
            )
            
            result.onSuccess { feed ->
                val updatedPosts = if (refresh) feed.posts else _uiState.value.posts + feed.posts
                val profilePet = when {
                    updatedPosts.isNotEmpty() -> updatedPosts.first().pet ?: _uiState.value.profilePet
                    refresh                   -> null
                    else                       -> _uiState.value.profilePet
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    posts = updatedPosts,
                    nextCursor = feed.nextCursor,
                    hasMore = feed.hasMore,
                    profilePet = profilePet
                )
            }.onFailure { throwable ->
                Log.e(TAG, "사용자 게시물 조회 실패", throwable)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = throwable.message ?: "게시물 조회 실패"
                )
            }
        }
    }
    
    fun refresh() {
        currentAuthorId?.let { loadUserPosts(it, refresh = true) }
    }
    
    fun loadMore() {
        if (_uiState.value.isLoading || !_uiState.value.hasMore) return
        currentAuthorId?.let { loadUserPosts(it, refresh = false) }
    }
    
    fun toggleLike(postId: String) {
        viewModelScope.launch {
            // 낙관적 UI 업데이트
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
            
            // API 호출
            val result = postRepository.toggleLike(postId)
            
            if (result.isFailure) {
                // 실패 시 롤백
                _uiState.value = _uiState.value.copy(
                    posts = _uiState.value.posts.map { post ->
                        if (post.postId == postId) {
                            post.copy(
                                isLiked = !post.isLiked,
                                likesCount = if (post.isLiked) post.likesCount + 1 else post.likesCount - 1
                            )
                        } else {
                            post
                        }
                    }
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
