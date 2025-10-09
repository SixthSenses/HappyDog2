package com.example.pet_project_frontend.presentation.mypage.profile.gender

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pet_project_frontend.core.components.TopBar
import com.example.pet_project_frontend.presentation.mypage.common.GestureBar
import com.example.pet_project_frontend.presentation.mypage.common.UiColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderSelectScreen(
    onBack: () -> Unit,
    onSaved: (GenderUi) -> Unit,
    viewModel: GenderSelectViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopBar(title = {}, onNavigateBack = onBack) },
        bottomBar = {
            Column(Modifier.fillMaxWidth()) {
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
                GestureBar()
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
                style = MaterialTheme.typography.headlineSmall,
                color = UiColors.TitleText,
                fontWeight = FontWeight.SemiBold,
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
            color = UiColors.BodyText,
            style = MaterialTheme.typography.titleMedium
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
