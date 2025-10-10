@file:OptIn(ExperimentalMaterial3Api::class)

// 변경의도: 견종 선택 결과를 MyPage에 즉시 반영하면서 향후 서버 연동 시 재활용할 수 있도록 콜백을 확장한다.
package com.example.pet_project_frontend.presentation.mypage.profile.breed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.components.TopBar
import androidx.compose.foundation.layout.size

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp)
                    .background(Color.White)
            ) {
                Button(
                    onClick = { viewModel.confirmSelection(onNext) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .width(370.dp)
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue,
                        contentColor = Color.White
                    ),
                    enabled = ui.selectedBreedName != null && !ui.isSaving
                ) {
                    val buttonText = if (ui.isSaving) "저장 중..." else "선택 완료"
                    Text(buttonText, style = nextButtonTextStyle())
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(inner)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.White)
            ) {
                Text(
                    text = "반려견의 견종을 선택해 주세요",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 24.dp),
                    style = headerStyle(),
                    color = Gray900
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .background(Color.White)
            ) {
                BreedSearchBar(
                    value = ui.query,
                    onValueChange = viewModel::onQueryChange,
                    onClear = viewModel::clearQuery,
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, top = 14.dp)
                        .fillMaxWidth()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
                        ) {
                            items(ui.breeds) { breed ->
                                BreedOptionRow(
                                    name = breed,
                                    selected = breed == ui.selectedBreedName,
                                    onClick = { viewModel.onBreedSelected(breed) }
                                )
                                Spacer(Modifier.height(20.dp))
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }

                    ui.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                    }

                    ui.query.isNotBlank() -> {
                        EmptyResult()
                    }

                    else -> {
                        Text(
                            text = ui.error ?: "등록된 견종이 없어요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600,
                            modifier = Modifier.padding(top = 32.dp)
                        )
                    }
                }

                if (ui.isLoading && ui.breeds.isNotEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp),
                        strokeWidth = 2.dp
                    )
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
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = if (selected) "선택됨" else "선택 안 됨",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun EmptyResult() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 232.dp, start = 176.dp, end = 176.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.breed_error),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(116f / 105f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "검색 결과가 없어요",
            style = emptyResultStyle(),
            color = Gray600,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun headerStyle(): TextStyle =
    MaterialTheme.typography.titleLarge.copy(
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp
    )

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

private val Gray900 = Color(0xFF333D4B)
private val Gray600 = Color(0xFF6B7684)
private val GrayCheck = Color(0xFFD1D6DA)
private val Blue = Color(0xFF3182F6)
