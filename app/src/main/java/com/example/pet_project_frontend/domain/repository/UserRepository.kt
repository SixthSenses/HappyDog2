package com.example.pet_project_frontend.domain.repository

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.data.remote.dto.request.UserUpdateRequest
import com.example.pet_project_frontend.domain.model.User

interface UserRepository {
    suspend fun getUserInfo(): AppResult<User>
    suspend fun updateUserProfile(request: UserUpdateRequest): AppResult<User>
    // Update only the profile image; returns updated User domain model
    suspend fun updateProfileImage(filePath: String): AppResult<User>
    suspend fun deleteUser(): AppResult<Unit>
    suspend fun saveAccessToken(token: String)
    fun getAccessToken(): kotlinx.coroutines.flow.Flow<String?>

    // Selected pet persistence
    suspend fun getSelectedPet(): AppResult<com.example.pet_project_frontend.data.remote.dto.response.SelectedPetResponse?>
    suspend fun setSelectedPet(petId: String): AppResult<com.example.pet_project_frontend.data.remote.dto.response.SelectedPetResponse>
}