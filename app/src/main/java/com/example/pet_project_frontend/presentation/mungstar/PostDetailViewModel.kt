package com.example.pet_project_frontend.presentation.mungstar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.model.Comment
import com.example.pet_project_frontend.domain.model.Post
import com.example.pet_project_frontend.domain.model.PetInfo
import com.example.pet_project_frontend.domain.repository.CommentRepository
import com.example.pet_project_frontend.domain.repository.PostRepository
import com.example.pet_project_frontend.domain.repository.UserRepository
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
    val isDeleting: Boolean = false,
    val showCommentToast: Boolean = false,
    val currentUserPet: PetInfo? = null // Pet 정보로 변경하여 일관성 유지
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "PostDetailViewModel"
    }
    
    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()
    
    // 게시글 ID 저장
    private var currentPostId: String? = null
    
    init {
        // 현재 사용자의 프로필 이미지 가져오기
        loadCurrentUserProfile()
    }
    
    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            try {
                // /api/users/me/summary를 사용하여 Pet 정보 가져오기
                val profileImageUrl = userRepository.getUserProfileImageUrl()
                
                if (profileImageUrl != null) {
                    // Pet 정보를 임시로 생성 (프로필 이미지만 필요)
                    val petInfo = PetInfo(
                        petId = "", // 실제로는 사용하지 않음
                        name = "",
                        breed = "",
                        age = 0,
                        profileImageUrl = profileImageUrl,
                        isVerified = false
                    )
                    _uiState.value = _uiState.value.copy(currentUserPet = petInfo)
                    Log.d(TAG, "사용자 Pet 프로필 이미지 로드 성공: $profileImageUrl")
                } else {
                    Log.w(TAG, "사용자 Pet 프로필 이미지를 가져올 수 없습니다")
                }
            } catch (e: Exception) {
                Log.e(TAG, "프로필 로드 중 오류", e)
            }
        }
    }
    
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
                // 토스트 표시
                _uiState.value = _uiState.value.copy(showCommentToast = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "댓글 작성 실패"
                )
            }
        }
    }
    
    fun hideCommentToast() {
        _uiState.value = _uiState.value.copy(showCommentToast = false)
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
