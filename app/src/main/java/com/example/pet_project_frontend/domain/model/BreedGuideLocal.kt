package com.example.pet_project_frontend.domain.model

/**
 * 로컬 견종 가이드북 데이터
 * API 호출 없이 26개 품종 리스트를 제공
 */
data class BreedGuideLocal(
    val breedName: String
)

/**
 * 가이드북이 제공되는 26개 품종 목록
 */
object BreedGuideData {
    val guidebookBreeds = listOf(
        BreedGuideLocal("말티즈"),
        BreedGuideLocal("푸들 (스탠더드)"),
        BreedGuideLocal("푸들 (미니어처)"),
        BreedGuideLocal("푸들 (토이)"),
        BreedGuideLocal("시추"),
        BreedGuideLocal("비숑 프리제"),
        BreedGuideLocal("포메라니안"),
        BreedGuideLocal("치와와"),
        BreedGuideLocal("요크셔 테리어"),
        BreedGuideLocal("닥스훈트"),
        BreedGuideLocal("골든 리트리버"),
        BreedGuideLocal("래브라도 리트리버"),
        BreedGuideLocal("보더 콜리"),
        BreedGuideLocal("저먼 스피츠"),
        BreedGuideLocal("웰시 코기"),
        BreedGuideLocal("퍼그"),
        BreedGuideLocal("재패니즈 스피츠"),
        BreedGuideLocal("복서"),
        BreedGuideLocal("프렌치 불도그"),
        BreedGuideLocal("진돗개"),
        BreedGuideLocal("허스키"),
        BreedGuideLocal("시바 이누"),
        BreedGuideLocal("코커 스패니얼"),
        BreedGuideLocal("러셀 테리어"),
        BreedGuideLocal("미니어처 슈나우저"),
        BreedGuideLocal("비글")
    )
}
