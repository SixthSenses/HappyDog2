package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.data.remote.dto.request.UserUpdateRequest
import com.example.pet_project_frontend.data.remote.result.NetworkResult
import com.example.pet_project_frontend.domain.model.User

interface UserRepository {
    suspend fun getUserInfo(): NetworkResult<User>
    suspend fun updateUserProfile(request: UserUpdateRequest): NetworkResult<User>
    suspend fun deleteUser(): NetworkResult<Unit>
    suspend fun saveAccessToken(token: String)
    fun getAccessToken(): kotlinx.coroutines.flow.Flow<String?>
}