package com.example.pet_project_frontend.presentation.mypage.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme

/**
 * 화면 하단 제스처 바 (handle만 보이는 형태)
 *
 * 디자인 스펙:
 * - 컨테이너 높이: 24dp
 * - 핸들: 108x4dp, radius 12dp, 색상 #1B1B1B
 * - 배경: #FFFFFF
 */
@Composable
fun GestureBar(
    modifier: Modifier = Modifier,
    containerHeight: Dp = 24.dp,
    handleWidth: Dp = 108.dp,
    handleHeight: Dp = 4.dp,
    backgroundColor: Color = Color.White,
    handleColor: Color = Color(0xFF1B1B1B)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(containerHeight)
            .background(backgroundColor)
            // 네비게이션 바 영역을 침범하지 않도록 패딩
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(handleWidth)
                .height(handleHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(handleColor)
        )
    }
}

/**
 * 키보드(IME)가 올라와도 항상 화면 맨 아래 고정시키고 싶다면
 * root Box에 align(Alignment.BottomCenter)로 배치하고
 * GestureBar 자체에는 imePadding()을 주지 마세요.
 *
 * 반대로, 키보드 위로 끌어올리고 싶다면 아래처럼 감싸서 사용:
 *
 * Box(Modifier.imePadding()) {
 *   GestureBar(Modifier.align(Alignment.BottomCenter))
 * }
 */

