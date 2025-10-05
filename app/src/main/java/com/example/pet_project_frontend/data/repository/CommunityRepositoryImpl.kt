package com.example.pet_project_frontend.data.repository

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.core.common.SafeApi
import com.example.pet_project_frontend.data.mapper.CommunityMapper.toDomain
import com.example.pet_project_frontend.data.remote.api.CommunityApi
import com.example.pet_project_frontend.data.remote.dto.request.CommentCreateRequest
import com.example.pet_project_frontend.data.remote.dto.request.EmptyRequest
import com.example.pet_project_frontend.data.remote.dto.request.PostCreateRequest
import com.example.pet_project_frontend.data.remote.dto.request.PostUpdateRequest
import com.example.pet_project_frontend.domain.model.Comment
import com.example.pet_project_frontend.domain.model.CommentsPage
import com.example.pet_project_frontend.domain.model.Post
import com.example.pet_project_frontend.domain.model.PostsPage
import com.example.pet_project_frontend.domain.repository.CommunityRepository
import javax.inject.Inject

/**
 * CommunityRepository 구현체
 * SafeApi를 사용하여 모든 API 호출을 AppResult로 래핑
 */
class CommunityRepositoryImpl @Inject constructor(
    private val communityApi: CommunityApi
) : CommunityRepository {
    
    // ==================== 게시글 ====================
    
    override suspend fun getPostsFeed(limit: Int?, cursor: String?): AppResult<PostsPage> {
        return when (val result = SafeApi.response { communityApi.getPostsFeed(limit, cursor) }) {
            is AppResult.Success -> AppResult.Success(result.data.toDomain())
            is AppResult.Error -> result
            is AppResult.Exception -> result
        }
    }
    
    override suspend fun getPost(postId: String): AppResult<Post> {
        return when (val result = SafeApi.response { communityApi.getPost(postId) }) {
            is AppResult.Success -> AppResult.Success(result.data.toDomain())
            is AppResult.Error -> result
            is AppResult.Exception -> result
        }
    }
    
    override suspend fun createPost(text: String, filePaths: List<String>): AppResult<Post> {
        return when (val result = SafeApi.response { communityApi.createPost(PostCreateRequest(text, filePaths)) }) {
            is AppResult.Success -> AppResult.Success(result.data.toDomain())
            is AppResult.Error -> result
            is AppResult.Exception -> result
        }
    }
    
    override suspend fun updatePost(postId: String, text: String): AppResult<Post> {
        return when (val result = SafeApi.response { communityApi.updatePost(postId, PostUpdateRequest(text)) }) {
            is AppResult.Success -> AppResult.Success(result.data.toDomain())
            is AppResult.Error -> result
            is AppResult.Exception -> result
        }
    }
    
    override suspend fun deletePost(postId: String): AppResult<Unit> {
        return SafeApi.responseUnit { communityApi.deletePost(postId) }
    }
    
    override suspend fun togglePostLike(postId: String): AppResult<String> {
        return when (val result = SafeApi.response { communityApi.togglePostLike(postId, EmptyRequest()) }) {
            is AppResult.Success -> AppResult.Success(result.data.message)
            is AppResult.Error -> result
            is AppResult.Exception -> result
        }
    }
    
    override suspend fun getUserPosts(
        authorId: String,
        limit: Int?,
        cursor: String?
    ): AppResult<PostsPage> {
        return when (val result = SafeApi.response { communityApi.getUserPosts(authorId, limit, cursor) }) {
            is AppResult.Success -> AppResult.Success(result.data.toDomain())
            is AppResult.Error -> result
            is AppResult.Exception -> result
        }
    }
    
    // ==================== 댓글 ====================
    
    override suspend fun getComments(
        postId: String,
        limit: Int?,
        cursor: String?
    ): AppResult<CommentsPage> {
        return when (val result = SafeApi.response { communityApi.getComments(postId, limit, cursor) }) {
            is AppResult.Success -> AppResult.Success(result.data.toDomain())
            is AppResult.Error -> result
            is AppResult.Exception -> result
        }
    }
    
    override suspend fun createComment(postId: String, text: String): AppResult<Comment> {
        return when (val result = SafeApi.response { communityApi.createComment(postId, CommentCreateRequest(text)) }) {
            is AppResult.Success -> AppResult.Success(result.data.toDomain())
            is AppResult.Error -> result
            is AppResult.Exception -> result
        }
    }
    
    override suspend fun deleteComment(commentId: String): AppResult<Unit> {
        return SafeApi.responseUnit { communityApi.deleteComment(commentId) }
    }
    
    override suspend fun toggleCommentLike(commentId: String): AppResult<Boolean> {
        return when (val result = SafeApi.response { communityApi.toggleCommentLike(commentId, EmptyRequest()) }) {
            is AppResult.Success -> AppResult.Success(result.data.liked)
            is AppResult.Error -> result
            is AppResult.Exception -> result
        }
    }
}
