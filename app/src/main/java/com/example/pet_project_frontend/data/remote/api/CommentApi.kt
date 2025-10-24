package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.CommentCreateDto
import com.example.pet_project_frontend.data.remote.dto.CommentListResponseDto
import com.example.pet_project_frontend.data.remote.dto.CommentResponseDto
import retrofit2.Response
import retrofit2.http.*

interface CommentApi {
    
    // 댓글 생성
    @POST("api/comments/posts/{post_id}/comments")
    suspend fun createComment(
        @Path("post_id") postId: String,
        @Body comment: CommentCreateDto
    ): Response<CommentResponseDto>
    
    // 댓글 목록 조회 (페이지네이션)
    @GET("api/comments/posts/{post_id}/comments")
    suspend fun getComments(
        @Path("post_id") postId: String,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): Response<CommentListResponseDto>
    
    // 댓글 삭제
    @DELETE("api/comments/comments/{comment_id}")
    suspend fun deleteComment(
        @Path("comment_id") commentId: String
    ): Response<Unit>
    
    // 댓글 좋아요 토글
    @POST("api/comments/comments/{comment_id}/like")
    suspend fun toggleCommentLike(
        @Path("comment_id") commentId: String
    ): Response<Unit>
}
