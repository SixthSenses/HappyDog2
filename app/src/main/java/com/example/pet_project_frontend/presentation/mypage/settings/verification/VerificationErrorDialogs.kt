package com.example.pet_project_frontend.presentation.mypage.settings.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class VerificationGuideError {
    Duplicate,
    DetectionFailed
}

@Composable
fun VerificationDuplicateErrorDialog(onConfirm: () -> Unit) {
    VerificationErrorDialogLayout(
        title = "이미 신원 인증을 했어요.",
        description = "멍스타그램에서 인증 배지를 볼 수 있어요.",
        onConfirm = onConfirm
    )
}

@Composable
fun VerificationUnknownErrorDialog(onConfirm: () -> Unit) {
    VerificationErrorDialogLayout(
        title = "알 수 없는 오류가 발생했어요.",
        description = "잠시 후에 다시 시도해주세요.",
        onConfirm = onConfirm
    )
}

@Composable
fun VerificationGuideDuplicateErrorDialog(onConfirm: () -> Unit) {
    VerificationErrorDialogLayout(
        title = "이미 등록된 비문이에요.",
        description = "등록되지 않은 반려견만 인증할 수 있어요.",
        onConfirm = onConfirm
    )
}

@Composable
fun VerificationGuideDetectionErrorDialog(onConfirm: () -> Unit) {
    VerificationErrorDialogLayout(
        title = "비문을 찾을 수 없어요.",
        description = "이미지가 반려견의 비문을 포함하고 있는지 확인해주세요.",
        onConfirm = onConfirm
    )
}

@Composable
private fun VerificationErrorDialogLayout(
    title: String,
    description: String,
    onConfirm: () -> Unit
) {
    val dialogWidth = 336.dp
    val dialogHeight = 173.dp
    val preferredVerticalPadding = 372.dp

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availablePadding = ((maxHeight - dialogHeight) / 2).coerceAtLeast(0.dp)
        val verticalPadding = if (preferredVerticalPadding < availablePadding) {
            preferredVerticalPadding
        } else {
            availablePadding
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 38.dp, vertical = verticalPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(dialogWidth)
                    .height(dialogHeight)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White)
            ) {
                Spacer(modifier = Modifier.height(27.5.dp))
                Text(
                    text = title,
                    color = Color(0xFF333D4B),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 21.sp,
                    letterSpacing = (-0.21).sp,
                    modifier = Modifier.padding(start = 23.dp, end = 113.dp)
                )
                Spacer(modifier = Modifier.height(13.5.dp))
                Text(
                    text = description,
                    color = Color(0xFF6B7684),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 23.2.sp,
                    letterSpacing = (-0.4).sp,
                    modifier = Modifier.padding(start = 23.dp, end = 62.dp)
                )
                Spacer(modifier = Modifier.height(21.dp))
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE4E7EA),
                        contentColor = Color(0xFF4E5968)
                    ),
                    contentPadding = PaddingValues(horizontal = 135.dp, vertical = 16.dp),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(302.dp)
                        .height(50.dp)
                ) {
                    Text(
                        text = "확인",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
