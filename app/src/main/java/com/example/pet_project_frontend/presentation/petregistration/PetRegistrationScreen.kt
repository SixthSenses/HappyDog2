package com.example.pet_project_frontend.presentation.petregistration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pet_project_frontend.core.navigation.NavigationRoutes
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetRegistrationScreen(
    navController: NavController,
    viewModel: PetRegistrationViewModel = hiltViewModel()
) {
    val registrationSuccess by viewModel.registrationSuccess.collectAsState()
    val petName by viewModel.petName.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val breed by viewModel.breed.collectAsState()
    val birthdate by viewModel.birthdate.collectAsState()
    val currentWeight by viewModel.currentWeight.collectAsState()
    val furColor by viewModel.furColor.collectAsState()
    val healthConcerns by viewModel.healthConcerns.collectAsState()

    // 등록 성공 시 펫 케어 화면으로 이동
    if (registrationSuccess) {
        LaunchedEffect(Unit) {
            navController.navigate(NavigationRoutes.PetCare.route) {
                // 펫 등록 화면까지 백스택에서 제거
                popUpTo(NavigationRoutes.PetRegistration.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("반려동물 정보 등록") }
            ) 
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "반려동물 정보를 입력해주세요.", 
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            // 이름 입력
            OutlinedTextField(
                value = petName,
                onValueChange = { viewModel.petName.value = it },
                label = { Text("이름") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 성별 선택
            Text("성별", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = gender == "MALE",
                        onClick = { viewModel.gender.value = "MALE" }
                    )
                    Text("수컷", modifier = Modifier.padding(start = 8.dp))
                }
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = gender == "FEMALE",
                        onClick = { viewModel.gender.value = "FEMALE" }
                    )
                    Text("암컷", modifier = Modifier.padding(start = 8.dp))
                }
            }
            
            // 품종 입력
            OutlinedTextField(
                value = breed,
                onValueChange = { viewModel.breed.value = it },
                label = { Text("품종") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 생년월일 입력
            OutlinedTextField(
                value = birthdate,
                onValueChange = { viewModel.birthdate.value = it },
                label = { Text("생년월일 (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 현재 체중 입력
            OutlinedTextField(
                value = currentWeight,
                onValueChange = { viewModel.currentWeight.value = it },
                label = { Text("현재 체중 (kg)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 털 색상 입력
            OutlinedTextField(
                value = furColor,
                onValueChange = { viewModel.furColor.value = it },
                label = { Text("털 색상 (선택사항)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 건강 고민사항 입력
            OutlinedTextField(
                value = healthConcerns,
                onValueChange = { viewModel.healthConcerns.value = it },
                label = { Text("건강 고민사항 (선택사항)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 등록 버튼
            Button(
                onClick = { viewModel.onRegisterClicked() },
                modifier = Modifier.fillMaxWidth(),
                enabled = petName.isNotEmpty() && breed.isNotEmpty() && 
                         birthdate.isNotEmpty() && currentWeight.isNotEmpty()
            ) {
                Text("등록하기", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
