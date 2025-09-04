package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.model.PostsPage

interface PostRepository {
    suspend fun getUserPosts(authorId: String, limit: Int? = 9, cursor: String? = null): AppResult<PostsPage>
}
