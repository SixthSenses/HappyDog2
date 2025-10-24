package com.example.pet_project_frontend.data.mapper

import com.example.pet_project_frontend.data.remote.dto.CommentListResponseDto
import com.example.pet_project_frontend.data.remote.dto.CommentResponseDto
import com.example.pet_project_frontend.domain.model.Comment
import com.example.pet_project_frontend.domain.model.CommentList

// Comment DTO → Domain
fun CommentResponseDto.toDomain(): Comment {
    return Comment(
        commentId = commentId,
        postId = postId,
        text = text,
        author = author.toDomain(),
        pet = pet?.toDomain(),
        likeCount = likeCount,
        isLiked = isLiked,
        createdAt = createdAt
    )
}

// CommentList DTO → Domain
fun CommentListResponseDto.toDomain(): CommentList {
    return CommentList(
        comments = comments.map { it.toDomain() },
        nextCursor = nextCursor
    )
}
