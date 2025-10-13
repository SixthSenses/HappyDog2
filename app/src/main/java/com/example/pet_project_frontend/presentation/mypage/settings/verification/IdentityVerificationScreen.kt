@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pet_project_frontend.presentation.mypage.settings.verification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.components.TopBar
import com.example.pet_project_frontend.presentation.mypage.settings.verification.components.VerificationFeatureItem

@Composable
fun VerificationMainScreen(
    onBack: () -> Unit = {},
    onVerifyClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopBar(
                title = "",
                onNavigateBack = onBack
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp, bottom = 16.dp)
        ) {
            Text(
                text = "멍스타그램에서\n실제 견주임을 증명하세요",
                color = Color(0xFF191F28),
                fontSize = 26.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                fontWeight = FontWeight(600),
                lineHeight = 36.4.sp,
                letterSpacing = (-0.65).sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Image(
                painter = painterResource(id = R.drawable.verification),
                contentDescription = "본인 인증 안내 이미지",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            VerificationFeatureItem(
                iconRes = R.drawable.noseprint,
                title = "반려견의 비문을 이용해요",
                subtitle = "간단하면서 빠르게 진행돼요"
            )

            Spacer(modifier = Modifier.height(30.dp))

            VerificationFeatureItem(
                iconRes = R.drawable.license,
                title = "견주 인증 배지를 받을 수 있어요",
                subtitle = "멍스타그램 프로필에서 확인할 수 있어요"
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onVerifyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = ButtonDefaults.shape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3182F6)),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                Text(
                    text = "신원 인증하고 배지 받기",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                    fontWeight = FontWeight(600),
                )
            }
        }
    }
}
