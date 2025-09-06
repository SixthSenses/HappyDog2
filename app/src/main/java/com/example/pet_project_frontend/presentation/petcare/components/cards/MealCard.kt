package com.example.pet_project_frontend.presentation.petcare.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import com.example.pet_project_frontend.data.remote.dto.response.PetCareSettings
import com.example.pet_project_frontend.presentation.petcare.components.CardHeader
import com.example.pet_project_frontend.presentation.petcare.TypeState
import com.example.pet_project_frontend.R

@Composable
fun MealCard(
    grouped: Map<String, List<CareRecordResponse>>,
    typeState: com.example.pet_project_frontend.presentation.petcare.TypeState? = null,
    settings: PetCareSettings?,
    onPlus: () -> Unit,
    onOpenChart: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDetail: () -> Unit,
    onRetry: () -> Unit = {}
) {
    val mealList = when (typeState) {
        is TypeState.Success -> typeState.records
        is TypeState.Loading -> null
        is TypeState.Error -> grouped["meal"]
        null -> grouped["meal"]
    }.orEmpty()
    val count = remember(mealList) { mealList.size }
    val goal = settings?.goalMealCount ?: 0
    val filledCount = count.coerceAtMost(goal)

    Card(onClick = onOpenDetail, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
        CardHeader(title = stringResource(R.string.card_meal), onOpenChart = onOpenChart, onOpenSettings = onOpenSettings)
            Spacer(Modifier.height(12.dp))
            when (typeState) {
                is TypeState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                is TypeState.Error -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "${typeState.message}", color = MaterialTheme.colorScheme.error)
            FilledTonalButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
                }
                else -> {}
            }
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
                Text(text = stringResource(R.string.meal_count_format, count, goal), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                val inc = settings?.mealIncrementCount
                FilledTonalButton(onClick = onPlus, enabled = inc != null) { Text(stringResource(R.string.meal_plus_format, inc ?: 0)) }
            }
        }
    }
}
