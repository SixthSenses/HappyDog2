package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.response.PostListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApi {
    // 특정 사용자의 게시물 목록 (jwt_optional)
    @GET("api/posts/users/{author_id}/posts")
    suspend fun getUserPosts(
        @Path("author_id") authorId: String,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): Response<PostListResponse>
}
