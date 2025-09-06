package com.example.pet_project_frontend.presentation.petcare.quickadd

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pet_project_frontend.data.remote.dto.response.PetCareSettings
import com.example.pet_project_frontend.presentation.petcare.util.deriveWaterCupPresets

enum class QuickAddTab(val label: String) { Water("물"), Meal("식사"), Activity("활동"), WeightBcs("체중/BCS") }

@Composable
fun QuickAddSheet(
    settings: PetCareSettings?,
    onAddWater: (amountMl: Int) -> Unit,
    onAddMeal: (count: Int) -> Unit,
    onAddActivity: (minutes: Int) -> Unit,
    onAddWeightBcs: (kg: Double?, bcs: Int?) -> Unit,
) {
    var selected by remember { mutableStateOf(QuickAddTab.Water) }
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        TabRow(selectedTabIndex = selected.ordinal) {
            QuickAddTab.values().forEachIndexed { index, tab ->
                Tab(selected = selected.ordinal == index, onClick = { selected = tab }, text = { Text(tab.label) })
            }
        }
        Spacer(Modifier.height(12.dp))
        when (selected) {
            QuickAddTab.Water -> WaterTab(settings, onAddWater)
            QuickAddTab.Meal -> MealTab(settings, onAddMeal)
            QuickAddTab.Activity -> ActivityTab(settings, onAddActivity)
            QuickAddTab.WeightBcs -> WeightBcsTab(onAddWeightBcs)
        }
    }
}

@Composable
private fun WaterTab(settings: PetCareSettings?, onAdd: (Int) -> Unit) {
    val presets = deriveWaterCupPresets(settings)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        presets.forEach { size ->
            FilledTonalButton(onClick = { onAdd(size) }) { Text("+${size}ml") }
        }
    }
}

@Composable
private fun MealTab(settings: PetCareSettings?, onAdd: (Int) -> Unit) {
    val inc = settings?.mealIncrementCount ?: 1
    FilledTonalButton(onClick = { onAdd(inc) }) { Text("+${inc}회") }
}

@Composable
private fun ActivityTab(settings: PetCareSettings?, onAdd: (Int) -> Unit) {
    val inc = settings?.activityIncrementMinutes ?: 10
    FilledTonalButton(onClick = { onAdd(inc) }) { Text("+${inc}분") }
}

@Composable
private fun WeightBcsTab(onAdd: (Double?, Int?) -> Unit) {
    // 간단한 CTA만 제공(상세 입력은 추후 확장)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(onClick = { onAdd(null, 3) }) { Text("BCS 3") }
        FilledTonalButton(onClick = { onAdd(4.0, null) }) { Text("체중 4.0kg") }
    }
}
