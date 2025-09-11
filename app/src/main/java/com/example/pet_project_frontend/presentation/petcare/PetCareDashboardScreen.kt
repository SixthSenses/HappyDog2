package com.example.pet_project_frontend.presentation.petcare

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordsResponse
import com.example.pet_project_frontend.data.remote.dto.response.PetCareSettings
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max

// 상단 스티키 탭 구성: 펫케어 | 안구질환분석 | 건강설문 | 견종백과사전
enum class TopTab(val label: String) { PET_CARE("펫케어"), EYE("안구질환분석"), SURVEY("건강설문"), ENCYCLOPEDIA("견종백과사전") }

@Composable
fun PetCareDashboardScreen(
    petId: String,
    viewModel: PetCareViewModel = hiltViewModel(),
    onSelectTopTab: (TopTab) -> Unit = {},
    onOpenDetail: (recordType: String) -> Unit = {},
    onOpenChart: (recordType: String) -> Unit = {},
    onOpenSettings: (recordType: String) -> Unit = {}
) {
    val careRecordsState by viewModel.careRecordsState.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()

    val today = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }

    LaunchedEffect(petId) {
        viewModel.getCareRecords(petId = petId, date = today, grouped = true)
        viewModel.loadPetCareSettings()
    }

    var selectedTab by remember { mutableStateOf(TopTab.PET_CARE) }
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            TopTab.values().forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab.ordinal == index,
                    onClick = {
                        selectedTab = tab
                        if (tab != TopTab.PET_CARE) onSelectTopTab(tab)
                    },
                    text = { Text(tab.label) }
                )
            }
        }

        when (val state = careRecordsState) {
            is CareRecordsState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is CareRecordsState.Error -> Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
            is CareRecordsState.Success -> {
                val grouped = state.response.grouped ?: emptyMap()
                val settings = (settingsState as? PetCareSettingsState.Success)?.settings

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        WaterCard(
                            grouped = grouped,
                            settings = settings,
                            onPlus = {
                                val inc = settings?.waterIncrementAmount ?: 250
                                viewModel.createCareRecord(
                                    petId = petId,
                                    recordType = "water",
                                    timestamp = System.currentTimeMillis(),
                                    data = inc,
                                    notes = null,
                                    requestId = null
                                )
                            },
                            onOpenChart = { onOpenChart("water") },
                            onOpenSettings = { onOpenSettings("water") },
                            onOpenDetail = { onOpenDetail("water") }
                        )
                    }

                    item {
                        ActivityCard(
                            grouped = grouped,
                            settings = settings,
                            onOpenChart = { onOpenChart("activity") },
                            onOpenDetail = { onOpenDetail("activity") }
                        )
                    }

                    item {
                        MealCard(
                            grouped = grouped,
                            settings = settings,
                            onPlus = {
                                viewModel.createCareRecord(
                                    petId = petId,
                                    recordType = "meal",
                                    timestamp = System.currentTimeMillis(),
                                    data = 1,
                                    notes = null,
                                    requestId = null
                                )
                            },
                            onOpenChart = { onOpenChart("meal") },
                            onOpenSettings = { onOpenSettings("meal") },
                            onOpenDetail = { onOpenDetail("meal") }
                        )
                    }

                    item {
                        WeightBcsCard(
                            grouped = grouped,
                            onOpenChart = { onOpenChart("weight") },
                            onOpenDetail = { onOpenDetail("weight") }
                        )
                    }

                    item {
                        BcsOnlyCard(
                            grouped = grouped,
                            onOpenDetail = { onOpenDetail("bcs") }
                        )
                    }
                }
            }
        }
    }
}

// =============== Cards ===============

@Composable
private fun CardHeader(
    title: String,
    onOpenChart: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onOpenChart != null) {
                IconButton(onClick = onOpenChart) { Icon(Icons.Filled.ShowChart, contentDescription = "차트") }
            }
            if (onOpenSettings != null) {
                IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, contentDescription = "설정") }
            }
        }
    }
}

@Composable
private fun WaterCard(
    grouped: Map<String, List<CareRecordResponse>>,
    settings: PetCareSettings?,
    onPlus: () -> Unit,
    onOpenChart: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDetail: () -> Unit
) {
    val todayMl = remember(grouped) { grouped["water"].orEmpty().sumOf { toLong(it.data).toInt() } }
    val goal = settings?.waterBowlCapacity ?: 2000
    val progress = (todayMl.toFloat() / max(1, goal).toFloat()).coerceIn(0f, 1f)

    Card(onClick = onOpenDetail, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardHeader(title = "물", onOpenChart = onOpenChart, onOpenSettings = onOpenSettings)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CircularProgressIndicator(progress = progress, strokeWidth = 8.dp, modifier = Modifier.size(64.dp))
                Icon(Icons.Outlined.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text(text = "$todayMl ml", style = MaterialTheme.typography.titleLarge)
                    Text(text = "/ $goal ml", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FilledTonalButton(onClick = onPlus) { Text("+${settings?.waterIncrementAmount ?: 250} ml") }
            }
        }
    }
}

