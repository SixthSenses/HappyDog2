package com.example.pet_project_frontend.data.repository

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.core.common.SafeApi
import com.example.pet_project_frontend.data.mapper.PostMapper
import com.example.pet_project_frontend.data.remote.api.PostApi
import com.example.pet_project_frontend.domain.model.PostsPage
import com.example.pet_project_frontend.domain.repository.PostRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postApi: PostApi
) : PostRepository {

    override suspend fun getUserPosts(authorId: String, limit: Int?, cursor: String?): AppResult<PostsPage> {
        return SafeApi.response { postApi.getUserPosts(authorId, limit, cursor) }
            .let { res ->
                when (res) {
                    is AppResult.Success -> AppResult.Success(PostMapper.mapPage(res.data))
                    is AppResult.Error -> res
                    is AppResult.Exception -> res
                }
            }
    }
}
