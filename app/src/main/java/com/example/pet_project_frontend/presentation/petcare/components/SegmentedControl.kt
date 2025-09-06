package com.example.pet_project_frontend.presentation.petcare.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

enum class RangeTab(val label: String) { Today("오늘"), Week("주"), Month("월") }

@Composable
fun SegmentedControl(
    tabs: Array<RangeTab> = RangeTab.values(),
    selected: RangeTab,
    onSelect: (RangeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tabs.forEach { tab ->
            val isSelected = selected == tab
            Surface(
                tonalElevation = if (isSelected) 3.dp else 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onSelect(tab) }
            ) {
                Text(
                    text = tab.label,
                    modifier = Modifier
                        .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
    Spacer(Modifier.height(4.dp))
}
