package com.example.pet_project_frontend.domain.usecase.settings

import com.example.pet_project_frontend.core.common.AppResult
import com.example.pet_project_frontend.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

// 회원 탈퇴 API 호출을 감싸는 유스케이스. 서버 연동 시 예외 처리 로직을 확장한다.
@Singleton
class WithdrawAccountUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        // TODO: 서버 명세 확정 시 추가 검증/로그 처리
        return userRepository.deleteUser()
    }
}
