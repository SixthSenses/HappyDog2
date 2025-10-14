package com.example.pet_project_frontend.presentation.eye_health

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
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet_project_frontend.R
import com.example.pet_project_frontend.core.theme.MyPageColors
import com.example.pet_project_frontend.domain.model.EyeAnalysis

// 확률에 따른 색상 정의
private data class RiskColorPair(val text: Color, val background: Color)

private object RiskColors {
    // 낮은 위험도 (0-40%)
    val LowRisk = RiskColorPair(
        text = MyPageColors.Green700,     // 초록색 텍스트
        background = MyPageColors.Green50 // 연한 초록색 배경
    )
    
    // 중간 위험도 (41-70%)
    val MediumRisk = RiskColorPair(
        text = MyPageColors.Orange700,    // 주황색 텍스트
        background = MyPageColors.Yellow50 // 연한 노란색 배경
    )
    
    // 높은 위험도 (71-100%)
    val HighRisk = RiskColorPair(
        text = MyPageColors.Red700,      // 빨간색 텍스트
        background = MyPageColors.Red50  // 연한 빨간색 배경
    )
}

// 질환 결과 데이터 클래스 (API 응답에서 변환용)
data class DiseaseResultItem(
    val name: String,
    val percentage: Int
)

// 질환 정보 데이터
data class DiseaseInfo(
    val name: String,
    val description: String,
    val symptoms: List<String>
)

// 질환 정보 매핑
private val diseaseInfoMap = mapOf(
    "안검내반증" to DiseaseInfo(
        name = "안검내반증",
        description = "속눈썹이 눈을 찔러 불편함을 유발하는 질환이에요.\n다음과 같은 증상이 있다면 안검내반증일 수 있어요.",
        symptoms = listOf(
            "눈 깜빡임이 많아져요",
            "눈물이나 눈곱이 많아요",
            "눈을 자꾸 비비거나 찡그려요"
        )
    ),
    "결막염" to DiseaseInfo(
        name = "결막염",
        description = "결막에 염증이 생겨 충혈되는 질환이에요.\n다음과 같은 증상이 있다면 결막염일 수 있어요.",
        symptoms = listOf(
            "충혈되고 부어보여요",
            "눈곱이 많고 끈적해요",
            "눈을 가려워해요"
        )
    ),
    "백내장" to DiseaseInfo(
        name = "백내장",
        description = "수정체가 혼탁해 시야가 흐려지는 질환이에요.\n다음과 같은 증상이 있다면 백내장일 수 있어요.",
        symptoms = listOf(
            "중심부가 하얗게 보여요",
            "물체를 잘 못알아봐요",
            "시야가 점점 흐려져요"
        )
    ),
    "궤양성 각막질환" to DiseaseInfo(
        name = "궤양성 각막질환",
        description = "각막 궤양으로 심한 통증이 생기는 질환이에요.\n다음과 같은 증상이 있다면 궤양성 각막질환일 수 있어요.",
        symptoms = listOf(
            "눈에 하얀 점이 보여요",
            "한쪽 눈을 자주 감아요",
            "통증으로 눈을 잘 못 떠요"
        )
    )
)

// 확률에 따른 색상 반환 함수
private fun getRiskColors(percentage: Int): RiskColorPair {
    return when {
        percentage <= 40 -> RiskColors.LowRisk
        percentage <= 70 -> RiskColors.MediumRisk
        else -> RiskColors.HighRisk
    }
}

