package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.request.CommentCreateRequest
import com.example.pet_project_frontend.data.remote.dto.request.EmptyRequest
import com.example.pet_project_frontend.data.remote.dto.request.PostCreateRequest
import com.example.pet_project_frontend.data.remote.dto.request.PostUpdateRequest
import com.example.pet_project_frontend.data.remote.dto.response.CommentListResponse
import com.example.pet_project_frontend.data.remote.dto.response.CommentLikeToggleResponse
import com.example.pet_project_frontend.data.remote.dto.response.CommentResponse
import com.example.pet_project_frontend.data.remote.dto.response.PostListResponse
import com.example.pet_project_frontend.data.remote.dto.response.PostLikeToggleResponse
import com.example.pet_project_frontend.data.remote.dto.response.PostResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 멍스타그램(Community) API
 * - 게시글 CRUD, 좋아요
 * - 댓글 CRUD, 좋아요
 */
interface CommunityApi {
    
    // ==================== 게시글 ====================
    
    /**
     * 게시글 피드 조회 (커서 기반 페이지네이션)
     * GET /api/posts/
     */
    @GET("api/posts/")
    suspend fun getPostsFeed(
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): Response<PostListResponse>
    
    /**
     * 게시글 상세 조회
     * GET /api/posts/{post_id}
     */
    @GET("api/posts/{post_id}")
    suspend fun getPost(
        @Path("post_id") postId: String
    ): Response<PostResponse>
    
    /**
     * 게시글 생성 (멱등성 적용)
     * POST /api/posts/
     */
    @POST("api/posts/")
    suspend fun createPost(
        @Body request: PostCreateRequest,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null
    ): Response<PostResponse>
    
    /**
     * 게시글 수정 (작성자만)
     * PATCH /api/posts/{post_id}
     */
    @PATCH("api/posts/{post_id}")
    suspend fun updatePost(
        @Path("post_id") postId: String,
        @Body request: PostUpdateRequest
    ): Response<PostResponse>
    
    /**
     * 게시글 삭제 (작성자만)
     * DELETE /api/posts/{post_id}
     */
    @DELETE("api/posts/{post_id}")
    suspend fun deletePost(
        @Path("post_id") postId: String
    ): Response<Unit>
    
    /**
     * 게시글 좋아요 토글
     * POST /api/posts/{post_id}/like
     */
    @POST("api/posts/{post_id}/like")
    suspend fun togglePostLike(
        @Path("post_id") postId: String,
        @Body request: EmptyRequest = EmptyRequest()
    ): Response<PostLikeToggleResponse>
    
    /**
     * 특정 사용자의 게시글 목록 (프로필용)
     * GET /api/posts/users/{author_id}/posts
     */
    @GET("api/posts/users/{author_id}/posts")
    suspend fun getUserPosts(
        @Path("author_id") authorId: String,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): Response<PostListResponse>
    
    // ==================== 댓글 ====================
    
    /**
     * 댓글 목록 조회
     * GET /api/comments/posts/{post_id}/comments
     */
    @GET("api/comments/posts/{post_id}/comments")
    suspend fun getComments(
        @Path("post_id") postId: String,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): Response<CommentListResponse>
    
    /**
     * 댓글 생성 (멱등성 적용)
     * POST /api/comments/posts/{post_id}/comments
     */
    @POST("api/comments/posts/{post_id}/comments")
    suspend fun createComment(
        @Path("post_id") postId: String,
        @Body request: CommentCreateRequest
    ): Response<CommentResponse>
    
    /**
     * 댓글 삭제 (작성자만)
     * DELETE /api/comments/comments/{comment_id}
     */
    @DELETE("api/comments/comments/{comment_id}")
    suspend fun deleteComment(
        @Path("comment_id") commentId: String
    ): Response<Unit>
    
    /**
     * 댓글 좋아요 토글
     * POST /api/comments/comments/{comment_id}/like
     */
    @POST("api/comments/comments/{comment_id}/like")
    suspend fun toggleCommentLike(
        @Path("comment_id") commentId: String,
        @Body request: EmptyRequest = EmptyRequest()
    ): Response<CommentLikeToggleResponse>
}
