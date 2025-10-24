package com.example.pet_project_frontend.data.remote.api

import com.example.pet_project_frontend.data.remote.dto.request.EmptyRequest
import com.example.pet_project_frontend.data.remote.dto.request.LogoutRequest
import com.example.pet_project_frontend.data.remote.dto.request.SocialLoginRequest
import com.example.pet_project_frontend.data.remote.dto.response.LogoutResponse
import com.example.pet_project_frontend.data.remote.dto.response.SocialLoginResponse
import com.example.pet_project_frontend.data.remote.dto.response.TokenRefreshResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    // POST /api/auth/social – SocialLoginSchema → AuthTokensResponseSchema
    @POST("api/auth/social")
    suspend fun socialLogin(@Body socialLoginRequest: SocialLoginRequest): Response<SocialLoginResponse>

    // POST /api/auth/logout – LogoutRequestSchema → AuthLogoutResponseSchema
    @POST("api/auth/logout")
    suspend fun logout(@Body logoutRequest: LogoutRequest): Response<LogoutResponse>

    // POST /api/auth/token/refresh – EmptyRequestSchema → AuthTokensRefreshResponseSchema
    @POST("api/auth/token/refresh")
    suspend fun refreshToken(
        @Header("Authorization") refreshToken: String,
        @Body body: EmptyRequest
    ): Response<TokenRefreshResponse>

    // DELETE /api/users/me - EmptyRequestSchema -> EmptyResponseSchema
    @DELETE("/api/users/me")
    suspend fun withdraw(@Header("Authorization") accessToken: String): Response<Unit>
}