package com.example.pet_project_frontend.data.mungstar_model

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ===== 새로 추가된 게시물 목록 조회 API =====
    @GET("api/posts/")
    suspend fun getPosts(
        @Header("Authorization") token: String
    ): Response<List<CreatePostResponse>>

    @POST("api/posts/")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Body request: CreatePostRequest
    ): Response<CreatePostResponse>

    @Multipart
    @POST("api/uploads/")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<ImageUploadResponse>
}