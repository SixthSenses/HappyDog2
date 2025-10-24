package com.example.pet_project_frontend.presentation.eye_health

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.core.theme.MyPageColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EyeHealthLoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MyPageColors.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ThreeDotsLoadingAnimation()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "AI가 반려견의",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MyPageColors.Grey900
        )
        Text(
            text = "안구를 살펴보고 있어요",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MyPageColors.Grey900
        )
    }
}

@Composable
private fun ThreeDotsLoadingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(
        modifier = Modifier.size(width = 80.dp, height = 20.dp)
    ) {
        drawThreeDotsLoading(animationProgress)
    }
}

private fun DrawScope.drawThreeDotsLoading(animationProgress: Float) {
    val centerY = size.height / 2
    val dotRadius = 4.dp.toPx()
    val dotSpacing = 20.dp.toPx()
    val startX = (size.width - 2 * dotSpacing) / 2

    // 3개의 점을 일직선으로 배치
    for (i in 0..2) {
        val x = startX + i * dotSpacing
        
        // 애니메이션 진행에 따라 활성화된 점을 진한 회색으로
        val activeDot = (animationProgress % 3).toInt()
        val color = if (i == activeDot) MyPageColors.Grey500 else MyPageColors.GreyOpacity200
        
        drawCircle(
            color = color,
            radius = dotRadius,
            center = Offset(x, centerY)
        )
    }
}