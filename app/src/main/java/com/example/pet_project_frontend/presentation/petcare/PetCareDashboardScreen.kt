@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pet_project_frontend.presentation.petcare

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pet_project_frontend.core.utils.DateFormatter
import com.example.pet_project_frontend.presentation.petcare.components.RangeTab
import com.example.pet_project_frontend.presentation.petcare.components.SegmentedControl
import com.example.pet_project_frontend.presentation.petcare.components.PlaceholderCard
import com.example.pet_project_frontend.presentation.petcare.components.cards.ActivityCard
import com.example.pet_project_frontend.presentation.petcare.components.cards.BcsOnlyCard
import com.example.pet_project_frontend.presentation.petcare.components.cards.MealCard
import com.example.pet_project_frontend.presentation.petcare.components.cards.WaterCard
import com.example.pet_project_frontend.presentation.petcare.components.cards.WeightBcsCard
import com.example.pet_project_frontend.presentation.petcare.quickadd.QuickAddSheet
import java.util.UUID
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.pet_project_frontend.presentation.petcare.header.ProfileHeader
import com.example.pet_project_frontend.core.remoteconfig.CardType
import androidx.compose.runtime.getValue
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.ui.platform.LocalContext
import com.example.pet_project_frontend.PetCareApplication

// 상단 스티키 탭은 별도 파일로 분리
// enum class TopTab ... -> see TopTab.kt

