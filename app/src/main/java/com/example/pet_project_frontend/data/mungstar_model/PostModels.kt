package com.example.pet_project_frontend.data.mungstar_model

import com.google.gson.annotations.SerializedName

// 게시물 작성 요청 모델
data class CreatePostRequest(
    @SerializedName("text")
    val text: String,
    @SerializedName("file_paths")
    val filePaths: List<String>
)

// 게시물 작성 응답 모델
data class CreatePostResponse(
    @SerializedName("post_id")
    val postId: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("image_urls")
    val imageUrls: List<String>,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("like_count")
    val likeCount: Int,
    @SerializedName("comment_count")
    val commentCount: Int,
    @SerializedName("is_liked")
    val isLiked: Boolean,
    @SerializedName("author")
    val author: Author,
    @SerializedName("pet")
    val pet: Pet
)

// 작성자 정보
data class Author(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("profile_image_url")
    val profileImageUrl: String?
)

// 펫 정보
data class Pet(
    @SerializedName("pet_id")
    val petId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("breed")
    val breed: String,
    @SerializedName("birthdate")
    val birthdate: String
)

// 이미지 업로드 응답 모델
data class ImageUploadResponse(
    @SerializedName("file_path")
    val filePath: String,
    @SerializedName("message")
    val message: String
)