package com.example.pet_project_frontend.core.theme

// Type.kt (기존 파일에 추가/치환)

// 1) imports
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import com.example.pet_project_frontend.R

// 2) Pretendard 패밀리 (이미 font 리소스가 res/font에 있어야 함)
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular,  FontWeight.Normal),
    Font(R.font.pretendard_medium,   FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold,     FontWeight.Bold),
)

// 3) 확장함수 (15개 스타일에 일괄 적용)
private fun Typography.setDefaultFontFamily(ff: FontFamily) = Typography(
    displayLarge  = displayLarge.copy(fontFamily = ff),
    displayMedium = displayMedium.copy(fontFamily = ff),
    displaySmall  = displaySmall.copy(fontFamily = ff),
    headlineLarge  = headlineLarge.copy(fontFamily = ff),
    headlineMedium = headlineMedium.copy(fontFamily = ff),
    headlineSmall  = headlineSmall.copy(fontFamily = ff),
    titleLarge  = titleLarge.copy(fontFamily = ff),
    titleMedium = titleMedium.copy(fontFamily = ff),
    titleSmall  = titleSmall.copy(fontFamily = ff),
    bodyLarge  = bodyLarge.copy(fontFamily = ff),
    bodyMedium = bodyMedium.copy(fontFamily = ff),
    bodySmall  = bodySmall.copy(fontFamily = ff),
    labelLarge  = labelLarge.copy(fontFamily = ff),
    labelMedium = labelMedium.copy(fontFamily = ff),
    labelSmall  = labelSmall.copy(fontFamily = ff),
)

// 4) 이 줄만 교체 (기존에 있던 Typography 선언부)
val Typography = Typography().setDefaultFontFamily(Pretendard)
