package com.example.pet_project_frontend.presentation.petregistration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pet_project_frontend.core.navigation.Screen // [수정됨] Screen import
import com.example.pet_project_frontend.domain.model.Gender
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetRegistrationScreen(
    navController: NavController,
    viewModel: PetRegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val petName by viewModel.petName.collectAsState()
    val selectedGender by viewModel.selectedGender.collectAsState()
    val selectedBreed by viewModel.selectedBreed.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val weight by viewModel.weight.collectAsState()
    val furColor by viewModel.furColor.collectAsState()
    val healthConcerns by viewModel.healthConcerns.collectAsState()
    val showBreedDialog by viewModel.showBreedDialog.collectAsState()

    // 등록 성공 시 펫케어 화면으로 이동
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            // [수정됨] NavigationRoutes -> Screen.route 로 변경
            navController.navigate(Screen.PetCare.route) {
                popUpTo(Screen.PetRegistration.route) { inclusive = true }
            }
        }
    }

    Scaffold() { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(21.dp, 16.dp),
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                item {
                    Text(
                        text = "회원가입",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3182F6),
                        lineHeight = 1.40.em,
                        letterSpacing = (-0.015).em,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "반려견의 정보를\n알려주세요",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 1.40.em,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                // 이름 입력
                item {
                    OutlinedTextField(
                        value = petName,
                        onValueChange = viewModel::updatePetName,
                        label = { Text("이름 *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = uiState.error?.contains("이름") == true
                    )
                }

                // 성별 선택
                item {
                    Column {
                        Text(
                            text = "성별 *",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedGender == Gender.MALE,
                                onClick = { viewModel.updateGender(Gender.MALE) },
                                label = { Text("수컷") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedGender == Gender.FEMALE,
                                onClick = { viewModel.updateGender(Gender.FEMALE) },
                                label = { Text("암컷") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 품종 선택
                item {
                    OutlinedTextField(
                        value = selectedBreed?.breedName ?: "",
                        onValueChange = { },
                        label = { Text("품종 *") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { viewModel.showBreedDialog() }) {
                                Icon(Icons.Default.Search, contentDescription = "품종 검색")
                            }
                        },
                        isError = uiState.error?.contains("품종") == true
                    )
                }

                // 생년월일 선택
                item {
                    var showDatePicker by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = birthDate?.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")) ?: "",
                        onValueChange = { },
                        label = { Text("생년월일 *") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "날짜 선택")
                            }
                        },
                        isError = uiState.error?.contains("생년월일") == true
                    )

                    if (showDatePicker) {
                        PetDatePickerDialog(
                            onDateSelected = { date ->
                                viewModel.updateBirthDate(date)
                            },
                            onDismiss = { showDatePicker = false }
                        )
                    }
                }

                // 체중 입력
                item {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = viewModel::updateWeight,
                        label = { Text("체중 (kg) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = uiState.error?.contains("체중") == true
                    )
                }

                // 털 색상 (선택)
                item {
                    OutlinedTextField(
                        value = furColor,
                        onValueChange = viewModel::updateFurColor,
                        label = { Text("털 색상 (선택)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // 건강 관심사 (선택)
                item {
                    HealthConcernsSection(
                        healthConcerns = healthConcerns,
                        onAdd = viewModel::addHealthConcern,
                        onRemove = viewModel::removeHealthConcern
                    )
                }

                // 건강 관심사 (선택)
                item {
                    HealthConcernsSection(
                        healthConcerns = healthConcerns,
                        onAdd = viewModel::addHealthConcern,
                        onRemove = viewModel::removeHealthConcern
                    )
                }

                // 건강 관심사 (선택)
                item {
                    HealthConcernsSection(
                        healthConcerns = healthConcerns,
                        onAdd = viewModel::addHealthConcern,
                        onRemove = viewModel::removeHealthConcern
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(92.dp))
                }

                // 에러 메시지
                uiState.error?.let {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = it,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .background(
                            brush = verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0f),
                                    Color.White,
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(78.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { viewModel.registerPet() },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .padding(horizontal = 21.dp),
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3182F6),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "완료",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                // 품종 선택 다이얼로그
                if (showBreedDialog) {
                    BreedSelectionDialog(
                        viewModel = viewModel,
                        onDismiss = { viewModel.hideBreedDialog() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HealthConcernsSection(
    healthConcerns: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    Column {
        Text(
            text = "건강 관심사 (선택)",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("예: 알러지, 관절염") },
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onAdd(inputText)
                        inputText = ""
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "추가")
            }
        }

        if (healthConcerns.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                healthConcerns.forEach { concern ->
                    InputChip(
                        selected = false,
                        onClick = { /* 선택 기능 없음 */ },
                        label = { Text(concern) },
                        trailingIcon = {
                            IconButton(
                                onClick = { onRemove(concern) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "삭제")
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                    onDismiss()
                }
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedSelectionDialog(
    viewModel: PetRegistrationViewModel,
    onDismiss: () -> Unit
) {
    val searchQuery by viewModel.breedSearchQuery.collectAsState()
    val searchResults by viewModel.breedSearchResults.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.8f)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "품종 선택",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::updateBreedSearchQuery,
                    label = { Text("품종 검색") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "검색")
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(searchResults) { breed ->
                        Card(
                            onClick = { viewModel.selectBreed(breed) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = breed.breedName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "평균 수명: ${breed.lifeExpectancy}년",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("취소")
                }
            }
        }
    }
}