package com.example.pet_project_frontend.data.mapper

import com.example.pet_project_frontend.data.remote.dto.response.PostListResponse
import com.example.pet_project_frontend.data.remote.dto.response.PostResponse
import com.example.pet_project_frontend.domain.model.Post
import com.example.pet_project_frontend.domain.model.PostsPage

object PostMapper {
    fun map(dto: PostResponse): Post = Post(
        postId = dto.postId,
        imageUrls = dto.imageUrls ?: emptyList(),
        text = dto.text,
        likeCount = dto.likeCount ?: 0,
        commentCount = dto.commentCount ?: 0
    )

    fun mapPage(dto: PostListResponse): PostsPage = PostsPage(
        items = dto.posts.map { map(it) },
        nextCursor = dto.nextCursor
    )
}
