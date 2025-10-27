package com.example.pet_project_frontend.data.remote.dto

import com.google.gson.annotations.SerializedName

// 게시글 작성 요청 DTO
data class PostCreateDto(
    @SerializedName("text")
    val text: String,
    
    @SerializedName("file_paths")
    val filePaths: List<String> = emptyList()
)

// 게시글 수정 요청 DTO
data class PostUpdateDto(
    @SerializedName("text")
    val text: String
)

// 게시글 작성자 정보 DTO
data class AuthorDto(
    @SerializedName("user_id")
    val userId: String,
    
    @SerializedName("nickname")
    val nickname: String, // OpenAPI 스펙: "nickname" 필드 사용
    
    @SerializedName("profile_picture_url")
    val profilePictureUrl: String? // 스펙에 없지만 nullable로 처리
)

// 반려동물 정보 DTO
data class PetInfoDto(
    @SerializedName("pet_id")
    val petId: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("breed")
    val breed: String,
    
    @SerializedName("birthdate")  // OpenAPI 스펙: birthdate (date-time)
    val birthdate: String,
    
    @SerializedName("profile_image_url")
    val profileImageUrl: String?,
    
    @SerializedName("is_verified")  // 신원 인증 여부 (비문 등록 완료)
    val isVerified: Boolean? = false
)

// 게시글 응답 DTO
data class PostResponseDto(
    @SerializedName("post_id")
    val postId: String,
    
    @SerializedName("text")
    val text: String,
    
    @SerializedName("image_urls") // OpenAPI 스펙: image_urls
    val imageUrls: List<String>,
    
    @SerializedName("author")
    val author: AuthorDto,
    
    @SerializedName("pet")
    val pet: PetInfoDto?,
    
    @SerializedName("like_count") // OpenAPI 스펙: like_count
    val likeCount: Int,
    
    @SerializedName("comment_count") // OpenAPI 스펙: comment_count
    val commentCount: Int,
    
    @SerializedName("is_liked")
    val isLiked: Boolean,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String?
)

// 피드 응답 DTO (페이지네이션)
data class PostsFeedResponseDto(
    @SerializedName("posts")
    val posts: List<PostResponseDto>,
    
    @SerializedName("next_cursor")
    val nextCursor: String?,
    
    @SerializedName("has_more")
    val hasMore: Boolean
)
