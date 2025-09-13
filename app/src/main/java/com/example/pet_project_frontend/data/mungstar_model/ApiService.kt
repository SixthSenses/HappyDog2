package com.example.pet_project_frontend.data.mungstar_model

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/posts/")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Body request: CreatePostRequest
    ): Response<PostResponse>
}