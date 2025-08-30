package com.example.pet_project_frontend.presentation.petcare

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PetCareDashboardScreen(
    petId: String,
    viewModel: PetCareViewModel = hiltViewModel()
) {
    val careRecordsState by viewModel.careRecordsState.collectAsState()
    val createRecordState by viewModel.createRecordState.collectAsState()
    
    // 오늘 날짜
    val today = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
    
    // 초기 데이터 로드
    LaunchedEffect(petId) {
        viewModel.getCareRecords(
            petId = petId,
            date = today,
            grouped = true
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 제목
        Text(
            text = "오늘의 케어 기록",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 로딩 상태
        if (careRecordsState is CareRecordsState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // 성공 상태
        if (careRecordsState is CareRecordsState.Success) {
            val response = (careRecordsState as CareRecordsState.Success).response
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 체중 기록
                item {
                    CareRecordCard(
                        title = "체중",
                        value = response.records.firstOrNull { it.recordType == "weight" }?.data?.toString() ?: "기록 없음",
                        unit = "kg",
                        onAddRecord = {
                            // 체중 기록 추가 다이얼로그 표시
                        }
                    )
                }
                
                // 물 섭취 기록
                item {
                    CareRecordCard(
                        title = "물 섭취",
                        value = response.records.firstOrNull { it.recordType == "water" }?.data?.toString() ?: "기록 없음",
                        unit = "ml",
                        onAddRecord = {
                            // 물 섭취 기록 추가 다이얼로그 표시
                        }
                    )
                }
                
                // 활동 기록
                item {
                    CareRecordCard(
                        title = "활동",
                        value = response.records.firstOrNull { it.recordType == "activity" }?.data?.toString() ?: "기록 없음",
                        unit = "분",
                        onAddRecord = {
                            // 활동 기록 추가 다이얼로그 표시
                        }
                    )
                }
                
                // 식사 기록
                item {
                    CareRecordCard(
                        title = "식사",
                        value = response.records.firstOrNull { it.recordType == "meal" }?.data?.toString() ?: "기록 없음",
                        unit = "회",
                        onAddRecord = {
                            // 식사 기록 추가 다이얼로그 표시
                        }
                    )
                }
            }
        }
        
        // 에러 상태
        if (careRecordsState is CareRecordsState.Error) {
            Text(
                text = (careRecordsState as CareRecordsState.Error).message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CareRecordCard(
    title: String,
    value: String,
    unit: String,
    onAddRecord: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$value $unit",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Button(
                onClick = onAddRecord,
                modifier = Modifier.height(40.dp)
            ) {
                Text("기록 추가")
            }
        }
    }
}