@Composable
private fun ActivityCard(
    grouped: Map<String, List<CareRecordResponse>>,
    settings: PetCareSettings?,
    onOpenChart: () -> Unit,
    onOpenDetail: () -> Unit
) {
    val perHour = remember(grouped) { aggregatePerHour(grouped["activity"].orEmpty()) }
    val total = perHour.sum()
    val goal = settings?.goalActivityMinutes ?: 60

    Card(onClick = onOpenDetail, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardHeader(title = "활동량", onOpenChart = onOpenChart)
            Spacer(Modifier.height(8.dp))
            HourlyBarChart(values = perHour.map { it.toFloat() }, barHeight = 48.dp)
            Spacer(Modifier.height(8.dp))
            Text(text = "$total 분 / 목표 ${goal}분", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MealCard(
    grouped: Map<String, List<CareRecordResponse>>,
    settings: PetCareSettings?,
    onPlus: () -> Unit,
    onOpenChart: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDetail: () -> Unit
) {
    val count = remember(grouped) { grouped["meal"].orEmpty().size }
    val goal = settings?.goalMealCount ?: 2
    val filledCount = count.coerceAtMost(goal)

    Card(onClick = onOpenDetail, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardHeader(title = "사료", onOpenChart = onOpenChart, onOpenSettings = onOpenSettings)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(goal) { idx ->
                    val filled = idx < filledCount
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(text = "$count / $goal 회", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                FilledTonalButton(onClick = onPlus) { Text("+1 회") }
            }
        }
    }
}

@Composable
private fun WeightBcsCard(
    grouped: Map<String, List<CareRecordResponse>>,
    onOpenChart: () -> Unit,
    onOpenDetail: () -> Unit
) {
    val latestWeight = remember(grouped) { latestValue(grouped["weight"].orEmpty()) }
    val prevWeight = remember(grouped) { previousValue(grouped["weight"].orEmpty()) }
    val latestBcs = remember(grouped) { latestValue(grouped["bcs"].orEmpty()).toIntOrNull() }

    val delta = latestWeight.toFloatOrNull()?.let { w -> prevWeight.toFloatOrNull()?.let { w - it } }

    Card(onClick = onOpenDetail, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardHeader(title = "몸무게", onOpenChart = onOpenChart)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = if (latestWeight.isNotBlank()) "$latestWeight kg" else "기록 없음", style = MaterialTheme.typography.titleLarge)
                if (delta != null && delta != 0f) {
                    val up = delta > 0
                    Icon(if (up) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown, contentDescription = null, tint = if (up) Color.Red else Color(0xFF2E7D32))
                    Text(text = String.format("%+.1f kg", delta))
                }
                Spacer(Modifier.weight(1f))
                Text(text = "BCS ${latestBcs ?: '-'}")
            }
        }
    }
}

@Composable
private fun BcsOnlyCard(
    grouped: Map<String, List<CareRecordResponse>>,
    onOpenDetail: () -> Unit
) {
    val latestBcs = remember(grouped) { latestValue(grouped["bcs"].orEmpty()).toIntOrNull() }
    Card(onClick = onOpenDetail, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "BCS", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(text = if (latestBcs != null) "$latestBcs 단계" else "기록 없음", style = MaterialTheme.typography.titleLarge)
        }
    }
}

// =============== Small charts ===============

@Composable
private fun HourlyBarChart(values: List<Float>, modifier: Modifier = Modifier, barHeight: Dp = 48.dp) {
    val maxVal = values.maxOrNull() ?: 0f
    val density = LocalDensity.current
    val heightPx = with(density) { barHeight.toPx() }

    // 24시간을 한 줄 바 차트로 단순 표현
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        values.take(24).forEach { v ->
            val ratio = if (maxVal == 0f) 0f else (v / maxVal).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(barHeight)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((barHeight * ratio))
                        .align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

// =============== Helpers ===============

private fun toLong(any: Any?): Long = when (any) {
    is Number -> any.toLong()
    is String -> any.toLongOrNull() ?: 0L
    else -> 0L
}

private fun toFloat(any: Any?): Float = when (any) {
    is Number -> any.toFloat()
    is String -> any.toFloatOrNull() ?: 0f
    else -> 0f
}

private fun latestValue(list: List<CareRecordResponse>): String {
    val sorted = list.sortedBy { it.timestamp }
    return sorted.lastOrNull()?.data?.toString() ?: ""
}

private fun previousValue(list: List<CareRecordResponse>): String {
    val sorted = list.sortedBy { it.timestamp }
    return if (sorted.size >= 2) sorted[sorted.size - 2].data.toString() else ""
}

private fun aggregatePerHour(list: List<CareRecordResponse>): IntArray {
    val hours = IntArray(24)
    list.forEach { r ->
        val tsMs = if (r.timestamp > 10_000_000_000L) r.timestamp else r.timestamp * 1000
        val hour = Instant.ofEpochMilli(tsMs).atZone(ZoneId.systemDefault()).hour
        hours[hour] += toFloat(r.data).toInt()
    }
    return hours
}
