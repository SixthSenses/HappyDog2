package com.example.pet_project_frontend.presentation.breed_guide

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors
import com.example.pet_project_frontend.domain.model.BreedGuideLocal

/**
 * 견종 가이드북 리스트 화면
 * 로컬 데이터로 26개 견종을 리스트로 표시
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedGuideListScreen(
    onBackClick: () -> Unit = {},
    onBreedClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: BreedGuideViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    
    // 화면 진입 시 데이터 로드
    LaunchedEffect(Unit) {
        viewModel.loadBreeds()
    }
    
    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // 뒤로가기 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = MyPageColors.Grey900
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 견종 백과사전 아이콘
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.breed_guide_icon),
                    contentDescription = "견종 백과사전 아이콘",
                    modifier = Modifier.size(width = 69.dp, height = 61.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 견종 백과사전 타이틀
            Text(
                text = "견종 백과사전",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = MyPageColors.Grey900
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 검색바
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "견종 검색",
                        fontSize = 17.sp,
                        color = MyPageColors.Grey500
                    )
                },
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = "검색 아이콘",
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearSearch() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "검색어 지우기",
                                tint = MyPageColors.Grey500
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MyPageColors.Grey100,
                    unfocusedContainerColor = MyPageColors.Grey100,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MyPageColors.Grey900,
                    unfocusedTextColor = MyPageColors.Grey900
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 콘텐츠 영역
            when (val state = uiState) {
                is BreedGuideUiState.Loading -> {
                    println("BreedGuideScreen: UI State = Loading")
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MyPageColors.Blue500
                        )
                    }
                }
            
                is BreedGuideUiState.Success -> {
                    println("BreedGuideScreen: UI State = Success with ${state.breeds.size} breeds")
                    
                    if (state.breeds.isEmpty() && searchQuery.isNotEmpty()) {
                        // 검색 결과가 없는 경우
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "검색 결과가 없습니다",
                                    fontSize = 16.sp,
                                    color = MyPageColors.Grey600
                                )
                                Text(
                                    text = "'$searchQuery'에 대한 견종을 찾을 수 없습니다",
                                    fontSize = 14.sp,
                                    color = MyPageColors.Grey500
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.breeds,
                                key = { it.breedName }
                            ) { breed ->
                                BreedListItem(
                                    breed = breed,
                                    onClick = { onBreedClick(breed.breedName) }
                                )
                            }
                            
                            // 하단 여백
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            
                is BreedGuideUiState.Error -> {
                    println("BreedGuideScreen: UI State = Error - ${state.message}")
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "견종 정보를 불러올 수 없습니다",
                                fontSize = 16.sp,
                                color = MyPageColors.Grey600
                            )
                            
                            Button(
                                onClick = { viewModel.loadBreeds() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MyPageColors.Blue500
                                )
                            ) {
                                Text(
                                    text = "다시 시도",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 견종 리스트 아이템 컴포넌트
 */
@Composable
private fun BreedListItem(
    breed: BreedGuideLocal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = breed.breedName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = MyPageColors.Grey700,
            modifier = Modifier.weight(1f)
        )
        
        Image(
            painter = painterResource(id = R.drawable.arrow),
            contentDescription = "상세보기",
            modifier = Modifier.size(20.dp)
        )
    }
}