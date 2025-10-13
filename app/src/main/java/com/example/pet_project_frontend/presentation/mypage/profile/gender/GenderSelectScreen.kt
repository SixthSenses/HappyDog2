// 변경의도: 성별 선택 저장 시 MyPage 상태가 즉시 반영되도록 콜백과 UI를 조정한다.
package com.example.pet_project_frontend.presentation.mypage.profile.gender

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.components.TopBar
import com.example.pet_project_frontend.presentation.mypage.common.GestureBar
import com.example.pet_project_frontend.presentation.mypage.common.UiColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderSelectScreen(
    onBack: () -> Unit,
    onSaved: (GenderUi, Boolean) -> Unit,
    viewModel: GenderSelectViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopBar(title = {}, onNavigateBack = onBack) },
        bottomBar = {
            Column(Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        viewModel.onSave(onSaved)
                    },
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
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(top = 24.dp, start = 21.dp, end = 21.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "반려견의 성별을 선택해 주세요",
                fontSize = 24.sp,
                lineHeight = 24.sp,
                color = UiColors.TitleText,
                fontFamily = FontFamily(Font(R.font.pretendard_medium)),
                fontWeight = FontWeight(600),
                modifier = Modifier.padding(start = 10.dp)
            )
            Spacer(Modifier.height(24.dp))

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
                Text(ui.error ?: "", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun GenderOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 21.dp, vertical = 12.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            lineHeight = 18.sp,
            fontFamily = FontFamily(Font(R.font.pretendard_regular)),
            fontWeight = FontWeight(500),
            color = UiColors.BodyText,
        )

        val background = if (selected) UiColors.PrimaryBlue else Color(0xFFD1D6DA)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(background),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "선택됨",
                    tint = Color.White
                )
            }
        }
    }
}
