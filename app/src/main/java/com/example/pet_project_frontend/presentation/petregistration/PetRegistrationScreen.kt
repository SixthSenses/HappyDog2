package com.example.pet_project_frontend.presentation.petregistration

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pet_project_frontend.core.navigation.NavigationRoutes
import com.example.pet_project_frontend.domain.model.Gender
import java.time.LocalDate
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
            navController.navigate(NavigationRoutes.PET_CARE) {
                popUpTo(NavigationRoutes.PET_REGISTRATION) { inclusive = true }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("반려동물 등록") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                        DatePickerDialog(
                            onDateSelected = { date ->
                                viewModel.updateBirthDate(date)
                                showDatePicker = false
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
                
                // 에러 메시지
                if (uiState.error != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = uiState.error,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                // 등록 버튼
                item {
                    Button(
                        onClick = { viewModel.registerPet() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = "등록하기",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("예: 알러지, 관절염") },
                singleLine = true
            )
            
            Button(
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
        
        // 추가된 건강 관심사 표시
        if (healthConcerns.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            healthConcerns.forEach { concern ->
                Chip(
                    onClick = { onRemove(concern) },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                ) {
                    Text(concern)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "삭제",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // 추후 Material3 DatePicker 사용
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("생년월일 선택") },
        text = {
            // DatePicker 구현
            Text("날짜 선택 UI")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(LocalDate.now().minusYears(1))
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
    )
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