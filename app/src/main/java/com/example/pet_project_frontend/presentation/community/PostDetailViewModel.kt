package com.example.pet_project_frontend.presentation.community

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.model.Comment
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
 * 게시글 상세 + 댓글 화면 ViewModel
 * MVVM + UDF 패턴
 */
@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _uiState = MutableStateFlow<PostDetailUiState>(PostDetailUiState.Loading)
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    init {
        loadPostDetail()
    }

    fun loadPostDetail() {
        viewModelScope.launch {
            _uiState.update { PostDetailUiState.Loading }
            
            // 게시글과 댓글 동시 로드
            val postResult = communityRepository.getPost(postId)
            val commentsResult = communityRepository.getComments(postId, limit = 50, cursor = null)
            
            when {
                postResult is AppResult.Success && commentsResult is AppResult.Success -> {
                    val post = postResult.data
                    val commentsPage = commentsResult.data
                    _uiState.update {
                        PostDetailUiState.Success(
                            post = post,
                            comments = commentsPage.comments,
                            nextCursor = commentsPage.nextCursor,
                            isLoadingMoreComments = false,
                            isSubmittingComment = false
                        )
                    }
                }
                postResult is AppResult.Error -> {
                    _uiState.update { PostDetailUiState.Error("게시글을 찾을 수 없습니다: ${postResult.message}") }
                }
                postResult is AppResult.Exception -> {
                    _uiState.update { PostDetailUiState.Error("네트워크 오류: ${postResult.throwable.message}") }
                }
                else -> {
                    _uiState.update { PostDetailUiState.Error("데이터를 불러올 수 없습니다") }
                }
            }
        }
    }

    fun loadMoreComments() {
        val currentState = _uiState.value as? PostDetailUiState.Success ?: return
        if (currentState.isLoadingMoreComments || currentState.nextCursor == null) return

        viewModelScope.launch {
            _uiState.update { currentState.copy(isLoadingMoreComments = true) }
            
            when (val result = communityRepository.getComments(
                postId = postId,
                limit = 50,
                cursor = currentState.nextCursor
            )) {
                is AppResult.Success -> {
                    val page = result.data
                    _uiState.update {
                        currentState.copy(
                            comments = currentState.comments + page.comments,
                            nextCursor = page.nextCursor,
                            isLoadingMoreComments = false
                        )
                    }
                }
                is AppResult.Error, is AppResult.Exception -> {
                    _uiState.update { currentState.copy(isLoadingMoreComments = false) }
                }
            }
        }
    }

    fun submitComment(text: String, onSuccess: () -> Unit) {
        val currentState = _uiState.value as? PostDetailUiState.Success ?: return
        
        viewModelScope.launch {
            _uiState.update { currentState.copy(isSubmittingComment = true) }
            
            when (val result = communityRepository.createComment(postId, text)) {
                is AppResult.Success -> {
                    val newComment = result.data
                    // 새 댓글을 리스트 맨 앞에 추가
                    _uiState.update {
                        currentState.copy(
                            comments = listOf(newComment) + currentState.comments,
                            post = currentState.post.copy(
                                commentCount = currentState.post.commentCount + 1
                            ),
                            isSubmittingComment = false
                        )
                    }
                    onSuccess()
                }
                is AppResult.Error -> {
                    _uiState.update { currentState.copy(isSubmittingComment = false) }
                }
                is AppResult.Exception -> {
                    _uiState.update { currentState.copy(isSubmittingComment = false) }
                }
            }
        }
    }

    fun togglePostLike() {
        val currentState = _uiState.value as? PostDetailUiState.Success ?: return
        
        viewModelScope.launch {
            when (communityRepository.togglePostLike(postId)) {
                is AppResult.Success -> {
                    val updatedPost = currentState.post.copy(
                        isLiked = !currentState.post.isLiked,
                        likeCount = if (currentState.post.isLiked) 
                            currentState.post.likeCount - 1 
                        else 
                            currentState.post.likeCount + 1
                    )
                    _uiState.update { currentState.copy(post = updatedPost) }
                }
                is AppResult.Error, is AppResult.Exception -> {
                    // 실패 처리 (선택적)
                }
            }
        }
    }

    fun toggleCommentLike(commentId: String) {
        val currentState = _uiState.value as? PostDetailUiState.Success ?: return
        
        viewModelScope.launch {
            when (val result = communityRepository.toggleCommentLike(commentId)) {
                is AppResult.Success -> {
                    val liked = result.data
                    val updatedComments = currentState.comments.map { comment ->
                        if (comment.commentId == commentId) {
                            comment.copy(
                                isLiked = liked,
                                likeCount = if (liked) comment.likeCount + 1 else comment.likeCount - 1
                            )
                        } else {
                            comment
                        }
                    }
                    _uiState.update { currentState.copy(comments = updatedComments) }
                }
                is AppResult.Error, is AppResult.Exception -> {
                    // 실패 처리
                }
            }
        }
    }

    fun deleteComment(commentId: String) {
        val currentState = _uiState.value as? PostDetailUiState.Success ?: return
        
        viewModelScope.launch {
            when (communityRepository.deleteComment(commentId)) {
                is AppResult.Success -> {
                    val updatedComments = currentState.comments.filter { it.commentId != commentId }
                    _uiState.update {
                        currentState.copy(
                            comments = updatedComments,
                            post = currentState.post.copy(
                                commentCount = currentState.post.commentCount - 1
                            )
                        )
                    }
                }
                is AppResult.Error, is AppResult.Exception -> {
                    // 삭제 실패 처리
                }
            }
        }
    }

    fun refresh() {
        loadPostDetail()
    }
}

/**
 * 게시글 상세 화면 UI 상태
 */
sealed interface PostDetailUiState {
    data object Loading : PostDetailUiState
    
    data class Success(
        val post: Post,
        val comments: List<Comment>,
        val nextCursor: String?,
        val isLoadingMoreComments: Boolean,
        val isSubmittingComment: Boolean
    ) : PostDetailUiState
    
    data class Error(val message: String) : PostDetailUiState
}
