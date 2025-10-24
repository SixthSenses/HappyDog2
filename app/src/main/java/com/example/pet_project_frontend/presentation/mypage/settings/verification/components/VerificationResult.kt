package com.example.pet_project_frontend.presentation.mypage.settings.verification.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.aspectRatio
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.PretendardFont

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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 72.dp, bottom = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 30.sp,
                    lineHeight = 38.sp,
                    color = Color(0xFF333D4B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = subtitle,
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    color = Color(0xFF6B7684),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(56.dp))

                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .aspectRatio(244.9529f / 314f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .background(
                        color = Color(0xFFFD8800),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(onClick = onConfirm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "확인",
                    fontFamily = PretendardFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    color = Color.White
                )
            }
        }
    }
}
