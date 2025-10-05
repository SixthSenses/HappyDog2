package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.model.Comment
import com.example.pet_project_frontend.domain.model.CommentsPage
import com.example.pet_project_frontend.domain.model.Post
import com.example.pet_project_frontend.domain.model.PostsPage

/**
 * 멍스타그램(Community) Repository
 */
interface CommunityRepository {
    
    // ==================== 게시글 ====================
    
    /**
     * 게시글 피드 조회 (커서 페이지네이션)
     */
    suspend fun getPostsFeed(
        limit: Int? = null,
        cursor: String? = null
    ): AppResult<PostsPage>
    
    /**
     * 게시글 상세 조회
     */
    suspend fun getPost(postId: String): AppResult<Post>
    
    /**
     * 게시글 생성
     */
    suspend fun createPost(
        text: String,
        filePaths: List<String>
    ): AppResult<Post>
    
    /**
     * 게시글 수정 (작성자만)
     */
    suspend fun updatePost(
        postId: String,
        text: String
    ): AppResult<Post>
    
    /**
     * 게시글 삭제 (작성자만)
     */
    suspend fun deletePost(postId: String): AppResult<Unit>
    
    /**
     * 게시글 좋아요 토글
     */
    suspend fun togglePostLike(postId: String): AppResult<String>
    
    /**
     * 특정 사용자의 게시글 목록 (프로필용)
     */
    suspend fun getUserPosts(
        authorId: String,
        limit: Int? = null,
        cursor: String? = null
    ): AppResult<PostsPage>
    
    // ==================== 댓글 ====================
    
    /**
     * 댓글 목록 조회
     */
    suspend fun getComments(
        postId: String,
        limit: Int? = null,
        cursor: String? = null
    ): AppResult<CommentsPage>
    
    /**
     * 댓글 생성
     */
    suspend fun createComment(
        postId: String,
        text: String
    ): AppResult<Comment>
    
    /**
     * 댓글 삭제 (작성자만)
     */
    suspend fun deleteComment(commentId: String): AppResult<Unit>
    
    /**
     * 댓글 좋아요 토글
     */
    suspend fun toggleCommentLike(commentId: String): AppResult<Boolean>
}
