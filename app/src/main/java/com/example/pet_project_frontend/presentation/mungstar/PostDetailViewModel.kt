package com.example.pet_project_frontend.presentation.mungstar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.domain.model.Comment
import com.example.pet_project_frontend.domain.model.Post
import com.example.pet_project_frontend.domain.repository.CommentRepository
import com.example.pet_project_frontend.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostDetailUiState(
    val isLoading: Boolean = false,
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val error: String? = null,
    val showMoreMenu: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "PostDetailViewModel"
    }
    
    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()
    
    // 게시글 ID 저장
    private var currentPostId: String? = null
    
    fun loadPostDetail(postId: String) {
        currentPostId = postId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // 게시글 조회
            val postResult = postRepository.getPost(postId)
            
            if (postResult.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    post = postResult.getOrNull(),
                    isLoading = false
                )
                // 댓글 조회
                loadComments(postId)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = postResult.exceptionOrNull()?.message ?: "게시글 조회 실패"
                )
            }
        }
    }
    
    private fun loadComments(postId: String, cursor: String? = null) {
        viewModelScope.launch {
            val result = commentRepository.getComments(
                postId = postId,
                limit = 20,
                cursor = cursor
            )
            
            if (result.isSuccess) {
                val commentList = result.getOrNull()!!
                _uiState.value = _uiState.value.copy(
                    comments = if (cursor == null) commentList.comments 
                               else _uiState.value.comments + commentList.comments,
                    nextCursor = commentList.nextCursor,
                    hasMore = commentList.nextCursor != null
                )
            } else {
                Log.e(TAG, "댓글 조회 실패", result.exceptionOrNull())
            }
        }
    }
    
    fun loadMoreComments() {
        val postId = currentPostId ?: return
        val cursor = _uiState.value.nextCursor ?: return
        if (!_uiState.value.hasMore) return
        
        loadComments(postId, cursor)
    }
    
    fun createComment(text: String) {
        val postId = currentPostId ?: return
        
        viewModelScope.launch {
            val result = commentRepository.createComment(postId, text)
            
            if (result.isSuccess) {
                // 댓글 목록 새로고침
                loadComments(postId)
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "댓글 작성 실패"
                )
            }
        }
    }
    
    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            val result = commentRepository.deleteComment(commentId)
            
            if (result.isSuccess) {
                // 댓글 목록에서 제거
                _uiState.value = _uiState.value.copy(
                    comments = _uiState.value.comments.filter { it.commentId != commentId }
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "댓글 삭제 실패"
                )
            }
        }
    }
    
    fun toggleCommentLike(commentId: String) {
        viewModelScope.launch {
            // 낙관적 UI 업데이트
            _uiState.value = _uiState.value.copy(
                comments = _uiState.value.comments.map { comment ->
                    if (comment.commentId == commentId) {
                        comment.copy(
                            isLiked = !comment.isLiked,
                            likeCount = if (comment.isLiked) comment.likeCount - 1 else comment.likeCount + 1
                        )
                    } else {
                        comment
                    }
                }
            )
            
            // API 호출
            val result = commentRepository.toggleCommentLike(commentId)
            
            if (result.isFailure) {
                // 실패 시 롤백
                _uiState.value = _uiState.value.copy(
                    comments = _uiState.value.comments.map { comment ->
                        if (comment.commentId == commentId) {
                            comment.copy(
                                isLiked = !comment.isLiked,
                                likeCount = if (comment.isLiked) comment.likeCount + 1 else comment.likeCount - 1
                            )
                        } else {
                            comment
                        }
                    }
                )
            }
        }
    }
    
    fun togglePostLike() {
        val postId = currentPostId ?: return
        val post = _uiState.value.post ?: return
        
        viewModelScope.launch {
            // 낙관적 UI 업데이트
            _uiState.value = _uiState.value.copy(
                post = post.copy(
                    isLiked = !post.isLiked,
                    likesCount = if (post.isLiked) post.likesCount - 1 else post.likesCount + 1
                )
            )
            
            // API 호출
            val result = postRepository.toggleLike(postId)
            
            if (result.isFailure) {
                // 실패 시 롤백
                _uiState.value = _uiState.value.copy(
                    post = post.copy(
                        isLiked = !post.isLiked,
                        likesCount = if (post.isLiked) post.likesCount + 1 else post.likesCount - 1
                    )
                )
            }
        }
    }
    
    fun showMoreMenu() {
        _uiState.value = _uiState.value.copy(showMoreMenu = true)
    }
    
    fun hideMoreMenu() {
        _uiState.value = _uiState.value.copy(showMoreMenu = false)
    }
    
    fun showDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true, showMoreMenu = false)
    }
    
    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
    }
    
    fun deletePost(onSuccess: () -> Unit) {
        val postId = currentPostId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)
            
            val result = postRepository.deletePost(postId)
            
            if (result.isSuccess) {
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    showDeleteDialog = false,
                    error = result.exceptionOrNull()?.message ?: "게시글 삭제 실패"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
