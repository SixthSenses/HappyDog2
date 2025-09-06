package com.example.pet_project_frontend.presentation.petcare.components.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse
import com.example.pet_project_frontend.presentation.petcare.util.latestValue
import com.example.pet_project_frontend.presentation.petcare.TypeState

@Composable
fun BcsOnlyCard(
    grouped: Map<String, List<CareRecordResponse>>,
    typeState: com.example.pet_project_frontend.presentation.petcare.TypeState? = null,
    onOpenDetail: () -> Unit,
    onRetry: () -> Unit = {}
) {
    val bcsList = when (typeState) {
        is TypeState.Success -> typeState.records
        is TypeState.Loading -> null
        is TypeState.Error -> grouped["bcs"]
        null -> grouped["bcs"]
    }.orEmpty()
    val latestBcs = remember(bcsList) { latestValue(bcsList).toIntOrNull() }
    Card(onClick = onOpenDetail, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "BCS", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            when (typeState) {
                is TypeState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                is TypeState.Error -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "새로고침 실패: ${typeState.message}", color = MaterialTheme.colorScheme.error)
                    FilledTonalButton(onClick = onRetry) { Text("재시도") }
                }
                else -> {}
            }
            Text(text = if (latestBcs != null) "$latestBcs 단계" else "기록 없음", style = MaterialTheme.typography.titleLarge)
        }
    }
}
