package com.example.pet_project_frontend.presentation.mypage.profile.gender

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pet_project_frontend.core.components.TopBar
import com.example.pet_project_frontend.presentation.mypage.common.GestureBar
import com.example.pet_project_frontend.presentation.mypage.common.UiColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.LocalTextStyle


/**
 * 마이페이지 - 성별
 * 와이어프레임(412 x 917) 기준 레이아웃/색상 반영
 * - 제목: "반려견의 성별을 선택해주세요"
 * - 옵션: 수컷, 암컷 (우측 원형 체크)
 * - 하단 고정 버튼: "다음", 370x58, radius=16, #3182F6
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderSelectScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: GenderSelectViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopBar(title = {}, onNavigateBack = onBack) }, // 🔹화살표만
        bottomBar = {
            Column(Modifier.fillMaxWidth()) {
                // 동작 버튼
                Button(
                    onClick = { viewModel.onSave(onSaved) },
                    enabled = ui.selected != null && !ui.isSaving,
                    modifier = Modifier
                            .padding(horizontal = 21.dp, vertical = 12.dp)
                            .height(58.dp)
                            .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UiColors.PrimaryBlue,
                    disabledContainerColor = UiColors.PrimaryBlue.copy(alpha = 0.25f)
                                        )
                ) {
                Text(if (ui.isSaving) "저장 중..." else "다음")
                }
                // 제스처 바만 표시
                GestureBar() // presentation/mypage/common/GestureBar.kt
                }
            }
            ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(top = 24.dp, start = 21.dp, end = 21.dp), // 제목 아래 여백
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "반려견의 성별을 선택해주세요",
                style = MaterialTheme.typography.headlineSmall,
                color = UiColors.TitleText,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 10.dp)
                        )
            Spacer(Modifier.height(24.dp))

            // 옵션 영역 (wireframe상 넉넉한 공백 유지)
            GenderOptionRow(
                label = "수컷",
                selected = ui.selected == GenderUi.MALE,
                onClick = { viewModel.onSelect(GenderUi.MALE) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            GenderOptionRow(
                label = "암컷",
                selected = ui.selected == GenderUi.FEMALE,
                onClick = { viewModel.onSelect(GenderUi.FEMALE) }
            )

            if (ui.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(ui.error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/** 우측 원형 체크 아이콘 포함한 한 줄 옵션 */
@Composable
private fun GenderOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // wireframe 가로 여백: 좌우 21dp
            .padding(horizontal = 21.dp, vertical = 12.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = UiColors.BodyText,
            style = MaterialTheme.typography.titleMedium
        )

        // 우측 원형 체크 (선택: #3182F6, 비선택: #D1D6DA)
        val bg = if (selected) UiColors.PrimaryBlue else Color(0xFFD1D6DA)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = if (selected) "선택됨" else "선택 안 됨",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/** 와이어프레임 색상 세트 */
private object GenderColors {
    val PrimaryBlue = Color(0xFF3182F6) // 버튼/선택 체크
    val GrayCheck   = Color(0xFFD1D6DA) // 비선택 체크
    val TitleText   = Color(0xFF191F28)
    val BodyText    = Color(0xFF333D4B)
}
