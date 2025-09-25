package com.example.pet_project_frontend.domain.model

/**
 * 안구 분석 결과 Domain Model (업데이트된 스펙)
 * 비즈니스 로직과 UI 표현을 담당
 */
data class EyeAnalysis(
    val analysisId: String,
    val diseaseName: String,
    val probability: Float,
    val probabilityPercent: Int,
    val imageUrl: String?,
    val predictions: List<EyeAnalysisPrediction>,
    val isNormal: Boolean,
    val riskLevel: RiskLevel
) {
    /**
     * 위험도 분류 Enum
     * isNormal과 확률값을 기반으로 비즈니스 로직 적용
     */
    enum class RiskLevel {
        NORMAL, LOW, MEDIUM, HIGH;
        
        companion object {
            /**
             * 정상 여부와 확률값을 위험도로 변환하는 비즈니스 로직
             */
            fun fromAnalysis(isNormal: Boolean, probability: Float): RiskLevel = when {
                isNormal -> NORMAL
                probability >= 0.7f -> HIGH
                probability >= 0.4f -> MEDIUM 
                else -> LOW
            }
        }
    }
    
    /**
     * 사용자에게 표시할 위험도 텍스트
     */
    val riskLevelText: String
        get() = when (riskLevel) {
            RiskLevel.NORMAL -> "정상"
            RiskLevel.LOW -> "낮음"
            RiskLevel.MEDIUM -> "보통" 
            RiskLevel.HIGH -> "높음"
        }
    
    /**
     * 위험도에 따른 색상 코드 (UI용)
     */
    val riskColor: String
        get() = when (riskLevel) {
            RiskLevel.NORMAL -> "#4CAF50"  // 초록색
            RiskLevel.LOW -> "#FFC107"     // 노란색
            RiskLevel.MEDIUM -> "#FF9800"  // 주황색
            RiskLevel.HIGH -> "#F44336"    // 빨간색
        }
}