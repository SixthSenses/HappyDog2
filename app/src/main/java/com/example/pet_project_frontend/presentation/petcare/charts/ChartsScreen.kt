@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pet_project_frontend.presentation.petcare.charts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pet_project_frontend.presentation.petcare.PetCareViewModel
import com.example.pet_project_frontend.core.utils.DateFormatter
import com.example.pet_project_frontend.presentation.petcare.components.charts.HourlyBarChart
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    type: String,
    start: String,
    end: String,
    vm: PetCareViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val careState by vm.careRecordsState.collectAsState()
    val petId = remember { vm.activePetId.value }

    LaunchedEffect(type, start, end, petId) {
        val id = vm.activePetId.value ?: return@LaunchedEffect
        vm.getCareRecords(petId = id, startDate = start, endDate = end, recordTypes = listOf(type), grouped = true, limit = 200)
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("차트: $type") }, navigationIcon = {
            IconButton(onClick = onBack) { Text("<") }
        })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when (val s = careState) {
                is com.example.pet_project_frontend.presentation.petcare.CareRecordsState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                is com.example.pet_project_frontend.presentation.petcare.CareRecordsState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
                is com.example.pet_project_frontend.presentation.petcare.CareRecordsState.Success -> {
                    Text("$start ~ $end")
                    val grouped = s.response.grouped ?: emptyMap()
                    val list = grouped[type].orEmpty()
                    if (type == "activity") {
                        // 시간대별 분포 (간단 바차트)
                        val perHour = IntArray(24)
                        list.forEach { rec ->
                            val hourUtc = java.time.Instant.ofEpochMilli(rec.timestamp)
                                .atOffset(java.time.ZoneOffset.UTC).hour
                            val minutes = (rec.data as? Number)?.toInt() ?: 0
                            perHour[hourUtc] += minutes
                        }
                        HourlyBarChart(values = perHour.map { it.toFloat() }, barHeight = 64.dp)
                    } else {
                        // 총합/카운트 요약
                        val total = list.sumOf { (it.data as? Number)?.toLong() ?: 0L }
                        Text("총합: $total")
                        if (list.isEmpty()) {
                            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text("기록 없음")
                            }
                        }
                    }
                }
            }
        }
    }
}
