package com.example.pet_project_frontend.domain.usecase.auth

import com.example.pet_project_frontend.data.remote.dto.response.SocialLoginResponse
import com.example.pet_project_frontend.data.remote.dto.response.UserInfo
import com.example.pet_project_frontend.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SocialLoginUseCaseTest {

    private lateinit var useCase: SocialLoginUseCase
    private lateinit var mockRepository: AuthRepository

    @Before
    fun setUp() {
        mockRepository = mock()
        useCase = SocialLoginUseCase(mockRepository)
    }

    @Test
    fun `socialLogin returns success when repository succeeds`() = runTest {
        // Given
        val authCode = "test_auth_code"
        val expectedResponse = SocialLoginResponse(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            userId = "user_id",
            isNewUser = false,
            userInfo = UserInfo(
                userId = "user_id",
                email = "test@example.com",
                nickname = "Test User",
                profileImageUrl = null
            )
        )
        whenever(mockRepository.socialLogin(authCode)).thenReturn(Result.success(expectedResponse))

        // When
        val result = useCase(authCode)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
    }

    @Test
    fun `socialLogin returns failure when repository fails`() = runTest {
        // Given
        val authCode = "test_auth_code"
        val exception = Exception("Network error")
        whenever(mockRepository.socialLogin(authCode)).thenReturn(Result.failure(exception))

        // When
        val result = useCase(authCode)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
