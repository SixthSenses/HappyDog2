package com.example.pet_project_frontend.domain.usecase.breed

import com.example.pet_project_frontend.data.remote.dto.response.BreedResponse
import com.example.pet_project_frontend.domain.repository.BreedRepository
import javax.inject.Inject

/**
 * 가이드북이 있는 견종만 조회하는 UseCase
 * 백엔드에서 26개 품종에 대한 가이드북만 제공하므로,
 * 해당 품종들만 필터링하여 반환
 */
class GetGuidebookBreedsUseCase @Inject constructor(
    private val breedRepository: BreedRepository
) {
    // 가이드북이 있는 26개 품종 목록
    private val guidebookBreedNames = setOf(
        "말티즈", "푸들 (스탠더드)", "푸들 (미니어처)", "푸들 (토이)", 
        "시추", "비숑 프리제", "포메라니안", "치와와", "요크셔 테리어", 
        "닥스훈트", "골든 리트리버", "래브라도 리트리버", "보더 콜리", 
        "저먼 스피츠", "웰시 코기", "퍼그", "재패니즈 스피츠", "복서",
        "프렌치 불도그", "진돗개", "허스키", "말티푸", "시바 이누",
        "코커 스패니얼", "러셀 테리어", "미니어처 슈나우저"
    )
    
    suspend operator fun invoke(): List<BreedResponse> {
        return try {
            println("GetGuidebookBreedsUseCase: Starting to load breeds")
            
            breedRepository.getAllBreeds(summary = false)
                .fold(
                    onSuccess = { response ->
                        println("GetGuidebookBreedsUseCase: Loaded ${response.breeds.size} total breeds")
                        
                        // 디버깅: 처음 5개 품종 이름 출력
                        response.breeds.take(5).forEach { breed ->
                            println("Available breed: '${breed.breedName}'")
                        }
                        
                        val guidebookBreeds = response.breeds.filter { breed ->
                            val isMatch = breed.breedName in guidebookBreedNames
                            if (isMatch) {
                                println("Matched guidebook breed: '${breed.breedName}'")
                            }
                            isMatch
                        }
                        
                        println("GetGuidebookBreedsUseCase: Filtered to ${guidebookBreeds.size} guidebook breeds")
                        guidebookBreeds
                    },
                    onFailure = { error ->
                        println("GetGuidebookBreedsUseCase: Failed to load breeds - ${error.message}")
                        emptyList()
                    }
                )
        } catch (e: Exception) {
            println("GetGuidebookBreedsUseCase: Exception - ${e.message}")
            emptyList()
        }
    }
}