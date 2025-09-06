package com.example.pet_project_frontend.presentation.petcare.util

import com.example.pet_project_frontend.data.remote.dto.response.PetCareSettings

/**
 * 물 컵 프리셋 생성 규칙
 * base = waterIncrementAmount
 * presets = [base, 2×base, 3×base, 4×base] 중 waterBowlCapacity 이하만 사용
 */
fun deriveWaterCupPresets(settings: PetCareSettings?): List<Int> {
    val base = settings?.waterIncrementAmount ?: return emptyList()
    val cap = settings.waterBowlCapacity
    return listOf(1, 2, 3, 4)
        .map { it * base }
        .filter { it in 1..cap }
        .distinct()
}
