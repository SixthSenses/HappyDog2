@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pet_project_frontend.presentation.petcare.insights

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pet_project_frontend.core.utils.DateFormatter
import com.example.pet_project_frontend.presentation.petcare.components.charts.HourlyBarChart
import com.example.pet_project_frontend.presentation.petcare.util.aggregatePerHour
import com.example.pet_project_frontend.presentation.petcare.PetCareViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun InsightsScreen(
    petId: String,
    vm: PetCareViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val careState by vm.careRecordsState.collectAsState()

    LaunchedEffect(petId) {
        val (start, end) = DateFormatter.monthRangeUtc()
        vm.getCareRecords(petId = petId, startDate = start, endDate = end, grouped = true, limit = 200)
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("인사이트") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
        })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when (val s = careState) {
                is com.example.pet_project_frontend.presentation.petcare.CareRecordsState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                is com.example.pet_project_frontend.presentation.petcare.CareRecordsState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
                is com.example.pet_project_frontend.presentation.petcare.CareRecordsState.Success -> {
                    val water = s.response.grouped?.get("water").orEmpty()
                    val hours = aggregatePerHour(water)
                    Card { Column(Modifier.padding(16.dp)) {
                        Text("월간 물 섭취 분포")
                        Spacer(Modifier.height(8.dp))
                        HourlyBarChart(values = hours.map { it.toFloat() })
                    }}
                    // 추가 KPI/추세 카드들을 이어서 배치 가능
                }
            }
        }
    }
}
