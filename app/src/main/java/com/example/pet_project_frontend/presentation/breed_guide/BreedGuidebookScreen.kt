package com.example.pet_project_frontend.presentation.breed_guide

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors

/**
 * 품종 가이드북 상세 화면
 */
// 견종 이름(한글)과 이미지 파일 이름(영문)을 매핑하는 Map (Composable 함수 바깥에 추가)
private val breedImageMap = mapOf(
    "말티즈" to "maltese",
    "푸들 (스탠더드)" to "poodle_standard",
    "푸들 (미니어처)" to "poodle_miniature",
    "푸들 (토이)" to "poodle_toy",
    "시추" to "shih_tzu",
    "포메라니안" to "pomeranian",
    "치와와" to "chihuahua",
    "요크셔 테리어" to "yorkshire_terrier",
    "닥스훈트" to "dachshund",
    "골든 리트리버" to "golden_retriever",
    "래브라도 리트리버" to "labrador_retriever",
    "보더 콜리" to "border_collie",
    "저먼 스피츠" to "german_spitz",
    "웰시 코기" to "welsh_corgi",
    "퍼그" to "pug",
    "재패니즈 스피츠" to "japanese_spitz",
    "복서" to "boxer",
    "프렌치 불도그" to "french_bulldog",
    "진돗개" to "jindo_dog",
    "허스키" to "husky",
    "시바 이누" to "shiba_inu",
    "코커 스패니얼" to "cocker_spaniel",
    "러셀 테리어" to "russell_terrier",
    "미니어처 슈나우저" to "miniature_schnauzer",
    "비숑 프리제" to "bichon",
    "비글" to "beagle"
)
@Composable
fun BreedGuidebookScreen(
    breedName: String,
    onBackClick: () -> Unit,
    viewModel: BreedGuidebookViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(breedName) {
        viewModel.loadGuidebook(breedName)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 상단 뒤로가기 버튼
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 견종 백과사전 타이틀
        Text(
            text = "견종 백과사전",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = MyPageColors.Blue500,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 품종명
        Text(
            text = breedName,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = MyPageColors.Grey900,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        // 사진
        val context = LocalContext.current
        // 1. Map에서 한글 breedName으로 영문 파일 이름을 찾는다. 없으면 breedName 그대로 사용(대비용)
        val imageResName = breedImageMap[breedName] ?: breedName.lowercase()

        val imageResId = remember(imageResName) {
            context.resources.getIdentifier(imageResName, "drawable", context.packageName)
        }

        Image(
            painter = if (imageResId != 0) painterResource(id = imageResId) else painterResource(id = R.drawable.ic_launcher_background), // 기본 이미지
            contentDescription = "$breedName 이미지",
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))
        val currentState = uiState
        when (currentState) {
            is BreedGuidebookUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MyPageColors.Blue500
                    )
                }
            }
            
            is BreedGuidebookUiState.Success -> {
                val guidebook = currentState.guidebook
                
                // 기본 정보 박스
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MyPageColors.Grey100,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoRow("원산지", guidebook.basicInfo.origin)
                        InfoRow("체고", guidebook.basicInfo.height)
                        InfoRow("체중", guidebook.basicInfo.weight)
                        InfoRow("수명", guidebook.basicInfo.lifeSpan)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 성격 특징 박스
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MyPageColors.Grey100,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(start = 13.dp, top = 14.dp, end = 13.dp, bottom = 12.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CharacteristicRow(
                            iconRes = R.drawable.up,
                            text = guidebook.personality.strengths
                        )
                        CharacteristicRow(
                            iconRes = R.drawable.down,
                            text = guidebook.personality.weaknesses
                        )
                        CharacteristicRow(
                            iconRes = R.drawable.info,
                            text = guidebook.personality.traits
                        )
                    }
                }
            }
            
            is BreedGuidebookUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = currentState.message,
                            textAlign = TextAlign.Center,
                            color = MyPageColors.Grey700
                        )
                        
                        Button(
                            onClick = { viewModel.loadGuidebook(breedName) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MyPageColors.Blue500
                            )
                        ) {
                            Text("다시 시도")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 기본 정보 행 컴포넌트
 */
@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    )
     {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MyPageColors.Grey700
        )
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = MyPageColors.Grey900
        )
    }
}

/**
 * 성격 특징 행 컴포넌트
 */
@Composable
private fun CharacteristicRow(
    iconRes: Int,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = MyPageColors.Grey900,
            modifier = Modifier.weight(1f)
        )
    }
}