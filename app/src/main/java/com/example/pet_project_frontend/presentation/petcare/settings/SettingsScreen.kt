package com.example.pet_project_frontend.presentation.petcare.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pet_project_frontend.presentation.petcare.PetCareSettingsState
import com.example.pet_project_frontend.presentation.petcare.PetCareViewModel
import com.example.pet_project_frontend.data.remote.dto.response.PetCareSettings
// KeyboardOptions intentionally omitted to avoid unresolved symbols during stabilization
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: PetCareViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by vm.settingsState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.loadPetCareSettings() }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, topBar = {
        TopAppBar(title = { Text("펫케어 설정") }, navigationIcon = {
            IconButton(onClick = onBack) { Text("<") }
        })
    }) { padding ->
        when (val s = state) {
            is PetCareSettingsState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is PetCareSettingsState.Error -> Column(Modifier.padding(padding).padding(16.dp)) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(onClick = { vm.loadPetCareSettings() }) { Text("다시 시도") }
            }
            is PetCareSettingsState.Success -> SettingsForm(Modifier.padding(padding).padding(16.dp), s.settings) { updated ->
                vm.updatePetCareSettings(updated)
            }
        }
    }
}

@Composable
private fun SettingsForm(modifier: Modifier = Modifier, initial: PetCareSettings, onSave: (PetCareSettings) -> Unit) {
    var weight by remember(initial) { mutableStateOf(initial.goalWeight.toString()) }
    var waterBowl by remember(initial) { mutableStateOf(initial.waterBowlCapacity.toString()) }
    var waterInc by remember(initial) { mutableStateOf(initial.waterIncrementAmount.toString()) }
    var actGoal by remember(initial) { mutableStateOf(initial.goalActivityMinutes.toString()) }
    var actInc by remember(initial) { mutableStateOf(initial.activityIncrementMinutes.toString()) }
    var mealGoal by remember(initial) { mutableStateOf(initial.goalMealCount.toString()) }
    var mealInc by remember(initial) { mutableStateOf(initial.mealIncrementCount.toString()) }

    var error by remember { mutableStateOf<String?>(null) }

    fun validate(): PetCareSettings? {
        val w = weight.toFloatOrNull()
        val wb = waterBowl.toIntOrNull()
        val wi = waterInc.toIntOrNull()
        val ag = actGoal.toIntOrNull()
        val ai = actInc.toIntOrNull()
        val mg = mealGoal.toIntOrNull()
        val mi = mealInc.toIntOrNull()
        if (w == null || w <= 0f) { error = "목표 체중을 올바르게 입력하세요"; return null }
        if (wb == null || wb <= 0) { error = "물 그릇 용량을 올바르게 입력하세요"; return null }
        if (wi == null || wi <= 0) { error = "물 증분을 올바르게 입력하세요"; return null }
        if (ag == null || ag < 0) { error = "활동 목표를 올바르게 입력하세요"; return null }
        if (ai == null || ai <= 0) { error = "활동 증분을 올바르게 입력하세요"; return null }
        if (mg == null || mg < 0) { error = "식사 목표를 올바르게 입력하세요"; return null }
        if (mi == null || mi <= 0) { error = "식사 증분을 올바르게 입력하세요"; return null }
        error = null
        return PetCareSettings(
            goalWeight = w,
            waterBowlCapacity = wb,
            waterIncrementAmount = wi,
            goalActivityMinutes = ag,
            activityIncrementMinutes = ai,
            goalMealCount = mg,
            mealIncrementCount = mi
        )
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = weight,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) weight = it },
            label = { Text("목표 체중(kg)") },
            // keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = waterBowl,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*$"))) waterBowl = it },
            label = { Text("물 그릇 용량(ml)") },
            // keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = waterInc,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*$"))) waterInc = it },
            label = { Text("물 증분(ml)") },
            // keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = actGoal,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*$"))) actGoal = it },
            label = { Text("활동 목표(분)") },
            // keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = actInc,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*$"))) actInc = it },
            label = { Text("활동 증분(분)") },
            // keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = mealGoal,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*$"))) mealGoal = it },
            label = { Text("식사 목표(회)") },
            // keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = mealInc,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*$"))) mealInc = it },
            label = { Text("식사 증분(회)") },
            // keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                // reset to initial
                weight = initial.goalWeight.toString()
                waterBowl = initial.waterBowlCapacity.toString()
                waterInc = initial.waterIncrementAmount.toString()
                actGoal = initial.goalActivityMinutes.toString()
                actInc = initial.activityIncrementMinutes.toString()
                mealGoal = initial.goalMealCount.toString()
                mealInc = initial.mealIncrementCount.toString()
            }) { Text("초기값") }
            Spacer(Modifier.weight(1f))
            FilledTonalButton(onClick = { validate()?.let(onSave) }) { Text("저장") }
        }
    }
}
