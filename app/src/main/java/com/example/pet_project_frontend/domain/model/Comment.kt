package com.example.pet_project_frontend.domain.model

import java.time.LocalDateTime

/**
 * 댓글 도메인 모델
 */
data class Comment(
    val commentId: String,
    val postId: String,
    val author: Author,
    val pet: PetInfo?,
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
