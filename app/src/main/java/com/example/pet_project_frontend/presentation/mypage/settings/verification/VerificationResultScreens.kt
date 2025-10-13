package com.example.pet_project_frontend.presentation.mypage.settings.verification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.R

@Composable
fun VerificationResultSuccessScreen(
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF9E5), Color.White)
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(172.dp))

            Text(
                text = "신원 인증 성공!",
                color = Color(0xFF333D4B),
                textAlign = TextAlign.Center,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 39.sp,
                letterSpacing = (-0.75).sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "멍스타그램에서\n인증 배지를 받았어요.",
                color = Color(0xFF6B7684),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 23.4.sp,
                letterSpacing = (-0.18).sp
            )

            Spacer(Modifier.height(60.dp))

            Image(
                painter = painterResource(id = R.drawable.dog),
                contentDescription = "인증 완료 일러스트",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 84.dp)
                    .height(314.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(184.dp))

            Button(
                onClick = onClose,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFD8800)),
                contentPadding = PaddingValues(horizontal = 169.dp, vertical = 20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 21.dp)
                    .height(58.dp)
            ) {
                Text(
                    text = "확인",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
