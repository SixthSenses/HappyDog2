package com.example.pet_project_frontend.presentation.mypage.settings.verification.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.presentation.mypage.settings.verification.VerificationColors

@Composable
fun VerificationFeatureItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)                                   // 42 x 42
                .clip(CircleShape)
                .background(VerificationColors.IconBg),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = VerificationColors.Body,               // #333D4B
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp,                            // 100%
                letterSpacing = (-0.36).sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = VerificationColors.Sub,                // #4E5968
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 15.sp,                            // 100%
                letterSpacing = 0.375.sp
            )
        }
    }
}
