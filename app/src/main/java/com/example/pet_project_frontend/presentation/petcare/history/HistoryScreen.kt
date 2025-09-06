@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.pet_project_frontend.presentation.petcare.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

@Composable
fun HistoryScreen(
    petId: String,
    type: String? = null,
    vm: HistoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val pagingItems: LazyPagingItems<com.example.pet_project_frontend.data.remote.dto.response.CareRecordResponse> = vm.pager(petId, type).collectAsLazyPagingItems()

    Scaffold(topBar = {
        TopAppBar(title = { Text("히스토리") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
        })
    }) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            items(pagingItems.itemCount) { idx ->
                val r = pagingItems[idx]
                if (r != null) {
                    ListItem(
                        headlineContent = { Text("${r.recordType} • ${r.timestamp}") },
                        supportingContent = { Text(r.notes ?: r.data.toString()) }
                    )
                    HorizontalDivider()
                }
            }
            when {
                pagingItems.loadState.refresh is LoadState.Loading -> item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                pagingItems.loadState.append is LoadState.Loading -> item { CircularProgressIndicator(Modifier.padding(16.dp)) }
                pagingItems.loadState.refresh is LoadState.Error -> item {
                    val e = pagingItems.loadState.refresh as LoadState.Error
                    Text(e.error.message ?: "")
                    FilledTonalButton(onClick = { pagingItems.retry() }) { Text("재시도") }
                }
            }
        }
    }
}
