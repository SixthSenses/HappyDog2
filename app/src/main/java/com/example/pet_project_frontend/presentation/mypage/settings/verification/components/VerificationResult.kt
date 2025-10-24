package com.example.pet_project_frontend.presentation.mypage.settings.verification.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.R

sealed class VerificationResult(val name: String) {
    data class Success(val matchRate: Int? = null) : VerificationResult("Success")
    object Failed : VerificationResult("Failed")
    object Duplicate : VerificationResult("Duplicate")
    object DetectionFailed : VerificationResult("DetectionFailed")
    object InvalidImage : VerificationResult("InvalidImage")
    object AlreadyVerified : VerificationResult("AlreadyVerified")
    object Unknown : VerificationResult("Unknown")
}

@Composable
fun VerificationResultScreen(
    modifier: Modifier = Modifier,
    title: String = "신원 인증 성공!",
    subtitle: String = "멍스타그램에서\n인증 배지를 받았어요",
    imageResId: Int = R.drawable.dog,
    onConfirm: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = (configuration.screenWidthDp.takeIf { it > 0 } ?: 412)
    val screenHeight = (configuration.screenHeightDp.takeIf { it > 0 } ?: 917)
    val widthRatio = screenWidth / 412f
    val heightRatio = screenHeight / 917f

    val scaledWidth = remember(widthRatio) { { value: Float -> (value * widthRatio).dp } }
    val scaledHeight = remember(heightRatio) { { value: Float -> (value * heightRatio).dp } }
    val pretendard = remember {
        FontFamily(Font(R.font.pretendard))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF9E5),
                        Color.White
                    )
                )
            )
            .padding(horizontal = scaledWidth(21f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = scaledHeight(120f))
                .padding(bottom = scaledHeight(12f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                modifier = Modifier
                    .width(scaledWidth(173f))
                    .height(scaledHeight(39f)),
                style = TextStyle(
                    fontSize = 30.sp,
                    lineHeight = 39.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF333D4B),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(scaledHeight(8f)))

            Text(
                text = subtitle,
                modifier = Modifier
                    .width(scaledWidth(224f))
                    .height(scaledHeight(46f)),
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 23.4.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF6B7684),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(scaledHeight(60f)))

            Image(
                painter = painterResource(id = imageResId),
                contentDescription = "신원 인증 결과 이미지",
                modifier = Modifier
                    .width(scaledWidth(244.9529f))
                    .height(scaledHeight(314f))
            )

            Spacer(modifier = Modifier.height(scaledHeight(184f)))

            Box(
                modifier = Modifier
                    .width(scaledWidth(370f))
                    .height(scaledHeight(58f))
                    .background(
                        color = Color(0xFFFD8800),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(onClick = onConfirm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "확인",
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 18.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(scaledHeight(12f)))
        }
    }
}
