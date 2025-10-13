@file:OptIn(ExperimentalMaterial3Api::class)

// 변경의도: 와이어프레임에 맞춰 UI를 재구성하고, 고정 너비/높이 대신 유연한 레이아웃을 사용하여 다양한 해상도에 대응합니다.
package com.example.pet_project_frontend.presentation.mypage.profile.breed

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.components.TopBar

// Re-using existing colors and defining new ones from wireframe
private val TitleColor = Color(0xFF191F28)
private val Gray900 = Color(0xFF333D4B)
private val Gray600 = Color(0xFF6B7684)
private val GrayCheck = Color(0xFFD1D6DA)
private val Blue = Color(0xFF3182F6)

@Composable
fun BreedSelectScreen(
    onBack: () -> Unit,
    onNext: (String, Boolean) -> Unit,
    viewModel: BreedSelectViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopBar(
                title = {},
                onNavigateBack = onBack
            )
        },
        bottomBar = {
            // Use bottomBar for the CTA button to anchor it to the bottom.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 21.dp, vertical = 10.dp)
            ) {
                Button(
                    onClick = { viewModel.confirmSelection(onNext) },
                    // Use fillMaxWidth to make it responsive. Padding is handled by the parent.
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue,
                        contentColor = Color.White,
                        disabledContainerColor = Blue.copy(alpha = 0.4f)
                    ),
                    enabled = ui.selectedBreedName != null && !ui.isSaving
                ) {
                    val buttonText = if (ui.isSaving) "저장 중..." else "다음"
                    Text(buttonText, style = nextButtonTextStyle())
                }
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Text
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "반려견의 견종을 선택해주세요",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                style = TextStyle(
                    fontSize = 22.sp,
                    lineHeight = 30.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_semibold)),
                    fontWeight = FontWeight(600),
                    color = TitleColor,
                )
            )

            // Search Field Container
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center // 수직 중앙 정렬로 여백을 만듭니다.
            ) {
                BreedSearchBar(
                    value = ui.query,
                    onValueChange = viewModel::onQueryChange,
                    onClear = viewModel::clearQuery,
                    modifier = Modifier.fillMaxWidth() // 너비를 채웁니다.
                )
            }

            // Spacer below search bar
            Spacer(modifier = Modifier.height(35.dp))

            // Content Area (List or Empty/Error states)
            Box(
                // Use weight to make this Box fill all remaining vertical space
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                when {
                    ui.error != null -> {
                        Text(
                            text = ui.error ?: "견종 정보를 불러오는 중 문제가 발생했어요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE42A38),
                            modifier = Modifier.padding(top = 32.dp)
                        )
                    }

                    ui.breeds.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(ui.breeds) { breed ->
                                BreedOptionRow(
                                    name = breed,
                                    selected = breed == ui.selectedBreedName,
                                    onClick = { viewModel.onBreedSelected(breed) }
                                )
                                Spacer(Modifier.height(20.dp))
                            }
                        }
                    }

                    ui.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                    }

                    // This is the "Empty Result" state
                    ui.query.isNotBlank() -> {
                        EmptyResult()
                    }

                    else -> {
                        // This is the initial empty state before backend responds
                        // Or if backend returns an empty list for an empty query
                    }
                }
            }
        }
    }
}

@Composable
private fun BreedOptionRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = Gray900,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (selected) Blue else GrayCheck),
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

@Composable
private fun EmptyResult() {
    // Use Box with Center alignment to properly center the content
    // This replaces the huge, hardcoded padding.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.breed_error),
                contentDescription = null,
                // Give the image a reasonable, fixed size that works on most screens
                modifier = Modifier.size(96.dp)
            )
            Text(
                text = "검색 결과가 없어요",
                style = emptyResultStyle(),
                color = Gray600,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun emptyResultStyle(): TextStyle =
    MaterialTheme.typography.bodyMedium.copy(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        letterSpacing = (-0.4).sp
    )

@Composable
private fun nextButtonTextStyle(): TextStyle =
    MaterialTheme.typography.titleMedium.copy(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold
    )
