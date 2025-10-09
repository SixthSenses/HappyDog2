package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface PostApi {
    // 게시글 생성
    @POST("api/posts/")
    suspend fun createPost(
        @Body request: PostCreateDto
    ): Response<PostResponseDto>
    
    // 게시글 피드 목록 조회 (페이지네이션)
    @GET("api/posts/")
    suspend fun getPostsFeed(
        @Query("limit") limit: Int? = 20,
        @Query("cursor") cursor: String? = null
    ): Response<PostsFeedResponseDto>
    
    // 특정 게시글 상세 조회
    @GET("api/posts/{post_id}")
    suspend fun getPost(
        @Path("post_id") postId: String
    ): Response<PostResponseDto>
    
    // 특정 사용자의 게시물 목록
    @GET("api/posts/users/{author_id}/posts")
    suspend fun getUserPosts(
        @Path("author_id") authorId: String,
        @Query("limit") limit: Int? = 20,
        @Query("cursor") cursor: String? = null
    ): Response<PostsFeedResponseDto>
    
    // 게시글 좋아요 토글
    @POST("api/posts/{post_id}/like")
    suspend fun toggleLike(
        @Path("post_id") postId: String
    ): Response<Unit>
    
    // 게시글 수정
    @PATCH("api/posts/{post_id}")
    suspend fun updatePost(
        @Path("post_id") postId: String,
        @Body request: PostUpdateDto
    ): Response<PostResponseDto>
    
    // 게시글 삭제
    @DELETE("api/posts/{post_id}")
    suspend fun deletePost(
        @Path("post_id") postId: String
    ): Response<Unit>
}
