package com.example.pet_project_frontend.presentation.breed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun BreedSearchComposable(
    selectedBreed: String,
    onBreedSelected: (String) -> Unit,
    viewModel: BreedSearchViewModel = hiltViewModel()
) {
    val searchState by viewModel.searchState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(false) }
    
    // 검색어 변경 시 API 호출
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            delay(300) // 디바운싱
            viewModel.searchBreeds(searchQuery)
            showDropdown = true
        } else {
            showDropdown = false
        }
    }
    
    Column {
        // 검색 입력 필드
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                if (it.isBlank()) {
                    showDropdown = false
                }
            },
            label = { Text("품종 검색") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // 드롭다운 결과
        if (showDropdown && searchState is BreedSearchState.Success) {
            val response = (searchState as BreedSearchState.Success).response
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                LazyColumn {
                    items(response.breeds) { breed ->
                        BreedSearchItem(
                            breedName = breed.breedName,
                            lifeExpectancy = breed.lifeExpectancy,
                            onClick = {
                                onBreedSelected(breed.breedName)
                                searchQuery = breed.breedName
                                showDropdown = false
                            }
                        )
                    }
                }
            }
        }
        
        // 로딩 상태
        if (searchState is BreedSearchState.Loading && showDropdown) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        // 에러 상태
        if (searchState is BreedSearchState.Error && showDropdown) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = (searchState as BreedSearchState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun BreedSearchItem(
    breedName: String,
    lifeExpectancy: Float,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = breedName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "평균 수명: ${lifeExpectancy}년",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
