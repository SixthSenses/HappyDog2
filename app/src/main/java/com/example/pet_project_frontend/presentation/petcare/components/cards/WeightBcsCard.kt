package com.example.pet_project_frontend.presentation.petcare.components.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import com.example.pet_project_frontend.presentation.petcare.components.CardHeader
import com.example.pet_project_frontend.presentation.petcare.util.latestValue
import com.example.pet_project_frontend.presentation.petcare.util.previousValue
import com.example.pet_project_frontend.presentation.petcare.TypeState
import com.example.pet_project_frontend.R

@Composable
fun WeightBcsCard(
    grouped: Map<String, List<CareRecordResponse>>,
    weightTypeState: com.example.pet_project_frontend.presentation.petcare.TypeState? = null,
    bcsTypeState: com.example.pet_project_frontend.presentation.petcare.TypeState? = null,
    onOpenChart: () -> Unit,
    onOpenDetail: () -> Unit,
    onRetryWeight: () -> Unit = {},
    onRetryBcs: () -> Unit = {}
) {
    val weightList = when (weightTypeState) {
        is TypeState.Success -> weightTypeState.records
        is TypeState.Loading -> null
        is TypeState.Error -> grouped["weight"]
        null -> grouped["weight"]
    }.orEmpty()
    val bcsList = when (bcsTypeState) {
        is TypeState.Success -> bcsTypeState.records
        is TypeState.Loading -> null
        is TypeState.Error -> grouped["bcs"]
        null -> grouped["bcs"]
    }.orEmpty()
    val latestWeight = remember(weightList) { latestValue(weightList) }
    val prevWeight = remember(weightList) { previousValue(weightList) }
    val latestBcs = remember(bcsList) { latestValue(bcsList).toIntOrNull() }

    val delta = latestWeight.toFloatOrNull()?.let { w -> prevWeight.toFloatOrNull()?.let { w - it } }

    Card(onClick = onOpenDetail, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            CardHeader(title = stringResource(R.string.card_weight), onOpenChart = onOpenChart)
            Spacer(Modifier.height(8.dp))
            when (weightTypeState) {
                is TypeState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                is TypeState.Error -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "${weightTypeState.message}", color = MaterialTheme.colorScheme.error)
                    FilledTonalButton(onClick = onRetryWeight) { Text(stringResource(R.string.action_retry)) }
                }
                else -> {}
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = if (latestWeight.isNotBlank()) "$latestWeight kg" else stringResource(R.string.msg_no_records), style = MaterialTheme.typography.titleLarge)
                if (delta != null && delta != 0f) {
                    val up = delta > 0
                    Icon(if (up) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown, contentDescription = null, tint = if (up) Color.Red else Color(0xFF2E7D32))
                    Text(text = stringResource(R.string.weight_delta_format, delta))
                }
                Spacer(Modifier.weight(1f))
                Text(text = "BCS ${latestBcs ?: '-'}")
            }
            when (bcsTypeState) {
                is TypeState.Loading -> {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is TypeState.Error -> {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "${bcsTypeState.message}", color = MaterialTheme.colorScheme.error)
                        FilledTonalButton(onClick = onRetryBcs) { Text(stringResource(R.string.action_retry)) }
                    }
                }
                else -> {}
            }
        }
    }
}
