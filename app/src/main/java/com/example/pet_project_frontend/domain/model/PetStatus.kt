package com.example.pet_project_frontend.domain.model

/**
 * 반려동물 존재 여부 상태
 * 앱 시작 시 로딩 중, 존재 여부를 명확히 구분하기 위한 sealed class
 */
sealed class PetStatus {
    /**
     * 초기화 중 또는 서버에서 펫 상태를 확인 중
     */
    object Loading : PetStatus()
    
    /**
     * 반려동물이 등록되어 있음
     */
    object HasPet : PetStatus()
    
    /**
     * 반려동물이 등록되어 있지 않음
     */
    object NoPet : PetStatus()
    
    /**
     * 로딩이 완료되었는지 여부
     */
    val isLoaded: Boolean
        get() = this is HasPet || this is NoPet
    
    /**
     * 반려동물이 있는지 여부 (로딩 중이면 false)
     */
    val hasPet: Boolean
        get() = this is HasPet
}
