package com.example.pet_project_frontend.data.remote.dto

import com.google.gson.annotations.SerializedName

// 댓글 생성 요청 DTO
data class CommentCreateDto(
    @SerializedName("text")
    val text: String
)

// 댓글 응답 DTO
data class CommentResponseDto(
    @SerializedName("comment_id")
    val commentId: String,
    
    @SerializedName("post_id")
    val postId: String,
    
    @SerializedName("text")
    val text: String,
    
    @SerializedName("author")
    val author: AuthorDto,
    
    @SerializedName("pet")
    val pet: PetInfoDto?,
    
    @SerializedName("like_count")
    val likeCount: Int,
    
    @SerializedName("is_liked")
    val isLiked: Boolean,
    
    @SerializedName("created_at")
    val createdAt: String
)

// 댓글 목록 응답 DTO (페이지네이션)
data class CommentListResponseDto(
    @SerializedName("comments")
    val comments: List<CommentResponseDto>,
    
    @SerializedName("next_cursor")
    val nextCursor: String?
)
