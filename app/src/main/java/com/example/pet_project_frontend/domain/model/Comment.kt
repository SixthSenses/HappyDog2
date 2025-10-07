package com.example.pet_project_frontend.domain.model

import java.time.LocalDateTime

/**
 * 댓글 도메인 모델
 * Note: OpenAPI 스키마에 따라 pet은 항상 존재 (Required)
 */
data class Comment(
    val commentId: String,
    val postId: String,
    val author: Author,
    val pet: PetInfo, // OpenAPI: Required (nullable 제거)
    val text: String,
    val likeCount: Int,
    val createdAt: LocalDateTime,
    val isLiked: Boolean
)

/**
 * 댓글 페이지 (페이지네이션)
 */
data class CommentsPage(
    val comments: List<Comment>,
    val nextCursor: String?
)