/**
 * 안구 분석 결과 화면 컴포넌트 (새로운 디자인)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EyeAnalysisResultScreen(
    analysis: EyeAnalysis,
    onBackClick: () -> Unit,
    onRetakeClick: () -> Unit
) {
    // 바텀시트 상태
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedDisease by remember { mutableStateOf<String?>(null) }
    
    // API 응답에서 질환 결과 데이터 변환 - 확률 높은 순으로 정렬
    val diseaseResults = analysis.predictions.map { prediction ->
        DiseaseResultItem(
            name = prediction.diseaseName,
            percentage = prediction.probabilityPercent
        )
    }.sortedByDescending { it.percentage }

    // 1. 가장 높은 확률 값을 찾습니다. 예측값이 하나도 없으면 0으로 간주합니다.
    val maxProbability = diseaseResults.maxByOrNull { it.percentage }?.percentage ?: 0

    // 2. 가장 높은 확률이 40% 미만이면 '정상'으로 간주합니다.
    val isConsideredNormal = maxProbability < 40


    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // 뒤로가기 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = MyPageColors.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // "검사 결과" 파란 텍스트
            Text(
                text = "검사 결과",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = MyPageColors.Blue500,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 상태에 따른 메시지
            if (isConsideredNormal) {
                Text(
                    text = "특별한 문제가 발견되지 않았어요",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MyPageColors.Grey900,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "각 질환별 확률을 확인해보세요",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MyPageColors.Grey700,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            } else {
                Text(
                    text = "일부 질환에 걸렸을\n가능성이 있어요",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MyPageColors.Grey900,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "전문병원에서 정밀 검사를 받아보는걸 추천해요",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MyPageColors.Grey700,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 반려견 안구 사진
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MyPageColors.Grey100)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // API에서 받은 실제 분석 이미지 표시
                    if (analysis.imageUrl != null) {
                        AsyncImage(
                            model = analysis.imageUrl,
                            contentDescription = "분석한 안구 이미지",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // 이미지 URL이 없는 경우 기본 이미지 표시
                        Image(
                            painter = painterResource(id = R.drawable.eye_disease_image),
                            contentDescription = "분석한 안구 이미지",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 질환별 퍼센트 결과
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                diseaseResults.forEach { disease ->
                    DiseaseResultRow(
                        disease = disease,
                        onDiseaseClick = { diseaseName ->
                            selectedDisease = diseaseName
                            showBottomSheet = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 하단 확인 버튼
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MyPageColors.Blue500
                )
            ) {
                Text(
                    text = "확인",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // 질환 정보 바텀시트
    if (showBottomSheet && selectedDisease != null) {
        val diseaseInfo = diseaseInfoMap[selectedDisease]
        if (diseaseInfo != null) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showBottomSheet = false
                    selectedDisease = null
                },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                DiseaseInfoBottomSheet(diseaseInfo = diseaseInfo)
            }
        }
    }
}

/**
 * 질환별 결과 행 컴포넌트 (확률에 따른 색상 적용)
 */
@Composable
private fun DiseaseResultRow(
    disease: DiseaseResultItem,
    onDiseaseClick: (String) -> Unit
) {
    // 확률에 따른 색상 계산
    val riskColors = getRiskColors(disease.percentage)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                onDiseaseClick(disease.name)
            }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽 질환명
        Text(
            text = disease.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MyPageColors.Grey800
        )
        
        // 오른쪽 퍼센트 (확률에 따른 색상 배경)
        Box(
            modifier = Modifier
                .background(
                    color = riskColors.background,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "${disease.percentage}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = riskColors.text
            )
        }
    }
}

/**
 * 질환 정보 바텀시트 컴포넌트
 */
@Composable
private fun DiseaseInfoBottomSheet(diseaseInfo: DiseaseInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .padding(bottom = 32.dp) // 하단 여백 추가
    ) {
        // 질환명
        Text(
            text = diseaseInfo.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MyPageColors.Grey900
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 설명
        Text(
            text = diseaseInfo.description,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = MyPageColors.Grey700
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 증상 목록
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            diseaseInfo.symptoms.forEach { symptom ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // 점 표시
                    Text(
                        text = "• ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MyPageColors.Grey600
                    )
                    // 증상 텍스트
                    Text(
                        text = symptom,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MyPageColors.Grey600,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}