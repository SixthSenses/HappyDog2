package com.example.pet_project_frontend.presentation.petcare.components.cards

import androidx.compose.foundation.layout.*
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
import com.example.pet_project_frontend.presentation.petcare.components.charts.HourlyBarChart
import com.example.pet_project_frontend.presentation.petcare.util.aggregatePerHour
import com.example.pet_project_frontend.presentation.petcare.TypeState
import com.example.pet_project_frontend.R

@Composable
fun ActivityCard(
    grouped: Map<String, List<CareRecordResponse>>,
    typeState: com.example.pet_project_frontend.presentation.petcare.TypeState? = null,
    settings: PetCareSettings?,
    onOpenChart: () -> Unit,
    onOpenDetail: () -> Unit,
    onRetry: () -> Unit = {}
) {
    val activityList = when (typeState) {
        is TypeState.Success -> typeState.records
        is TypeState.Loading -> null
        is TypeState.Error -> grouped["activity"]
        null -> grouped["activity"]
    }.orEmpty()
    val perHour = remember(activityList) { aggregatePerHour(activityList) }
    val total = perHour.sum()
    val goal = settings?.goalActivityMinutes ?: 0

    Card(onClick = onOpenDetail, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
        CardHeader(title = stringResource(R.string.card_activity), onOpenChart = onOpenChart)
            Spacer(Modifier.height(8.dp))
            when (typeState) {
                is TypeState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                is TypeState.Error -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "${typeState.message}", color = MaterialTheme.colorScheme.error)
            FilledTonalButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
                }
                else -> {}
            }
            HourlyBarChart(values = perHour.map { it.toFloat() }, barHeight = 48.dp)
            Spacer(Modifier.height(8.dp))
        Text(text = stringResource(R.string.activity_total_goal_format, total, goal), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
