package com.example.pet_project_frontend.presentation.petcare.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HourlyBarChart(values: List<Float>, modifier: Modifier = Modifier, barHeight: Dp = 48.dp) {
    val maxVal = values.maxOrNull() ?: 0f

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