@Composable
fun PetCareDashboardScreen(
    petId: String,
    viewModel: PetCareViewModel = hiltViewModel(),
    onSelectTopTab: (TopTab) -> Unit = {},
    onOpenDetail: (recordType: String) -> Unit = {},
    onOpenChart: (recordType: String, start: String, end: String) -> Unit = { _, _, _ -> },
    onOpenSettings: (recordType: String) -> Unit = {}
) {
    val careRecordsState by viewModel.careRecordsState.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()
    val petProfileState by viewModel.petProfileState.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val loadMoreError by viewModel.loadMoreError.collectAsState()
    val typeStates by viewModel.typeStates.collectAsState()
    val activePetId by viewModel.activePetId.collectAsState(initial = null)
    val cardOrder by viewModel.cardOrder.collectAsState()

    val today = remember { DateFormatter.todayUtcDate() }
    val context = LocalContext.current

    // Remote Config 적용 브로드캐스트 수신 -> 카드 순서 새로고침
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == PetCareApplication.RC_APPLIED_ACTION) {
                    viewModel.refreshToggles()
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(PetCareApplication.RC_APPLIED_ACTION))
        onDispose { context.unregisterReceiver(receiver) }
    }

    // 인자로 petId가 비어오면 DataStore의 selected_pet_id로 보완
    LaunchedEffect(petId) {
        viewModel.setActivePetId(petId.ifBlank { null })
        val resolved = viewModel.activePetId.value
        if (!resolved.isNullOrBlank()) {
            viewModel.getCareRecords(petId = resolved, date = today, grouped = true)
            viewModel.loadPetProfile(resolved)
        }
        viewModel.loadPetCareSettings()
    }

    var selectedTab by remember { mutableStateOf(TopTab.PET_CARE) }
    var rangeTab by remember { mutableStateOf(RangeTab.Today) }
    var showQuickAdd by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // 재시도 및 세그먼트 전환에 공통 사용될 조회 함수
    fun fetchFor(tab: RangeTab) {
        val id = activePetId ?: petId
        if (id.isBlank()) return
        when (tab) {
            RangeTab.Today -> viewModel.getCareRecords(petId = id, date = today, grouped = true)
            RangeTab.Week -> {
                val (start, end) = DateFormatter.weekRangeUtc()
                viewModel.getCareRecords(petId = id, startDate = start, endDate = end, grouped = true)
            }
            RangeTab.Month -> {
                val (start, end) = DateFormatter.monthRangeUtc()
                viewModel.getCareRecords(petId = id, startDate = start, endDate = end, grouped = true)
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // 이벤트 수신
    LaunchedEffect(true) {
        viewModel.events.collect { ev ->
            when (ev) {
                is UiEvent.Snack -> snackbarHostState.showSnackbar(ev.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        when (val state = careRecordsState) {
            CareRecordsState.Loading -> {
                Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                }
            }
            is CareRecordsState.Error -> {
                Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(text = "데이터를 불러오지 못했습니다.", color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(Modifier.height(8.dp))
                            Text(text = state.message, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(12.dp))
                            FilledTonalButton(onClick = { fetchFor(rangeTab) }) { Text("다시 시도") }
                        }
                    }
                }
            }
            is CareRecordsState.Success -> {
                val (startRange, endRange) = when (rangeTab) {
                    RangeTab.Today -> DateFormatter.todayUtcDate().let { it to it }
                    RangeTab.Week -> DateFormatter.weekRangeUtc()
                    RangeTab.Month -> DateFormatter.monthRangeUtc()
                }
                val grouped = state.response.grouped ?: emptyMap()
                val settings = (settingsState as? PetCareSettingsState.Success)?.settings
                val id = activePetId ?: petId

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 프로필 헤더 (성공 시)
                    item {
                        when (val p = petProfileState) {
                            is PetProfileState.Success -> ProfileHeader(p.pet)
                            else -> {}
                        }
                    }

                    // 카드 렌더링
                    cardOrder.forEach { t ->
                        when (t) {
                            CardType.water -> item {
                                WaterCard(
                                    grouped = grouped,
                                    typeState = typeStates["water"],
                                    settings = settings,
                                    onPlus = {
                                        val inc = settings?.waterIncrementAmount ?: 250
                                        viewModel.createCareRecord(
                                            petId = id,
                                            recordType = "water",
                                            timestamp = System.currentTimeMillis(),
                                            data = inc,
                                            notes = null,
                                            requestId = UUID.randomUUID().toString()
                                        )
                                    },
                                    onOpenChart = { onOpenChart("water", startRange, endRange) },
                                    onOpenSettings = { onOpenSettings("water") },
                                    onOpenDetail = { onOpenDetail("water") },
                                    onRetry = { viewModel.refreshType(id, "water") }
                                )
                            }
                            CardType.activity -> item {
                                ActivityCard(
                                    grouped = grouped,
                                    typeState = typeStates["activity"],
                                    settings = settings,
                                    onOpenChart = { onOpenChart("activity", startRange, endRange) },
                                    onOpenDetail = { onOpenDetail("activity") },
                                    onRetry = { viewModel.refreshType(id, "activity") }
                                )
                            }
                            CardType.meal -> item {
                                MealCard(
                                    grouped = grouped,
                                    typeState = typeStates["meal"],
                                    settings = settings,
                                    onPlus = {
                                        viewModel.createCareRecord(
                                            petId = id,
                                            recordType = "meal",
                                            timestamp = System.currentTimeMillis(),
                                            data = 1,
                                            notes = null,
                                            requestId = null
                                        )
                                    },
                                    onOpenChart = { onOpenChart("meal", startRange, endRange) },
                                    onOpenSettings = { onOpenSettings("meal") },
                                    onOpenDetail = { onOpenDetail("meal") },
                                    onRetry = { viewModel.refreshType(id, "meal") }
                                )
                            }
                            CardType.weight -> item {
                                WeightBcsCard(
                                    grouped = grouped,
                                    weightTypeState = typeStates["weight"],
                                    bcsTypeState = typeStates["bcs"],
                                    onOpenChart = { onOpenChart("weight", startRange, endRange) },
                                    onOpenDetail = { onOpenDetail("weight") },
                                    onRetryWeight = { viewModel.refreshType(id, "weight") },
                                    onRetryBcs = { viewModel.refreshType(id, "bcs") }
                                )
                            }
                            CardType.bcs -> item {
                                BcsOnlyCard(
                                    grouped = grouped,
                                    typeState = typeStates["bcs"],
                                    onOpenDetail = { onOpenDetail("bcs") },
                                    onRetry = { viewModel.refreshType(id, "bcs") }
                                )
                            }
                        }
                    }

                    // 하단: 로딩 더보기 / 에러 / 트리거
                    item {
                        when {
                            isLoadingMore -> {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                            loadMoreError != null -> {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = loadMoreError ?: "")
                                    Spacer(Modifier.width(8.dp))
                                    FilledTonalButton(onClick = { viewModel.appendCareRecords() }) { Text("재시도") }
                                }
                            }
                            state.response.meta.hasMore -> {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    FilledTonalButton(onClick = { viewModel.appendCareRecords() }) { Text("더 보기") }
                                }
                            }
                        }
                    }
                }

                // FAB + QuickAddSheet
                Box(Modifier.fillMaxSize()) {
                    FloatingActionButton(
                        onClick = { showQuickAdd = true },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                    ) { Text("+") }
                }

                if (showQuickAdd) {
                    ModalBottomSheet(onDismissRequest = { showQuickAdd = false }) {
                        QuickAddSheet(
                            settings = settings,
                            onAddWater = { amount ->
                                viewModel.createCareRecord(
                                    petId = id,
                                    recordType = "water",
                                    timestamp = System.currentTimeMillis(),
                                    data = amount,
                                    requestId = UUID.randomUUID().toString()
                                )
                                showQuickAdd = false
                            },
                            onAddMeal = { count ->
                                viewModel.createCareRecord(
                                    petId = id,
                                    recordType = "meal",
                                    timestamp = System.currentTimeMillis(),
                                    data = count,
                                    requestId = UUID.randomUUID().toString()
                                )
                                showQuickAdd = false
                            },
                            onAddActivity = { minutes ->
                                viewModel.createCareRecord(
                                    petId = id,
                                    recordType = "activity",
                                    timestamp = System.currentTimeMillis(),
                                    data = minutes,
                                    requestId = UUID.randomUUID().toString()
                                )
                                showQuickAdd = false
                            },
                            onAddWeightBcs = { kg, bcs ->
                                kg?.let {
                                    viewModel.createCareRecord(
                                        petId = id,
                                        recordType = "weight",
                                        timestamp = System.currentTimeMillis(),
                                        data = it,
                                        requestId = UUID.randomUUID().toString()
                                    )
                                }
                                bcs?.let {
                                    viewModel.createCareRecord(
                                        petId = id,
                                        recordType = "bcs",
                                        timestamp = System.currentTimeMillis(),
                                        data = it,
                                        requestId = UUID.randomUUID().toString()
                                    )
                                }
                                showQuickAdd = false
                            }
                        )
                    }
                }
            }
        }
    }
}
// helpers moved to presentation.petcare.util.CareRecordUtils
