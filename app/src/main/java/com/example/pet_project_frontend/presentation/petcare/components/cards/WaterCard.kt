package com.example.pet_project_frontend.presentation.petcare.components.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import com.example.pet_project_frontend.data.remote.dto.response.PetCareSettings
import com.example.pet_project_frontend.presentation.petcare.components.CardHeader
import com.example.pet_project_frontend.presentation.petcare.TypeState
import com.example.pet_project_frontend.presentation.petcare.util.toLong
import kotlin.math.max
import com.example.pet_project_frontend.R

@Composable
fun WaterCard(
    grouped: Map<String, List<CareRecordResponse>>,
    typeState: com.example.pet_project_frontend.presentation.petcare.TypeState? = null,
    settings: PetCareSettings?,
    onPlus: () -> Unit,
    onOpenChart: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDetail: () -> Unit,
    onRetry: () -> Unit = {}
) {
    val waterList = when (typeState) {
        is TypeState.Success -> typeState.records
        is TypeState.Loading -> null
        is TypeState.Error -> grouped["water"]
        null -> grouped["water"]
    }.orEmpty()
    val todayMl = remember(waterList) { waterList.sumOf { toLong(it.data).toInt() } }
    val goal = settings?.waterBowlCapacity
    val progress = (todayMl.toFloat() / max(1, (goal ?: 0))).coerceIn(0f, 1f)

    Card(onClick = onOpenDetail, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardHeader(title = stringResource(R.string.card_water), onOpenChart = onOpenChart, onOpenSettings = onOpenSettings)
            Spacer(Modifier.height(12.dp))
            when (typeState) {
                is TypeState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                }
                is TypeState.Error -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "${typeState.message}", color = MaterialTheme.colorScheme.error)
                        FilledTonalButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                else -> {}
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CircularProgressIndicator(progress = { progress }, strokeWidth = 8.dp, modifier = Modifier.size(64.dp))
                Icon(Icons.Outlined.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text(text = "$todayMl ml", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = goal?.let { stringResource(R.string.water_goal_format, it) } ?: stringResource(R.string.msg_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val inc = settings?.waterIncrementAmount
                FilledTonalButton(onClick = onPlus, enabled = inc != null) { Text(stringResource(R.string.water_plus_format, inc ?: 0)) }
            }
        }
    }
}
