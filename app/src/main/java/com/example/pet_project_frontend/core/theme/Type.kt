package com.example.pet_project_frontend.core.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.pet_project_frontend.R

/**
 * Pretendard 폰트 패밀리
 * 
 * 사용법:
 * Text(
 *     text = "안녕하세요",
 *     fontFamily = PretendardFont,
 *     fontWeight = FontWeight(400) // 또는 500, 600
 * )
 */
val PretendardFont = FontFamily(
    Font(R.font.pretendard_400, FontWeight.W400),   // 400 (Regular)
    Font(R.font.pretendard_500, FontWeight.W500),    // 500 (Medium)
    Font(R.font.pretendard_600, FontWeight.W600)   // 600 (SemiBold)
)

/**
 * FontWeight 확장 함수 - 숫자로 직접 사용
 * 
 * 사용 예시:
 * fontWeight = 400.toFontWeight()
 * fontWeight = 500.toFontWeight()
 * fontWeight = 600.toFontWeight()
 */
fun Int.toFontWeight(): FontWeight = when (this) {
    100 -> FontWeight.W100
    200 -> FontWeight.W200
    300 -> FontWeight.W300
    400 -> FontWeight.W400
    500 -> FontWeight.W500
    600 -> FontWeight.W600
    700 -> FontWeight.W700
    800 -> FontWeight.W800
    900 -> FontWeight.W900
    else -> FontWeight.Normal
}
