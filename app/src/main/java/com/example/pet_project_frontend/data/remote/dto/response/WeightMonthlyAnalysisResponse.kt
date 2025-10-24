package com.example.pet_project_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * 몸무게 월간 분석 응답
 * GET /api/pet-care/{pet_id}/weight/monthly-analysis
 */
data class WeightMonthlyAnalysisResponse(
    @SerializedName("analysis")
    val analysis: AnalysisData,
    
    @SerializedName("monthly_data")
    val monthlyData: List<MonthlyData>,
    
    @SerializedName("meta")
    val meta: MetaData
)

data class AnalysisData(
    @SerializedName("title")
    val title: String, // "몸무게가 비슷해요" / "몸무게가 늘었어요" / "몸무게가 줄었어요"
    
    @SerializedName("description")
    val description: String, // "6개월 전보다 0.5kg 차이나요"
    
    @SerializedName("current_month_avg")
    val currentMonthAvg: Double?,
    
    @SerializedName("six_months_ago_avg")
    val sixMonthsAgoAvg: Double?,
    
    @SerializedName("difference")
    val difference: Double?
)

data class MonthlyData(
    @SerializedName("year_month")
    val yearMonth: String, // "YYYY-MM" 형식
    
    @SerializedName("label")
    val label: String, // "5월", "6월", ...
    
    @SerializedName("average_weight")
    val averageWeight: Double?,
    
    @SerializedName("record_count")
    val recordCount: Int
)

data class MetaData(
    @SerializedName("reference_date")
    val referenceDate: String, // "YYYY-MM-DD"
    
    @SerializedName("timezone")
    val timezone: String // "Asia/Seoul"
)
