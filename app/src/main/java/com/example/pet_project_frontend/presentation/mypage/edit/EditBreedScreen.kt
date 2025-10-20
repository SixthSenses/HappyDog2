package com.example.pet_project_frontend.presentation.mypage.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.core.theme.PretendardFont
import com.example.pet_project_frontend.presentation.mypage.main.MyPageViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditBreedScreen(
    onDismiss: () -> Unit,
    viewModel: MyPageViewModel = hiltViewModel(),
    breedViewModel: BreedSelectionViewModel = hiltViewModel()
) {
    val breedSearchState by breedViewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        breedViewModel.loadAllBreeds()
    }
    
    LaunchedEffect(searchQuery) {
        coroutineScope.launch {
            delay(300) // 디바운스
            if (searchQuery.isNotBlank()) {
                breedViewModel.searchBreeds(searchQuery)
            } else {
                breedViewModel.loadAllBreeds()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 검은색 배경
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        onClick = { onDismiss() },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )

            // 다이얼로그 박스
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .heightIn(max = 600.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(26.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // 제목
                    Text(
                        text = "견종 선택",
                        fontFamily = PretendardFont,
                        fontWeight = FontWeight(600),
                        fontSize = 21.sp,
                        color = Color(0xFF333D4B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 검색 입력 필드
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "견종을 검색하세요",
                                fontFamily = PretendardFont,
                                fontWeight = FontWeight(400),
                                fontSize = 16.sp,
                                color = Color(0xFF9EA4AA)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "검색",
                                tint = Color(0xFF9EA4AA)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3182F6),
                            unfocusedBorderColor = Color(0xFFE5E8EB),
                            focusedTextColor = Color(0xFF333D4B),
                            unfocusedTextColor = Color(0xFF333D4B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = PretendardFont,
                            fontWeight = FontWeight(500),
                            fontSize = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 품종 리스트
                    when {
                        breedSearchState.isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF3182F6))
                            }
                        }
                        breedSearchState.error != null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = breedSearchState.error ?: "오류가 발생했습니다",
                                    fontFamily = PretendardFont,
                                    fontSize = 14.sp,
                                    color = Color(0xFFEC4453)
                                )
                            }
                        }
                        breedSearchState.breeds.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "검색 결과가 없습니다",
                                    fontFamily = PretendardFont,
                                    fontSize = 14.sp,
                                    color = Color(0xFF9EA4AA)
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 350.dp)
                            ) {
                                items(breedSearchState.breeds) { breed ->
                                    BreedItem(
                                        breedName = breed.breedName,
                                        onClick = {
                                            viewModel.updateBreed(breed.breedName)
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 취소 버튼
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(
                                color = Color(0xFFF3F4F6),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "취소",
                            fontFamily = PretendardFont,
                            fontWeight = FontWeight(600),
                            fontSize = 16.sp,
                            color = Color(0xFF4E5968)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BreedItem(
    breedName: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = breedName,
            fontFamily = PretendardFont,
            fontWeight = FontWeight(500),
            fontSize = 16.sp,
            color = Color(0xFF333D4B)
        )
    }
}
