package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * 작성자 정보
 */
data class AuthorDto(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("nickname")
    val nickname: String
)

/**
 * 반려견 정보
 */
data class PetInfoDto(
    @SerializedName("pet_id")
    val petId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("breed")
    val breed: String,
    @SerializedName("birthdate")
    val birthdate: String?, // ISO 8601 format (nullable)
    @SerializedName("profile_image_url")
    val profileImageUrl: String?
)

// PostResponse, PostListResponse, PostLikeToggleResponse는 PostResponse.kt에 정의됨

/**
 * 댓글 응답
 * Note: OpenAPI 스키마에서는 pet이 Required이지만, 방어적으로 nullable 처리
 */
data class CommentResponse(
    @SerializedName("comment_id")
    val commentId: String,
    @SerializedName("post_id")
    val postId: String,
    @SerializedName("author")
    val author: AuthorDto,
    @SerializedName("pet")
    val pet: PetInfoDto, // OpenAPI: Required, 실제로는 항상 존재
    @SerializedName("text")
    val text: String,
    @SerializedName("like_count")
    val likeCount: Int,
    @SerializedName("created_at")
    val createdAt: String, // ISO 8601 format
    @SerializedName("is_liked")
    val isLiked: Boolean? = null // 로그인 사용자만 포함
)

/**
 * 댓글 목록 응답 (커서 페이지네이션)
 */
data class CommentListResponse(
    @SerializedName("comments")
    val comments: List<CommentResponse>,
    @SerializedName("next_cursor")
    val nextCursor: String?
)

/**
 * 댓글 좋아요 토글 응답
 */
data class CommentLikeToggleResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("liked")
    val liked: Boolean,
    @SerializedName("comment_id")
    val commentId: String
)
