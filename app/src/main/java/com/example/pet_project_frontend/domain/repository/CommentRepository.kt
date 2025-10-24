package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.domain.model.Comment
import com.example.pet_project_frontend.domain.model.CommentList

interface CommentRepository {
    
    // 댓글 생성
    suspend fun createComment(postId: String, text: String): Result<Comment>
    
    // 댓글 목록 조회
    suspend fun getComments(
        postId: String,
        limit: Int = 20,
        cursor: String? = null
    ): Result<CommentList>
    
    // 댓글 삭제
    suspend fun deleteComment(commentId: String): Result<Unit>
    
    // 댓글 좋아요 토글
    suspend fun toggleCommentLike(commentId: String): Result<Unit>
}
